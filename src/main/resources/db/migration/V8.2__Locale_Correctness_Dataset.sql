ALTER TABLE datasets RENAME TO datasets_orig;

CREATE TABLE IF NOT EXISTS datasets (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name varchar(64) NOT NULL,
  description varchar(128),
  client_id varchar(16) NOT NULL,
  uri varchar(128) NOT NULL,
  data_type varchar(32) NOT NULL,
  created_at bigint NOT NULL,
  created_by varchar(64) NOT NULL,
  received_at bigint NOT NULL,
  modified_at bigint,
  modified_by varchar(64),
  locale varchar(32) NOT NULL DEFAULT 'en-US',
  UNIQUE(name, client_id)
);

CREATE INDEX datasets_name on datasets(name);

INSERT INTO datasets (
    id, name, description, client_id, uri, data_type, created_at,
    created_by, received_at, modified_at, modified_by)
SELECT
    id, name, description, client_id, uri, data_type, created_at,
    created_by, received_at, modified_at, modified_by
FROM
    datasets_orig;

DROP TABLE datasets_orig;
