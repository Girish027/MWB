alter table datasets ADD project_id varchar(16) NOT NULL;
SET SQL_SAFE_UPDATES=0;
update datasets ds inner join datasets_projects dp ON ds.id = dp.dataset_id set ds.project_id = dp.project_id;
SET SQL_SAFE_UPDATES=1;
ALTER TABLE datasets DROP INDEX name, ADD UNIQUE KEY `name` (`name`,`client_id`,`project_id`);