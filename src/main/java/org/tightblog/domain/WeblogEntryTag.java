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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Table(name = "weblog_entry_tag")
public class WeblogEntryTag extends AbstractEntity implements Comparable<WeblogEntryTag> {

    private static final Comparator<WeblogEntryTag> COMPARATOR =
            Comparator.comparing(WeblogEntryTag::getWeblog, Weblog.HANDLE_COMPARATOR)
                    .thenComparing(WeblogEntryTag::getName);

    public WeblogEntryTag() {
    }

    public WeblogEntryTag(Weblog weblog, WeblogEntry weblogEntry, String name) {
        this.weblog = weblog;
        this.weblogEntry = weblogEntry;
        this.name = name;
    }

    @ManyToOne
    private Weblog weblog;

    @ManyToOne
    @JoinColumn(name = "weblog_entry_id", nullable = false)
    private WeblogEntry weblogEntry;

    private String name;

    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog website) {
        this.weblog = website;
    }

    @JsonIgnore
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry data) {
        weblogEntry = data;
    }

    /**
     * Tag value.
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(WeblogEntryTag o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeblogEntryTag)) {
            return false;
        }
        WeblogEntryTag that = (WeblogEntryTag) other;
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

    public String toString() {
        return "WeblogEntryTag: id=" + id + ", name=" + name + ", weblog=" + weblog.getHandle()
                + ", entry=" + weblogEntry.getAnchor();
    }
}
