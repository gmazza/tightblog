/*
 *
 * Copyright 2016 the original author or authors.
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
package org.tightblog.bloggerui.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.config.DynamicProperties;
import org.tightblog.bloggerui.model.Violation;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.AtomEnclosure;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogCategory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntry.PubStatus;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.domain.WeblogRole;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogCategoryDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
@RequestMapping(path = "/tb-ui/authoring/rest/weblogentries")
public class WeblogEntryController {

    private static final Logger LOG = LoggerFactory.getLogger(WeblogEntryController.class);

    private final WeblogDao weblogDao;
    private final WeblogEntryDao weblogEntryDao;
    private final WeblogCategoryDao weblogCategoryDao;
    private final UserDao userDao;
    private final UserManager userManager;
    private final WeblogManager weblogManager;
    private final WeblogEntryManager weblogEntryManager;
    private final LuceneIndexer luceneIndexer;
    private final URLService urlService;
    private final MessageSource messages;
    private final WeblogEntryCommentDao weblogEntryCommentDao;
    private final DynamicProperties dp;

    // Max Tag options to display for autocomplete
    private final int maxAutocompleteTags;

    private record TagAutocompleteData(String prefix, List<WeblogEntryTagAggregate> tagcounts) { }
    private record WeblogEntryData(List<WeblogEntry> entries, boolean hasMore) { }
    private record RecentWeblogEntryData(String id, String title, String entryEditURL) { }

    @Autowired
    public WeblogEntryController(WeblogDao weblogDao, WeblogCategoryDao weblogCategoryDao,
                                 UserDao userDao, UserManager userManager, WeblogManager weblogManager,
                                 WeblogEntryManager weblogEntryManager, LuceneIndexer luceneIndexer,
                                 URLService urlService, MessageSource messages,
                                 WeblogEntryDao weblogEntryDao, DynamicProperties dp,
                                 WeblogEntryCommentDao weblogEntryCommentDao,
                                 @Value("${max.autocomplete.tags:20}") int maxAutocompleteTags) {
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.weblogCategoryDao = weblogCategoryDao;
        this.userDao = userDao;
        this.userManager = userManager;
        this.weblogManager = weblogManager;
        this.weblogEntryManager = weblogEntryManager;
        this.luceneIndexer = luceneIndexer;
        this.urlService = urlService;
        this.messages = messages;
        this.dp = dp;
        this.weblogEntryCommentDao = weblogEntryCommentDao;
        this.maxAutocompleteTags = maxAutocompleteTags;
    }

    // number of entries to show per page
    private static final int ITEMS_PER_PAGE = 20;

    @GetMapping(value = "/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntry), #id, 'POST')")
    public WeblogEntry getWeblogEntry(@PathVariable String id, Principal p) {

        WeblogEntry entry = weblogEntryDao.findByIdOrNull(id);
        entry.setWeblogEntryCommentDao(weblogEntryCommentDao);
        entry.setPermalink(urlService.getWeblogEntryURL(entry));
        entry.setPreviewUrl(urlService.getWeblogEntryDraftPreviewURL(entry));

        if (entry.getPublishTime() != null) {
            entry.setCreator(null);
        }

        return entry;
    }

    @PostMapping(value = "/{weblogId}/page/{page}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public WeblogEntryData getWeblogEntries(@PathVariable String weblogId, @PathVariable int page,
                                            @RequestBody WeblogEntrySearchCriteria criteria, Principal p) {

        Weblog weblog = weblogDao.findByIdOrNull(weblogId);

        criteria.setWeblog(weblog);
        criteria.setOffset(page * ITEMS_PER_PAGE);
        criteria.setMaxResults(ITEMS_PER_PAGE + 1);
        criteria.setCalculatePermalinks(true);
        List<WeblogEntry> rawEntries = weblogEntryManager.getWeblogEntries(criteria);

        List<WeblogEntry> entries = rawEntries.stream()
                .peek(re -> re.setWeblog(null))
                .peek(re -> re.getCategory().setWeblog(null)).collect(Collectors.toList());

        boolean hasMore = false;
        if (rawEntries.size() > ITEMS_PER_PAGE) {
            entries.remove(entries.size() - 1);
            hasMore = true;
        }

        return new WeblogEntryData(entries, hasMore);
    }

    @GetMapping(value = "/{weblogId}/recententries/{pubStatus}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public List<RecentWeblogEntryData> getRecentEntries(@PathVariable String weblogId, @PathVariable PubStatus pubStatus,
                                              Principal p) {

        Weblog weblog = weblogDao.findByIdOrNull(weblogId);

        List<RecentWeblogEntryData> entries = new ArrayList<>();
        if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setMaxResults(20);
            wesc.setStatus(pubStatus);

            List<WeblogEntry> fullEntries = weblogEntryManager.getWeblogEntries(wesc);
            entries.addAll(fullEntries.stream().map(e -> new RecentWeblogEntryData(e.getId(),
                    e.getTitle(), urlService.getEntryEditURL(e))).collect(Collectors.toList()));
        }
        return entries;
    }

    @GetMapping(value = "/{id}/tagdata")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public TagAutocompleteData getWeblogTagData(@PathVariable String id, @RequestParam("prefix") String prefix) {
        List<WeblogEntryTagAggregate> tags;
        Weblog weblog = weblogDao.findById(id).orElse(null);
        tags = weblogManager.getTags(weblog, null, prefix, 0, maxAutocompleteTags);
        return new TagAutocompleteData(prefix, tags);
    }

    // publish
    // save
    @PostMapping(value = "/{weblogId}/entries")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public ResponseEntity<?> postEntry(@PathVariable String weblogId, @Valid @RequestBody WeblogEntry entryData,
                                       Locale locale, Principal p) {

        boolean createNew = false;
        WeblogEntry entry = null;

        if (entryData.getId() != null) {
            entry = weblogEntryDao.findByIdOrNull(entryData.getId());
        }

        // Check user permissions
        User user = userDao.findEnabledByUserName(p.getName());
        Weblog weblog = (entry == null) ? weblogDao.findById(weblogId).orElse(null)
                : entry.getWeblog();

        WeblogRole necessaryRole = WeblogRole.POST;
        if (weblog != null && userManager.checkWeblogRole(user, weblog, necessaryRole)) {

            // create new?
            if (entry == null) {
                createNew = true;
                entry = new WeblogEntry();
                entry.setCreator(user);
                entry.setWeblog(weblog);
                entryData.setWeblog(weblog);
            }

            entry.setPublishTime((entryData.getPublishTime() != null) ? entryData.getPublishTime() : Instant.now());

            if (PubStatus.PUBLISHED.equals(entryData.getStatus()) &&
                    entry.getPublishTime().isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
                entryData.setStatus(PubStatus.SCHEDULED);
            }

            entry.setEditFormat(entryData.getEditFormat());
            entry.setStatus(entryData.getStatus());
            entry.setTitle(entryData.getTitle());
            entry.setText(entryData.getText());
            entry.setSummary(entryData.getSummary());
            entry.setNotes(entryData.getNotes());
            entry.updateTags(Arrays.stream(entryData.getTagsAsString().split("\\s+"))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet()));
            entry.setSearchDescription(entryData.getSearchDescription());
            entry.setEnclosureUrl(entryData.getEnclosureUrl());
            WeblogCategory category = weblogCategoryDao.findByWeblogAndName(weblog, entryData.getCategory().getName());
            if (category != null) {
                entry.setCategory(category);
            } else {
                throw new IllegalArgumentException("Category is invalid.");
            }

            entry.setCommentDays(entryData.getCommentDays());

            if (!StringUtils.isEmpty(entry.getEnclosureUrl())) {
                // Fetch MediaCast resource
                LOG.debug("Checking MediaCast attributes");
                AtomEnclosure enclosure;

                try {
                    enclosure = weblogEntryManager.generateEnclosure(entry.getEnclosureUrl());
                } catch (IllegalArgumentException e) {
                    return ValidationErrorResponse.badRequest(new Violation(
                            messages.getMessage("entryEdit.enclosureURL", null, locale),
                            messages.getMessage(e.getMessage(), null, locale)));
                }

                // set enclosure attributes
                entry.setEnclosureUrl(enclosure.getUrl());
                entry.setEnclosureType(enclosure.getContentType());
                entry.setEnclosureLength(enclosure.getLength());
            }

            weblogEntryManager.saveWeblogEntry(entry);
            dp.updateLastSitewideChange();

            // notify search of the new entry
            if (entry.isPublished()) {
                luceneIndexer.updateIndex(entry, false);
            } else if (!createNew) {
                luceneIndexer.updateIndex(entry, true);
            }

            return ResponseEntity.ok(entry.getId());
        } else {
            return ResponseEntity.status(403).body(messages.getMessage("error.title.403", null, locale));
        }
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntry), #id, 'POST')")
    public ResponseEntity<?> deleteWeblogEntry(@PathVariable String id, Principal p, Locale locale) {
        LOG.info("Call to remove entry {}", id);

        WeblogEntry entry = weblogEntryDao.findByIdOrNull(id);
        if (entry != null) {
            // remove from search index
            if (entry.isPublished()) {
                luceneIndexer.updateIndex(entry, true);
            }
            weblogEntryManager.removeWeblogEntry(entry);
            dp.updateLastSitewideChange();

            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(404).body(messages.getMessage("error.title.404", null, locale));
        }
    }
}
