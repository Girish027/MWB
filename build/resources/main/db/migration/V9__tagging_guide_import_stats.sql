DROP TABLE IF EXISTS tagging_guide_import_stats;
CREATE TABLE IF NOT EXISTS tagging_guide_import_stats (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  imported_by varchar(128) NOT NULL,
  imported_at bigint NOT NULL,
  valid_tag_count INTEGER NOT NULL,
  invalid_tags varchar(128),
  missing_tags varchar(128)
);
