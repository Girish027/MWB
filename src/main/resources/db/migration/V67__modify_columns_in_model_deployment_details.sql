ALTER TABLE model_deployment_details CHANGE COLUMN deployed_at deployed_start BIGINT NOT NULL;

ALTER TABLE model_deployment_details ADD deployed_end BIGINT;
