ALTER TABLE model_deployment_details DROP INDEX git_hub_tag;

ALTER TABLE model_deployment_details DROP INDEX deployment_job_id;

ALTER TABLE model_deployment_details MODIFY COLUMN deployment_job_id VARCHAR(128)  null;

ALTER TABLE model_deployment_details MODIFY COLUMN git_hub_tag VARCHAR(128)  null;