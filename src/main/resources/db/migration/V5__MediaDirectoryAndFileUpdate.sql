ALTER TABLE media_file DROP FOREIGN KEY mf_directoryid_fk;

ALTER TABLE media_directory
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36),
    CHANGE COLUMN name name VARCHAR(255) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT md_weblog_name_uk UNIQUE (weblog_id, name),
    ADD CONSTRAINT md_weblog_id_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id);

ALTER TABLE media_file
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN directoryid directory_id CHAR(36) NOT NULL,
    ADD COLUMN file_id CHAR(36) AFTER directory_id,
    CHANGE COLUMN creatorid creator_id CHAR(36) NOT NULL,
    CHANGE COLUMN date_uploaded date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    CHANGE COLUMN last_updated date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    CHANGE COLUMN alt_attr alt_attribute VARCHAR(255),
    CHANGE COLUMN title_attr title_attribute VARCHAR(255),
    ADD CONSTRAINT mf_directory_name_uk UNIQUE (directory_id, name),
    ADD CONSTRAINT mf_file_id_uk UNIQUE (file_id),
    ADD CONSTRAINT mf_directory_id_fk
        FOREIGN KEY (directory_id) REFERENCES media_directory(id),
    ADD CONSTRAINT mf_creator_id_fk
        FOREIGN KEY (creator_id) REFERENCES weblogger_user(id);

update media_file set file_id = id;

ALTER TABLE media_file
    CHANGE COLUMN file_id file_id CHAR(36) NOT NULL;
