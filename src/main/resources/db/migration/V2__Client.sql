DROP TABLE IF EXISTS client;
CREATE TABLE IF NOT EXISTS client (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name varchar(64) NOT NULL,
  internal_id varchar(128) NOT NULL,
  description varchar(128),
  address varchar(128),
  is_vertical BOOLEAN DEFAULT FALSE,
  created_at bigint NOT NULL,
  modified_at bigint DEFAULT 1,
  state ENUM('ENABLED', 'DISABLED') DEFAULT 'ENABLED'
);
CREATE UNIQUE INDEX client_name on client(name);
CREATE UNIQUE INDEX client_internal_id on client(internal_id);

