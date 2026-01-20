package org.tightblog.dto;

public record WebloggerPropertiesDto(
        String mainBlogId,
        String registrationPolicy,
        int commentPolicyLevel,
        int spamPolicyLevel,
        boolean usersCreateBlogs,
        String blogHtmlPolicy,
        boolean usersCustomizeThemes,
        String defaultAnalyticsCode,
        boolean usersOverrideAnalyticsCode,
        int commentHtmlPolicyLevel,
        boolean usersCommentNotifications,
        String globalSpamFilter,
        int maxFileUploadsSizeMb
) {
}
