package com.tfs.learningsystems.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import java.util.HashMap;
import java.util.List;
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
 * The table was named as 'model_configs'. we will rename it to 'model_config' (without the s )
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "model_configs")
public class ModelConfigBO extends BusinessObject<ModelConfigBO, String> {

  public static final String DB_PREFIX = "mcf";

  public static final String FLD_NAME = "name";
  public static final String FLD_PROJECT_ID = "projectId";
  public static final String FLD_CREATED_AT = "createdAt";

  public static final String FLD_CID = "cid";

  public static final String FLD_CONFIG_ID = "id";

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

  @Column(name = "user_id")
  protected String userId;

  @Column(name = "name")
  protected String name;

  @Column(name = "description")
  protected String description;

  @Column(name = "config_file")
  protected String configFile;

  @Column(name = "legacy_config_file")
  protected String legacyConfigFile;

  @Column(name = "word_classes_file")
  protected String wordClassesFile;

  @Column(name = "stopwords_file")
  protected String stopwordsFile;

  @Column(name = "contractions_file")
  protected String contractionsFile;

  @Column(name = "stemming_exceptions_file")
  protected String stemmingExceptionsFile;

  @Column(name = "created_at")
  protected Long createdAt;

  @Column(name = "modified_at")
  protected Long modifiedAt;

  @Override
  @JsonIgnore
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public ModelConfigBO getProjectsLatestConfig(final String projectId) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(FLD_PROJECT_ID, projectId);
    Sort sort = Sort.by(Sort.Direction.DESC, OBJ_ID);
    ModelConfigBO config = findOne(paramMap, sort);

    if (config == null) {
      log.info("No latest config with project id: {} ", projectId);
    }
    return config;
  }

  public ModelConfigCollection getModelConfigsByProjectId(final String projectId) {
    ModelConfigCollection modelConfigDetails = new ModelConfigCollection();
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(FLD_PROJECT_ID, projectId);
    Sort sort = Sort.by(Sort.Direction.DESC, FLD_CREATED_AT);

    List<ModelConfigBO> configs = this.list(paramMap, sort);
    if (configs == null || configs.isEmpty()) {
      log.warn("No configuration files with project id: {} ", projectId);
    } else {
      modelConfigDetails.addAll(configs);
    }
    return (modelConfigDetails);
  }


  public ModelConfigBO getModelConfigsByName(final String name) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(FLD_NAME, name);
    Sort sort = Sort.by(Sort.Direction.DESC, OBJ_ID);
    ModelConfigBO config = findOne(paramMap, sort);

    if (config == null) {
      log.warn("No configuration files with project id: {} ", name);
    }
    return (config);
  }

  public ModelConfigCollection listConfigs(final Integer limit,
      final Integer startIndex) {

    ModelConfigCollection modelConfigDetails = new ModelConfigCollection();
    Map<String, Object> paramMap = new HashMap<>();

    Sort sort = Sort.by(Sort.Direction.ASC, OBJ_ID);
    List<ModelConfigBO> configs = this.page(paramMap, startIndex, limit, sort);
    if (configs == null || configs.isEmpty()) {
      log.warn("Failed fetching configs ");
    } else {
      modelConfigDetails.addAll(configs);
    }
    return (modelConfigDetails);
  }

}
