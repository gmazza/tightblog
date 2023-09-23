/*
   Copyright 2020 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.bloggerui.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.safety.Safelist;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.tightblog.config.DynamicProperties;
import org.tightblog.service.EmailService;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
public class CommentController {

    // number of comments to show per page
    private static final int ITEMS_PER_PAGE = 30;

    private final WeblogDao weblogDao;
    private final WeblogEntryDao weblogEntryDao;
    private final WeblogEntryCommentDao weblogEntryCommentDao;
    private final WebloggerPropertiesDao webloggerPropertiesDao;
    private final WeblogEntryManager weblogEntryManager;
    private final LuceneIndexer luceneIndexer;
    private final URLService urlService;
    private final EmailService emailService;
    private final DynamicProperties dp;

    record CommentData(String entryTitle, List<WeblogEntryComment> comments, boolean hasMore) { }
    record UnsubscribeResults(boolean foundEntry, String entryTitle, boolean foundSubscription) { }

    @Autowired
    public CommentController(WeblogDao weblogDao, WeblogEntryManager weblogEntryManager, DynamicProperties dp,
                             LuceneIndexer luceneIndexer, URLService urlService, EmailService emailService,
                             WebloggerPropertiesDao webloggerPropertiesDao,
                             WeblogEntryDao weblogEntryDao,
                             WeblogEntryCommentDao weblogEntryCommentDao) {
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.weblogEntryCommentDao = weblogEntryCommentDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.weblogEntryManager = weblogEntryManager;
        this.luceneIndexer = luceneIndexer;
        this.urlService = urlService;
        this.emailService = emailService;
        this.dp = dp;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/comments/{weblogId}/page/{page}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public CommentData getWeblogComments(@PathVariable String weblogId, @PathVariable int page,
                                         @RequestParam(required = false) String entryId,
                                         @RequestBody CommentSearchCriteria criteria, Principal p) {

        Weblog weblog = weblogDao.getById(weblogId);

        criteria.setWeblog(weblog);
        if (entryId != null) {
            criteria.setEntry(weblogEntryDao.getById(entryId));
        }
        criteria.setOffset(page * ITEMS_PER_PAGE);
        criteria.setMaxResults(ITEMS_PER_PAGE + 1);

        List<WeblogEntryComment> entryComments = weblogEntryManager.getComments(criteria).stream()
                .peek(c -> c.getWeblogEntry().setPermalink(
                        urlService.getWeblogEntryURL(c.getWeblogEntry())))
                .collect(Collectors.toList());

        boolean hasMore = false;
        if (entryComments.size() > ITEMS_PER_PAGE) {
            entryComments.remove(entryComments.size() - 1);
            hasMore = true;
        }

        return new CommentData(entryId != null ? criteria.getEntry().getTitle() : null, entryComments, hasMore);
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/comments/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id,  'POST')")
    public void deleteComment(@PathVariable String id, Principal p) {
        WeblogEntryComment itemToRemove = weblogEntryCommentDao.getById(id);
        weblogEntryManager.removeComment(itemToRemove);
        luceneIndexer.updateIndex(itemToRemove.getWeblogEntry(), false);
        dp.updateLastSitewideChange();
    }

    @PostMapping(value = "/tb-ui/authoring/rest/comments/{id}/approve")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id, 'POST')")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void approveComment(@PathVariable String id, Principal p) {
        changeApprovalStatus(id, ApprovalStatus.APPROVED);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/comments/{id}/hide")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id, 'POST')")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void hideComment(@PathVariable String id, Principal p) {
        changeApprovalStatus(id, ApprovalStatus.DISAPPROVED);
    }

    private void changeApprovalStatus(@PathVariable String id, ApprovalStatus newStatus) {

        WeblogEntryComment comment = weblogEntryCommentDao.getById(id);
        ApprovalStatus oldStatus = comment.getStatus();
        comment.setStatus(newStatus);
        // send approval notification only first time, not after any subsequent hide and approves.
        if ((oldStatus == ApprovalStatus.PENDING || oldStatus == ApprovalStatus.SPAM) &&
                newStatus == ApprovalStatus.APPROVED) {
            emailService.sendYourCommentWasApprovedNotifications(Collections.singletonList(comment));
        }
        boolean needRefresh = ApprovalStatus.APPROVED.equals(oldStatus) ^ ApprovalStatus.APPROVED.equals(newStatus);
        weblogEntryManager.saveComment(comment, needRefresh);
        luceneIndexer.updateIndex(comment.getWeblogEntry(), false);
    }

    @PutMapping(value = "/tb-ui/authoring/rest/comments/{id}/content")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id, 'POST')")
    public WeblogEntryComment updateComment(@PathVariable String id, Principal p, HttpServletRequest request)
            throws IOException {

        WeblogEntryComment wec = weblogEntryCommentDao.getById(id);
        String content = Utilities.apiValueToFormSubmissionValue(request.getInputStream());

        // Validate content
        HTMLSanitizer.Level sanitizerLevel = webloggerPropertiesDao.findOrNull().getCommentHtmlPolicy();
        Safelist commentHTMLSafelist = sanitizerLevel.getSafelist();
        wec.setContent(Jsoup.clean(content, commentHTMLSafelist));

        weblogEntryManager.saveComment(wec, true);
        return wec;
    }

    @PostMapping(value = "/tb-ui/blogreader/unsubscribe/{commentId}")
    public UnsubscribeResults unsubscribeNotifications(@PathVariable String commentId, HttpServletRequest request)
            throws IOException {

        boolean foundSubscription;
        String entryTitle;

        Pair<String, Boolean> results = weblogEntryManager.stopNotificationsForCommenter(commentId);
        foundSubscription = results.getRight();
        entryTitle = results.getLeft();

        return new UnsubscribeResults(entryTitle != null, entryTitle, foundSubscription);
    }
}
