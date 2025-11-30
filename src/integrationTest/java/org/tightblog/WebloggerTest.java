/*
   Copyright 2015-2021 Glen Mazza

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
package org.tightblog;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.UserStatus;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.service.LuceneIndexer;

import java.time.Instant;

@SpringBootTest
public abstract class WebloggerTest {

    @Container
    private static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        mysql.start();
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    protected WeblogDao weblogDao;

    @Autowired
    protected WeblogEntryDao weblogEntryDao;

    @Autowired
    protected WeblogEntryCommentDao weblogEntryCommentDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected WeblogManager weblogManager;

    @Autowired
    protected WeblogEntryManager weblogEntryManager;

    @Autowired
    private DynamicProperties dynamicProperties;

    @Autowired
    private LuceneIndexer luceneIndexer;

    @BeforeEach
    public void setUp() throws Exception {
        if (!dynamicProperties.isDatabaseReady()) {
            dynamicProperties.setDatabaseReady(true);
            luceneIndexer.initialize();
        }
    }

    protected User setupUser(String userName) {
        User testUser = new User();
        testUser.setUserName(userName.toLowerCase());
        testUser.setGlobalRole(GlobalRole.BLOGGER);
        testUser.setScreenName(userName);
        testUser.setEmailAddress(userName + "@dev.null");
        testUser.setDateCreated(Instant.now());
        testUser.setStatus(UserStatus.ENABLED);
        userDao.saveAndFlush(testUser);

        // query for the user to make sure we return the persisted object
        User user = userDao.findEnabledByUserName(userName.toLowerCase());

        if (user == null) {
            throw new IllegalStateException("error inserting new user");
        }

        return user;
    }

    protected Weblog setupWeblog(String handle, User creator) {

        Weblog testWeblog = new Weblog();
        testWeblog.setName("Test Weblog");
        testWeblog.setTagline("Test Weblog");
        testWeblog.setHandle(handle);
        testWeblog.setEditFormat(Weblog.EditFormat.HTML);
        testWeblog.setCommentSpamFilter("");
        testWeblog.setTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setCreator(creator);

        // add weblog
        weblogManager.addWeblog(testWeblog);

        // query for the new weblog and return it
        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(handle);

        if (weblog == null) {
            throw new IllegalStateException("error setting up weblog");
        }

        return weblog;
    }

    protected WeblogEntry setupWeblogEntry(String anchor, Weblog weblog, User user) {
        Weblog weblogInSession = weblogDao.findByIdOrNull(weblog.getId());
        return setupWeblogEntry(anchor, weblogInSession.getWeblogCategories().stream().findFirst().get().getName(),
                WeblogEntry.PubStatus.PUBLISHED, weblog, user);
    }

    protected WeblogEntry setupWeblogEntry(String anchor, String catName,
                                           WeblogEntry.PubStatus status, Weblog weblog, User user) {

        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setTitle(anchor);
        testEntry.setText("blah blah entry");
        testEntry.setEditFormat(Weblog.EditFormat.HTML);
        testEntry.setAnchor(anchor);
        testEntry.setPubTime(Instant.now());
        testEntry.setUpdateTime(Instant.now());
        testEntry.setStatus(status);
        testEntry.setWeblog(weblog);
        testEntry.setCreator(user);
        testEntry.setCategory(weblog.getWeblogCategories()
                .stream()
                .filter(cat -> cat.getName().equals(catName))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("need a category for the entry")));

        // store entry
        weblogEntryManager.saveWeblogEntry(testEntry);

        // query for object
        WeblogEntry entry = weblogEntryDao.findByIdOrNull(testEntry.getId());

        if (entry == null) {
            throw new IllegalStateException("error setting up weblog entry");
        }

        return entry;
    }

    protected WeblogEntryComment setupComment(String comment, WeblogEntry entry) {

        WeblogEntryComment testComment = new WeblogEntryComment();
        testComment.setName("test");
        testComment.setEmail("test");
        testComment.setUrl("test");
        testComment.setRemoteHost("foofoo");
        testComment.setContent(comment);
        testComment.setPostTime(Instant.now());
        testComment.setWeblogEntry(entry);
        testComment.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);

        // store testComment
        weblogEntryManager.saveComment(testComment, true);

        // query for object
        WeblogEntryComment commentTest = weblogEntryCommentDao.findByIdOrNull(testComment.getId());

        if (commentTest == null) {
            throw new IllegalStateException("error setting up comment");
        }

        return commentTest;
    }

    /**
     * Some tests are expected to log an exception while running, this notice
     * helps to separate expected exceptions vs. non-expected ones in the logging
     * @param log - logger to log to
     * @param exceptionType - name of exception (IllegalArgumentException, FileNotFoundException, etc.)
     */
    public static void logExpectedException(Logger log, String exceptionType) {
        log.info("Test is expected to log a/an {}:", exceptionType);
    }
}
