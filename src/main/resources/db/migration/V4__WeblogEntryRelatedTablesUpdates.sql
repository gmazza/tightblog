ALTER TABLE weblog_entry_comment
    DROP FOREIGN KEY co_entryid_fk;

ALTER TABLE weblog_entry_tag
    DROP FOREIGN KEY wtag_entryid_fk;

ALTER TABLE weblog_entry
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36) NOT NULL,
    CHANGE COLUMN creatorid creator_id CHAR(36) NOT NULL,
    CHANGE COLUMN categoryid category_id CHAR(36) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL AFTER enclosure_length,
    CHANGE COLUMN updatetime date_updated TIMESTAMP DEFAULT NOW() NOT NULL AFTER date_created,
    CHANGE COLUMN pubtime publish_time DATETIME NOT NULL,
    CHANGE COLUMN editformat edit_format VARCHAR(20) NOT NULL,
    CHANGE COLUMN commentdays comment_days INTEGER NULL,
    ADD CONSTRAINT we_weblog_id_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id),
    ADD CONSTRAINT we_creator_id_fk
        FOREIGN KEY (creator_id) REFERENCES weblogger_user(id),
    ADD CONSTRAINT we_category_id_fk
        FOREIGN KEY (category_id) REFERENCES weblog_category(id);

ALTER TABLE weblog_entry_comment
    DROP COLUMN weblogid,
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN entryid weblog_entry_id CHAR(36) NOT NULL,
    CHANGE COLUMN bloggerid creator_id CHAR(36),
    CHANGE COLUMN posttime post_time DATETIME NOT NULL,
    CHANGE COLUMN remotehost remote_host VARCHAR(255),
    CHANGE COLUMN useragent user_agent VARCHAR(255),
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT wec_weblog_entry_id_fk
        FOREIGN KEY (weblog_entry_id) REFERENCES weblog_entry(id),
    ADD CONSTRAINT wec_creator_id_fk
        FOREIGN KEY (creator_id) REFERENCES weblogger_user(id);

ALTER TABLE weblog_entry_tag
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36) NOT NULL,
    CHANGE COLUMN entryid weblog_entry_id CHAR(36) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT wtag_weblog_id_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id),
    ADD CONSTRAINT wtag_entry_id_fk
        FOREIGN KEY (weblog_entry_id) REFERENCES weblog_entry(id);
