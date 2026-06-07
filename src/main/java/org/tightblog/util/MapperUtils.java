package org.tightblog.util;

import org.tightblog.domain.BlogrollLink;
import org.tightblog.domain.CommentPolicy;
import org.tightblog.domain.SpamPolicy;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.domain.WebloggerProperties.RegistrationPolicy;
import org.tightblog.dto.BlogrollLinkDto;
import org.tightblog.dto.WebloggerPropertiesDto;

public final class MapperUtils {

    private MapperUtils() {
        // utility class
    }

    public static BlogrollLinkDto toDto(BlogrollLink link) {
        return new BlogrollLinkDto(
                link.getId(),
                link.getName(),
                link.getUrl(),
                link.getDescription()
        );
    }

    public static void updateWebloggerPropertiesFromDto(WebloggerProperties properties, WebloggerPropertiesDto dto,
                                                        Weblog mainBlog) {
        properties.setMainBlog(mainBlog);
        properties.setRegistrationPolicy(RegistrationPolicy.valueOf(dto.registrationPolicy()));
        properties.setCommentPolicy(CommentPolicy.fromLevel(dto.commentPolicyLevel()));
        properties.setSpamPolicy(SpamPolicy.fromLevel(dto.spamPolicyLevel()));
        properties.setUsersCreateBlogs(dto.usersCreateBlogs());
        properties.setBlogHtmlPolicy(HTMLSanitizer.Level.valueOf(dto.blogHtmlPolicy()));
        properties.setUsersCustomizeThemes(dto.usersCustomizeThemes());
        properties.setDefaultAnalyticsCode(dto.defaultAnalyticsCode());
        properties.setUsersOverrideAnalyticsCode(dto.usersOverrideAnalyticsCode());
        properties.setCommentHtmlPolicy(HTMLSanitizer.Level.fromSanitizingLevel(dto.commentHtmlPolicyLevel()));
        properties.setUsersCommentNotifications(dto.usersCommentNotifications());
        properties.setGlobalSpamFilter(dto.globalSpamFilter());
        properties.setMaxFileUploadsSizeMb(dto.maxFileUploadsSizeMb());
    }

    public static WebloggerPropertiesDto toDto(WebloggerProperties properties) {
        return new WebloggerPropertiesDto(
                properties.getMainBlog() != null ? properties.getMainBlog().getId() : null,
                properties.getRegistrationPolicy().name(),
                properties.getCommentPolicy().getLevel(),
                properties.getSpamPolicy().getLevel(),
                properties.isUsersCreateBlogs(),
                properties.getBlogHtmlPolicy().name(),
                properties.isUsersCustomizeThemes(),
                properties.getDefaultAnalyticsCode(),
                properties.isUsersOverrideAnalyticsCode(),
                properties.getCommentHtmlPolicy().getSanitizingLevel(),
                properties.isUsersCommentNotifications(),
                properties.getGlobalSpamFilter(),
                properties.getMaxFileUploadsSizeMb()
        );
    }
}
