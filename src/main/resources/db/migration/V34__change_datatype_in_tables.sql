-- upload any number of datasets (NT-2952)
ALTER TABLE dataset_intent_inheritance MODIFY inherited_from_dataset_ids TEXT;

-- create a model with more than 4 datasets
ALTER TABLE models MODIFY dataset_ids TEXT;

-- increase size of uri column
ALTER TABLE datasets MODIFY uri VARCHAR(256) NOT NULL;

-- match the same length as in table : jobs
ALTER TABLE files MODIFY file_id VARCHAR(256) NOT NULL;
ALTER TABLE files MODIFY name VARCHAR(256) NOT NULL;
ALTER TABLE files MODIFY system_name VARCHAR(256) NOT NULL;

-- model config names are in modelName-cfg format. ModelName can have 64 char
ALTER TABLE model_configs MODIFY name VARCHAR(70) NOT NULL;