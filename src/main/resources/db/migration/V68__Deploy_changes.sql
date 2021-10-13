UPDATE model_deployment_details curmdd,
(select
      jointmdd.id,
      jointmdd.deployed_start,
      if( @lastproject_id = jointmdd.project_id,@lastdeployed_start, 0 ) as updatedEndTime,
      @lastproject_id := jointmdd.project_id,
      @lastdeployed_start := jointmdd.deployed_start
   from
      (SELECT a.id as id, a.deployed_start as deployed_start,a.deployed_end as deployed_end, b.project_id as project_id FROM
       (SELECT id, deployed_start, deployed_end FROM model_deployment_details) a INNER JOIN
        (SELECT id, deployment_id, project_id, model_id from model_deployment_map) b
         ON a.id = b.deployment_id ORDER BY project_id,deployed_start) jointmdd,
      ( select @lastproject_id := 0,
               @lastdeployed_start := 0 ) SQLVars
   order by
      jointmdd.project_id,
      jointmdd.deployed_start desc) endmdd
SET curmdd.deployed_end = endmdd.updatedEndTime
WHERE (curmdd.id = endmdd.id AND curmdd.id != 0)