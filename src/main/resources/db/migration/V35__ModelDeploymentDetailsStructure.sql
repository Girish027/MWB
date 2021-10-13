DROP TABLE IF EXISTS model_deployment_map;

DROP TABLE IF EXISTS model_deployment_details;


CREATE TABLE IF NOT EXISTS model_deployment_details (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  git_hub_tag VARCHAR(128) NOT NULL,
  deployment_job_id  VARCHAR(128) NOT NULL,
  client_id INT(50) NOT NULL,
  deployed_by varchar(64) NOT NULL,
  deployed_at bigint NOT NULL,
  status VARCHAR(50) NOT NULL,
  UNIQUE KEY (`deployment_job_id`),
  UNIQUE KEY (`git_hub_tag`),
  UNIQUE KEY `client_model_deployment` (`git_hub_tag`,`deployment_job_id`,`client_id`)
);


CREATE TABLE IF NOT EXISTS model_deployment_map (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  deployment_id INTEGER NOT NULL,
  git_hub_tag  VARCHAR(128) NOT NULL,
  project_id INTEGER NOT NULL,
  model_id VARCHAR(256) NOT NULL,
  UNIQUE KEY `project_model_deployment` (`deployment_id`,`git_hub_tag`,`project_id`,`model_id`)
);