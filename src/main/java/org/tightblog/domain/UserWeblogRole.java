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
package org.tightblog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * WeblogRole that a user has for a specific weblog
 */
@Entity
@Table(name = "user_weblog_role")
public class UserWeblogRole extends AbstractEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Weblog weblog;

    @Column(name = "weblog_role")
    @Enumerated(EnumType.STRING)
    private WeblogRole weblogRole;

    @Column(name = "email_comments")
    private boolean emailComments;

    public UserWeblogRole() {
    }

    public UserWeblogRole(User user, Weblog weblog, WeblogRole weblogRole) {
        setWeblogRole(weblogRole);
        this.user = user;
        this.weblog = weblog;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public WeblogRole getWeblogRole() {
        return weblogRole;
    }

    public void setWeblogRole(WeblogRole weblogRole) {
        this.weblogRole = weblogRole;
    }

    public boolean isEmailComments() {
        return emailComments;
    }

    public void setEmailComments(boolean emailComments) {
        this.emailComments = emailComments;
    }

    public boolean hasEffectiveWeblogRole(WeblogRole roleToCheck) {
        return weblogRole.getWeight() >= roleToCheck.getWeight();
    }

    @Override
    public String toString() {
        return "UserWeblogRole: user=" + (user != null ? user.getUserName() : "(empty)")
                + ", weblog=" + weblog.getHandle() + ", role=" + weblogRole;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof UserWeblogRole && Objects.equals(id, ((UserWeblogRole) other).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
