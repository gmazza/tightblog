package org.tightblog.domain;

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

    public static SpamPolicy fromLevel(int level) {
        for (SpamPolicy policy : SpamPolicy.values()) {
            if (policy.getLevel() == level) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Invalid spam policy level: " + level);
    }
}
