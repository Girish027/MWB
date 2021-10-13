ALTER TABLE projects DROP INDEX projects_name;
CREATE INDEX projects_name on projects(name);