package org.tightblog.domain;

import org.tightblog.util.HTMLSanitizer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "weblogger_properties")
public class WebloggerProperties extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "main_blog_id")
    private Weblog mainBlog;

    @Column(name = "registration_policy")
    @Enumerated(EnumType.STRING)
    private RegistrationPolicy registrationPolicy = RegistrationPolicy.DISABLED;

    @Column(name = "users_create_blogs")
    private boolean usersCreateBlogs = true;

    @Column(name = "blog_html_policy")
    @Enumerated(EnumType.STRING)
    private HTMLSanitizer.Level blogHtmlPolicy = HTMLSanitizer.Level.RELAXED;

    @Column(name = "users_customize_themes")
    private boolean usersCustomizeThemes = true;

    @Column(name = "default_analytics_code")
    private String defaultAnalyticsCode;

    @Column(name = "users_override_analytics_code")
    private boolean usersOverrideAnalyticsCode = true;

    @Column(name = "comment_policy")
    @Enumerated(EnumType.STRING)
    private CommentPolicy commentPolicy = CommentPolicy.MODERATE_NONPUB;

    @Column(name = "comment_html_policy")
    @Enumerated(EnumType.STRING)
    private HTMLSanitizer.Level commentHtmlPolicy = HTMLSanitizer.Level.BASIC;

    @Column(name = "spam_policy")
    @Enumerated(EnumType.STRING)
    private SpamPolicy spamPolicy = SpamPolicy.MARK_SPAM;

    @Column(name = "users_comment_notifications")
    private boolean usersCommentNotifications = true;

    @Column(name = "global_spam_filter")
    private String globalSpamFilter;

    @Column(name = "max_file_uploads_size_mb")
    private int maxFileUploadsSizeMb = 20;

    // temporary non-persisted fields used for form entry & retrieving associated data
    @Transient
    private String mainBlogId;

    public Weblog getMainBlog() {
        return mainBlog;
    }

    public void setMainBlog(Weblog mainBlog) {
        this.mainBlog = mainBlog;
    }

    public RegistrationPolicy getRegistrationPolicy() {
        return registrationPolicy;
    }

    public void setRegistrationPolicy(RegistrationPolicy registrationPolicy) {
        this.registrationPolicy = registrationPolicy;
    }

    public boolean isUsersCreateBlogs() {
        return usersCreateBlogs;
    }

    public void setUsersCreateBlogs(boolean usersCreateBlogs) {
        this.usersCreateBlogs = usersCreateBlogs;
    }

    public HTMLSanitizer.Level getBlogHtmlPolicy() {
        return blogHtmlPolicy;
    }

    public void setBlogHtmlPolicy(HTMLSanitizer.Level blogHtmlPolicy) {
        this.blogHtmlPolicy = blogHtmlPolicy;
    }

    public boolean isUsersCustomizeThemes() {
        return usersCustomizeThemes;
    }

    public void setUsersCustomizeThemes(boolean usersCustomizeThemes) {
        this.usersCustomizeThemes = usersCustomizeThemes;
    }

    public String getDefaultAnalyticsCode() {
        return defaultAnalyticsCode;
    }

    public void setDefaultAnalyticsCode(String defaultAnalyticsCode) {
        this.defaultAnalyticsCode = defaultAnalyticsCode;
    }

    public boolean isUsersOverrideAnalyticsCode() {
        return usersOverrideAnalyticsCode;
    }

    public void setUsersOverrideAnalyticsCode(boolean usersOverrideAnalyticsCode) {
        this.usersOverrideAnalyticsCode = usersOverrideAnalyticsCode;
    }

    public CommentPolicy getCommentPolicy() {
        return commentPolicy;
    }

    public void setCommentPolicy(CommentPolicy commentPolicy) {
        this.commentPolicy = commentPolicy;
    }

    public HTMLSanitizer.Level getCommentHtmlPolicy() {
        return commentHtmlPolicy;
    }

    public void setCommentHtmlPolicy(HTMLSanitizer.Level commentHtmlPolicy) {
        this.commentHtmlPolicy = commentHtmlPolicy;
    }

    public SpamPolicy getSpamPolicy() {
        return spamPolicy;
    }

    public void setSpamPolicy(SpamPolicy spamPolicy) {
        this.spamPolicy = spamPolicy;
    }

    public boolean isUsersCommentNotifications() {
        return usersCommentNotifications;
    }

    public void setUsersCommentNotifications(boolean usersCommentNotifications) {
        this.usersCommentNotifications = usersCommentNotifications;
    }

    public String getGlobalSpamFilter() {
        return globalSpamFilter;
    }

    public void setGlobalSpamFilter(String globalSpamFilter) {
        this.globalSpamFilter = globalSpamFilter;
    }

    public int getMaxFileUploadsSizeMb() {
        return maxFileUploadsSizeMb;
    }

    public void setMaxFileUploadsSizeMb(int maxFileUploadsSizeMb) {
        this.maxFileUploadsSizeMb = maxFileUploadsSizeMb;
    }

    public String getMainBlogId() {
        return mainBlogId;
    }

    public int getCommentPolicyLevel() {
       return commentPolicy.getLevel();
    }

    public int getSpamPolicyLevel() {
        return spamPolicy.getLevel();
    }

    public void setMainBlogId(String mainBlogId) {
        this.mainBlogId = mainBlogId;
    }

    public enum RegistrationPolicy {
        APPROVAL_REQUIRED("globalConfig.registration.approvalRequired"),
        DISABLED("globalConfig.registration.disabled");

        private String label;

        RegistrationPolicy(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum CommentPolicy {
        NONE(0, "common.no"),
        MODERATE_NONPUB(1, "globalConfig.commentmod.nonPubMustModerate"),
        MODERATE_NONAUTH(2, "globalConfig.commentmod.nonAuthMustModerate");

        private String label;

        private int level;

        CommentPolicy(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getLevel() {
            return level;
        }
    }

    public enum SpamPolicy {
        DONT_CHECK(0, "globalConfig.spam.skipCheck"),
        MARK_SPAM(1, "globalConfig.spam.markAsSpam"),
        NO_EMAIL(2, "globalConfig.spam.noEmailNotification"),
        JUST_DELETE(3, "globalConfig.spam.autoDelete");

        private String label;

        private int level;

        SpamPolicy(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getLevel() {
            return level;
        }
    }

}
