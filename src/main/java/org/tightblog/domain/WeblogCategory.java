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

@Entity
@Table(name = "weblog_category")
public class WeblogCategory extends AbstractEntity implements Comparable<WeblogCategory>, WeblogOwned {

    private static final Comparator<WeblogCategory> COMPARATOR =
            Comparator.comparing(WeblogCategory::getWeblog, Weblog.HANDLE_COMPARATOR)
                    .thenComparingInt(WeblogCategory::getPosition);

    // parent weblog of category
    @ManyToOne
    private Weblog weblog;

    // category name
    private String name;

    // left-to-right comparative ordering of category, higher numbers go to the right
    private int position;

    WeblogCategory() {
    }

    // no public constructor, desired way to create is via Weblog.addCategory()
    WeblogCategory(
            Weblog weblog,
            String name) {
        this.name = name;
        this.weblog = weblog;
        calculatePosition();
    }

    public Weblog getWeblog() {
        return weblog;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // algorithm assumes category not yet added to the weblog's list
    private void calculatePosition() {
        this.position = weblog.getWeblogCategories().size();
    }

    @Override
    public int compareTo(WeblogCategory o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return "WeblogCategory: id=" + id + ", weblog=" + weblog.getHandle() + ", name=" + name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeblogCategory that)) {
            return false;
        }
        return Objects.equals(weblog, that.weblog)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weblog, name);
    }
}
