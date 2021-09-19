/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.bloggerui.controller;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.bloggerui.model.SuccessResponse;
import org.tightblog.bloggerui.model.Violation;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WeblogTemplateDao;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
public class TemplateController {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateController.class);

    private final WeblogDao weblogDao;
    private final WeblogTemplateDao weblogTemplateDao;
    private final WeblogManager weblogManager;
    private final ThemeManager themeManager;
    private final MessageSource messages;

    private record TemplateRole(String constant, String readableName, String descriptionProperty) { }
    private record WeblogThemeData(List<SharedTheme> themes, List<? extends Template> templates,
                                   List<TemplateRole> availableTemplateRoles) { }

    @Autowired
    public TemplateController(WeblogDao weblogDao, WeblogTemplateDao weblogTemplateDao,
                              WeblogManager weblogManager, ThemeManager themeManager, MessageSource messages) {
        this.weblogDao = weblogDao;
        this.weblogTemplateDao = weblogTemplateDao;
        this.weblogManager = weblogManager;
        this.themeManager = themeManager;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/templates")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public WeblogThemeData getWeblogTemplates(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.getById(id);
        WeblogTheme theme = new WeblogTheme(weblogTemplateDao, weblog, themeManager.getSharedTheme(weblog.getTheme()));

        // build list of template role types that may be added
        List<Template.Role> availableRoles = Arrays.stream(Template.Role.values()).
                filter(Template.Role::isBlogComponent).
                collect(Collectors.toList());

        // remove from above list any already existing for the theme
        theme.getTemplates().stream().filter(t -> t.getRole().isSingleton()).forEach(t ->
                availableRoles.removeIf(r -> r.name().equals(t.getRole().name())));

        List<TemplateRole> roles = new ArrayList<>();
        availableRoles.forEach(role -> roles.add(new TemplateRole(role.getName(), role.getReadableName(),
                role.getDescriptionProperty())));

        return new WeblogThemeData(themeManager.getEnabledSharedThemesList(), theme.getTemplates(), roles);
    }

    @GetMapping(value = "/tb-ui/authoring/rest/template/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogTemplate), #id, 'OWNER')")
    public WeblogTemplate getWeblogTemplate(@PathVariable String id, Principal p) {

        WeblogTemplate template = weblogTemplateDao.getById(id);

        if (themeManager.getSharedTheme(template.getWeblog().getTheme()).getTemplateByName(template.getName()) != null) {
            template.setDerivation(Template.Derivation.OVERRIDDEN);
        }
        return template;
    }

    // Used to obtain shared templates not yet customized for a particular weblog
    // need to add / at end of URL due to template name possibly having a period in it (e.g., rolling.css).
    // none of other solutions (http://stackoverflow.com/questions/16332092/spring-mvc-pathvariable-with-dot-is-getting-truncated)
    // seemed to work.
    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templatename/{templateName}/")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public WeblogTemplate getWeblogTemplateByName(@PathVariable String weblogId, @PathVariable String templateName, Principal p,
                                                  HttpServletResponse response) {

        Weblog weblog = weblogDao.getById(weblogId);
        SharedTheme sharedTheme = themeManager.getSharedTheme(weblog.getTheme());
        Template sharedTemplate = sharedTheme.getTemplateByName(templateName);
        if (sharedTemplate != null) {
            return themeManager.createWeblogTemplate(weblog, sharedTemplate);
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templates", consumes = { "application/json" })
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public ResponseEntity<?> postTemplate(@PathVariable String weblogId, @Valid @RequestBody WeblogTemplate templateData,
                                      Principal p, Locale locale) {

        Weblog weblog = weblogDao.getById(weblogId);
        WeblogTemplate templateToSave = weblogTemplateDao.findByWeblogIdAndId(weblog.getId(), templateData.getId());

        String originalName = null;
        // create new?
        if (templateToSave == null) {
            templateToSave = new WeblogTemplate();
            if (templateData.getRole() != null) {
                templateToSave.setRole(templateData.getRole());
            } else {
                templateToSave.setRole(Template.Role.valueOf(templateData.getRoleName()));
            }
            templateToSave.setName(templateData.getName());
            templateToSave.setWeblog(weblog);
        } else {
            originalName = templateToSave.getName();
            if (Template.Derivation.SPECIFICBLOG.equals(templateToSave.getDerivation())) {
                templateToSave.setName(templateData.getName());
            }
        }

        templateToSave.setTemplate(templateData.getTemplate());
        templateToSave.setLastModified(Instant.now());

        // some properties relevant only for certain template roles
        if (!templateToSave.getRole().isSingleton()) {
            templateToSave.setDescription(templateData.getDescription());
        }

        List<Violation> violations = validateTemplates(templateToSave, locale);
        if (violations.size() > 0) {
            return ValidationErrorResponse.badRequest(violations);
        }

        weblogTemplateDao.save(templateToSave);
        weblogManager.evictWeblogTemplateCaches(templateToSave.getWeblog(), templateToSave.getName(),
                templateToSave.getRole());
        if (originalName != null) {
            weblogTemplateDao.evictWeblogTemplateByName(templateToSave.getWeblog(), originalName);
        }
        weblogManager.saveWeblog(templateToSave.getWeblog(), true);

        return SuccessResponse.textMessage(templateToSave.getId());
    }

    private List<Violation> validateTemplates(WeblogTemplate templateToCheck, Locale locale) {
        List<Violation> violations = new ArrayList<>();

        WeblogTemplate template = weblogTemplateDao.findByWeblogAndName(templateToCheck.getWeblog(),
                templateToCheck.getName());
        if (template != null && !template.getId().equals(templateToCheck.getId())) {
            violations.add(new Violation(messages.getMessage("templates.error.nameAlreadyExists", null, locale)));
        }

        if (templateToCheck.getRole().isSingleton()) {
            template = weblogTemplateDao.findByWeblogAndRole(templateToCheck.getWeblog(), templateToCheck.getRole());
            if (template != null && !template.getId().equals(templateToCheck.getId())) {
                violations.add(new Violation(
                        messages.getMessage("templates.error.singletonActionAlreadyExists", null, locale)));
            }
        }

        return violations;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templates/delete")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public void deleteTemplates(@PathVariable String weblogId, @RequestBody List<String> ids, Principal p,
                                HttpServletResponse response) {
        for (String id : ids) {
            deleteTemplate(weblogId, id, response);
        }
    }

    private void deleteTemplate(String weblogId, String id, HttpServletResponse response) {
        LOG.info("Deleting template with ID {}...", id);
        // searching by both weblog ID and ID to ensure that templates from another weblogID (different from one
        // @PreAuthorize security check was made) aren't deleted.
        WeblogTemplate template = weblogTemplateDao.findByWeblogIdAndId(weblogId, id);
        if (template != null) {
            weblogTemplateDao.delete(template);
            weblogManager.evictWeblogTemplateCaches(template.getWeblog(), template.getName(), template.getRole());
            weblogManager.saveWeblog(template.getWeblog(), true);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/switchtheme/{newThemeId}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public ResponseEntity<?> switchTheme(@PathVariable String weblogId, @PathVariable String newThemeId, Principal p,
                                         Locale locale) {

        Weblog weblog = weblogDao.getById(weblogId);
        SharedTheme newTheme;
        try {
            newTheme = themeManager.getSharedTheme(newThemeId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Violation> errors = validateTheme(weblog, newTheme, locale);
        if (errors.size() > 0) {
            return ValidationErrorResponse.badRequest(errors);
        }

        // Remove old template overrides
        List<WeblogTemplate> oldTemplates = weblogTemplateDao.getWeblogTemplateMetadata(weblog);

        for (WeblogTemplate template : oldTemplates) {
            if (template.getDerivation() == Template.Derivation.OVERRIDDEN) {
                weblogTemplateDao.deleteById(template.getId());
            }
            weblogManager.evictWeblogTemplateCaches(weblog, template.getName(), template.getRole());
        }

        weblog.setTheme(newThemeId);

        LOG.debug("Switching to theme {} for weblog {}", newThemeId, weblog.getHandle());

        // save updated weblog so its cached pages will expire
        weblogManager.saveWeblog(weblog, true);
        weblogTemplateDao.evictWeblogTemplates(weblog);

        return ResponseEntity.noContent().build();
    }

    private List<Violation> validateTheme(Weblog weblog, SharedTheme newTheme, Locale locale) {
        List<Violation> violations = new ArrayList<>();

        WeblogTheme oldTheme = new WeblogTheme(weblogTemplateDao, weblog,
                themeManager.getSharedTheme(weblog.getTheme()));

        oldTheme.getTemplates().stream().filter(
                old -> old.getDerivation() == Template.Derivation.SPECIFICBLOG).forEach(old -> {
            if (old.getRole().isSingleton() && newTheme.getTemplateByRole(old.getRole()) != null) {
                violations.add(new Violation(messages.getMessage("templates.conflicting.singleton.role",
                        new Object[]{old.getRole().getReadableName()}, locale)));
            } else if (newTheme.getTemplateByName(old.getName()) != null) {
                violations.add(new Violation(messages.getMessage("templates.conflicting.name",
                        new Object[]{old.getName()}, locale)));
            }
        });

        return violations;
    }
}
