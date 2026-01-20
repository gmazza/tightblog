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

import java.security.Principal;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.dto.WeblogDto;
import org.tightblog.service.WeblogManager;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import static java.lang.Math.min;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
public class WeblogController {

    private static final Logger LOG = LoggerFactory.getLogger(WeblogController.class);

    private final UserDao userDao;
    private final WeblogManager weblogManager;
    private final WeblogDao weblogDao;
    private final WeblogEntryDao weblogEntryDao;
    private final MessageSource messages;

    @Value("${site.pages.maxEntries:30}")
    private int maxEntriesPerPage;

    @Autowired
    public WeblogController(UserDao userDao, WeblogManager weblogManager,
                            WeblogDao weblogDao, MessageSource messages,
                            WeblogEntryDao weblogEntryDao) {
        this.userDao = userDao;
        this.weblogManager = weblogManager;
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/loggedin")
    public boolean loggedIn() {
        return true;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity<WeblogDto> getWeblogData(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.findByIdOrNull(id);
        return weblog == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(weblogManager.toDto(weblog));
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogs")
    public ResponseEntity<?> addWeblog(@Valid @RequestBody WeblogDto newDataDto, Principal p) {

        User user = userDao.findEnabledByUserName(p.getName());

        if (!user.hasEffectiveGlobalRole(GlobalRole.BLOGCREATOR)) {
            return ResponseEntity.status(403).body(messages.getMessage("weblogConfig.createNotAuthorized",
                    null, Locale.getDefault()));
        }

        if (weblogDao.findByHandle(newDataDto.handle()) != null) {
            return ValidationErrorResponse.badRequest(messages.getMessage("weblogConfig.error.handleExists",
                    null, Locale.getDefault()));
        }

        Weblog weblog = new Weblog(
                newDataDto.handle().trim(),
                user,
                newDataDto.name().trim(),
                newDataDto.theme());

        return ResponseEntity.ok(saveWeblog(weblog, newDataDto, true));
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity<WeblogDto> updateWeblog(@PathVariable String id, @Valid @RequestBody WeblogDto newDataDto,
                                                  Principal p) {
        Weblog weblog = weblogDao.findByIdOrNull(id);
        return weblog == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(saveWeblog(weblog, newDataDto,
                false));
    }

    private WeblogDto saveWeblog(Weblog weblog, WeblogDto newDataDto, boolean newWeblog) {
        weblog.setName(newDataDto.name());
        weblog.setTagline(StringUtils.trimToEmpty(newDataDto.tagline()));
        weblog.setEditFormat(newDataDto.editFormat());
        weblog.setVisible(newDataDto.visible());
        weblog.setEntriesPerPage(newDataDto.entriesPerPage());
        weblog.setCommentSpamFilter(newDataDto.commentSpamFilter());
        weblog.setCommentPolicy(newDataDto.commentPolicy());
        weblog.setSpamPolicy(newDataDto.spamPolicy());
        weblog.setLocale(newDataDto.locale());
        weblog.setTimeZone(newDataDto.timeZone());

        // make sure user didn't enter an invalid entry display count
        weblog.setEntriesPerPage(min(newDataDto.entriesPerPage(), maxEntriesPerPage));

        weblog.setAbout(newDataDto.about());
        weblog.setAnalyticsCode(newDataDto.analyticsCode() != null ? newDataDto.analyticsCode().trim() : null);
        weblog.setDefaultCommentDays(newDataDto.defaultCommentDays());

        // save config
        if (newWeblog) {
            weblogManager.addWeblog(weblog);
        } else {
            weblogManager.saveWeblog(weblog, false);
        }

        // ROL-1050: apply comment defaults to existing entries
        if (newDataDto.applyCommentDefaults()) {
            weblogEntryDao.applyDefaultCommentDaysToWeblogEntries(weblog, weblog.getDefaultCommentDays());
        }
        return weblogManager.toDto(weblog);
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity<String> deleteWeblog(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.findByIdOrNull(id);
        weblogManager.removeWeblog(weblog);
        return ResponseEntity.noContent().build();
    }

}
