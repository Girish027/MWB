DROP TABLE IF EXISTS model_test_batch;
CREATE TABLE IF NOT EXISTS model_test_batch (
  id varchar(36) PRIMARY KEY NOT NULL,
  client_id INTEGER NOT NULL,
  project_id INTEGER NOT NULL,
  status varchar(20) NOT NULL,
  model_id varchar(128) NOT NULL,
  request_payload varchar(256) NOT NULL,
  result_file varchar(256),
  created_at bigint NOT NULL,
  created_by varchar(36) NOT NULL,
  modified_at bigint,
  modified_by varchar(36)
);
