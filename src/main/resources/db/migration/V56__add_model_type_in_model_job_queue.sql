ALTER TABLE model_job_queue ADD model_type varchar(50) DEFAULT NULL;
SET SQL_SAFE_UPDATES=0;
UPDATE model_job_queue a SET a.model_type = ( SELECT b.model_type FROM models b WHERE b.model_id = a.model_id limit 1);
UPDATE model_job_queue SET model_type = 'DIGITAL' where model_type is null;
SET SQL_SAFE_UPDATES=1;