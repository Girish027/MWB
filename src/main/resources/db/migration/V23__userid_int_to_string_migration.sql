
ALTER TABLE tagging_guide_column_mappings MODIFY user_id varchar(64);

ALTER TABLE dataset_intent_inheritance MODIFY requested_by varchar(64);

ALTER TABLE models MODIFY user_id varchar(64);

ALTER TABLE file_column_mappings MODIFY user_id varchar(64);

ALTER TABLE projects MODIFY owner_id varchar(64);
ALTER TABLE projects MODIFY modified_by varchar(64);

ALTER TABLE datasets MODIFY created_by varchar(64);
ALTER TABLE datasets MODIFY modified_by varchar(64);

ALTER TABLE jobs MODIFY created_by varchar(64);
ALTER TABLE jobs MODIFY modified_by varchar(64);



