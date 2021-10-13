DROP TABLE IF EXISTS preferences;
CREATE TABLE preferences
(
 `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `client_id` int(11) NOT NULL,
  `level` varchar(100) NOT NULL DEFAULT '',
  `type` varchar(100) NOT NULL DEFAULT '',
  `key` varchar(200) NOT NULL DEFAULT '',
  `value` varchar(200) DEFAULT '',
  `status` varchar(100) NOT NULL DEFAULT 'ENABLED',
  `created_by` varchar(100) DEFAULT '',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by` varchar(100) DEFAULT '',
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`));


DROP TABLE IF EXISTS vectorizer;
CREATE TABLE vectorizer (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  type varchar(100) NOT NULL DEFAULT '',
  version varchar(64) DEFAULT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by varchar(100) DEFAULT NULL,
  modified_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  modified_by varchar(100) DEFAULT NULL,
  PRIMARY KEY (id));
INSERT INTO vectorizer (type, version, created_by, modified_by)
        VALUES ('n-gram', NULL, 'mwb_user', 'mwb_user');


ALTER TABLE models ADD vectorizer_type INTEGER NOT NULL default 1;


