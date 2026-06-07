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
package org.tightblog.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;

@Entity
@Table(name = "weblogger_user")
public class User extends AbstractEntity {

    @NotBlank(message = "{error.add.user.missingUserName}")
    @Pattern(regexp = "[a-z0-9]*", message = "{error.add.user.badUserName}")
    private String userName;

    @Column(name = "screen_name")
    @NotBlank(message = "{Register.error.screenNameNull}")
    private String screenName;

    @Column(name = "global_role")
    @Enumerated(EnumType.STRING)
    private GlobalRole globalRole;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.DISABLED;

    @Column(name = "email_address")
    @NotBlank(message = "{Register.error.emailAddressNull}")
    @Email(message = "{error.add.user.badEmail}")
    private String emailAddress;

    @Column(name = "activation_code")
    private String registrationActivationCode;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "password_hash")
    @NotBlank
    private String passwordHash;

    // for Authenticator app use
    // see http://www.baeldung.com/spring-security-two-factor-authentication-with-soft-token
    @Column(name = "mfa_secret")
    private String mfaSecret;

    public User() {
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public GlobalRole getGlobalRole() {
        return this.globalRole;
    }

    public void setGlobalRole(GlobalRole globalRole) {
        this.globalRole = globalRole;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean hasEffectiveGlobalRole(GlobalRole roleToCheck) {
        return globalRole.getWeight() >= roleToCheck.getWeight();
    }

    public String getScreenName() {
        return this.screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getRegistrationActivationCode() {
        return registrationActivationCode;
    }

    public void setRegistrationActivationCode(String activationCode) {
        this.registrationActivationCode = activationCode;
    }

    public String toString() {
        return "User: id=" + id + ", screenName=" + screenName + ", email=" + emailAddress + ", status=" + status;
    }

}
