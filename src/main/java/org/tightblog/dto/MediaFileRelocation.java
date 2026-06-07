package org.tightblog.dto;

import java.util.List;

public record MediaFileRelocation(List<String> fileIdsToMove, String destinationFolderId) {
}
