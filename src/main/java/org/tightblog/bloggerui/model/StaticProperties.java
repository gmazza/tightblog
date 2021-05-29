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
import org.tightblog.util.Utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StaticProperties {
    private boolean mfaEnabled;

    private Map<String, String> userStatuses;
    private Map<String, String> globalRoles;

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

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

}
