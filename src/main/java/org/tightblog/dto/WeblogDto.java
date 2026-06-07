package org.tightblog.dto;

import org.tightblog.domain.EditFormat;
import org.tightblog.domain.CommentPolicy;
import org.tightblog.domain.SpamPolicy;

import java.util.List;

public record WeblogDto(String id, String name, String tagline, String about,
                        String handle, String theme,
                        String locale, String timeZone, boolean visible,
                        int entriesPerPage, int defaultCommentDays,
                        SpamPolicy spamPolicy, EditFormat editFormat,
                        CommentPolicy commentPolicy,
                        boolean applyCommentDefaults,
                        List<String> weblogCategoryNames,
                        int hitsToday, String analyticsCode, String commentSpamFilter) {
}
