DROP TABLE IF EXISTS dataset_intent_inheritance;
CREATE TABLE IF NOT EXISTS dataset_intent_inheritance (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  dataset_id INTEGER NOT NULL,
  project_id INTEGER NOT NULL,
  requested_at bigint NOT NULL,
  requested_by varchar(64),
  total_tagged INTEGER,
  unique_tagged INTEGER,
  total_tagged_multiple_intents INTEGER,
  unique_tagged_multiple_intents INTEGER,
  inherited_from_dataset_ids varchar(128),
  updated_at bigint NOT NULL,
  status varchar(32) NOT NULL
);
CREATE INDEX idx_dataset_intent_inheritance_dataset_id on dataset_intent_inheritance(dataset_id);
CREATE INDEX idx_dataset_intent_inheritance_project_id on dataset_intent_inheritance(project_id);
