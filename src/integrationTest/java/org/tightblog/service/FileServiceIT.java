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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.tightblog.WebloggerTest;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WebloggerProperties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertNull;

public class FileServiceIT extends WebloggerTest {

    private static final Logger LOG = LoggerFactory.getLogger(FileServiceIT.class);

    private User testUser;
    private Weblog testWeblog;

    @Value("${mediafiles.storage.dir}")
    private String storageDir;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("FCMTestUserName1");
        testWeblog = setupWeblog("fcm-test-handle1", testUser);
    }

    @AfterEach
    public void tearDown() {
        WebloggerProperties props = webloggerPropertiesDao.findOrNull();
        props.setMaxFileUploadsSizeMb(30000);
        webloggerPropertiesDao.saveAndFlush(props);
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    @Test
    public void testFileSaveAndDelete() throws Exception {
        WebloggerProperties props = webloggerPropertiesDao.findOrNull();
        props.setMaxFileUploadsSizeMb(1);
        webloggerPropertiesDao.saveAndFlush(props);

        FileService fileService = new FileService(webloggerPropertiesDao,
                true, storageDir, Set.of("image/jpeg"), "3MB");

        // File should not exist initially
        WebloggerTest.logExpectedException(LOG, "FileNotFoundException");
        File test = fileService.getFileContent(testWeblog, "bookmarks-file-id");
        assertNull("Non-existent file retrieved", test);

        // store a file
        try (InputStream is = getClass().getResourceAsStream("/hawk.jpg")) {
            fileService.saveFileContent(testWeblog, "bookmarks-file-id", is);
        }

        // make sure file was stored successfully
        WebloggerTest.logExpectedException(LOG, "FileNotFoundException");
        File fileContent1 = fileService.getFileContent(testWeblog, "bookmarks-file-id");
        assertNotNull(fileContent1);

        // delete file
        fileService.deleteFile(testWeblog, "bookmarks-file-id");

        // File should not exist after delete
        test = fileService.getFileContent(testWeblog, "bookmarks-file-id");
        assertNull("Non-existent file retrieved", test);
    }

    @Test
    public void testCanSave() throws IOException {
        FileService fileService = new FileService(webloggerPropertiesDao,
                true, storageDir, Set.of("image/*"), "1MB");
        FileService fileService1KB = new FileService(webloggerPropertiesDao,
                true, storageDir, Set.of("image/*"), "1KB");

        InputStream inputStream = getClass().getResourceAsStream("/hawk.jpg");
        MultipartFile mockMultipartFile = new MockMultipartFile(
                "file",          // field name
                "hawk.jpg",            // original file name
                "image/jpeg",          // 99KB,
                inputStream            // file content
        );

        boolean canSave = fileService1KB.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        // file too big
        assertFalse(canSave);

        // file small enough
        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertTrue(canSave);

        // gifs no longer allowed
        fileService = new FileService(webloggerPropertiesDao,
                true, storageDir, Set.of("image/png"), "1MB");

        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertFalse(canSave);

        // right-side wildcards work
        fileService = new FileService(webloggerPropertiesDao,
                true, storageDir, Set.of("image/*"), "1MB");

        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertTrue(canSave);

        // uploads disabled should fail
        fileService = new FileService(webloggerPropertiesDao,
                false, storageDir, Set.of("image/png"), "1MB");

        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertFalse(canSave);
    }
}
