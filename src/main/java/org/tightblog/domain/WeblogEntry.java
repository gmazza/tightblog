/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.util.Utilities;

import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Weblog Entry.
 */
@Entity
@Table(name = "weblog_entry")
public class WeblogEntry extends AbstractEntity implements WeblogOwned {

    // ugh, dependency injection presently used for comment counts
    @Transient
    private WeblogEntryCommentDao weblogEntryCommentDao;

    @NotBlank(message = "{entryEdit.error.titleNull}")
    private String title;

    @NotBlank(message = "{entryEdit.error.textNull}")
    private String text;

    private String summary;
    private String notes;

    @Column(name = "edit_format")
    @Enumerated(EnumType.STRING)
    private Weblog.EditFormat editFormat = Weblog.EditFormat.HTML;

    @Column(name = "enclosure_url")
    private String enclosureUrl;

    @Column(name = "enclosure_type")
    private String enclosureType;

    @Column(name = "enclosure_length")
    private Long enclosureLength;

    private String anchor;

    @Column(name = "publish_time")
    private Instant publishTime;

    @Column(name = "comment_days")
    private Integer commentDays = CommentDayOption.SEVEN.getDays();

    @Enumerated(EnumType.STRING)
    private PubStatus status;

    /**
     * Search description for weblog entry (intended for HTML header).
     */
    @Column(name = "search_description")
    private String searchDescription;

    @ManyToOne
    private Weblog weblog;

    @ManyToOne
    private WeblogCategory category;

    @ManyToOne
    private User creator;

    @OneToMany(targetEntity = WeblogEntryTag.class,
            cascade = CascadeType.ALL, mappedBy = "weblogEntry", orphanRemoval = true)
    @OrderBy("name")
    private Set<WeblogEntryTag> tagSet = new HashSet<>();

    @Transient
    private String permalink;

    @Transient
    private String previewUrl;

    @Transient
    private Set<String> tags;

    @Transient
    private String tagsAsString;

    public WeblogEntry() {
    }

    public WeblogCategory getCategory() {
        return this.category;
    }

    public void setCategory(WeblogCategory category) {
        this.category = category;
    }

    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = Utilities.removeHTML(title);
    }

    /**
     * Summary for weblog entry (maps to Atom summary).
     */
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = Utilities.removeHTML(searchDescription);
    }

    /**
     * Content text for weblog entry
     */
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Weblog.EditFormat getEditFormat() {
        return this.editFormat;
    }

    public void setEditFormat(Weblog.EditFormat editFormat) {
        this.editFormat = editFormat;
    }

    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    public String getEnclosureType() {
        return enclosureType;
    }

    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    public Long getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Long enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * <p>Publish time is the time that an entry is to be (or was) made available
     * for viewing by newsfeed readers and visitors to the weblogger site.</p>
     * <p>
     * <p>TightBlog stores time using the timezone of the server itself. When
     * times are displayed in a user's weblog they must be translated
     * to the user's timeZone.</p>
     */
    public Instant getPublishTime() {
        return this.publishTime;
    }

    public void setPublishTime(Instant publishTime) {
        this.publishTime = publishTime;
    }

    public PubStatus getStatus() {
        return this.status;
    }

    public void setStatus(PubStatus status) {
        this.status = status;
    }

    /**
     * Number of days after publishTime that comments should be allowed, -1 for no limit.
     */
    public Integer getCommentDays() {
        return commentDays;
    }

    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }

    public Set<WeblogEntryTag> getTagSet() {
        return tagSet;
    }

    // due to orphanRemoval = true, best not to use this setter but
    // instead manipulate the existing set (clear, addAll, etc.)
    public void setTagSet(Set<WeblogEntryTag> tagSet) {
        this.tagSet = tagSet;
    }

    /**
     * Replace the current set of tags with those in the new list.  Any WeblogEntryTags
     * already attached to the instance and remaining in the new list will be reused.
     */
    public void updateTags(Set<String> newTags) {
        Locale localeObject = getWeblog().getLocaleInstance();
        Set<WeblogEntryTag> newTagSet = new HashSet<>();
        for (String tagStr : newTags) {
            String normalizedString = Utilities.normalizeTag(tagStr, localeObject);
            boolean found = false;
            for (WeblogEntryTag currentTag : getTagSet()) {
                if (currentTag.getName().equals(normalizedString)) {
                    // reuse currently existing
                    newTagSet.add(currentTag);
                    found = true;
                }
            }
            if (!found) {
                // new tag added by user, has to be created.
                WeblogEntryTag tag = new WeblogEntryTag();
                tag.setName(normalizedString);
                tag.setWeblog(getWeblog());
                tag.setWeblogEntry(this);
                newTagSet.add(tag);
            }
        }
        // will erase tags not in the new list, JPA will delete them from DB.
        getTagSet().clear();
        getTagSet().addAll(newTagSet);
    }

    public Set<String> getTags() {
        if (getTagSet() == null) {
            return new HashSet<>();
        }
        return getTagSet().stream().map(WeblogEntryTag::getName).collect(Collectors.toSet());
    }

    @JsonProperty
    public String getTagsAsString() {
        if (tagsAsString == null) {
            tagsAsString = String.join(" ", new HashSet<>(getTags()));
        }
        return tagsAsString;
    }

    public void setTagsAsString(String tagsAsString) {
        this.tagsAsString = tagsAsString;
    }

    public void setWeblogEntryCommentDao(WeblogEntryCommentDao weblogEntryCommentDao) {
        this.weblogEntryCommentDao = weblogEntryCommentDao;
    }

    @Transient
    @JsonIgnore
    public List<WeblogEntryComment> getComments() {
        return weblogEntryCommentDao != null ? weblogEntryCommentDao.findByWeblogEntryAndStatusApproved(this)
                : new ArrayList<>();
    }

    @Transient
    public int getCommentCount() {
        return weblogEntryCommentDao != null ? weblogEntryCommentDao.countByWeblogEntryAndStatusApproved(this) : 0;
    }

    @JsonProperty
    public int getCommentCountIncludingUnapproved() {
        return weblogEntryCommentDao != null ? weblogEntryCommentDao.countByWeblogEntry(this) : 0;
    }

    // Transient field, so Jackson requires this annotation for serializing to JSON
    @JsonProperty
    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    /**
     * Convenience method for checking published status
     */
    @Transient
    public boolean isPublished() {
        return PubStatus.PUBLISHED.equals(getStatus());
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    @Override
    public String toString() {
        return "WeblogEntry: id=" + id + ", weblog=" + ((weblog == null) ? "(null)" : weblog.getHandle()) +
                ", anchor=" + anchor + ", pub time=" + publishTime;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeblogEntry)) {
            return false;
        }
        WeblogEntry that = (WeblogEntry) other;
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

    public enum PubStatus {
        DRAFT("entries.label.draftOnly"),
        PUBLISHED("entries.label.publishedOnly"),
        SCHEDULED("entries.label.scheduledOnly");

        private final String filterMessageConstant;

        PubStatus(String filterMessageConstant) {
            this.filterMessageConstant = filterMessageConstant;
        }

        public String getFilterMessageConstant() {
            return filterMessageConstant;
        }
    }

    public enum CommentDayOption {
        UNLIMITED(-1, "weblogConfig.commentperiod.unlimited"),
        ZERO(0, "weblogConfig.commentperiod.days0"),
        THREE(3, "weblogConfig.commentperiod.days3"),
        SEVEN(7, "weblogConfig.commentperiod.days7"),
        FOURTEEN(14, "weblogConfig.commentperiod.days14"),
        THIRTY(30, "weblogConfig.commentperiod.days30"),
        NINETY(90, "weblogConfig.commentperiod.days90"),
        ONEYEAR(365, "weblogConfig.commentperiod.days365");

        int days;

        String descriptionKey;

        CommentDayOption(int days, String descriptionKey) {
            this.days = days;
            this.descriptionKey = descriptionKey;
        }

        public Integer getDays() {
            return days;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }
    }

}
