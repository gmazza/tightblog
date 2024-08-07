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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.service.ThymeleafRenderer;
import org.tightblog.dao.WeblogDao;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreviewControllerTest {

    private static final String ENTRY_ANCHOR = "entry-anchor";

    private PreviewController controller;
    private Weblog weblog;

    private WeblogEntryManager mockWEM;
    private WeblogDao mockWeblogDao;
    private ThymeleafRenderer mockRenderer;
    private WeblogTheme mockTheme;
    private ApplicationContext mockApplicationContext;
    private Principal mockPrincipal;
    private AutoCloseable mockCloseable;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @BeforeEach
    public void initializeMocks() {
        try {
            mockPrincipal = mock(Principal.class);
            when(mockPrincipal.getName()).thenReturn("bob");

            mockWeblogDao = mock(WeblogDao.class);
            weblog = new Weblog();
            when(mockWeblogDao.findByIdOrNull(weblog.getId())).thenReturn(weblog);

            mockRenderer = mock(ThymeleafRenderer.class);
            CachedContent cachedContent = new CachedContent(Template.Role.JAVASCRIPT);
            when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

            ThemeManager mockThemeManager = mock(ThemeManager.class);
            mockWEM = mock(WeblogEntryManager.class);
            WeblogEntry entry = new WeblogEntry();
            entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
            when(mockWEM.getWeblogEntryByAnchor(weblog, ENTRY_ANCHOR)).thenReturn(entry);

            mockTheme = mock(WeblogTheme.class);
            when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
            SharedTheme sharedTheme = new SharedTheme();
            when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

            controller = new PreviewController(mockWeblogDao, mockRenderer, mockThemeManager, mock(PageModel.class),
                    mockWEM);

            mockApplicationContext = mock(ApplicationContext.class);
            when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet<>());
            controller.setApplicationContext(mockApplicationContext);

            mockCloseable = MockitoAnnotations.openMocks(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterEach
    public void closeMocks() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWeblogDao.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
        ResponseEntity<Resource> result = controller.getEntryPreview(weblog.getId(), "myanchor", mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testCorrectTemplatesChosen() throws IOException {
        SharedTemplate weblogTemplate = new SharedTemplate();
        weblogTemplate.setRole(Template.Role.WEBLOG);
        weblogTemplate.setName("myweblogtemplate");
        when(mockTheme.getTemplateByRole(Template.Role.WEBLOG)).thenReturn(weblogTemplate);

        // Permalink template retrieved for a weblog entry
        Mockito.clearInvocations(mockRenderer);
        SharedTemplate permalinkTemplate = new SharedTemplate();
        permalinkTemplate.setRole(Template.Role.PERMALINK);
        permalinkTemplate.setName("mypermalinktemplate");
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(permalinkTemplate);
        controller.getEntryPreview(weblog.getId(), ENTRY_ANCHOR, mockPrincipal);
        ArgumentCaptor<Template> templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        Template results = templateCaptor.getValue();
        assertEquals(permalinkTemplate.getName(), results.getName());
        assertEquals(Template.Role.PERMALINK, results.getRole());

        // Weblog template retrieved for a weblog entry if no permalink template
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(null);
        Mockito.clearInvocations(mockRenderer);
        controller.getEntryPreview(weblog.getId(), ENTRY_ANCHOR, mockPrincipal);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals(weblogTemplate.getName(), results.getName());
        assertEquals(Template.Role.WEBLOG, results.getRole());

        // not found returned if no weblog entry for given anchor
        when(mockWEM.getWeblogEntryByAnchor(weblog, ENTRY_ANCHOR)).thenReturn(null);
        ResponseEntity<Resource> result = controller.getEntryPreview(weblog.getId(), ENTRY_ANCHOR, mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException {
        URLModel mockURLModel = mock(URLModel.class);
        when(mockURLModel.getModelName()).thenReturn("model");
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(mockURLModel);
        when(mockApplicationContext.getBean(eq("pageModelSet"), eq(Set.class))).thenReturn(pageModelSet);
        // setting custom page name to allow for a template to be chosen and hence the rendering to occur
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Template.Role.CUSTOM_EXTERNAL);

        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(sharedTemplate);

        // testing that themes get "model" added to the rendering map.
        controller.getEntryPreview(weblog.getId(), ENTRY_ANCHOR, mockPrincipal);
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());

        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
    }
}
