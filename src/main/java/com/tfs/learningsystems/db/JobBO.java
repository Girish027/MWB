package com.tfs.learningsystems.db;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'jobs'. we will rename it to 'job' (without the s )
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "jobs")
public class JobBO extends BusinessObject<JobBO, String> {

  public static final String DB_PREFIX = "job";
  public static final String FLD_PROJECT_ID = "projectId";
  public static final String FLD_DATASET_ID = "datasetId";


  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "cid")
  protected String cid = DbId.EMPTY.toString();

  @Column(name = "dbid")
  protected String dbid = DbId.EMPTY.toString();      // we will use this to replace the current ID

  @Column(name = "project_id")
  protected Integer projectId;

  @Column(name = "dataset_id")
  protected Integer datasetId;

  @Column(name = "file_name")
  protected String fileName;

  @Column(name = "created_at")
  protected Long createdAt;

  @Column(name = "modified_at")
  protected Long modifiedAt;

  @Column(name = "created_by")
  protected String createdBy;

  @Column(name = "modified_by")
  protected String modifiedBy;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public JobBO getJobByProjectDataset(String projectId, String datasetId) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(FLD_PROJECT_ID, projectId);
    paramMap.put(FLD_DATASET_ID, datasetId);

    Sort sort = Sort.by(Sort.Direction.DESC, new String[]{OBJ_ID});
    JobBO job = findOne(paramMap, sort);

    if (job == null) {
      log.warn("Job based on params: {} not found", paramMap);
    }
    return job;
  }
}
