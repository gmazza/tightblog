/*
   Copyright 2015 Glen Mazza

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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

// Blog-specific roles for a User
public enum WeblogRole {
    // For non-blog related pages (initial install, user registration, login, etc.)
    NOBLOGNEEDED(0),
    // User can post and delete blog entries, comment management and tags
    POST(1),
    // Post + Settings, Categories, Blogroll & Templates (full control)
    OWNER(2);

    private final int weight;

    public int getWeight() {
        return weight;
    }

    public boolean hasEffectiveRole(WeblogRole roleToCheck) {
        return weight >= roleToCheck.getWeight();
    }

    WeblogRole(int weight) {
        this.weight = weight;
    }

    @JsonCreator
    public static WeblogRole forValue(String value) {
        return WeblogRole.valueOf(value);
    }
}
