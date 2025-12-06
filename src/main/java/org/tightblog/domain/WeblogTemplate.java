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
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

/**
 * POJO that represents a single user defined template page.
 * <p>
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 */
@Entity
@Table(name = "weblog_template")
public class WeblogTemplate extends AbstractEntity implements Template, WeblogOwned {

    @Enumerated(EnumType.STRING)
    private Role role;

    @NotBlank(message = "{templates.error.nameNull}")
    private String name;
    private String description;

    private String template = "";

    @Transient
    private Derivation derivation = Derivation.SPECIFICBLOG;

    // associations
    @JsonIgnore
    @ManyToOne
    private Weblog weblog;

    // used for form entry
    @Transient
    private String roleName;

    public WeblogTemplate() {
    }

    // used in WeblogTemplateDao where template metadata rather than template itself is needed
    public WeblogTemplate(String id, Role role, @NotBlank(message = "{templates.error.nameNull}") String name,
                          String description, Instant dateCreated, Instant dateUpdated) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.description = description;
        setDateCreated(dateCreated);
        setDateUpdated(dateUpdated);
    }

    @Override
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Override
    public Derivation getDerivation() {
        return derivation;
    }

    public void setDerivation(Derivation derivation) {
        this.derivation = derivation;
    }

    public String getRoleName() {
        return roleName;
    }

    // property used for form binding, JSON->Java
    @JsonProperty
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(String template) {
        this.template = template;
    }

    public String toString() {
        return "WeblogTemplate: id=" + id + ", name=" + name + ", role=" + role + ", derivation=" + derivation;
    }

    @Override
    public boolean equals(Object other) {
        return other == this ||
                (other instanceof WeblogTemplate && Objects.equals(id, ((WeblogTemplate) other).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
