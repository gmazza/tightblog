/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import org.apache.commons.text.StringEscapeUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "weblog_entry_comment")
public class WeblogEntryComment extends AbstractEntity implements WeblogOwned {

    public WeblogEntryComment() {
    }

    public WeblogEntryComment(String content) {
        this.content = content;
    }

    private String name;
    private String email;
    private String url;
    private String content;

    @Column(name = "post_time")
    private Instant postTime;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.DISAPPROVED;

    private Boolean notify = Boolean.FALSE;

    @Column(name = "remote_host")
    private String remoteHost;

    private String referrer;

    @Column(name = "user_agent")
    private String userAgent;

    @ManyToOne
    @JoinColumn(name = "weblog_entry_id")
    private WeblogEntry weblogEntry;

    // nullable, used in cases where comment is created by a registered user
    @ManyToOne
    private User creator;

    @Transient
    private String submitResponseMessage;

    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry entry) {
        weblogEntry = entry;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User blogger) {
        this.creator = blogger;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getPostTime() {
        return this.postTime;
    }

    public void setPostTime(Instant postTime) {
        this.postTime = postTime;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     */
    public Boolean getNotify() {
        return this.notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    /**
     * Host name or IP of person who wrote comment.
     */
    public String getRemoteHost() {
        return this.remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = StringEscapeUtils.escapeHtml4(referrer);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * True if comment is pending moderator approval.
     */
    @Transient
    public Boolean isPending() {
        return ApprovalStatus.PENDING.equals(getStatus());
    }

    /**
     * Indicates that comment has been approved for display on weblog.
     */
    @Transient
    public Boolean isApproved() {
        return ApprovalStatus.APPROVED.equals(getStatus());
    }

    @Transient
    public boolean isInvalid() {
        return ApprovalStatus.INVALID.equals(getStatus());
    }

    /**
     * Timestamp to be used to formulate comment permlink.
     */
    @Transient
    public String getTimestamp() {
        if (getPostTime() != null) {
            return Long.toString(getPostTime().toEpochMilli());
        }
        return null;
    }

    public String getSubmitResponseMessage() {
        return submitResponseMessage;
    }

    public void setSubmitResponseMessage(String submitResponseMessage) {
        this.submitResponseMessage = submitResponseMessage;
    }

    public void initializeFormFields() {
        setName("");
        setEmail("");
        setUrl("");
        setContent("");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeblogEntryComment)) {
            return false;
        }
        WeblogEntryComment that = (WeblogEntryComment) other;
        if (this.id == null || that.id == null) {
            // if not yet persisted, do not consider equal
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("WeblogEntryComment: id=%s, weblog=%s, entry=%s, name=%s, email=%s, postTime=%s", id,
                (weblogEntry != null && weblogEntry.getWeblog() != null) ? weblogEntry.getWeblog().getHandle() : "(no weblog)",
                (weblogEntry != null && weblogEntry.getAnchor() != null) ? weblogEntry.getAnchor() : "(no weblog entry)",
                name, email, postTime);
    }

    @Transient
    @JsonIgnore
    @Override
    public Weblog getWeblog() {
        return weblogEntry.getWeblog();
    }

    public enum ApprovalStatus {
        // Comment missing required fields like name or email.  Not serialized to database.
        INVALID(false, null),
        // Comment identified as spam, either deleted or subject to moderation depending on blog config.
        SPAM(true, "comments.onlySpam"),
        // Comment not identified as spam, subject to moderation.
        PENDING(true, "comments.onlyPending"),
        // Comment approved and visible (published)
        APPROVED(true, "comments.onlyApproved"),
        // Approved comment subsequently disapproved and not viewable on blog.  No email notification
        // sent to commenter if re-approved.
        DISAPPROVED(true, "comments.onlyDisapproved");

        private final boolean selectable;
        private final String messageConstant;

        ApprovalStatus(boolean selectable, String messageConstant) {
            this.selectable = selectable;
            this.messageConstant = messageConstant;
        }

        public boolean isSelectable() {
            return selectable;
        }

        public String getMessageConstant() {
            return messageConstant;
        }
    }

    public enum SpamCheckResult {
        SPAM, NOT_SPAM
    }

}
