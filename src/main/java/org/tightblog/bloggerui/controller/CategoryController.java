/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogCategoryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.service.WeblogManager.WeblogCategoryData;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class CategoryController {

    private final WeblogDao weblogDao;
    private final WeblogCategoryDao weblogCategoryDao;
    private final WeblogManager weblogManager;
    private final WeblogEntryManager weblogEntryManager;

    @Autowired
    public CategoryController(WeblogDao weblogDao, WeblogCategoryDao weblogCategoryDao, WeblogManager weblogManager,
                              WeblogEntryManager weblogEntryManager) {
        this.weblogDao = weblogDao;
        this.weblogCategoryDao = weblogCategoryDao;
        this.weblogManager = weblogManager;
        this.weblogEntryManager = weblogEntryManager;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/categories")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public List<WeblogCategoryData> getWeblogCategoryData(@RequestParam(name = "weblogId") String weblogId, Principal p) {
        return weblogManager.getWeblogCategoryData(weblogDao.findByIdOrNull(weblogId));
    }

    @PutMapping(value = "/tb-ui/authoring/rest/categories")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public void addCategory(@RequestParam(name = "weblogId") String weblogId, @RequestBody WeblogCategoryData newCategory,
                            Principal p, HttpServletResponse response) {

        Weblog weblog = weblogDao.findByIdOrNull(weblogId);

        if (weblog.getWeblogCategories().stream().anyMatch(c -> newCategory.name().equals(c.getName()))) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            WeblogCategory wc = new WeblogCategory(weblog, newCategory.name());
            weblog.addCategory(wc);
            weblogManager.saveWeblog(weblog, true);
        }
    }

    @PutMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/category")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public void updateCategory(@PathVariable String weblogId, @RequestBody WeblogCategoryData updatedCategory, Principal p,
                               HttpServletResponse response) {

        WeblogCategory c = weblogCategoryDao.findByIdAndWeblogId(updatedCategory.id(), weblogId);
        Weblog weblog = c.getWeblog();
        if (!c.getName().equals(updatedCategory.name())) {
            // can't change category name to one already existing for blog
            if (weblog.getWeblogCategories().stream().anyMatch(wc -> updatedCategory.name().equals(wc.getName()))) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {
                c.setName(updatedCategory.name());
                weblogManager.saveWeblog(weblog, true);
            }
        }
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/category/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public void removeCategory(@PathVariable String weblogId, @PathVariable String id, @RequestParam String targetCategoryId,
                               Principal p) {

        WeblogCategory categoryToRemove = weblogCategoryDao.findByIdAndWeblogId(id, weblogId);
        WeblogCategory targetCategory = weblogCategoryDao.findByIdAndWeblogId(targetCategoryId, weblogId);

        if (categoryToRemove != null && targetCategory != null) {
            Weblog weblog = categoryToRemove.getWeblog();
            weblogEntryManager.moveWeblogCategoryContents(categoryToRemove, targetCategory);
            weblog.getWeblogCategories().remove(categoryToRemove);
            weblogManager.saveWeblog(weblog, true);
        }
    }

}
