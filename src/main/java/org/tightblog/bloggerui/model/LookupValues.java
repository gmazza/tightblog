/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.bloggerui.model;

import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.UserStatus;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LookupValues {
    private Map<String, String> userStatuses;
    private Map<String, String> globalRoles;
    private Map<String, String> registrationOptions;
    private Map<String, String> blogHtmlLevels;
    private Map<String, String> commentOptions;
    private Map<String, String> commentHtmlLevels;
    private Map<String, String> spamOptions;

    public Map<String, String> getUserStatuses() {
        if (userStatuses == null) {
            userStatuses = new HashMap<>();
            userStatuses.putAll(Arrays.stream(UserStatus.values())
                    .collect(Utilities.toLinkedHashMap(UserStatus::name, UserStatus::name)));
        }
        return userStatuses;
    }

    public Map<String, String> getGlobalRoles() {
        if (globalRoles == null) {
            globalRoles = new HashMap<>();
            globalRoles.putAll(Arrays.stream(GlobalRole.values())
                    .filter(r -> r != GlobalRole.NOAUTHNEEDED)
                    .collect(Utilities.toLinkedHashMap(GlobalRole::name, GlobalRole::name)));
        }
        return globalRoles;
    }

    public Map<String, String> getRegistrationOptions() {
        if (registrationOptions == null) {
            registrationOptions = new LinkedHashMap<>();
            registrationOptions.putAll(Arrays.stream(WebloggerProperties.RegistrationPolicy.values())
                    .collect(Utilities.toLinkedHashMap(WebloggerProperties.RegistrationPolicy::name,
                            WebloggerProperties.RegistrationPolicy::getDescription)));
        }
        return registrationOptions;
    }

    public Map<String, String> getBlogHtmlLevels() {
        if (blogHtmlLevels == null) {
            blogHtmlLevels = new LinkedHashMap<>();
            blogHtmlLevels.putAll(Arrays.stream(HTMLSanitizer.Level.values())
                    .filter(r -> !r.equals(HTMLSanitizer.Level.NONE))
                    .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                            HTMLSanitizer.Level::getDescription)));
        }
        return blogHtmlLevels;
    }

    public Map<String, String> getCommentOptions() {
        if (commentOptions == null) {
            commentOptions = new LinkedHashMap<>();
            commentOptions.putAll(Arrays.stream(WebloggerProperties.CommentPolicy.values())
                    .collect(Utilities.toLinkedHashMap(WebloggerProperties.CommentPolicy::name,
                            WebloggerProperties.CommentPolicy::getLabel)));
        }
        return commentOptions;
    }

    public Map<String, String> getCommentHtmlLevels() {
        if (commentHtmlLevels == null) {
            commentHtmlLevels = new LinkedHashMap<>();
            commentHtmlLevels.putAll(Arrays.stream(HTMLSanitizer.Level.values())
                    .filter(r -> !r.equals(HTMLSanitizer.Level.NONE))
                    .filter(r -> r.getSanitizingLevel() < HTMLSanitizer.Level.BASIC_IMAGES.getSanitizingLevel())
                    .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                            HTMLSanitizer.Level::getDescription)));
        }
        return commentHtmlLevels;
    }

    public Map<String, String> getSpamOptions() {
        if (spamOptions == null) {
            spamOptions = new LinkedHashMap<>();
            spamOptions.putAll(Arrays.stream(WebloggerProperties.SpamPolicy.values())
                    .collect(Utilities.toLinkedHashMap(WebloggerProperties.SpamPolicy::name,
                            WebloggerProperties.SpamPolicy::getLabel)));
        }
        return spamOptions;
    }
}
