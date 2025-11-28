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
package org.tightblog.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.WebloggerTest;
import org.tightblog.dao.WeblogCategoryDao;
import org.tightblog.domain.User;
import org.tightblog.domain.WeblogCategory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntry.PubStatus;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.service.WeblogManager.WeblogCategoryData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Weblog Category related business operations.
 */
@SpringBootTest
@Transactional
public class WeblogManagerCategoryIT extends WebloggerTest {

    private User testUser;
    private Weblog testWeblog;
    private WeblogCategory testCat;

    @Autowired
    private UserManager userManager;

    @Autowired
    private WeblogCategoryDao weblogCategoryDao;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("categoryTestUser");
        testWeblog = setupWeblog("category-test-weblog", testUser);

        // setup several categories for testing
        setupWeblogCategory(testWeblog, "catTest-cat1");
        setupWeblogCategory(testWeblog, "catTest-cat2");

        // a simple test cat at the root level
        testCat = setupWeblogCategory(testWeblog, "catTest-testCat");
    }
    
    @AfterEach
    public void tearDown() {
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    @Test
    public void testHasCategory() {
        // check that root has category
        assertTrue(testWeblog.hasCategory(testCat.getName()));
    }
    
    @Test
    public void testLookupCategoryById() {
        Optional<WeblogCategory> maybeCat = weblogCategoryDao.findById(testCat.getId());
        assertTrue(maybeCat.isPresent());
        assertEquals(maybeCat.get(), testCat);
    }

    @Test
    public void testLookupAllCategoriesByWeblog() {
        List<WeblogCategoryData> cats = weblogManager.getWeblogCategoryData(testWeblog);
        assertNotNull(cats);
        assertEquals(4, cats.size());
    }

    @Test
    public void testMoveWeblogCategoryContents() {

        // add some categories and entries to test with
        testWeblog.addCategory("c1");
        testWeblog.addCategory("dest");
        weblogManager.saveWeblog(testWeblog, true);

        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        setupWeblogEntry("e1", "c1", PubStatus.PUBLISHED, testWeblog, testUser);
        setupWeblogEntry("e2", "c1", PubStatus.DRAFT, testWeblog, testUser);

        // need to query for cats again since session was closed
        WeblogCategory fromCat = weblogCategoryDao.findByWeblogAndName(testWeblog, "c1");
        WeblogCategory toCat = weblogCategoryDao.findByWeblogAndName(testWeblog, "dest");
        // verify number of entries in each category
        assertNotNull(fromCat);
        assertNotNull(toCat);
        assertEquals(0, retrieveWeblogEntries(toCat, false).size());
        assertEquals(0, retrieveWeblogEntries(toCat, true).size());
        assertEquals(2, retrieveWeblogEntries(fromCat, false).size());
        assertEquals(1, retrieveWeblogEntries(fromCat, true).size());

        // move contents of source category c1 to destination category dest
        weblogEntryManager.moveWeblogCategoryContents(fromCat, toCat);

        // after move, verify number of entries in each category
        fromCat = weblogCategoryDao.findByWeblogAndName(testWeblog, "c1");
        toCat = weblogCategoryDao.findByWeblogAndName(testWeblog, "dest");

        assertNotNull(fromCat);
        assertNotNull(toCat);

        // Hierarchy is flattened under dest
        assertEquals(2, retrieveWeblogEntries(toCat, false).size());
        assertEquals(1, retrieveWeblogEntries(toCat, true).size());

        // c1 category should be empty
        assertEquals(0, retrieveWeblogEntries(fromCat, false).size());
    }

    private List<WeblogEntry> retrieveWeblogEntries(WeblogCategory category, boolean publishedOnly) {
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(category.getWeblog());
        wesc.setCategoryName(category.getName());
        if (publishedOnly) {
            wesc.setStatus(PubStatus.PUBLISHED);
        }
        return weblogEntryManager.getWeblogEntries(wesc);
    }

    private WeblogCategory setupWeblogCategory(Weblog weblog, String name) {
        weblog.addCategory(name);
        weblogManager.saveWeblog(weblog, true);

        // query for object
        WeblogCategory checkCat = weblogCategoryDao.findByWeblogAndName(weblog, name);
        if (checkCat == null) {
            throw new IllegalStateException("error setting up weblog category");
        }
        return checkCat;
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testBasicCRUD() {
        String categoryName = "origCatName";
        String updatedCategoryName = "updatedCatName";

        // make sure we are starting with 4 categories from setup
        assertEquals(4, testWeblog.getWeblogCategories().size());

        // add a new category
        testWeblog.addCategory(categoryName);
        weblogManager.saveWeblog(testWeblog, true);

        // make sure category was added
        WeblogCategory catFromDb = weblogCategoryDao.findByWeblogAndName(testWeblog, categoryName);
        assertNotNull(catFromDb);

        // make sure category count increased
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertEquals(5, testWeblog.getWeblogCategories().size());

        // update category
        Optional<WeblogCategory> maybeTest = testWeblog.getWeblogCategories().stream()
                .filter(wc -> wc.getName().equals(categoryName)).findFirst();
        assertTrue(maybeTest.isPresent());
        maybeTest.get().setName(updatedCategoryName);
        weblogManager.saveWeblog(testWeblog, true);

        // verify category was updated
        catFromDb = weblogCategoryDao.findByWeblogAndName(testWeblog, updatedCategoryName);
        assertNotNull(catFromDb);
        WeblogCategory catFromDbOldName = weblogCategoryDao.findByWeblogAndName(testWeblog, categoryName);
        assertNull(catFromDbOldName);
        assertEquals(updatedCategoryName, catFromDb.getName());
        assertEquals(5, testWeblog.getWeblogCategories().size());

        // remove category
        testWeblog.getWeblogCategories().remove(catFromDb);
        weblogManager.saveWeblog(testWeblog, true);

        // make sure cat was removed
        catFromDb = weblogCategoryDao.findByWeblogAndName(testWeblog, updatedCategoryName);
        assertNull(catFromDb);

        // make sure category count decreased
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertEquals(4, testWeblog.getWeblogCategories().size());
    }

}
