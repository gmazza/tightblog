/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.MediaType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.tightblog.util.Utilities;

import java.io.File;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

// needs to implement WeblogOwned to satisfy security checks in MediaFileController
@Entity
@Table(name = "media_file")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaFile extends AbstractEntity implements Comparable<MediaFile>, WeblogOwned {

    public static final MediaType THUMBNAIL_CONTENT_TYPE = MediaType.IMAGE_PNG;
    public static final int MAX_THUMBNAIL_WIDTH = 120;
    public static final int MAX_THUMBNAIL_HEIGHT = 120;

    @NotBlank(message = "{mediaFile.error.nameNull}")
    private String name;

    @Column(name = "alt_attribute")
    private String altAttribute;

    @Column(name = "title_attribute")
    private String titleAttribute;

    // used as the name of the file when saved within the blogging tool storage
    // we don't rely on the DB id as we create the file before persisting the entity
    @Column(name = "file_id")
    private String fileId = Utilities.generateUUID();

    private String anchor;
    private String notes;

    @Column(name = "size_in_bytes")
    private long sizeInBytes;

    private int width = -1;
    private int height = -1;

    @Transient
    private int thumbnailHeight = -1;

    @Transient
    private int thumbnailWidth = -1;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @ManyToOne
    @JoinColumn(name = "directory_id", nullable = false)
    private MediaDirectory directory;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Transient
    private File content;

    @Transient
    private File thumbnail;

    @Transient
    private String permalink;

    @Transient
    private String thumbnailURL;

    @Transient
    private boolean imageFile;

    public MediaFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAltAttribute() {
        return altAttribute;
    }

    public void setAltAttribute(String altText) {
        this.altAttribute = altText;
    }

    public String getTitleAttribute() {
        return titleAttribute;
    }

    public void setTitleAttribute(String titleText) {
        this.titleAttribute = titleText;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long length) {
        this.sizeInBytes = length;
    }

    public MediaDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MediaDirectory dir) {
        this.directory = dir;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public File getContent() {
        return content;
    }

    public void setContent(File content) {
        this.content = content;
    }

    public void setThumbnail(File thumbnail) {
        this.thumbnail = thumbnail;
    }

    public File getThumbnail() {
        return thumbnail;
    }

    @JsonProperty
    public boolean isImageFile() {
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    @JsonProperty
    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    /**
     * Returns thumbnail URL for this media file resource. Resulting URL will be
     * a 404 if media file is not an image.
     */
    @JsonProperty
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getThumbnailHeight() {
        if (isImageFile() && (thumbnailWidth == -1 || thumbnailHeight == -1)) {
            figureThumbnailSize();
        }
        return thumbnailHeight;
    }

    public int getThumbnailWidth() {
        if (isImageFile() && (thumbnailWidth == -1 || thumbnailHeight == -1)) {
            figureThumbnailSize();
        }
        return thumbnailWidth;
    }

    @Override
    public Weblog getWeblog() {
        return Optional.ofNullable(directory).map(MediaDirectory::getWeblog).orElse(null);
    }

    private void figureThumbnailSize() {
        // image determines thumbnail size
        int newWidth = getWidth();
        int newHeight = getHeight();

        if (getWidth() > getHeight()) {
            if (getWidth() > MAX_THUMBNAIL_WIDTH) {
                newHeight = (int) ((float) getHeight() * ((float) MAX_THUMBNAIL_WIDTH / (float) getWidth()));
                newWidth = MAX_THUMBNAIL_WIDTH;
            }

        } else {
            if (getHeight() > MAX_THUMBNAIL_HEIGHT) {
                newWidth = (int) ((float) getWidth() * ((float) MAX_THUMBNAIL_HEIGHT / (float) getHeight()));
                newHeight = MAX_THUMBNAIL_HEIGHT;
            }
        }
        thumbnailHeight = newHeight;
        thumbnailWidth = newWidth;
    }

    @Override
    public String toString() {
        return "MediaFile: id=" + id + ", name=" + name + ", directory=" + directory;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MediaFile that)) {
            return false;
        }
        if (this.id == null || that.id == null) {
            // if not yet persisted, do not consider equal
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private static final Comparator<MediaFile> COMPARATOR =
            Comparator.comparing(MediaFile::getDirectory)
                    .thenComparing(MediaFile::getName);

    @Override
    public int compareTo(MediaFile o) {
        return COMPARATOR.compare(this, o);
    }

}
