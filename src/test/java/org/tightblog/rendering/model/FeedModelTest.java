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
package org.tightblog.rendering.model;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.service.WeblogEntryListGenerator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedModelTest {

    private WeblogEntryListGenerator mockWeblogEntryListGenerator;
    private FeedModel feedModel;
    private WeblogEntryManager mockWeblogEntryManager;
    private URLService mockURLService;

    @Before
    public void initialize() {
        mockWeblogEntryManager = mock(WeblogEntryManager.class);
        mockWeblogEntryListGenerator = mock(WeblogEntryListGenerator.class);
        mockURLService = mock(URLService.class);
        feedModel = new FeedModel(mockWeblogEntryListGenerator, mockWeblogEntryManager, mockURLService,
                "1.1", 20);
    }

    @Test
    public void testAccessors() {
        // make jacoco happy
        assertEquals("1.1", feedModel.getSystemVersion());
        assertEquals(mockURLService, feedModel.getURLService());
        assertEquals(mockWeblogEntryListGenerator, feedModel.getWeblogEntryListGenerator());
        assertEquals(20, feedModel.getNumEntriesPerPage());
    }

    @Test
    public void testPassThroughMethods() {
        when(mockWeblogEntryManager.processBlogText(Weblog.EditFormat.COMMONMARK, "testText"))
                .thenReturn("processedTestText");
        assertEquals("processedTestText", feedModel.render(Weblog.EditFormat.COMMONMARK, "testText"));
    }
}
