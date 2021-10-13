DROP TABLE IF EXISTS es_data_migration_tracker;
CREATE TABLE IF NOT EXISTS es_data_migration_tracker (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  client_id varchar(36) NOT NULL,
  project_id varchar(36) NOT NULL,
  dataset_id varchar(36) NOT NULL,
  status varchar(20) NOT NULL,
  migration_type varchar(50) NOT NULL,
  created_at bigint NOT NULL,
  created_by varchar(36) NOT NULL,
  modified_at bigint NOT NULL,
  modified_by varchar(36) NOT NULL
);


DROP TABLE IF EXISTS es_data_migration_flag;
CREATE TABLE IF NOT EXISTS es_data_migration_flag (
    id varchar(36) PRIMARY KEY NOT NULL,
    is_migration_done BOOLEAN DEFAULT FALSE
);

INSERT INTO es_data_migration_flag (id, is_migration_done) VALUES 
('1',false);

INSERT INTO es_data_migration_flag (id, is_migration_done) VALUES 
('2',false);


DROP TABLE IF EXISTS es_data_migration_conflicts;
CREATE TABLE IF NOT EXISTS es_data_migration_conflicts (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  client_id varchar(36) NOT NULL,
  project_id varchar(36) NOT NULL,
  intent_tag_data varchar(36) NOT NULL,
  rutag_data varchar(36) NOT NULL,
  intent_tag_data_used varchar(36) NOT NULL,
  rutag_data_used varchar(36) NOT NULL
);


