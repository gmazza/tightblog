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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

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
    public ResponseEntity getWeblogData(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.findByIdOrNull(id);
        return ResponseEntity.ok(weblog);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogs")
    public ResponseEntity addWeblog(@Valid @RequestBody Weblog newData, Principal p) {

        User user = userDao.findEnabledByUserName(p.getName());

        if (!user.hasEffectiveGlobalRole(GlobalRole.BLOGCREATOR)) {
            return ResponseEntity.status(403).body(messages.getMessage("weblogConfig.createNotAuthorized",
                    null, Locale.getDefault()));
        }

        if (weblogDao.findByHandle(newData.getHandle()) != null) {
            return ValidationErrorResponse.badRequest(messages.getMessage("weblogConfig.error.handleExists",
                    null, Locale.getDefault()));
        }

        Weblog weblog = new Weblog(
                newData.getHandle().trim(),
                user,
                newData.getName().trim(),
                newData.getTheme());

        return saveWeblog(weblog, newData, true);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity<Weblog> updateWeblog(@PathVariable String id, @Valid @RequestBody Weblog newData, Principal p) {
        Weblog weblog = weblogDao.findByIdOrNull(id);
        return saveWeblog(weblog, newData, false);
    }

    private ResponseEntity<Weblog> saveWeblog(Weblog weblog, Weblog newData, boolean newWeblog) {
        if (weblog != null) {

            if (newData.getAnalyticsCode() != null) {
                newData.setAnalyticsCode(newData.getAnalyticsCode().trim());
            }

            weblog.setName(newData.getName());
            weblog.setTagline(StringUtils.trimToEmpty(newData.getTagline()));
            weblog.setEditFormat(newData.getEditFormat());
            weblog.setVisible(newData.getVisible());
            weblog.setEntriesPerPage(newData.getEntriesPerPage());
            weblog.setBlacklist(newData.getBlacklist());
            weblog.setAllowComments(newData.getAllowComments());
            weblog.setSpamPolicy(newData.getSpamPolicy());
            weblog.setLocale(newData.getLocale());
            weblog.setTimeZone(newData.getTimeZone());

            // make sure user didn't enter an invalid entry display count
            if (newData.getEntriesPerPage() > maxEntriesPerPage) {
                newData.setEntriesPerPage(maxEntriesPerPage);
            }
            weblog.setEntriesPerPage(newData.getEntriesPerPage());

            weblog.setAbout(newData.getAbout());
            weblog.setAnalyticsCode(newData.getAnalyticsCode());
            weblog.setDefaultCommentDays(newData.getDefaultCommentDays());

            // save config
            if (newWeblog) {
                weblogManager.addWeblog(weblog);
                LOG.info("New weblog {} created by user {}", weblog, weblog.getCreator());
            } else {
                weblogManager.saveWeblog(weblog, true);
            }

            // ROL-1050: apply comment defaults to existing entries
            if (newData.isApplyCommentDefaults()) {
                weblogEntryDao.applyDefaultCommentDaysToWeblogEntries(weblog, weblog.getDefaultCommentDays());
            }

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(weblog);
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity<String> deleteWeblog(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.findByIdOrNull(id);
        weblogManager.removeWeblog(weblog);
        return ResponseEntity.noContent().build();
    }

}
