package org.tightblog.dto;

/**
 * Information held within the enclosure element of an Atom entry (blog entry).
 * Enclosures are usually URLs to podcasts and other multimedia targets.
 */
public record AtomEnclosureDto(String url, String contentType, long length) {
}
