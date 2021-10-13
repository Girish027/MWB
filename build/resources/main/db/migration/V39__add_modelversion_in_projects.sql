ALTER TABLE projects ADD model_version int(11) NOT NULL DEFAULT 0;
UPDATE projects p JOIN (SELECT project_id, MAX(version) AS max_version FROM models GROUP BY project_id) sp ON p.id = sp.project_id SET p.model_version = sp.max_version;
