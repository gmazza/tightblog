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
package org.tightblog.rendering.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template.Role;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.service.ThymeleafRenderer;
import org.tightblog.rendering.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogDao;
import org.tightblog.util.Utilities;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Shows preview of a blog entry prior to publishing.
 *
 * Previews are obtainable only through the authoring interface by a logged-in user
 * having at least POST rights on the blog being previewed.
 */
@RestController
@RequestMapping(path = "/tb-ui/authoring/preview")
public class PreviewController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(PreviewController.class);

    private final WeblogDao weblogDao;

    private final ThymeleafRenderer thymeleafRenderer;
    private final ThemeManager themeManager;
    private final WeblogEntryManager weblogEntryManager;
    private final PageModel pageModel;

    @Autowired
    PreviewController(WeblogDao weblogDao, @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer,
                      ThemeManager themeManager, PageModel pageModel,
                      WeblogEntryManager weblogEntryManager) {
        this.weblogDao = weblogDao;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.pageModel = pageModel;
        this.weblogEntryManager = weblogEntryManager;
    }

    @GetMapping(path = "/{weblogId}/entry/{anchor}")
    @PreAuthorize("@securityService.hasAccess(#principal.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    ResponseEntity<Resource> getEntryPreview(@PathVariable String weblogId, @PathVariable String anchor,
                                            Principal principal, Device device) throws IOException {

        Weblog weblog = weblogDao.findByIdOrNull(weblogId);
        if (weblog == null) {
            return ResponseEntity.notFound().build();
        }

        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblog.getHandle(), principal, pageModel, true);
        incomingRequest.setNoIndex(true);
        incomingRequest.setWeblog(weblog);
        incomingRequest.setWeblogEntryAnchor(Utilities.decode(anchor));
        incomingRequest.setDeviceType(Utilities.getDeviceType(device));

        WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog,
                incomingRequest.getWeblogEntryAnchor());

        if (entry == null) {
            LOG.warn("For weblog {}, invalid entry {} requested, returning 404", weblog.getHandle(), anchor);
            return ResponseEntity.notFound().build();
        } else {
            incomingRequest.setWeblogEntry(entry);
            incomingRequest.setTemplate(
                    themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.PERMALINK));

            if (incomingRequest.getTemplate() == null) {
                incomingRequest.setTemplate(themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.WEBLOG));
            }

            if (incomingRequest.getTemplate() == null) {
                LOG.warn("For weblog {}, entry {}, no template available, returning 404", weblog.getHandle(), anchor);
                return ResponseEntity.notFound().build();
            }
        }

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", incomingRequest);

        // Load models for page previewing
        Map<String, Object> model = getModelMap("pageModelSet", initData);
        model.put("model", incomingRequest);

        CachedContent rendererOutput = thymeleafRenderer.render(incomingRequest.getTemplate(), model);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(rendererOutput.getRole().getContentType()))
                .contentLength(rendererOutput.getContent().length)
                // no-store: must pull each time from server when requested
                .cacheControl(CacheControl.noStore())
                .body(new ByteArrayResource(rendererOutput.getContent()));
    }
}
