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
package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;


/**
 * Action which handles editing for a single WeblogTemplate.
 */
public class TemplateEdit extends UIAction {

    private static Log log = LogFactory.getLog(TemplateEdit.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // form bean for collection all template properties
    private WeblogTemplate bean = new WeblogTemplate();

    // the template we are working on
    private WeblogTemplate template = null;

    public TemplateEdit() {
        this.actionName = "templateEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public void prepare() {
        try {
            setTemplate(weblogManager.getTemplate(getBean().getId()));
        } catch (WebloggerException ex) {
            log.error("Error looking up template - " + getBean().getId(), ex);
        }
    }


    /**
     * Show template edit page.
     */
    @SkipValidation
    public String execute() {
        try {
            if (getTemplate() == null) {
                addError("Unable to locate specified template");
                return LIST;
            }

            bean.setId(template.getId());
            bean.setName(template.getName());
            bean.setRole(template.getRole());
            bean.setDescription(template.getDescription());
            bean.setRelativePath(template.getRelativePath());

            if (template.getTemplateRendition(RenditionType.STANDARD) != null) {
                bean.setContentsStandard(template.getTemplateRendition(RenditionType.STANDARD).getTemplate());
            } else {
                bean.setContentsStandard("");
            }
            if (template.getTemplateRendition(RenditionType.MOBILE) != null) {
                bean.setContentsMobile(template.getTemplateRendition(RenditionType.MOBILE).getTemplate());
            }
            log.debug("Standard: " + bean.getContentsStandard() + " Mobile: " + bean.getContentsMobile());

        } catch (WebloggerException ex) {
           log.error("Error updating page - " + getBean().getId(), ex);
           addError("Error saving template - check TightBlog logs");
        }

        return INPUT;
    }

    /**
     * Save an existing template.
     */
    public String save() {
        log.debug("Entering save()");

        if (getTemplate() == null) {
            addError("Unable to locate specified template");
            return LIST;
        }

        // validation
        myValidate();

        if (!hasActionErrors()) {
            try {
                WeblogTemplate templateToSave = getTemplate();

                if (templateToSave.getTemplateRendition(RenditionType.STANDARD) != null) {
                    // if we have a template, then set it
                    WeblogTemplateRendition tc = templateToSave.getTemplateRendition(RenditionType.STANDARD);
                    tc.setTemplate(bean.getContentsStandard());
                    weblogManager.saveTemplateRendition(tc);
                } else {
                    // otherwise create it, then set it
                    WeblogTemplateRendition tc = new WeblogTemplateRendition(templateToSave, RenditionType.STANDARD);
                    tc.setTemplate("");
                    weblogManager.saveTemplateRendition(tc);
                }

                if (templateToSave.getTemplateRendition(RenditionType.MOBILE) != null) {
                    WeblogTemplateRendition tc = templateToSave.getTemplateRendition(RenditionType.MOBILE);
                    tc.setTemplate(bean.getContentsMobile());
                    weblogManager.saveTemplateRendition(tc);
                }

                // some properties relevant only for certain template roles
                if (!templateToSave.getRole().isSingleton()) {
                    templateToSave.setName(bean.getName());
                    templateToSave.setDescription(bean.getDescription());
                }

                if (templateToSave.getRole().isAccessibleViaUrl()) {
                    templateToSave.setRelativePath(bean.getRelativePath());
                }

                templateToSave.setLastModified(new Date());

                // save template
                weblogManager.saveTemplate(templateToSave);
                log.debug("Saved template: " + templateToSave.getId());

                //flush
                WebloggerFactory.flush();

                // notify caches
                cacheManager.invalidate(templateToSave);

                // success message
                addMessage("pageForm.save.success", templateToSave.getName());

            } catch (Exception ex) {
                log.error("Error updating page - " + getBean().getId(), ex);
                addError("Error updating template - check TightBlog logs");
            }
        }

        log.debug("Leaving save()");
        return INPUT;
    }

    private void myValidate() {
        if (StringUtils.isEmpty(bean.getName())) {
            addError("Template.error.nameNull");
        } else {
            // if name changed make sure there isn't a conflict
            if (!getTemplate().getName().equals(getBean().getName())) {
                try {
                    if (weblogManager.getTemplateByName(getActionWeblog(), getBean().getName()) != null) {
                        addError("pagesForm.error.alreadyExists", getBean().getName());
                    }
                } catch (WebloggerException ex) {
                    log.error("Error checking page name uniqueness", ex);
                }
            }

            // if link changed make sure there isn't a conflict
            if (!StringUtils.isEmpty(getBean().getRelativePath()) &&
                    !getBean().getRelativePath().equals(getTemplate().getRelativePath())) {
                try {
                    if (weblogManager.getTemplateByPath(getActionWeblog(), getBean().getRelativePath()) != null) {
                        addError("pagesForm.error.alreadyExists", getBean().getRelativePath());
                    }
                } catch (WebloggerException ex) {
                    log.error("Error checking page link uniqueness", ex);
                }
            }
        }
    }

    public Map<TemplateLanguage, String> getTemplateLanguages() {
        Map<TemplateLanguage, String> langMap = new EnumMap<>(TemplateLanguage.class);
        for (TemplateLanguage lang : TemplateLanguage.values()) {
            langMap.put(lang, lang.getReadableName());
        }
        return langMap;
    }


    /**
     * Revert the stylesheet to its original state.  UI provides this only for shared themes.
     */
    public String revert() {
        if (!hasActionErrors()) {
            try {

                WeblogTemplate templateToRevert = getTemplate();

                // lookup the theme used by this weblog
                SharedTheme theme = themeManager.getSharedTheme(getActionWeblog().getEditorTheme());

                templateToRevert.setLastModified(new Date());

                if (templateToRevert.getTemplateRendition(RenditionType.STANDARD) != null) {
                    TemplateRendition templateCode = theme.getTemplateByName(templateToRevert.getName())
                            .getTemplateRendition(RenditionType.STANDARD);
                    // if we have a template, then set it
                    WeblogTemplateRendition existingTemplateCode = templateToRevert
                            .getTemplateRendition(RenditionType.STANDARD);
                    existingTemplateCode
                            .setTemplate(templateCode.getTemplate());
                    weblogManager.saveTemplateRendition(existingTemplateCode);
                }
                if (templateToRevert.getTemplateRendition(RenditionType.MOBILE) != null) {
                    TemplateRendition templateCode = theme.getTemplateByName(templateToRevert.getName())
                            .getTemplateRendition(RenditionType.MOBILE);
                    WeblogTemplateRendition existingTemplateCode = templateToRevert
                            .getTemplateRendition(RenditionType.MOBILE);
                    existingTemplateCode
                            .setTemplate(templateCode.getTemplate());
                }

                // save template and flush
                weblogManager.saveTemplate(templateToRevert);
                WebloggerFactory.flush();

                // notify caches
                cacheManager.invalidate(templateToRevert);

                // success message
                addMessage("templateEdit.revert.success",
                        templateToRevert.getName());

            } catch (WebloggerException ex) {
                log.error("Error updating stylesheet template for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("generic.error.check.logs");
            }
        }
        return execute();
    }

    /**
     * set theme to default stylesheet, ie delete it.
     */
    public String delete() {
        if (template != null && !hasActionErrors()) {
            try {
                // Delete template and flush

                // Remove template and page codes
                weblogManager.removeTemplate(template);

                Weblog weblog = getActionWeblog();

                // save updated weblog and flush
                weblogManager.saveWeblog(weblog);

                // Flush for operation
                WebloggerFactory.flush();

                // notify caches
                cacheManager.invalidate(template);

                // success message
                addMessage("templateEdit.default.success", template.getName());

                template = null;

            } catch (Exception e) {
                log.error("Error deleting template for weblog - "
                        + getActionWeblog().getHandle(), e);
                addError("generic.error.check.logs");
            }
        }
        return INPUT;
    }
    public WeblogTemplate getBean() {
        return bean;
    }

    public void setBean(WeblogTemplate bean) {
        this.bean = bean;
    }

    public WeblogTemplate getTemplate() {
        return template;
    }

    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }
}