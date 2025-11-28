ALTER TABLE weblog DROP FOREIGN KEY wlog_creatorid_fk;
ALTER TABLE weblog_entry_comment DROP FOREIGN KEY co_userid_fk;
ALTER TABLE media_file DROP FOREIGN KEY mf_creatorid_fk;

ALTER TABLE weblog_entry
    DROP FOREIGN KEY we_weblogid_fk,
    DROP FOREIGN KEY we_categoryid_fk,
    DROP FOREIGN KEY we_creatorid_fk;

ALTER TABLE user_weblog_role
    DROP FOREIGN KEY uwr_userid_fk,
    DROP FOREIGN KEY uwr_weblogid_fk;

ALTER TABLE weblogger_user
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN screenname screen_name VARCHAR(48) NOT NULL AFTER username,
    CHANGE COLUMN emailaddress email_address VARCHAR(128) NOT NULL AFTER screen_name,
    CHANGE COLUMN lastlogin last_login DATETIME AFTER status,
    CHANGE COLUMN activationcode activation_code VARCHAR(48) AFTER last_login,
    CHANGE COLUMN encr_password password_hash VARCHAR(255) AFTER mfa_secret,
    CHANGE COLUMN datecreated date_created TIMESTAMP NOT NULL AFTER password_hash,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL;

ALTER TABLE weblogger_properties DROP FOREIGN KEY wp_weblogid_fk;
ALTER TABLE weblog_template DROP FOREIGN KEY wt_weblogid_fk;
ALTER TABLE blogroll_link DROP FOREIGN KEY bl_weblogid_fk;
ALTER TABLE weblog_category DROP FOREIGN KEY wc_weblogid_fk;
ALTER TABLE media_directory DROP FOREIGN KEY md_weblogid_fk;

ALTER TABLE weblog
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN entriesperpage entries_per_page INTEGER NOT NULL,
    CHANGE COLUMN editformat edit_format VARCHAR(20) NOT NULL,
    CHANGE COLUMN creatorid creator_id CHAR(36) NOT NULL,
    CHANGE COLUMN datecreated date_created TIMESTAMP NOT NULL AFTER hits_today,
    CHANGE COLUMN lastmodified date_updated TIMESTAMP NOT NULL AFTER date_created,
    CHANGE COLUMN commentdays default_comment_days INTEGER NOT NULL,
    CHANGE COLUMN analyticscode analytics_code TEXT,
    CHANGE COLUMN blacklist comment_spam_filter TEXT,
    CHANGE COLUMN hitstoday hits_today INTEGER DEFAULT 0 NOT NULL,
    ADD CONSTRAINT wlog_creatorid_fk
        FOREIGN KEY (creator_id) REFERENCES weblogger_user(id);

-- hardcode UUID to enforce just one row
update weblogger_properties set id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE weblogger_properties
    CHANGE COLUMN id id CHAR(36) CHECK (id = '00000000-0000-0000-0000-000000000001'),
    DROP COLUMN database_version,
    CHANGE COLUMN main_blog_id main_blog_id CHAR(36),
    CHANGE COLUMN registration_policy registration_policy VARCHAR(24) NOT NULL,
    CHANGE COLUMN users_create_blogs users_create_blogs TINYINT NOT NULL,
    CHANGE COLUMN blog_html_policy blog_html_policy VARCHAR(24) NOT NULL,
    CHANGE COLUMN users_customize_themes users_customize_themes TINYINT NOT NULL,
    CHANGE COLUMN users_override_analytics_code users_override_analytics_code TINYINT NOT NULL,
    CHANGE COLUMN comment_policy comment_policy VARCHAR(24) NOT NULL,
    CHANGE COLUMN comment_html_policy comment_html_policy VARCHAR(24) NOT NULL,
    CHANGE COLUMN spam_policy spam_policy VARCHAR(20) NOT NULL,
    CHANGE COLUMN users_comment_notifications users_comment_notifications TINYINT NOT NULL,
    CHANGE COLUMN max_file_uploads_size_mb max_file_uploads_size_mb INTEGER NOT NULL,
    CHANGE COLUMN comment_spam_filter global_spam_filter text,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT wp_weblogid_fk
        FOREIGN KEY (main_blog_id) REFERENCES weblog(id);

ALTER TABLE user_weblog_role
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN userid user_id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT uwr_userid_fk
        FOREIGN KEY (user_id) REFERENCES weblogger_user(id),
    ADD CONSTRAINT uwr_weblogid_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id);

ALTER TABLE weblog_category
    CHANGE COLUMN id id CHAR(36) NOT NULL,
    CHANGE COLUMN weblogid weblog_id CHAR(36) NOT NULL,
    ADD COLUMN date_created TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN date_updated TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD CONSTRAINT wc_weblogid_fk
        FOREIGN KEY (weblog_id) REFERENCES weblog(id);
