

DROP TABLE IF EXISTS mwb_its_client_map;
CREATE TABLE IF NOT EXISTS mwb_its_client_map (
  id INT(11) NOT NULL,
  its_client_id varchar(128) NOT NULL,
  its_app_id varchar(128) NOT NULL,
  description varchar(128),
  created_at bigint NOT NULL,
  created_by varchar(64) NOT NULL,
  modified_at bigint,
  modified_by varchar(64),
  UNIQUE KEY (`id`),
  FOREIGN KEY (id)
        REFERENCES client(id)
        ON DELETE CASCADE,
  UNIQUE KEY `its_client_app` (`its_client_id`,`its_app_id`)
);



    