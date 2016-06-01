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
package org.apache.roller.weblogger.ui.restapi;

import java.security.Principal;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.roller.weblogger.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class WeblogController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    @Autowired
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private List<WeblogEntryPlugin> weblogEntryPlugins;

    public List<WeblogEntryPlugin> getWeblogEntryPlugins() {
        return weblogEntryPlugins;
    }

    public WeblogController() {
    }

    public void setWeblogEntryPlugins(List<WeblogEntryPlugin> weblogEntryPlugins) {
        this.weblogEntryPlugins = weblogEntryPlugins;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}", method = RequestMethod.GET)
    public Weblog getWeblogData(@PathVariable String id, HttpServletResponse response) throws ServletException {
        Weblog weblog = weblogManager.getWeblog(id);
        if (weblog != null) {
            return weblog;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblogs", method = RequestMethod.PUT)
    public ResponseEntity addWeblog(@Valid @RequestBody Weblog newData, Principal p, HttpServletResponse response) throws ServletException {

        if(!propertiesManager.getBooleanProperty("site.allowUserWeblogCreation")) {
            return ResponseEntity.status(403).body("createWebsite.disabled");
        }

        User user = userManager.getUserByUserName(p.getName());

        if (!WebloggerStaticConfig.getBooleanProperty("groupblogging.enabled")) {
            List<UserWeblogRole> permissions = userManager.getWeblogRoles(user);
            if (permissions.size() > 0) {
                // sneaky user trying to get around 1 blog limit that applies
                // only when group blogging is disabled
                return ResponseEntity.status(403).body("createWebsite.oneBlogLimit");
            }
        }

        ValidationError maybeError = advancedValidate(newData, true);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }

        Weblog weblog = new Weblog(
                newData.getHandle().trim(),
                user,
                newData.getName().trim(),
                newData.getTagline().trim(),
                newData.getTheme(),
                newData.getLocale(),
                newData.getTimeZone());

        return saveWeblog(weblog, newData, response);
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}", method = RequestMethod.POST)
    public ResponseEntity updateWeblog(@PathVariable String id, @Valid @RequestBody Weblog newData, Principal p,
                                            HttpServletResponse response) throws ServletException {
        Weblog weblog = weblogManager.getWeblog(id);
        if (weblog != null) {
            if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                ValidationError maybeError = advancedValidate(newData, false);
                if (maybeError != null) {
                    return ResponseEntity.badRequest().body(maybeError);
                }
                return saveWeblog(weblog, newData, response);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity saveWeblog(Weblog weblog, Weblog newData, HttpServletResponse response) throws ServletException {
        try {
            if (weblog != null) {

                if (newData.getAnalyticsCode() != null) {
                    newData.setAnalyticsCode(newData.getAnalyticsCode().trim());
                }

                weblog.setName(newData.getName());
                weblog.setTagline(newData.getTagline());
                weblog.setEditorPage(newData.getEditorPage());
                weblog.setVisible(newData.getVisible());
                weblog.setEntriesPerPage(newData.getEntriesPerPage());
                weblog.setBlacklist(newData.getBlacklist());
                weblog.setAllowComments(newData.getAllowComments());
                weblog.setApproveComments(newData.getApproveComments());
                weblog.setEmailComments(newData.getEmailComments());
                weblog.setLocale(newData.getLocale());
                weblog.setTimeZone(newData.getTimeZone());
                weblog.setDefaultPlugins(newData.getDefaultPlugins());

                // make sure user didn't enter an invalid entry display count
                int maxEntries = propertiesManager.getIntProperty("site.pages.maxEntries");
                if (newData.getEntriesPerPage() > maxEntries) {
                    newData.setEntriesPerPage(maxEntries);
                }
                weblog.setEntriesPerPage(newData.getEntriesPerPage());

                weblog.setAbout(newData.getAbout());
                weblog.setAnalyticsCode(newData.getAnalyticsCode());
                weblog.setDefaultCommentDays(newData.getDefaultCommentDays());

                // save config
                weblogManager.saveWeblog(weblog);

                // ROL-1050: apply comment defaults to existing entries
                if(newData.isApplyCommentDefaults()) {
                    weblogEntryManager.applyCommentDefaultsToEntries(weblog);
                }

                // flush and clear cache
                try {
                    persistenceStrategy.flushAndInvalidateWeblog(weblog);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (RollbackException ex) {
                    log.error("Error updating weblog config", ex);
                    return ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body("Persistence Problem, check logs");
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(weblog);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }


    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}", method = RequestMethod.DELETE)
    public void deleteWeblog(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {
        Weblog weblog = weblogManager.getWeblog(id);
        if (weblog != null) {
            if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                try {
                    // remove website
                    weblogManager.removeWeblog(weblog);
                    persistenceStrategy.flushAndInvalidateWeblog(weblog);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (Exception ex) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    log.error("Error removing weblog - {}", weblog.getHandle(), ex);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private ValidationError advancedValidate(Weblog data, boolean isAdd) {
        BindException be = new BindException(data, "new data object");

        ValidationError.fromBindingErrors(be);

        // make sure handle isn't already taken
        if(isAdd) {
            if (weblogManager.getWeblogByHandle(data.getHandle()) != null) {
                be.addError(new ObjectError("Weblog object", bundle.getString("createWeblog.error.handleExists")));
            }
        }

        // check blacklist
        try {
            // just for testing/counting, this does not persist rules in any way
            Blacklist testBlacklist = new Blacklist(data.getBlacklist(), null);
        } catch (Exception e) {
            be.addError(new ObjectError("Weblog object",
                    bundle.getString("websiteSettings.error.processingBlacklist")));
            be.addError(new ObjectError("Weblog object", e.getMessage()));
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

}
