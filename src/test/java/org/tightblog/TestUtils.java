/*
 * Copyright 2019 the original author or authors.
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
package org.tightblog;

import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.tightblog.domain.EntryPublishStatus;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.dto.WeblogEntryDto;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.rendering.service.ThymeleafRenderer;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static final String BLOG_HANDLE = "myblog";
    public static final String ENTRY_ANCHOR = "entry-anchor";

    public static HttpServletRequest createMockServletRequestForWeblogEntryRequest() {
        return createBaseMockServletRequest(addBlogHandle("page/%s/entry/" + ENTRY_ANCHOR));
    }

    public static HttpServletRequest createMockServletRequestForWeblogHomePageRequest() {
        return createBaseMockServletRequest(addBlogHandle("page/%s"));
    }

    public static HttpServletRequest createMockServletRequestForWeblogFeedRequest() {
        return createBaseMockServletRequest(addBlogHandle("feed/%s"));
    }

    private static String addBlogHandle(String urlPath) {
        return String.format("/tb-ui/rendering/" + urlPath, BLOG_HANDLE);
    }

    @SuppressWarnings("unchecked")
    public static WeblogPageRequest extractWeblogPageRequestFromMockRenderer(ThymeleafRenderer mockRenderer)
            throws IOException {
        ArgumentCaptor<Map<String, Object>> captor = (ArgumentCaptor<Map<String, Object>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(Map.class);
        verify(mockRenderer).render(any(), captor.capture());
        Map<String, Object> results = captor.getValue();
        return (WeblogPageRequest) results.get("model");
    }

    @SuppressWarnings("unchecked")
    public static WeblogSearchRequest extractWeblogSearchRequestFromMockRenderer(ThymeleafRenderer mockRenderer)
            throws IOException {
        ArgumentCaptor<Map<String, Object>> captor = (ArgumentCaptor<Map<String, Object>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(Map.class);
        verify(mockRenderer).render(any(), captor.capture());
        Map<String, Object> results = captor.getValue();
        return (WeblogSearchRequest) results.get("model");
    }

    // Spring REST parses servlet path directly, so not necessary to place in HttpServletRequest object.
    public static HttpServletRequest createMockServletRequest() {
        return createBaseMockServletRequest(null);
    }

    private static HttpServletRequest createBaseMockServletRequest(String servletPath) {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("bob");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getServletPath()).thenReturn(servletPath);
        when(mockRequest.getLocale()).thenReturn(Locale.GERMAN);
        when(mockRequest.getUserPrincipal()).thenReturn(mockPrincipal);

        // have page need refreshing
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli());

        return mockRequest;
    }

    /**
     * Some tests are expected to log an exception while running, this notice
     * helps to separate expected exceptions vs. non-expected ones in the logging
     *
     * @param log           - logger to log to
     * @param exceptionType - name of exception (IllegalArgumentException, FileNotFoundException, etc.)
     */
    public static void logExpectedException(Logger log, String exceptionType) {
        log.info("Test is expected to log a/an {}:", exceptionType);
    }

    public static WeblogEntryDto genWeblogEntryDto(String anchor, Instant publishTime) {
        return new WeblogEntryDto(UUID.randomUUID().toString(), null, anchor, publishTime,
                EntryPublishStatus.PUBLISHED, null);
    }

    public static WeblogEntryDto genWeblogEntryDto(String id, String anchor, Instant publishTime, String title,
                                                   EntryPublishStatus status) {
        return new WeblogEntryDto(id, title, anchor, publishTime, status, null);
    }

    public static WeblogEntry genWeblogEntry(Weblog weblog, String anchor, Instant publishTime) {
        WeblogEntry entry = new WeblogEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setWeblog(weblog);
        entry.setAnchor(anchor);
        entry.setStatus(EntryPublishStatus.PUBLISHED);
        entry.setPublishTime(publishTime);
        return entry;
    }

    /**
     * Sets up a mock WeblogEntryManager so that toDto() calls TestUtils.genWeblogEntryDto
     * with anchor and publishTime from the WeblogEntry argument.
     */
    public static void mockWeblogEntryManagerToDto(org.tightblog.service.WeblogEntryManager mockManager) {
        when(mockManager.toDto(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg instanceof WeblogEntry entry) {
                String anchor = entry.getAnchor() != null ? entry.getAnchor() : "default-anchor";
                java.time.Instant publishTime = entry.getPublishTime();
                return genWeblogEntryDto(entry.getId(), anchor, publishTime, entry.getTitle(), entry.getStatus());
            }
            return null;
        });
    }

    public static boolean weblogEntryEqualsDto(WeblogEntry entry, WeblogEntryDto dto) {
        if (entry == null || entry.getId() == null || dto == null) {
            return false;
        }
        boolean publishTimeEqual = (entry.getPublishTime() == null && dto.publishTime() == null)
                || (entry.getPublishTime() != null && entry.getPublishTime().equals(dto.publishTime()));
        return entry.getId().equals(dto.id())
                && entry.getAnchor().equals(dto.anchor())
                && publishTimeEqual
                && entry.getStatus().equals(dto.status());
    }
}