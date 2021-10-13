DROP TABLE IF EXISTS jobs;
CREATE TABLE IF NOT EXISTS jobs (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  project_id INTEGER NOT NULL,
  dataset_id INTEGER NOT NULL,
  created_at bigint NOT NULL,
  created_by varchar(64) NOT NULL,
  modified_at bigint,
  modified_by varchar(64),
  file_name varchar(256),
  UNIQUE (project_id, dataset_id)
);
CREATE INDEX job_projectid on jobs(project_id);
CREATE INDEX job_datasetid on jobs(dataset_id);

DROP TABLE IF EXISTS taskevents;
CREATE TABLE IF NOT EXISTS taskevents (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  job_id INTEGER NOT NULL,
  task varchar(32) NOT NULL,
  created_at bigint NOT NULL,
  modified_at bigint NOT NULL,
  status varchar(32) NOT NULL,
  error_code varchar(32),
  message varchar(128)
);
CREATE INDEX taskevents_jobid on taskevents(job_id);

