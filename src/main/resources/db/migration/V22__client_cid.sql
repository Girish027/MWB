--
-- generate DbId in MySQL. Please refer to DbId.java for details
--
SET GLOBAL log_bin_trust_function_creators = 1;

delimiter $
DROP FUNCTION IF EXISTS guv$

CREATE FUNCTION guv(seqID VARCHAR(10))
RETURNS varchar(17)
BEGIN
    DECLARE uniqueValue VARCHAR(17);
    SET uniqueValue = seqID;
    WHILE LENGTH(uniqueValue) < 17 DO
        SELECT CONCAT(SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ', RAND()*24+1, 1), uniqueValue) INTO @newUniqueValue;
        SET uniqueValue = @newUniqueValue ;
    END WHILE ;
    RETURN uniqueValue;
END$
delimiter ;

--
-- add 'cid' in 'client' table, and set values
--
Alter TABLE client ADD COLUMN cid varchar(20);
UPDATE client SET cid = '00000000000000000000' where is_vertical = 1;
UPDATE client set cid = CONCAT('clt', guv(CONV(id , 10 , 36))) where is_vertical = 0;

--
-- add 'cid' in 'datasets' table, and set values
--
Alter TABLE datasets ADD COLUMN cid varchar(20);
UPDATE datasets
INNER JOIN client ON datasets.client_id = client.id
SET datasets.cid = client.cid;

Alter TABLE datasets ADD COLUMN dbid varchar(20);
UPDATE datasets SET dbid = '00000000000000000000';
UPDATE datasets set dbid = CONCAT('dst', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'projects' table, and set values
--
Alter TABLE projects ADD COLUMN cid varchar(20);
UPDATE projects
INNER JOIN client ON projects.client_id = client.id
SET projects.cid = client.cid;

Alter TABLE projects ADD COLUMN dbid varchar(20);
UPDATE projects SET dbid = '00000000000000000000';
UPDATE projects set dbid = CONCAT('prj', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'model_test_batch' table, and set values
--
Alter TABLE model_test_batch ADD COLUMN cid varchar(20);
UPDATE model_test_batch
INNER JOIN client ON model_test_batch.client_id = client.id
SET model_test_batch.cid = client.cid;

Alter TABLE model_test_batch ADD COLUMN dbid varchar(20);
UPDATE model_test_batch SET dbid = '00000000000000000000';
--
-- for now, not to convert these M.T.B.. They don't have integer IDs
-- UPDATE model_test_batch set dbid = CONCAT('mtb', guv(CONV(id , 10 , 36)));

--
-- following tables doesn't have direct reference to 'client'. So this is run after 'projects' table
--
-- add 'cid' in 'models' table, and set values
--
Alter TABLE models ADD COLUMN cid varchar(20);
UPDATE models
INNER JOIN projects ON models.project_id = projects.id
SET models.cid = projects.cid;

Alter TABLE models ADD COLUMN dbid varchar(20);
UPDATE models SET dbid = '00000000000000000000';
UPDATE models set dbid = CONCAT('mod', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'tagging_guide_column_mappings' table, and set values
--
Alter TABLE tagging_guide_column_mappings ADD COLUMN cid varchar(20);
UPDATE tagging_guide_column_mappings
INNER JOIN projects ON tagging_guide_column_mappings.project_id = projects.id
SET tagging_guide_column_mappings.cid = projects.cid;

Alter TABLE tagging_guide_column_mappings ADD COLUMN dbid varchar(20);
UPDATE tagging_guide_column_mappings SET dbid = '00000000000000000000';
UPDATE tagging_guide_column_mappings set dbid = CONCAT('tgm', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'tagging_guide_import_stats' table, and set values
--
Alter TABLE tagging_guide_import_stats ADD COLUMN cid varchar(20);
UPDATE tagging_guide_import_stats
INNER JOIN projects ON tagging_guide_import_stats.project_id = projects.id
SET tagging_guide_import_stats.cid = projects.cid;

Alter TABLE tagging_guide_import_stats ADD COLUMN dbid varchar(20);
UPDATE tagging_guide_import_stats SET dbid = '00000000000000000000';
UPDATE tagging_guide_import_stats set dbid = CONCAT('tis', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'jobs' table, and set values
--
Alter TABLE jobs ADD COLUMN cid varchar(20);
UPDATE jobs
INNER JOIN projects ON jobs.project_id = projects.id
SET jobs.cid = projects.cid;

Alter TABLE jobs ADD COLUMN dbid varchar(20);
UPDATE jobs SET dbid = '00000000000000000000';
UPDATE jobs set dbid = CONCAT('job', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'model_configs' table, and set values
--
Alter TABLE model_configs ADD COLUMN cid varchar(20);
UPDATE model_configs
INNER JOIN projects ON model_configs.project_id = projects.id
SET model_configs.cid = projects.cid;

Alter TABLE model_configs ADD COLUMN dbid varchar(20);
UPDATE model_configs SET dbid = '00000000000000000000';
UPDATE model_configs set dbid = CONCAT('mcf', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'dataset_intent_inheritance' table, and set values
--
Alter TABLE dataset_intent_inheritance ADD COLUMN cid varchar(20);
UPDATE dataset_intent_inheritance
INNER JOIN projects ON dataset_intent_inheritance.project_id = projects.id
SET dataset_intent_inheritance.cid = projects.cid;

Alter TABLE dataset_intent_inheritance ADD COLUMN dbid varchar(20);
UPDATE dataset_intent_inheritance SET dbid = '00000000000000000000';
UPDATE dataset_intent_inheritance set dbid = CONCAT('dii', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'datasets_projects' table, and set values
-- No DbId here
--
Alter TABLE datasets_projects ADD COLUMN cid varchar(20);
UPDATE datasets_projects
INNER JOIN projects ON datasets_projects.project_id = projects.id
SET datasets_projects.cid = projects.cid;

--
-- following tables doesn't have direct reference to 'client'. So this is run after 'models' table
--
--
-- add 'cid' in 'model_job_queue' table, and set values
--
Alter TABLE model_job_queue ADD COLUMN cid varchar(20);
UPDATE model_job_queue
INNER JOIN models ON model_job_queue.model_id = models.id
SET model_job_queue.cid = models.cid;

Alter TABLE model_job_queue ADD COLUMN dbid varchar(20);
UPDATE model_job_queue SET dbid = '00000000000000000000';
UPDATE model_job_queue set dbid = CONCAT('mjq', guv(CONV(id , 10 , 36)));

--
-- add 'cid' in 'taskevents' table, and set values
--
Alter TABLE taskevents ADD COLUMN cid varchar(20);
UPDATE taskevents
INNER JOIN model_job_queue ON taskevents.job_id = model_job_queue.id
SET taskevents.cid = model_job_queue.cid;

Alter TABLE taskevents ADD COLUMN dbid varchar(20);
UPDATE taskevents SET dbid = '00000000000000000000';
UPDATE taskevents set dbid = CONCAT('tet', guv(CONV(id , 10 , 36)));

--
-- add 'dbid' in 'files' table, and set values
--
Alter TABLE files ADD COLUMN dbid varchar(20);
UPDATE files SET dbid = '00000000000000000000';
UPDATE files set dbid = CONCAT('fil', guv(CONV(id , 10 , 36)));

--
-- add 'dbid' in 'file_column_mappings' table, and set values
--
Alter TABLE file_column_mappings ADD COLUMN dbid varchar(20);
UPDATE file_column_mappings SET dbid = '00000000000000000000';
UPDATE file_column_mappings set dbid = CONCAT('fcm', guv(CONV(id , 10 , 36)));

--
-- add 'dbid' in 'file_column_mappings' table, and set values
--
Alter TABLE file_columns ADD COLUMN dbid varchar(20);
UPDATE file_columns SET dbid = '00000000000000000000';
UPDATE file_columns set dbid = CONCAT('fcs', guv(CONV(id , 10 , 36)));

--
-- add 'dbid' in 'tagging_guide_columns' table, and set values
--
Alter TABLE tagging_guide_columns ADD COLUMN dbid varchar(20);
UPDATE tagging_guide_columns SET dbid = '00000000000000000000';
UPDATE tagging_guide_columns set dbid = CONCAT('tcs', guv(CONV(id , 10 , 36)));

DROP FUNCTION IF EXISTS guv;
SET GLOBAL log_bin_trust_function_creators = 0;