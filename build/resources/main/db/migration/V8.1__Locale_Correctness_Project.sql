ALTER TABLE projects RENAME TO projects_orig;

CREATE TABLE IF NOT EXISTS projects (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  group_id INTEGER NOT NULL,
  client_id INTEGER NOT NULL,
  owner_id varchar(64) NOT NULL,
  name varchar(64) NOT NULL,
  description varchar(128),
  start_at bigint,
  end_at bigint,
  vertical varchar(32),
  created_at bigint NOT NULL,
  modified_at bigint,
  modified_by varchar(64),
  locale varchar(32) NOT NULL DEFAULT 'en-US',
  state varchar(16) NOT NULL DEFAULT 'ENABLED',
  UNIQUE(name, client_id)
);

CREATE INDEX projects_name on projects(name);

INSERT INTO projects (
    id, group_id, client_id, owner_id, name,
    description, start_at, end_at, vertical, created_at,
    modified_at, modified_by, state)
SELECT
    id, group_id, client_id, owner_id, name,
    description, start_at, end_at, vertical, created_at,
    modified_at, modified_by, state
FROM
    projects_orig;

DROP TABLE projects_orig;
