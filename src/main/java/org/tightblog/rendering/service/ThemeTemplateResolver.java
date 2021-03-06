/*
   Copyright 2017 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.dao.WeblogTemplateDao;

import java.util.Map;

@Component
public class ThemeTemplateResolver extends AbstractConfigurableTemplateResolver {

    private static Logger logger = LoggerFactory.getLogger(ThemeTemplateResolver.class);

    private ThemeManager themeManager;
    private WeblogTemplateDao weblogTemplateDao;

    @Autowired
    public ThemeTemplateResolver(ThemeManager themeManager, WeblogTemplateDao weblogTemplateDao) {
        this.themeManager = themeManager;
        this.weblogTemplateDao = weblogTemplateDao;
        setTemplateMode(TemplateMode.HTML);
        setOrder(1);
        setCheckExistence(true);
        // Have changes to blog templates to propagate immediately: https://stackoverflow.com/a/28530365
        setCacheable(false);
    }

    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate,
            String resourceId, String resourceName, String characterEncoding, Map<String, Object> templateResolutionAttributes) {

        logger.debug("Looking for: {}", resourceId);

        if (resourceId == null || resourceId.length() < 1) {
            logger.error("No resourceId provided");
            return null;
        }

        Template template = null;

        if (resourceId.contains(":")) {
            // shared theme, stored in a file
            String[] sharedThemeParts = resourceId.split(":", 3);
            if (sharedThemeParts.length != 2) {
                logger.error("Invalid Theme resource key {}", resourceId);
                return null;
            }
            SharedTheme theme = themeManager.getSharedTheme(sharedThemeParts[0]);
            if (theme != null) {
                template = theme.getTemplateByName(sharedThemeParts[1]);
            }
        } else if (resourceId.length() == 36) {
            // Additional templates (not part of theme) added by blogger are indexed by 36 char UUIDs
            // in DB; 36 requirement blocks most DB calls for static resources in Thymeleaf folder, e.g.,
            // "fragments", these are resolved and cached subsequently by Thymeleaf.
            // Below method would return null anyway for any 36 char non-UUIDs
            template = weblogTemplateDao.findById(resourceId).orElse(null);
        }

        if (template == null) {
            // forward to next resolver to find (if any defined)
            return null;
        }

        final String contents = template.getTemplate();
        return new StringTemplateResource(contents);
    }
}
