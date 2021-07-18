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
import org.tightblog.domain.SharedTheme;
import org.tightblog.domain.UserStatus;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class LookupValues {
    private Map<String, String> userStatuses;
    private Map<String, String> globalRoles;
    private Map<String, String> registrationOptions;
    private Map<String, String> blogHtmlLevels;
    private List<EnumAsObj> commentOptionList;
    private Map<String, String> commentHtmlLevels;
    private List<EnumAsObj> spamOptionList;

    private Map<String, SharedTheme> sharedThemeMap;
    private Map<String, String> editFormats;
    private Map<String, String> locales;
    private Map<String, String> timezones;
    private Map<Integer, String> commentDayOptions;

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

    public List<EnumAsObj> getCommentOptionList() {
        if (commentOptionList == null) {
            commentOptionList = new ArrayList<>();
            for (WebloggerProperties.CommentPolicy policy : WebloggerProperties.CommentPolicy.values()) {
                EnumAsObj eao = new EnumAsObj();
                eao.setConstant(policy.name());
                eao.setLabel(policy.getLabel());
                eao.setLevel(policy.getLevel());
                commentOptionList.add(eao);
            }
        }
        return commentOptionList;
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

    public List<EnumAsObj> getSpamOptionList() {
        if (spamOptionList == null) {
            spamOptionList = new ArrayList<>();
            for (WebloggerProperties.SpamPolicy policy : WebloggerProperties.SpamPolicy.values()) {
                EnumAsObj eao = new EnumAsObj();
                eao.setConstant(policy.name());
                eao.setLabel(policy.getLabel());
                eao.setLevel(policy.getLevel());
                spamOptionList.add(eao);
            }
        }
        return spamOptionList;
    }

    public Map<String, SharedTheme> getSharedThemeMap() {
        if (sharedThemeMap == null) {
            sharedThemeMap = new LinkedHashMap<>();
        }
        return sharedThemeMap;
    }

    public Map<String, String> getEditFormats() {
        if (editFormats == null) {
            editFormats = new LinkedHashMap<>();
            editFormats.putAll(Arrays.stream(Weblog.EditFormat.values())
                    .collect(Utilities.toLinkedHashMap(Weblog.EditFormat::name, Weblog.EditFormat::getDescriptionKey)));
        }
        return editFormats;
    }

    public Map<Integer, String> getCommentDayOptions() {
        if (commentDayOptions == null) {
            commentDayOptions = new LinkedHashMap<>();
            commentDayOptions.putAll(Arrays.stream(WeblogEntry.CommentDayOption.values())
                    .collect(Utilities.toLinkedHashMap(WeblogEntry.CommentDayOption::getDays,
                            WeblogEntry.CommentDayOption::getDescriptionKey)));
        }
        return commentDayOptions;
    }

    public Map<String, String> getLocales() {
        if (locales == null) {
            locales = new LinkedHashMap<>();
            locales.putAll(Arrays.stream(Locale.getAvailableLocales())
                    .sorted(Comparator.comparing(Locale::getDisplayName))
                    .collect(Utilities.toLinkedHashMap(Locale::toString, Locale::getDisplayName)));
        }
        return locales;
    }

    public Map<String, String> getTimezones() {
        if (timezones == null) {
            timezones = new LinkedHashMap<>();
            timezones.putAll(Arrays.stream(TimeZone.getAvailableIDs())
                    .sorted(Comparator.comparing(tz -> tz))
                    .collect(Utilities.toLinkedHashMap(tz -> tz, tz -> tz)));
        }
        return timezones;
    }

    public static class EnumAsObj {
        private String constant;
        private String label;
        private int level;

        public String getConstant() {
            return constant;
        }

        public void setConstant(String constant) {
            this.constant = constant;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }
}
