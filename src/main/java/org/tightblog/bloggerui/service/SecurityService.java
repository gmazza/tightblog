/*
   Copyright 2020 Glen Mazza

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
package org.tightblog.bloggerui.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.tightblog.domain.WeblogOwned;
import org.tightblog.domain.WeblogRole;
import org.tightblog.service.UserManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class SecurityService {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final UserManager userManager;

    public SecurityService(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean hasAccess(String username, Class<WeblogOwned> clazz, String id, String weblogRole) {
        WeblogOwned wo = entityManager.find(clazz, id);
        if (wo == null) {
            LOG.warn("User {} trying to access unavailable {} entity with id {}", username, clazz.getSimpleName(), id);
            return false;
        }
        WeblogRole role = WeblogRole.valueOf(weblogRole);
        return userManager.checkWeblogRole(username, wo, role);
    }
}
