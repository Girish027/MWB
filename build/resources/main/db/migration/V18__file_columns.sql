DROP TABLE IF EXISTS file_columns;
CREATE TABLE IF NOT EXISTS file_columns (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name varchar(128) NOT NULL,
  required BOOLEAN NOT NULL,
  display_name varchar(128)
);
CREATE INDEX idx_file_columns_name on file_columns(name);

DROP TABLE IF EXISTS file_column_mappings;
CREATE TABLE IF NOT EXISTS file_column_mappings (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  user_id varchar(64) NOT NULL,
  column_id INTEGER NOT NULL,
  column_index INTEGER NOT NULL,
  display_name varchar(128)
);
CREATE UNIQUE INDEX idx_user_column on file_column_mappings(user_id, column_id);
