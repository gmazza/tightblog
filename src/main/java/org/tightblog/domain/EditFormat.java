package org.tightblog.domain;

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
