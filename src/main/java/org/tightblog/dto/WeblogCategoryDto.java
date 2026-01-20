package org.tightblog.dto;

import java.time.LocalDate;

public record WeblogCategoryDto(String id, String name, LocalDate firstEntry, LocalDate lastEntry, int numEntries) {
}
