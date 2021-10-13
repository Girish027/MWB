DROP TABLE IF EXISTS files;
CREATE TABLE IF NOT EXISTS files (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  file_id varchar(64) NOT NULL,
  name varchar(128) NOT NULL,
  system_name varchar(64) NOT NULL,
  user varchar(128) NOT NULL,
  created_at bigint NOT NULL,
  modified_at bigint NOT NULL
);
CREATE UNIQUE INDEX files_fileid on files(file_id);
CREATE INDEX files_user on files(user);


