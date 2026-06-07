package org.tightblog.dto;

import org.tightblog.domain.EntryPublishStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record WeblogEntryDto(
    String id, String title, String text, String summary,
    String notes, String anchor, int commentDays,
    String categoryName,
    String tagsAsString,
    String permalink,
    String previewUrl,
    EntryPublishStatus status, String searchDescription,
    String enclosureUrl, String enclosureType,
    long enclosureLength,
    int approvedCommentCount,
    int commentCountIncludingUnapproved,
    String editFormat,
    Instant publishTime,
    Instant updateTime,
    // below used in rendering blog entries (Thymeleaf), not the Vue front-end
    String creatorScreenName) {

    public WeblogEntryDto {
    }

    public WeblogEntryDto(String id, String title, String anchor, Instant publishTime, EntryPublishStatus status,
                          String creatorScreenName) {
        this(id, title, "", "", "", anchor, 0, "", "",
            "", "", status, "", "", "",
            0, 0, 0, "", publishTime, null,
            creatorScreenName);
    }

    public WeblogEntryDto(String id, String anchor, EntryPublishStatus status) {
        this(id, "", "", "", "", anchor, 0, "", "",
            "", "", status, "", "", "",
            0, 0, 0, "", null, null,
            "");
    }

    public Set<String> getTagsAsSet() {
        if (tagsAsString() == null || tagsAsString().isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tagsAsString().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
