ALTER TABLE tagging_guide_import_stats RENAME TO tagging_guide_import_stats_orig;

CREATE TABLE IF NOT EXISTS tagging_guide_import_stats (
  id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  project_id INTEGER NOT NULL,
  imported_by varchar(128) NOT NULL,
  imported_at bigint NOT NULL,
  valid_tag_count INTEGER NOT NULL,
  invalid_tags varchar(128),
  missing_tags varchar(128)
);

INSERT INTO tagging_guide_import_stats (
    id, project_id, imported_by, imported_at,
    valid_tag_count, invalid_tags, missing_tags)
SELECT
    id, -1, imported_by, imported_at,
    valid_tag_count, invalid_tags, missing_tags
FROM
    tagging_guide_import_stats_orig;

DROP TABLE tagging_guide_import_stats_orig;
