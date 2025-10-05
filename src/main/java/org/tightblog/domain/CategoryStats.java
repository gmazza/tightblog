package org.tightblog.domain;

import java.time.Instant;
import java.time.LocalDate;

public class CategoryStats {
    WeblogCategory category;
    private final int numEntries;
    private final LocalDate firstEntry;
    private final LocalDate lastEntry;

    public CategoryStats(WeblogCategory category, Instant firstEntry, Instant lastEntry, Long numEntries) {
        this.category = category;
        this.numEntries = numEntries.intValue();
        this.firstEntry = firstEntry.atZone(category.getWeblog().getZoneId()).toLocalDate();
        this.lastEntry = lastEntry.atZone(category.getWeblog().getZoneId()).toLocalDate();
    }

    public WeblogCategory getCategory() {
        return category;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public LocalDate getFirstEntry() {
        return firstEntry;
    }

    public LocalDate getLastEntry() {
        return lastEntry;
    }
}
