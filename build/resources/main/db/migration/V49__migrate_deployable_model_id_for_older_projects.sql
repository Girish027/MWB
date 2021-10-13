SET SQL_SAFE_UPDATES=0;
UPDATE projects,
       (SELECT models.project_id,
               id
        FROM   (SELECT project_id,
                       Max(created_at) AS created_at
                FROM   models
                       INNER JOIN model_job_queue
                               ON model_job_queue.model_id = models.model_id
                                  AND model_job_queue.status = 'COMPLETED'
                GROUP  BY project_id) AS latest_models
               INNER JOIN models
                       ON models.project_id = latest_models.project_id
                          AND models.created_at = latest_models.created_at)
       tempTable
SET    projects.deployable_model_id = tempTable.id
WHERE  projects.id = tempTable.project_id;
SET SQL_SAFE_UPDATES=1;
