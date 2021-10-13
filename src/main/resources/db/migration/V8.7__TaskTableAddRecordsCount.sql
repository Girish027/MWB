ALTER TABLE taskevents RENAME TO taskevents_orig;

CREATE TABLE IF NOT EXISTS taskevents (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  job_id INTEGER NOT NULL,
  task ENUM('CATEGORIZE', 'INDEX') DEFAULT NULL,
  created_at bigint NOT NULL,
  modified_at bigint NOT NULL,
  records_imported INTEGER DEFAULT 0,
  records_processed INTEGER DEFAULT 0,
  status ENUM('STARTED', 'QUEUED', 'RUNNING', 'COMPLETED', 'CANCELLED', 'FAILED') DEFAULT NULL,
  error_code ENUM('OK', 'CANCELLED', 'ELKNOTFOUND', 'ELKFAILED', 'CATFAILED') DEFAULT NULL,
  message TEXT
);
CREATE INDEX taskevents_jobid on taskevents(job_id);

INSERT INTO taskevents (
    id, job_id, task, created_at, modified_at,
    status, error_code, message)
SELECT
    id, job_id, task, created_at, modified_at,
    status, error_code, message
FROM
    taskevents_orig;

DROP TABLE taskevents_orig;
