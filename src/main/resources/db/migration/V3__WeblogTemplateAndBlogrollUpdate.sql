ALTER TABLE weblog_template
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    CHANGE COLUMN updatetime date_updated TIMESTAMP DEFAULT NOW() NOT NULL AFTER date_created,
    ADD CONSTRAINT wt_weblogid_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id);

ALTER TABLE blogroll_link
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT bl_weblogid_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id);
