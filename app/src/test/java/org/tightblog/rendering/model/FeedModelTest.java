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
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.requests.WeblogFeedRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeedModelTest {

    private WebloggerProperties webloggerProperties;
    private WeblogFeedRequest feedRequest;
    private FeedModel feedModel;
    private Weblog weblog;

    @Before
    public void initialize() {
        JPAPersistenceStrategy jpaMock = mock(JPAPersistenceStrategy.class);
        webloggerProperties = new WebloggerProperties();
        when(jpaMock.getWebloggerProperties()).thenReturn(webloggerProperties);
        feedRequest = new WeblogFeedRequest();
        weblog = new Weblog();
        feedRequest.setWeblog(weblog);
        feedRequest.setWeblogCategoryName("stamps");
        feedRequest.setTag("collectibles");
        feedRequest.setPageNum(16);
        feedRequest.setSiteWide(true);
        feedModel = new FeedModel();
        feedModel.setPersistenceStrategy(jpaMock);
        Map<String, Object> initVals = new HashMap<>();
        initVals.put("parsedRequest", feedRequest);
        feedModel.init(initVals);
    }

    @Test
    public void getWeblogEntriesPager() throws Exception {
        WeblogEntryListGenerator generator = mock(WeblogEntryListGenerator.class);
        feedModel.setWeblogEntryListGenerator(generator);
        webloggerProperties.setNewsfeedItemsPage(21);
        feedModel.getWeblogEntriesPager();
        verify(generator).getChronoPager(weblog, null, "stamps",
                "collectibles", 16, 21, true);
    }

    @Test
    public void getLastUpdated() throws Exception {
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);
        webloggerProperties.setLastWeblogChange(threeDaysAgo);

        Instant test = feedModel.getLastUpdated();
        assertEquals(threeDaysAgo, test);

        feedRequest.setSiteWide(false);
        test = feedModel.getLastUpdated();
        assertEquals(twoDaysAgo, test);
    }
}