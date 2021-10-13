ALTER TABLE preferences CHANGE COLUMN `key` attribute VARCHAR(200) NOT NULL;

ALTER TABLE preferences CHANGE COLUMN created_at created_at BIGINT NOT NULL;

ALTER TABLE preferences CHANGE COLUMN modified_at modified_at BIGINT NOT NULL;

ALTER TABLE vectorizer CHANGE COLUMN created_at created_at BIGINT NOT NULL;

ALTER TABLE vectorizer CHANGE COLUMN modified_at modified_at BIGINT NOT NULL;
