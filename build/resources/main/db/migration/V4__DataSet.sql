DROP TABLE IF EXISTS datasets;
CREATE TABLE IF NOT EXISTS datasets (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name varchar(64) NOT NULL,
  description varchar(128),
  client_id varchar(32) NOT NULL,
  uri varchar(128) NOT NULL,
  data_type varchar(32) NOT NULL,
  created_at bigint NOT NULL,
  created_by varchar(64) NOT NULL,
  received_at bigint NOT NULL,
  modified_at bigint,
  modified_by varchar(64),
  locale varchar(32) NOT NULL DEFAULT 'en_US.UTF-8',
  UNIQUE(name, client_id)
);
CREATE INDEX datasets_name on datasets(name);

DROP TABLE IF EXISTS datasets_projects;
CREATE TABLE IF NOT EXISTS datasets_projects (
  project_id INTEGER NOT NULL,
  dataset_id INTEGER NOT NULL,
  PRIMARY KEY (project_id, dataset_id)
);


