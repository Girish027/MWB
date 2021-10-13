ALTER TABLE models ADD model_type varchar(64) NOT NULL DEFAULT 'DIGITAL';
ALTER TABLE models MODIFY description varchar(512);
ALTER TABLE datasets MODIFY description varchar(512);