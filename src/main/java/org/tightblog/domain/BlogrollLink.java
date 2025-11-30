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

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a single link for a weblog's blogroll.
 */
@Entity
@Table(name = "blogroll_link")
public class BlogrollLink extends AbstractEntity implements Comparable<BlogrollLink>, WeblogOwned {

    @ManyToOne
    private Weblog weblog;

    private String name;
    private String description;
    private String url;
    private Integer position;

    public BlogrollLink() {
    }

    public BlogrollLink(Weblog parent, String name, String url, String desc) {
        this.weblog = parent;
        this.name = name;
        this.description = desc;
        this.url = url;
        calculatePosition();
    }

    // algorithm assumes bookmark not yet added to the weblog's list
    public void calculatePosition() {
        int size = weblog.getBlogrollLinks().size();
        if (size == 0) {
            this.position = 0;
        } else {
            this.position = weblog.getBlogrollLinks().get(size - 1).getPosition() + 1;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Position determines order of display
     */
    public java.lang.Integer getPosition() {
        return this.position;
    }

    public void setPosition(java.lang.Integer position) {
        this.position = position;
    }

    @ManyToOne
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public String toString() {
        return "WeblogBookmark: id=" + id + ", weblog=" + weblog.getHandle() + ", name=" + name + ", url=" + url;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof BlogrollLink && Objects.equals(id, ((BlogrollLink) other).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private static final Comparator<BlogrollLink> COMPARATOR =
            Comparator.comparing(BlogrollLink::getWeblog, Weblog.HANDLE_COMPARATOR)
                    .thenComparingInt(BlogrollLink::getPosition);

    @Override
    public int compareTo(BlogrollLink other) {
        return COMPARATOR.compare(this, other);
    }

}
