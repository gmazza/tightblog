package org.tightblog.domain;

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

    public static CommentPolicy fromLevel(int level) {
        for (CommentPolicy policy : CommentPolicy.values()) {
            if (policy.getLevel() == level) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Invalid comment policy level: " + level);
    }
}
