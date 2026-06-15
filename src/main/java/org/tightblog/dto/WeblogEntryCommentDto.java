package org.tightblog.dto;

import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;

import java.time.Instant;

public record WeblogEntryCommentDto(
    String id, Instant postTime, ApprovalStatus status,
    WeblogEntryDto weblogEntry, String name, String email,
    String url, String content, boolean notifyMe, String remoteHost,
    String submitResponseMessage
) {

    public WeblogEntryCommentDto(String name, String email, String url, String content, ApprovalStatus status) {
        this("", null, status, null, name, email, url, content, false, "", "");
    }

    public WeblogEntryCommentDto(String id) {
        this(id, null, null, null, "", "", "", "", false, "", "");
    }
}
