/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "media_directory")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaDirectory extends AbstractEntity implements Comparable<MediaDirectory>, WeblogOwned {

    @NotBlank(message = "{mediaFile.error.view.dirNameInvalid}")
    @Pattern(regexp = "[a-zA-Z0-9\\-]+", message = "{mediaFile.error.view.dirNameInvalid}")
    String name;

    @ManyToOne
    @JoinColumn(name = "weblog_id", nullable = false)
    @JsonIgnore
    Weblog weblog;

    @OneToMany(targetEntity = MediaFile.class,
            cascade = {CascadeType.ALL}, mappedBy = "directory")
    @OrderBy("name")
    @JsonIgnore
    Set<MediaFile> mediaFiles = new HashSet<>();

    public MediaDirectory() {
    }

    MediaDirectory(Weblog weblog, String name) {
        this.name = name;
        this.weblog = weblog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public Set<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(Set<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    /**
     * Indicates whether this directory contains the specified file.
     *
     * @param nameToCheck file name
     * @return true if the file is present in the directory, false otherwise.
     */
    public boolean hasMediaFile(String nameToCheck) {
        Set<MediaFile> fileSet = this.getMediaFiles();
        if (fileSet == null) {
            return false;
        }
        for (MediaFile mediaFile : fileSet) {
            if (mediaFile.getName().equals(nameToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns file with the given name, if present in this directory
     *
     * @param fileName file name
     * @return media file object
     */
    public MediaFile getMediaFile(String fileName) {
        Set<MediaFile> fileSet = this.getMediaFiles();
        if (fileSet == null) {
            return null;
        }
        for (MediaFile mediaFile : fileSet) {
            if (mediaFile.getName().equals(fileName)) {
                return mediaFile;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MediaDirectory that)) {
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

    @Override
    public String toString() {
        return "MediaDirectory: id=" + id + ", name=" + name + ", weblog=" + weblog;
    }

    private static final Comparator<MediaDirectory> COMPARATOR =
            Comparator.comparing(MediaDirectory::getWeblog, Weblog.HANDLE_COMPARATOR)
                    .thenComparing(MediaDirectory::getName);

    @Override
    public int compareTo(MediaDirectory o) {
        return COMPARATOR.compare(this, o);
    }
}
