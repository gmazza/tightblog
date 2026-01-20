package org.tightblog.dto;

import org.tightblog.domain.Weblog.EditFormat;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;
import org.tightblog.domain.WebloggerProperties.SpamPolicy;

import java.util.List;

public record WeblogDto(String id, String name, String tagline, String about,
                        String absoluteURL, String handle, String theme,
                        String locale, String timeZone, boolean visible,
                        int entriesPerPage, int defaultCommentDays,
                        SpamPolicy spamPolicy, EditFormat editFormat,
                        CommentPolicy commentPolicy,
                        boolean applyCommentDefaults,
                        List<String> weblogCategoryNames,
                        int hitsToday, int unapprovedComments,
                        String analyticsCode, String commentSpamFilter) {
}
