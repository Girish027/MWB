ALTER TABLE model_configs DROP INDEX idx_model_configs_name;
ALTER TABLE model_configs DROP INDEX name;
Alter TABLE model_configs MODIFY COLUMN config_file MEDIUMTEXT;
ALTER TABLE model_configs ADD INDEX idx_model_configs_project_id (project_id);
