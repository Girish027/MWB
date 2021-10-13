DROP TABLE IF EXISTS models;
CREATE TABLE IF NOT EXISTS models (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  model_id varchar(256) DEFAULT NULL,
  name varchar(64) NOT NULL,
  description varchar(128),
  user_id varchar(64) NOT NULL,
  dataset_ids varchar(32) NOT NULL,
  config_id INTEGER NOT NULL,
  project_id INTEGER NOT NULL,
  created_at bigint NOT NULL,
  updated_at bigint NOT NULL,
  version INTEGER NOT NULL
);
CREATE INDEX idx_model_model_id on models(model_id);
CREATE INDEX idx_model_project_id on models(project_id);

DROP TABLE IF EXISTS model_job_queue;
CREATE TABLE IF NOT EXISTS model_job_queue (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  model_id varchar(256) NOT NULL,
  token varchar(256) NOT NULL,
  status varchar(32) NOT NULL,
  started_at bigint NOT NULL,
  ended_at bigint
);
CREATE INDEX idx_model_job_queue_token on model_job_queue(token);
CREATE INDEX idx_model_job_queue_model_id on model_job_queue(model_id);
