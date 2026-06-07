package org.tightblog.domain;

public enum EntryPublishStatus {
    DRAFT("entries.label.draftOnly"),
    PUBLISHED("entries.label.publishedOnly"),
    SCHEDULED("entries.label.scheduledOnly");

    private final String filterMessageConstant;

    EntryPublishStatus(String filterMessageConstant) {
        this.filterMessageConstant = filterMessageConstant;
    }

    public String getFilterMessageConstant() {
        return filterMessageConstant;
    }
}
