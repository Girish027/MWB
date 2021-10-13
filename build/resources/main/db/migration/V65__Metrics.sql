DROP TABLE IF EXISTS metrics;
CREATE TABLE IF NOT EXISTS metrics (
  id int(11) unsigned PRIMARY KEY AUTO_INCREMENT NOT NULL,
  client_id INTEGER NOT NULL,
  model_name varchar(64),
  node_name varchar(64),
  metric_date timestamp NOT NULL,
  volume bigint,
  escalation bigint,
  created_at bigint NOT NULL,
  modified_at bigint,
  FOREIGN KEY (client_id)
        REFERENCES client(id),
  UNIQUE KEY `client_model_node_date` (`client_id`,`model_name`,`node_name`, `metric_date`)
);
