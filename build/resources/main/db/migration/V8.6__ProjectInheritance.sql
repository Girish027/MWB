DROP TABLE IF EXISTS project_inheritance;
CREATE TABLE IF NOT EXISTS project_inheritance (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  inheriting_from_client_id INTEGER NOT NULL,
  inheriting_from_project_id INTEGER NOT NULL,
  inheriting_from_dataset_id INTEGER NOT NULL,
  inheriting_into_client_id INTEGER NOT NULL,
  inheriting_into_project_id INTEGER NOT NULL,
  inheriting_into_dataset_id INTEGER,
  UNIQUE(inheriting_from_client_id, inheriting_from_project_id, inheriting_from_dataset_id, inheriting_into_client_id, inheriting_into_project_id, inheriting_into_dataset_id)
);
