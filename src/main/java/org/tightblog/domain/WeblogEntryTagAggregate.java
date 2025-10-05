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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * Collector for tag aggregate data
 */
public class WeblogEntryTagAggregate {

    private String name;
    private Weblog weblog;
    private long total;
    private int intensity;

    // temporary non-persisted fields
    private String viewUrl;
    private LocalDate firstEntry;
    private LocalDate lastEntry;

    public WeblogEntryTagAggregate() {
    }

    public WeblogEntryTagAggregate(String name, Long total, Instant firstEntry, Instant lastEntry) {
        this.name = name;
        this.total = total;
        this.firstEntry = firstEntry != null ? LocalDate.ofInstant(firstEntry, java.time.ZoneId.systemDefault()) : null;
        this.lastEntry = lastEntry != null ? LocalDate.ofInstant(lastEntry, java.time.ZoneId.systemDefault()) : null;
    }

    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    @JsonIgnore
    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String toString() {
        return "WeblogEntryTagAggregate: weblog=" + weblog.getHandle() + ", name=" + name + ", count=" + getTotal();
    }

    public LocalDate getFirstEntry() {
        return firstEntry;
    }

    public void setFirstEntry(LocalDate firstEntry) {
        this.firstEntry = firstEntry;
    }

    public LocalDate getLastEntry() {
        return lastEntry;
    }

    public void setLastEntry(LocalDate lastEntry) {
        this.lastEntry = lastEntry;
    }

    public static final Comparator<WeblogEntryTagAggregate> NAME_COMPARATOR = (weta1, weta2) ->
            weta1.getName().compareToIgnoreCase(weta2.getName());

    public static final Comparator<WeblogEntryTagAggregate> COUNT_COMPARATOR = (weta1, weta2) -> {
        // higher numbers first for counts
        int compVal = Long.compare(weta2.getTotal(), weta1.getTotal());

        // still alpha order if tied
        if (compVal == 0) {
            compVal = weta1.getName().compareTo(weta2.getName());
        }
        return compVal;
    };

}
