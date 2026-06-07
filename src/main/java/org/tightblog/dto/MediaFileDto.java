package org.tightblog.dto;

public record MediaFileDto(String id, String name, String altAttribute, String titleAttribute,
                           String anchor, String notes, long sizeInBytes, int width,
                           int height, String contentType, String permalink, String thumbnailURL,
                           String directoryId, boolean isImageFile) {
}
