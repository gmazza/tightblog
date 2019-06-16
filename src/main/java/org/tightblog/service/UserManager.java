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
package org.tightblog.service;

import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.UserCredentials;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tightblog.repository.UserCredentialsRepository;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.UserWeblogRoleRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * User and weblog role management.
 */
@Component
public class UserManager {

    private UserRepository userRepository;
    private UserWeblogRoleRepository userWeblogRoleRepository;
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    public UserManager(UserRepository userRepository, UserWeblogRoleRepository uwrRepository,
                       UserCredentialsRepository userCredentialsRepository) {
        this.userRepository = userRepository;
        this.userWeblogRoleRepository = uwrRepository;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    /**
     * Remove a user.
     *
     * @param user User to be removed.
     */

    public void removeUser(User user) {
        userWeblogRoleRepository.deleteByUser(user);
        userRepository.delete(user);
        userRepository.evictUser(user);
    }

    /**
     * Update user password.  This method takes an unencrypted password
     * and will encrypt it prior to storing in database.
     *
     * @param userId      internal ID of user
     * @param newPassword unencrypted password.
     */
    public void updateCredentials(String userId, String newPassword) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPwd = encoder.encode(newPassword);
        userCredentialsRepository.updatePassword(userId, encodedPwd);
    }

    /**
     * Check user's rights given username and weblog handle.  Convenience
     * overload for callers having the weblog but not the user object.
     *
     * @param username Username whose role is being checked
     * @param weblog   Target weblog of the role
     * @param role     Minimum WeblogRole being checked for
     * @return false if user cannot be identified from the passed in username,
     * otherwise whatever checkWeblogRole(User, Weblog, WeblogRole) would return.
     */
    public boolean checkWeblogRole(String username, Weblog weblog, WeblogRole role) {
        User userToCheck = userRepository.findEnabledByUserName(username);
        if (userToCheck != null) {
            return checkWeblogRole(userToCheck, weblog, role);
        }
        return false;
    }

    /**
     * Check user's rights for a specified weblog
     *
     * @param user   User whose role is being checked
     * @param weblog Target weblog of the role
     * @param role   Minimum WeblogRole being checked for
     * @return true if user is a global Admin, or has given WeblogRole or a more powerful one
     */
    public boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role) {
        boolean hasRole = false;

        if (user != null & weblog != null) {
            if (GlobalRole.ADMIN.equals(user.getGlobalRole())) {
                hasRole = true;
            } else {
                UserWeblogRole existingRole = userWeblogRoleRepository.findByUserAndWeblog(user, weblog);
                if (existingRole != null && existingRole.hasEffectiveWeblogRole(role)) {
                    hasRole = true;
                }
            }
        }
        return hasRole;
    }

    /**
     * Grant user specific WeblogRole for a weblog.
     *
     * @param user    User to grant weblog role to
     * @param weblog  Weblog being granted access to
     * @param role    WeblogRole to grant
     */
    public void grantWeblogRole(User user, Weblog weblog, WeblogRole role) {
        UserWeblogRole roleCheck = userWeblogRoleRepository.findByUserAndWeblog(user, weblog);

        if (roleCheck != null) {
            roleCheck.setWeblogRole(role);
        } else {
            roleCheck = new UserWeblogRole(user, weblog, role);
            roleCheck.setEmailComments(true);
        }
        userWeblogRoleRepository.saveAndFlush(roleCheck);
        userWeblogRoleRepository.evictUserWeblogRole(user, weblog);
        userRepository.evictUser(user);
    }

    public void deleteUserWeblogRole(UserWeblogRole uwr) {
        userWeblogRoleRepository.delete(uwr);
        userWeblogRoleRepository.evictUserWeblogRole(uwr.getUser(), uwr.getWeblog());
    }

    /**
     * Generate a URL for the user's smartphone authenticator app code (for MFA)
     * @param user User to obtain QA code for
     * @return url Image URL for the QR code
     */
    public String generateMFAQRUrl(User user) {
        String url = "";
        UserCredentials uc = userCredentialsRepository.findByUserName(user.getUserName());

        if (uc != null) {
            if (uc.getMfaSecret() == null) {
                uc.setMfaSecret(Base32.random());
                userCredentialsRepository.saveAndFlush(uc);
            }
            url = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl="
                    + URLEncoder.encode(String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    "TightBlog", user.getEmailAddress(), uc.getMfaSecret(), "TightBlog"),
                    StandardCharsets.UTF_8);
        }

        return url;
    }
}
