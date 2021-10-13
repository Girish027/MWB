ALTER TABLE projects DROP INDEX projects_name;
ALTER TABLE projects DROP INDEX name;
CREATE UNIQUE INDEX projects_name on projects(name, client_id, state);