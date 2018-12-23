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
package org.tightblog.rendering.cache;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.domain.Template;

import java.time.Instant;

import static org.junit.Assert.*;

public class LazyExpiringCacheTest {

    private LazyExpiringCache cache;
    private Instant twentySecondsAgo = Instant.now().minusSeconds(20);
    private Instant twentySecondsLater = twentySecondsAgo.plusSeconds(40);

    @Before
    public void initialize() {
        cache = new LazyExpiringCache();
        cache.setMaxEntries(10);
        cache.setTimeoutSec(3600);
        cache.setCacheHandlerId("testCache");
        cache.init();
    }

    @Test
    public void testAccessors() {
        assertEquals("testCache", cache.getCacheHandlerId());
        assertEquals(10, cache.getMaxEntries());
    }

    @Test
    public void testReturnNonExpiredValue() {
        CachedContent testContent = new CachedContent(Template.ComponentType.ATOMFEED);
        cache.put("abc", testContent);
        assertEquals(testContent, cache.get("abc", twentySecondsAgo));
        assertNotEquals(0, cache.getEstimatedSize());

        // test invalidate empties cache
        cache.invalidateAll();
        assertNull(cache.get("abc", twentySecondsAgo));

        // test cache stats are non-zero
        assertEquals(2, cache.getCacheRequestCount());
        assertEquals(1, cache.getCacheHitCount());
        assertEquals(1, cache.getCacheMissCount());
        assertEquals(0.5, cache.getCacheHitRate(), .001);
    }

    @Test
    public void testDisabledCacheResults() {
        cache.setMaxEntries(0);
        cache.init();
        CachedContent testContent = new CachedContent(Template.ComponentType.ATOMFEED);
        cache.put("abc", testContent);
        assertNull(cache.get("abc", twentySecondsAgo));
        assertEquals(0, cache.getCacheRequestCount());
        assertEquals(0, cache.getCacheHitCount());
        assertEquals(0, cache.getCacheMissCount());
        assertEquals(0, cache.getCacheHitRate(), .001);
        assertEquals(0, cache.getEstimatedSize());
    }

    @Test
    public void testReturnNullForExpiredValue() {
        CachedContent testContent = new CachedContent(Template.ComponentType.ATOMFEED);
        cache.put("abc", testContent);
        assertNull(cache.get("abc", twentySecondsLater));
    }

    @Test
    public void testReturnNullForNoncachedValue() {
        assertNull(cache.get("abc", twentySecondsAgo));
    }

    @Test
    public void testIncomingRequests() {
        assertEquals(0, cache.getIncomingRequests());
        cache.incrementIncomingRequests();
        cache.incrementIncomingRequests();
        cache.incrementIncomingRequests();
        assertEquals(3, cache.getIncomingRequests());
    }

    @Test
    public void testRequestsHandledBy304() {
        assertEquals(0, cache.getRequestsHandledBy304());
        cache.incrementRequestsHandledBy304();
        cache.incrementRequestsHandledBy304();
        assertEquals(2, cache.getRequestsHandledBy304());
    }

}
