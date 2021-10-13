DROP TABLE IF EXISTS tagging_guide_columns;
CREATE TABLE IF NOT EXISTS tagging_guide_columns (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name varchar(128) NOT NULL,
  required BOOLEAN NOT NULL,
  display_name varchar(128)
);
CREATE INDEX idx_tagging_guide_columns_name on tagging_guide_columns(name);

DROP TABLE IF EXISTS tagging_guide_column_mappings;
CREATE TABLE IF NOT EXISTS tagging_guide_column_mappings (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  user_id varchar(64),
  column_id INTEGER NOT NULL,
  project_id INTEGER NOT NULL,
  column_index INTEGER NOT NULL,
  display_name varchar(128)
);
CREATE UNIQUE INDEX idx_user_column on tagging_guide_column_mappings(user_id, column_id, project_id);
