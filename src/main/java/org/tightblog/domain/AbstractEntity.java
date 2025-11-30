package org.tightblog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.Instant;


// Base class for all tests that need an entity with id, dateCreated, dateUpdated
// Tables extending this will have at least these columns:
//  id             CHAR(36)     NOT NULL,
//  date_created   DATETIME     NOT NULL,
//  date_updated   DATETIME     NOT NULL,
@MappedSuperclass
public abstract class AbstractEntity implements Persistable<String>, Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    protected String id;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "date_created", nullable = false)
    private Instant dateCreated;

    @Column(name = "date_updated")
    private Instant dateUpdated;

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Instant getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Instant dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @PrePersist
    @PreUpdate
    public void setAuditFields() {
        Instant now = Instant.now();
        if (getDateCreated() == null) {
            setDateCreated(now);
        }

        setDateUpdated(now);
    }

    @Override
    @Transient
    public boolean isNew() {
        return null == getId();
    }

    @Override
    public int hashCode() {
        return (null == getId()) ? 0 : getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractEntity that = (AbstractEntity) o;
        return getId() != null && getId().equals(that.getId());
    }
}

