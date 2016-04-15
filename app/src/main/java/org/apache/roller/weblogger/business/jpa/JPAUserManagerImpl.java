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
package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.menu.Menu;
import org.apache.roller.weblogger.ui.core.menu.MenuHelper;
import org.apache.roller.weblogger.util.cache.ExpiringCache;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JPAUserManagerImpl implements UserManager {
    private static Log log = LogFactory.getLog(JPAUserManagerImpl.class);

    private final JPAPersistenceStrategy strategy;

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private MenuHelper menuHelper;

    public void setMenuHelper(MenuHelper menuHelper) {
        this.menuHelper = menuHelper;
    }

    private ExpiringCache editorMenuCache = null;

    public void setEditorMenuCache(ExpiringCache editorMenuCache) {
        this.editorMenuCache = editorMenuCache;
    }

    // cached mapping of userNames -> userIds
    private Map<String, String> userNameToIdMap = new HashMap<>();

    // cached mapping of screenNames -> userIds
    private Map<String, String> screenNameToIdMap = new HashMap<>();

    private Boolean makeFirstUserAdmin = true;

    public void setMakeFirstUserAdmin(Boolean makeFirstUserAdmin) {
        this.makeFirstUserAdmin = makeFirstUserAdmin;
    }

    protected JPAUserManagerImpl(JPAPersistenceStrategy strat) {
        log.debug("Instantiating JPA User Manager");
        this.strategy = strat;
    }


    //--------------------------------------------------------------- user CRUD
 
    public void saveUser(User data) throws WebloggerException {
        this.strategy.store(data);
    }

    
    public void removeUser(User user) throws WebloggerException {
        String userName = user.getUserName();
        
        // remove permissions, maintaining both sides of relationship
        List<UserWeblogRole> perms = getWeblogRolesIncludingPending(user);
        for (UserWeblogRole perm : perms) {
            this.strategy.remove(perm);
        }
        this.strategy.remove(user);

        // remove entry from cache mapping
        this.userNameToIdMap.remove(userName);
    }

    
    public void addUser(User newUser) throws WebloggerException {
        if (newUser == null) {
            throw new WebloggerException("cannot add null user");
        }
        
        List existingUsers = this.getUsers(null, Boolean.TRUE, 0, 1);

        if (existingUsers.size() == 0 && makeFirstUserAdmin) {
            // Make first user an admin
            newUser.setGlobalRole(GlobalRole.ADMIN);

            //if user was disabled (because of activation user 
            // account with e-mail property), enable it for admin user
            newUser.setEnabled(Boolean.TRUE);
            newUser.setActivationCode(null);
        } else {
            newUser.setGlobalRole(GlobalRole.BLOGGER);
        }

        if (getUserByUserName(newUser.getUserName(), null) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase(), null) != null) {
            throw new WebloggerException("error.add.user.userNameInUse");
        }

        if (getUserByScreenName(newUser.getScreenName()) != null ||
                getUserByScreenName(newUser.getScreenName().toLowerCase()) != null) {
            throw new WebloggerException("error.add.user.screenNameInUse");
        }

        this.strategy.store(newUser);
    }

    @Override
    public User getUser(String id) throws WebloggerException {
        return this.strategy.load(User.class, id);
    }

    @Override
    public SafeUser getSafeUser(String id) throws WebloggerException {
        return this.strategy.load(SafeUser.class, id);
    }

    //------------------------------------------------------------ user queries

    public User getUserByUserName(String userName) throws WebloggerException {
        return getUserByUserName(userName, Boolean.TRUE);
    }

    public User getUserByUserName(String userName, Boolean enabled)
            throws WebloggerException {

        if (userName==null) {
            throw new WebloggerException("userName cannot be null");
        }
        
        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {

            User user = this.getUser(
                    this.userNameToIdMap.get(userName));
            if (user != null) {
                // only return the user if the enabled status matches
                if(enabled == null || enabled.equals(user.getEnabled())) {
                    log.debug("userNameToIdMap CACHE HIT - "+userName);
                    return user;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.userNameToIdMap.remove(userName);
            }
        }

        // cache failed, do lookup
        TypedQuery<User> query;
        Object[] params;
        if (enabled != null) {
            query = strategy.getNamedQuery(
                    "User.getByUserName&Enabled", User.class);
            params = new Object[] {userName, enabled};
        } else {
            query = strategy.getNamedQuery(
                    "User.getByUserName", User.class);
            params = new Object[] {userName};
        }
        for (int i=0; i<params.length; i++) {
            query.setParameter(i+1, params[i]);
        }
        User user;
        try {
            user = query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }

        // add mapping to cache
        if(user != null) {
            log.debug("userNameToIdMap CACHE MISS - " + userName);
            this.userNameToIdMap.put(user.getUserName(), user.getId());
        }

        return user;
    }


    @Override
    public User getUserByScreenName(String screenName)
            throws WebloggerException {

        if (screenName==null) {
            throw new WebloggerException("screenName cannot be null");
        }

        // check cache first
        if(this.screenNameToIdMap.containsKey(screenName)) {

            User user = this.getUser(this.screenNameToIdMap.get(screenName));
            if (user != null) {
                log.debug("screenNameToIdMap CACHE HIT - "+screenName);
                return user;
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.screenNameToIdMap.remove(screenName);
            }
        }

        // cache failed, do lookup
        TypedQuery<User> query;
        Object[] params;
        query = strategy.getNamedQuery(
                "User.getByScreenName", User.class);
        params = new Object[] {screenName};

        for (int i=0; i<params.length; i++) {
            query.setParameter(i+1, params[i]);
        }
        User user;
        try {
            user = query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }

        // add mapping to cache
        if(user != null) {
            log.debug("screenNameToIdMap CACHE MISS - " + screenName);
            this.screenNameToIdMap.put(user.getUserName(), user.getId());
        }

        return user;
    }

    @Override
    public List<SafeUser> getUsers(String startsWith, Boolean enabled,
            int offset, int length) throws WebloggerException {
        TypedQuery<SafeUser> query;

        if (enabled != null) {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "SafeUser.getByEnabled&ScreenNameOrEmailAddressStartsWith", SafeUser.class);
                query.setParameter(1, enabled);
                query.setParameter(2, startsWith + '%');
                query.setParameter(3, startsWith + '%');
            } else {
                query = strategy.getNamedQuery(
                        "SafeUser.getByEnabled", SafeUser.class);
                query.setParameter(1, enabled);
            }
        } else {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "SafeUser.getByScreenNameOrEmailAddressStartsWith", SafeUser.class);
                query.setParameter(1, startsWith +  '%');
            } else {
                query = strategy.getNamedQuery("SafeUser.getAll", SafeUser.class);
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    
    public Map<String, Long> getUserNameLetterMap() throws WebloggerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<String, Long> results = new TreeMap<>();
        TypedQuery<Long> query = strategy.getNamedQuery(
                "User.getCountByUserNameLike", Long.class);
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            query.setParameter(1, currentChar + "%");
            List row = query.getResultList();
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }

    
    public List<SafeUser> getUsersByLetter(char letter, int offset, int length)
            throws WebloggerException {
        TypedQuery<SafeUser> query = strategy.getNamedQuery(
                "SafeUser.getByScreenNameOrderByScreenName", SafeUser.class);
        query.setParameter(1, letter + "%");
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    
    /**
     * Get count of users, enabled only
     */
    public long getUserCount() throws WebloggerException {
        TypedQuery<Long> q = strategy.getNamedQuery("User.getCountEnabledDistinct", Long.class);
        q.setParameter(1, Boolean.TRUE);
        List<Long> results = q.getResultList();
        return results.get(0);
    }

    public User getUserByActivationCode(String activationCode) throws WebloggerException {
        if (activationCode == null) {
            throw new WebloggerException("activationcode is null");
        }
        TypedQuery<User> q = strategy.getNamedQuery("User.getUserByActivationCode", User.class);
        q.setParameter(1, activationCode);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    
    //-------------------------------------------------------- permissions CRUD
 
    @Override
    public boolean checkWeblogRole(String username, String weblogHandle, WeblogRole role) throws WebloggerException {
        User userToCheck = getUserByUserName(username, true);
        Weblog weblogToCheck = weblogManager.getWeblogByHandle(weblogHandle);
        return checkWeblogRole(userToCheck, weblogToCheck, role);
    }

    @Override
    public boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role) {

        // if user has specified permission in weblog return true
        UserWeblogRole existingRole = getWeblogRole(user, weblog);
        if (existingRole != null && existingRole.hasEffectiveWeblogRole(role)) {
            return true;
        }

        // if Blog Server admin would still have any weblog role
        if (user.isGlobalAdmin()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("ROLE CHECK FAILED: user " + user.getUserName() + " does not have " + role.name()
                    + " or greater rights on weblog " + weblog.getHandle());
        }
        return false;
    }

    @Override
    public UserWeblogRole getWeblogRole(String username, String weblogHandle) throws WebloggerException {
        User userToCheck = getUserByUserName(username, true);
        Weblog weblogToCheck = weblogManager.getWeblogByHandle(weblogHandle);
        return getWeblogRole(userToCheck, weblogToCheck);
    }

    @Override
    public UserWeblogRole getWeblogRole(User user, Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogId"
                , UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public void grantWeblogRole(User user, Weblog weblog, WeblogRole role) throws WebloggerException {

        // first, see if user already has a permission for the specified object
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm = null;

        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // role already exists, so update it
        if (existingPerm != null) {
            existingPerm.setWeblogRole(role);
            existingPerm.setPending(false);
            this.strategy.store(existingPerm);
        } else {
            // it's a new association, so store it
            UserWeblogRole perm = new UserWeblogRole(user, weblog, role);
            this.strategy.store(perm);
        }
        editorMenuCache.remove(generateMenuCacheKey(user.getUserName(), weblog.getHandle()));
    }

    public void grantWeblogRole(String userId, Weblog weblog, WeblogRole role) throws WebloggerException {
        grantWeblogRole(getUser(userId), weblog, role);
    }

    @Override
    public void grantPendingWeblogRole(User user, Weblog weblog, WeblogRole desiredRole) throws WebloggerException {

        // first, see if user already has a role for the specified weblog
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingRole = null;
        try {
            existingRole = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // role already exists, so complain
        if (existingRole != null) {
            throw new WebloggerException("User already has a role for this weblog.");
        } else {
            // it's a new role, so store it
            UserWeblogRole newRole = new UserWeblogRole(user, weblog, desiredRole);
            newRole.setPending(true);
            this.strategy.store(newRole);
        }
    }

    
    public void acceptWeblogInvitation(User user, Weblog weblog) throws WebloggerException {

        // get specified permission
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm;
        try {
            existingPerm = q.getSingleResult();

        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }
        // set pending to false
        existingPerm.setPending(false);
        this.strategy.store(existingPerm);
        editorMenuCache.remove(generateMenuCacheKey(user.getUserName(), weblog.getHandle()));
    }

    
    public void declineWeblogInvitation(User user, Weblog weblog) throws WebloggerException {
        // get specified permission
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingRole;
        try {
            existingRole = q.getSingleResult();
        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: role not found");
        }
        this.strategy.remove(existingRole);
    }

    @Override
    public void revokeWeblogRole(User user, Weblog weblog) throws WebloggerException {
        // get specified role
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole oldrole;
        try {
            oldrole = q.getSingleResult();
        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: role not found");
        }
        this.strategy.remove(oldrole);
        editorMenuCache.remove(generateMenuCacheKey(user.getUserName(), weblog.getHandle()));
    }

    
    public List<UserWeblogRole> getWeblogRoles(User user) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        return q.getResultList();
    }

    public List<UserWeblogRole> getWeblogRolesIncludingPending(User user) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        return q.getResultList();
    }

    public List<UserWeblogRole> getWeblogRoles(Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogId",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    public List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    public List<UserWeblogRole> getPendingWeblogRoles(Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogId&Pending",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    @Override
    public Menu getEditorMenu(String username, String weblogHandle) {
        try {
            String cacheKey = generateMenuCacheKey(username, weblogHandle);
            Menu menu = (Menu) editorMenuCache.get(cacheKey);
            if (menu == null) {
                User user = getUserByUserName(username);
                UserWeblogRole uwr = getWeblogRole(username, weblogHandle);
                menu = menuHelper.getMenu("editor", user.getGlobalRole(), uwr.getWeblogRole(), null);
                editorMenuCache.put(cacheKey, menu);
            }
            return menu;
        } catch (WebloggerException e) {
            log.error("Problem generating editor window for User: "
                    + username + " and weblog: " + weblogHandle + ", message: " + e.getMessage());
        }
        return null;
    }

    private String generateMenuCacheKey(String username, String weblogHandle) {
        return "user/" + username + "/handle/" + weblogHandle;
    }

}
