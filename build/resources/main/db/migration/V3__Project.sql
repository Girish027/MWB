DROP TABLE IF EXISTS projects;
CREATE TABLE IF NOT EXISTS projects (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  group_id INTEGER NOT NULL,
  client_id INTEGER NOT NULL,
  owner_id varchar(64) NOT NULL,
  name varchar(64) NOT NULL,
  description varchar(128),
  start_at bigint,
  end_at bigint,
  vertical varchar(64),
  created_at bigint NOT NULL,
  modified_at bigint,
  modified_by varchar(64),
  locale varchar(32) NOT NULL DEFAULT 'en_US.UTF-8',
  state ENUM('ENABLED', 'DISABLED') DEFAULT 'ENABLED',
  UNIQUE(name, client_id)
);
CREATE INDEX projects_name on projects(name);
