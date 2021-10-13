DROP TABLE IF EXISTS model_configs;
CREATE TABLE IF NOT EXISTS model_configs (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name varchar(64) NOT NULL,
  user_id varchar(64) NOT NULL,
  description varchar(64),
  project_id INTEGER NOT NULL,
  created_at bigint NOT NULL,
  modified_at bigint NOT NULL,
  config_file TEXT,
  legacy_config_file TEXT,
  word_classes_file TEXT,
  stopwords_file TEXT,
  contractions_file TEXT,
  stemming_exceptions_file TEXT,
  UNIQUE(name, project_id)
);
CREATE INDEX idx_model_configs_name on model_configs(name);
