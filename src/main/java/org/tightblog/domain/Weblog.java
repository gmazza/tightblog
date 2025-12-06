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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;
import org.tightblog.domain.WebloggerProperties.SpamPolicy;
import org.tightblog.rendering.service.CommentSpamChecker;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Pattern;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Weblogs have a many-to-many association with users. They also have one-to-many and
 * one-direction associations with weblog entries, weblog categories, bookmarks and
 * other objects.
 */
@Entity
@Table(name = "weblog")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Weblog extends AbstractEntity implements WeblogOwned {

    @NotBlank(message = "{weblogConfig.error.nameNull}")
    private String name;

    @NotBlank(message = "{weblogConfig.error.handleNull}")
    @Pattern(regexp = "[a-z0-9\\-]*", message = "{weblogConfig.error.invalidHandle}")
    private String handle;

    private String tagline;
    private String about;
    private String locale;
    private String timeZone;
    private Boolean visible = Boolean.TRUE;

    @NotBlank(message = "{weblogConfig.error.themeNull}")
    private String theme;

    @Column(name = "entries_per_page")
    private int entriesPerPage = 12;

    @Column(name = "edit_format")
    @Enumerated(EnumType.STRING)
    private EditFormat editFormat = EditFormat.HTML;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User creator;

    @Column(name = "comment_policy")
    @Enumerated(EnumType.STRING)
    private CommentPolicy commentPolicy = CommentPolicy.MODERATE_NONPUB;

    @Column(name = "spam_policy")
    @Enumerated(EnumType.STRING)
    private SpamPolicy spamPolicy = SpamPolicy.NO_EMAIL;

    @Column(name = "default_comment_days")
    private int defaultCommentDays = -1;

    @Column(name = "analytics_code")
    private String analyticsCode;

    @Column(name = "comment_spam_filter")
    private String commentSpamFilter;

    @Column(name = "hits_today")
    private int hitsToday;

    @Transient
    private boolean applyCommentDefaults;

    @Transient
    // Transient, derived from and re-calculated each time commentSpamFilter is set
    @JsonIgnore
    private List<java.util.regex.Pattern> commentSpamRegexRules = new ArrayList<>();

    // temporary non-persisted fields used for form entry & retrieving associated data
    @Transient
    private int unapprovedComments;

    @Transient
    @JsonIgnore
    private Locale localeInstance;

    @Transient
    private String absoluteURL;

    @Transient
    private int hashCode;

    // Associated objects
    @JsonIgnore
    @OneToMany(mappedBy = "weblog", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("position")
    private Set<WeblogCategory> weblogCategories = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "weblog", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<BlogrollLink> blogrollLinks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "weblog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaDirectory> mediaDirectories = new ArrayList<>();

    public Weblog() {
    }

    public Weblog(
            String handle,
            User creator,
            String name,
            String theme) {

        this.handle = handle;
        this.creator = creator;
        this.name = name;
        this.theme = theme;
    }

    public Weblog(Weblog other) {
        this.setId(other.getId());
        this.setName(other.getName());
        this.setHandle(other.getHandle());
        this.setTagline(other.getTagline());
        this.setCreator(other.getCreator());
        this.setEditFormat(other.getEditFormat());
        this.setCommentSpamFilter(other.getCommentSpamFilter());
        this.setCommentPolicy(other.getCommentPolicy());
        this.setSpamPolicy(other.getSpamPolicy());
        this.setTheme(other.getTheme());
        this.setLocale(other.getLocale());
        this.setTimeZone(other.getTimeZone());
        this.setVisible(other.getVisible());
        this.setDateCreated(other.getDateCreated());
        this.setEntriesPerPage(other.getEntriesPerPage());
        this.setWeblogCategories(other.getWeblogCategories());
        this.setAnalyticsCode(other.getAnalyticsCode());
        this.setBlogrollLinks(other.getBlogrollLinks());
        this.setHitsToday(other.getHitsToday());
    }

    /**
     * Short URL safe string that uniquely identifies the weblog.
     */
    public String getHandle() {
        return this.handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Weblog name (title)
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHitsToday() {
        return hitsToday;
    }

    public void setHitsToday(int hitsToday) {
        this.hitsToday = hitsToday;
    }

    /**
     * Weblog subtitle
     */
    public String getTagline() {
        return this.tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public EditFormat getEditFormat() {
        return this.editFormat;
    }

    public void setEditFormat(EditFormat editFormat) {
        this.editFormat = editFormat;
    }

    public String getCommentSpamFilter() {
        return this.commentSpamFilter;
    }

    public void setCommentSpamFilter(String filter) {
        this.commentSpamFilter = filter;
        commentSpamRegexRules = CommentSpamChecker.compileSpamlist(filter);
    }

    public List<java.util.regex.Pattern> getCommentSpamRegexRules() {
        return commentSpamRegexRules;
    }

    public CommentPolicy getCommentPolicy() {
        return this.commentPolicy;
    }

    public void setCommentPolicy(CommentPolicy commentPolicy) {
        this.commentPolicy = commentPolicy;
    }

    public SpamPolicy getSpamPolicy() {
        return this.spamPolicy;
    }

    public void setSpamPolicy(SpamPolicy spamPolicy) {
        this.spamPolicy = spamPolicy;
    }

    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }

    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }

    public String getTheme() {
        return this.theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLocale() {
        return this.locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Parse locale value and instantiate a Locale object.
     *
     * @return Locale
     */
    public Locale getLocaleInstance() {
        if (localeInstance == null) {
            localeInstance = Locale.forLanguageTag(getLocale());
        }
        return localeInstance;
    }

    /**
     * Return TimeZone instance for value of timeZone, else return system default instance.
     *
     * @return TimeZone
     */
    @JsonIgnore
    public ZoneId getZoneId() {
        if (getTimeZone() == null) {
            this.setTimeZone(TimeZone.getDefault().getID());
        }
        return TimeZone.getTimeZone(getTimeZone()).toZoneId();
    }

    public int getEntriesPerPage() {
        return entriesPerPage;
    }

    public void setEntriesPerPage(int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }

    /**
     * If false, weblog will be hidden from public view.
     */
    public Boolean getVisible() {
        return this.visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    // Used in templates and a few JSP's, explicit JsonProperty annotation needed
    // because this property is marked @Transient, otherwise Jackson
    // would ignore it during serialization
    @JsonProperty
    public String getAbsoluteURL() {
        return absoluteURL;
    }

    public void setAbsoluteURL(String absoluteURL) {
        this.absoluteURL = absoluteURL;
    }

    public String getAnalyticsCode() {
        return analyticsCode;
    }

    public void setAnalyticsCode(String analyticsCode) {
        this.analyticsCode = analyticsCode;
    }

    /**
     * A description for the weblog (its purpose, authors, etc.), perhaps a paragraph or so in length.
     */
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = Utilities.removeHTML(about);
    }

    /**
     * Add a category to this weblog.
     */
    public void addCategory(String categoryName) {

        if (categoryName == null) {
            throw new IllegalArgumentException("Category must have a valid name");
        }

        // make sure we don't already have a category with that name
        if (hasCategory(categoryName)) {
            throw new IllegalArgumentException("Duplicate category name '" + categoryName + "'");
        }

        // add it to our list of categories
        WeblogCategory category = new WeblogCategory(this, categoryName);
        getWeblogCategories().add(category);
    }

    public Set<WeblogCategory> getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(Set<WeblogCategory> cats) {
        this.weblogCategories = cats;
    }

    public List<String> getWeblogCategoryNames() {
        return getWeblogCategories().stream().map(WeblogCategory::getName).collect(Collectors.toList());
    }

    public boolean hasCategory(String categoryName) {
        for (WeblogCategory cat : getWeblogCategories()) {
            if (categoryName.equals(cat.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<BlogrollLink> getBlogrollLinks() {
        return blogrollLinks;
    }

    public void setBlogrollLinks(List<BlogrollLink> bookmarks) {
        this.blogrollLinks = bookmarks;
    }

    public List<MediaDirectory> getMediaDirectories() {
        return mediaDirectories;
    }

    public void setMediaDirectories(List<MediaDirectory> mediaDirectories) {
        this.mediaDirectories = mediaDirectories;
    }

    public void addBlogrollLink(BlogrollLink item) {
        // make sure blogroll item is not null
        if (item == null || item.getName() == null) {
            throw new IllegalArgumentException("Bookmark cannot be null and must have a valid name");
        }

        if (this.hasBookmark(item.getName())) {
            throw new IllegalArgumentException("Duplicate bookmark name '" + item.getName() + "'");
        }

        // add it to our blogroll
        getBlogrollLinks().add(item);
    }

    /**
     * Does this Weblog have a bookmark with the same name?
     *
     * @param bookmarkName The name of the bookmark to check for.
     * @return boolean true if exists, false otherwise.
     */
    public boolean hasBookmark(String bookmarkName) {
        for (BlogrollLink bookmark : this.getBlogrollLinks()) {
            if (bookmarkName.toLowerCase().equals(bookmark.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether this weblog contains the specified media file directory
     *
     * @param directoryName directory name
     * @return true if directory is present, false otherwise.
     */
    public boolean hasMediaDirectory(String directoryName) {
        for (MediaDirectory directory : this.getMediaDirectories()) {
            if (directory.getName().equals(directoryName)) {
                return true;
            }
        }
        return false;
    }

    public MediaDirectory getMediaDirectory(String directoryName) {
        for (MediaDirectory dir : this.getMediaDirectories()) {
            if (directoryName.equals(dir.getName())) {
                return dir;
            }
        }
        return null;
    }

    // convenience methods for populating fields from forms

    public boolean isApplyCommentDefaults() {
        return applyCommentDefaults;
    }

    public void setApplyCommentDefaults(boolean applyCommentDefaults) {
        this.applyCommentDefaults = applyCommentDefaults;
    }

    @Override
    public String toString() {
        return String.format("Weblog: handle=%s, name=%s, id=%s", handle, name, id);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Weblog && Objects.equals(id, ((Weblog) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

    static final Comparator<Weblog> HANDLE_COMPARATOR = Comparator.comparing(Weblog::getHandle);

    public int getUnapprovedComments() {
        return unapprovedComments;
    }

    public void setUnapprovedComments(int unapprovedComments) {
        this.unapprovedComments = unapprovedComments;
    }

    @Override
    @JsonIgnore
    public Weblog getWeblog() {
        return this;
    }

    public enum EditFormat {
        HTML("weblogConfig.editFormatType.html"),
        COMMONMARK("weblogConfig.editFormatType.commonMark");

        private String descriptionKey;

        EditFormat(String descriptionKey) {
            this.descriptionKey = descriptionKey;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }
    }

}
