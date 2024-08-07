/*
 * Copyright 2018-2021 the original author or authors.
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
package org.tightblog.rendering.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogDao;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class AbstractControllerTest {

    @Test
    public void testGetModelMap() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        URLModel mockURLModel = mock(URLModel.class);
        when(mockURLModel.getModelName()).thenReturn("url");
        Model mockModel = mock(Model.class);
        when(mockModel.getModelName()).thenReturn("mockModel");
        Set<Model> modelSet = new HashSet<>();
        modelSet.add(mockURLModel);
        modelSet.add(mockModel);
        when(mockContext.getBean(eq("testBean"), eq(Set.class))).thenReturn(modelSet);
        WeblogPageRequest req = new WeblogPageRequest("myblog", null, mock(PageModel.class));
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", req);
        UserDao mockUD = mock(UserDao.class);
        WeblogDao mockWD = mock(WeblogDao.class);
        PageController processor = new PageController(mockWD, null, null,
                null, null, null, null,
                mockUD);
        processor.setApplicationContext(mockContext);
        Map<String, Object> modelMap = processor.getModelMap("testBean", initData);
        assertEquals(mockURLModel, modelMap.get("url"));
        assertEquals(mockModel, modelMap.get("mockModel"));
        verify(mockModel).init(initData);
    }

    @Test
    public void testRespondIfNotModified() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        // test return date if valid
        Instant now = Instant.now();
        when(mockRequest.getDateHeader("If-Modified-Since")).thenReturn(now.toEpochMilli());
        long val = PageController.getBrowserCacheExpireDate(mockRequest);
        assertEquals(now.toEpochMilli(), val);

        // test return -1 on invalid date
        when(mockRequest.getDateHeader("If-Modified-Since")).thenThrow(new IllegalArgumentException());
        val = PageController.getBrowserCacheExpireDate(mockRequest);
        assertEquals(-1, val);
    }

}
