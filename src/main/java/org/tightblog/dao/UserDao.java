/*
 * Copyright 2018 the original author or authors.
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
package org.tightblog.dao;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tightblog.domain.User;
import org.tightblog.domain.UserStatus;

import java.util.List;

@Repository
public interface UserDao extends JpaRepository<User, String> {

    @Cacheable(value = "enabledUsers")
    @Query("SELECT u FROM User u WHERE u.userName= ?1 AND u.status = org.tightblog.domain.UserStatus.ENABLED")
    User findEnabledByUserName(String userName);

    @CacheEvict(cacheNames = {"enabledUsers"}, key = "#user.userName")
    default void evictUser(User user) {
        // no-op
    }

    default List<User> findByStatusEnabled() {
        return findByStatusIn(List.of(UserStatus.ENABLED));
    }

    List<User> findByStatusIn(List<UserStatus> statuses);

    @Query("SELECT u FROM User u WHERE u.globalRole=org.tightblog.domain.GlobalRole.ADMIN " +
            "AND u.status = org.tightblog.domain.UserStatus.ENABLED")
    List<User> findAdmins();

    User findByActivationCode(String activationCode);

    @Query("SELECT u FROM User u WHERE u.status = org.tightblog.domain.UserStatus.EMAILVERIFIED " +
            "order by u.userName")
    List<User> findUsersToApprove();

    User findByUserName(String userName);

    User findByScreenName(String screenName);

    User findByEmailAddress(String emailAddress);

    default User findByIdOrNull(String id) {
        return findById(id).orElse(null);
    }
}
