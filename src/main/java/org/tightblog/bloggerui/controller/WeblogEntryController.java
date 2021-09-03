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
import org.tightblog.bloggerui.model.EntryEditMetadata;
import org.tightblog.bloggerui.model.TagAutocompleteData;
import org.tightblog.bloggerui.model.Violation;
import org.tightblog.bloggerui.model.WeblogEntryData;
import org.tightblog.bloggerui.model.WeblogEntrySaveResponse;
import org.tightblog.bloggerui.model.WeblogCategoryData;
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
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogCategoryDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.Utilities;
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

import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
@RequestMapping(path = "/tb-ui/authoring/rest/weblogentries")
public class WeblogEntryController {

    private static final Logger LOG = LoggerFactory.getLogger(WeblogEntryController.class);

    private static final DateTimeFormatter PUB_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

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
    private final WebloggerPropertiesDao webloggerPropertiesDao;
    private final WeblogEntryCommentDao weblogEntryCommentDao;
    private final DynamicProperties dp;

    // Max Tag options to display for autocomplete
    private final int maxAutocompleteTags;

    @Autowired
    public WeblogEntryController(WeblogDao weblogDao, WeblogCategoryDao weblogCategoryDao,
                                 UserDao userDao, UserManager userManager, WeblogManager weblogManager,
                                 WeblogEntryManager weblogEntryManager, LuceneIndexer luceneIndexer,
                                 URLService urlService, MessageSource messages,
                                 WebloggerPropertiesDao webloggerPropertiesDao,
                                 WeblogEntryDao weblogEntryDao, DynamicProperties dp,
                                 WeblogEntryCommentDao weblogEntryCommentDao,
                                 @Value("${max.autocomplete.tags:20}") int maxAutocompleteTags) {
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.weblogCategoryDao = weblogCategoryDao;
        this.userDao = userDao;
        this.userManager = userManager;
        this.weblogManager = weblogManager;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
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

        WeblogEntry entry = weblogEntryDao.getOne(id);
        Weblog weblog = entry.getWeblog();
        entry.setWeblogEntryCommentDao(weblogEntryCommentDao);
        entry.setCommentsUrl(urlService.getCommentManagementURL(weblog.getId(), entry.getId()));
        entry.setPermalink(urlService.getWeblogEntryURL(entry));
        entry.setPreviewUrl(urlService.getWeblogEntryDraftPreviewURL(entry));

        if (entry.getPubTime() != null) {
            LOG.debug("entry pubtime is {}", entry.getPubTime());
            ZonedDateTime zdt = entry.getPubTime().atZone(entry.getWeblog().getZoneId());
            entry.setHours(zdt.getHour());
            entry.setMinutes(zdt.getMinute());
            entry.setCreator(null);
            entry.setDateString(PUB_DATE_FORMAT.format(zdt.toLocalDate()));
        }

        return entry;
    }

    @PostMapping(value = "/{weblogId}/page/{page}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public WeblogEntryData getWeblogEntries(@PathVariable String weblogId, @PathVariable int page,
                                            @RequestBody WeblogEntrySearchCriteria criteria, Principal p) {

        Weblog weblog = weblogDao.getOne(weblogId);

        criteria.setWeblog(weblog);
        criteria.setOffset(page * ITEMS_PER_PAGE);
        criteria.setMaxResults(ITEMS_PER_PAGE + 1);
        criteria.setCalculatePermalinks(true);
        List<WeblogEntry> rawEntries = weblogEntryManager.getWeblogEntries(criteria);

        WeblogEntryData data = new WeblogEntryData();
        data.getEntries().addAll(rawEntries.stream()
                .peek(re -> re.setWeblog(null))
                .peek(re -> re.getCategory().setWeblog(null))
                .collect(Collectors.toList()));

        if (rawEntries.size() > ITEMS_PER_PAGE) {
            data.getEntries().remove(data.getEntries().size() - 1);
            data.setHasMore(true);
        }

        return data;
    }

    @GetMapping(value = "/{weblogId}/recententries/{pubStatus}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public List<WeblogEntry> getRecentEntries(@PathVariable String weblogId, @PathVariable PubStatus pubStatus,
                                              Principal p) {

        Weblog weblog = weblogDao.getOne(weblogId);

        List<WeblogEntry> entries = new ArrayList<>();
        if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setMaxResults(20);
            wesc.setStatus(pubStatus);

            List<WeblogEntry> fullEntries = weblogEntryManager.getWeblogEntries(wesc);
            entries.addAll(fullEntries.stream().map(e -> new WeblogEntry(e.getTitle(),
                    urlService.getEntryEditURL(e))).collect(Collectors.toList()));
        }
        return entries;
    }

    @GetMapping(value = "/{weblogId}/categorydata")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public WeblogCategoryData getWeblogCategoryData(@PathVariable String weblogId, Principal p) {

        Weblog weblog = weblogDao.getOne(weblogId);

        // categories
        WeblogCategoryData wcd = new WeblogCategoryData();
        weblog.getWeblogCategories().forEach(cat -> wcd.getCategories().put(cat.getName(), cat.getName()));

        return wcd;
    }

    @GetMapping(value = "/{id}/tagdata")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public TagAutocompleteData getWeblogTagData(@PathVariable String id, @RequestParam("prefix") String prefix) {
        List<WeblogEntryTagAggregate> tags;
        Weblog weblog = weblogDao.findById(id).orElse(null);
        tags = weblogManager.getTags(weblog, null, prefix, 0, maxAutocompleteTags);

        TagAutocompleteData wtd = new TagAutocompleteData();
        wtd.setPrefix(prefix);
        wtd.getTagcounts().addAll(tags);
        return wtd;
    }

    @GetMapping(value = "/{weblogId}/entryeditmetadata")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public EntryEditMetadata getEntryEditMetadata(@PathVariable String weblogId, Principal p, Locale locale) {

        // Get user permissions and locale
        User user = userDao.findEnabledByUserName(p.getName());
        Weblog weblog = weblogDao.getOne(weblogId);

        EntryEditMetadata fields = new EntryEditMetadata();

        // categories
        weblog.getWeblogCategories().forEach(cat -> fields.getCategories().put(cat.getId(), cat.getName()));

        fields.setAuthor(userManager.checkWeblogRole(user, weblog, WeblogRole.POST));
        fields.setCommentingEnabled(!WebloggerProperties.CommentPolicy.NONE.equals(
                webloggerPropertiesDao.findOrNull().getCommentPolicy()) &&
                !WebloggerProperties.CommentPolicy.NONE.equals(weblog.getAllowComments()));
        fields.setDefaultCommentDays(weblog.getDefaultCommentDays());
        fields.setDefaultEditFormat(weblog.getEditFormat());
        fields.setTimezone(weblog.getTimeZone());

        fields.getEditFormats().putAll(Arrays.stream(Weblog.EditFormat.values())
                .collect(Utilities.toLinkedHashMap(Weblog.EditFormat::name,
                        eF -> messages.getMessage(eF.getDescriptionKey(), null, locale))));

        // comment day options
        fields.getCommentDayOptions().putAll(Arrays.stream(WeblogEntry.CommentDayOption.values())
                .collect(Utilities.toLinkedHashMap(cdo -> Integer.toString(cdo.getDays()),
                        cdo -> messages.getMessage(cdo.getDescriptionKey(), null, locale))));

        return fields;
    }

    // publish
    // save
    // submit for review
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

            entry.setUpdateTime(Instant.now());
            Instant pubTime = calculatePubTime(entryData);
            entry.setPubTime((pubTime != null) ? pubTime : entry.getUpdateTime());

            if (PubStatus.PUBLISHED.equals(entryData.getStatus()) &&
                    entry.getPubTime().isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
                entryData.setStatus(PubStatus.SCHEDULED);
            }

            entry.setEditFormat(entryData.getEditFormat());
            entry.setStatus(entryData.getStatus());
            entry.setTitle(entryData.getTitle());
            entry.setText(entryData.getText());
            entry.setSummary(entryData.getSummary());
            entry.setNotes(entryData.getNotes());
            if (!StringUtils.isEmpty(entryData.getTagsAsString())) {
                entry.updateTags(new HashSet<>(Arrays.asList(entryData.getTagsAsString().trim().split("\\s+"))));
            } else {
                entry.updateTags(new HashSet<>());
            }
            entry.setSearchDescription(entryData.getSearchDescription());
            entry.setEnclosureUrl(entryData.getEnclosureUrl());
            WeblogCategory category = weblogCategoryDao.findById(entryData.getCategory().getId()).orElse(null);
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

            WeblogEntrySaveResponse wesr = new WeblogEntrySaveResponse();
            wesr.setEntryId(entry.getId());

            String message = null;
            switch (entry.getStatus()) {
                case DRAFT:
                    message = messages.getMessage("entryEdit.draftSaved", null, locale);
                    break;
                case PUBLISHED:
                    message = messages.getMessage("entryEdit.publishedEntry", null, locale);
                    break;
                case SCHEDULED:
                    message = messages.getMessage("entryEdit.scheduledEntry",
                            new Object[] {DateTimeFormatter.ISO_DATE_TIME.withZone(entry.getWeblog().getZoneId())
                                    .format(entry.getPubTime())}, null, locale);
                    break;
                default:
            }

            wesr.setMessage(message);
            return ResponseEntity.ok(wesr);
        } else {
            return ResponseEntity.status(403).body(messages.getMessage("error.title.403", null, locale));
        }
    }

    private static Instant calculatePubTime(WeblogEntry entry) {
        Instant pubtime = null;

        String dateString = entry.getDateString();
        if (!StringUtils.isEmpty(dateString)) {
            try {
                LocalDate newDate = LocalDate.parse(dateString, PUB_DATE_FORMAT);

                // Now handle the time from the hour, minute and second combos
                pubtime = newDate.atTime(entry.getHours(), entry.getMinutes())
                        .atZone(entry.getWeblog().getZoneId()).toInstant();
            } catch (Exception e) {
                LOG.error("Error calculating pubtime", e);
            }
        }
        return pubtime;
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
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
