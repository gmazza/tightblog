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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Remove a template.
 */
public class TemplateRemove extends UIAction {

	private static Log log = LogFactory.getLog(TemplateRemove.class);

	// id of template to remove
	private String removeId = null;

	// template object that we will remove
	private WeblogTemplate template = null;

	public TemplateRemove() {
		this.actionName = "templateRemove";
		this.desiredMenu = "editor";
		this.pageTitle = "editPages.title.removeOK";
	}

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

	public void prepare() {
		if (getRemoveId() != null) {
            try {
                setTemplate(weblogManager.getTemplate(getRemoveId()));
            } catch (WebloggerException ex) {
                log.error("Error looking up template by id - " + getRemoveId(),
                        ex);
                addError("editPages.remove.notFound", getRemoveId());
            }
        }
	}

	/**
	 * Display the remove template confirmation.
	 */
	public String execute() {
		return "confirm";
	}

	/**
	 * Remove a new template.
	 */
	public String remove() {

		if (getTemplate() != null) {
            try {
                // if weblog template remove custom style sheet also
                if (getTemplate().getName().equals(
                        WeblogTemplate.DEFAULT_PAGE)) {

                    ThemeTemplate stylesheet = getActionWeblog().getTheme()
                            .getTemplateByAction(ComponentType.STYLESHEET);

                    // Delete style sheet if the same name
                    if (stylesheet != null
                            && getActionWeblog().getTheme().getTemplateByAction(ComponentType.STYLESHEET) != null
                            && stylesheet.getLink().equals(
                            getActionWeblog().getTheme()
                                    .getTemplateByAction(ComponentType.STYLESHEET).getLink())) {
                        // Same so OK to delete
                        WeblogTemplate css = weblogManager.getTemplateByLink(
                                getActionWeblog(), stylesheet.getLink());

                        if (css != null) {
                            weblogManager.removeTemplate(css);
                        }
                    }
                }

                // notify cache
                CacheManager.invalidate(getTemplate());
                weblogManager.removeTemplate(getTemplate());
                WebloggerFactory.flush();

                return SUCCESS;

            } catch (Exception ex) {
                log.error("Error removing page - " + getRemoveId(), ex);
                addError("editPages.remove.error");
            }
        }

		return "confirm";
	}
	
    /**
     * Cancel.
     * 
     * @return the string
     */
    public String cancel() {
        return CANCEL;
    }

	public String getRemoveId() {
		return removeId;
	}

	public void setRemoveId(String removeId) {
		this.removeId = removeId;
	}

	public WeblogTemplate getTemplate() {
		return template;
	}

	public void setTemplate(WeblogTemplate template) {
		this.template = template;
	}

}
