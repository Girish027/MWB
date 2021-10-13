package com.tfs.learningsystems.ui.nlmodel.model.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("modelDaoBean")
@Slf4j
public class JdbcModelDao implements ModelDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcModelDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  @Override
  public List<TFSModel> getModelsForProject(String projectId) {
    List<TFSModel> listOfModelForProject = new ArrayList<>();

    //https://247inc.atlassian.net/browse/NT-2747
    /*
    final String query = "SELECT m.id, m.model_id, m.name, m.config_id, m.user_id, "
        + "m.project_id, m.version, m.description, m.created_at, m.updated_at, "
        + "m.dataset_ids FROM models m INNER JOIN client c ON m.cid = c.cid WHERE m.project_id = :projectId AND c.id=:clientId ";
    Map<String, String> paramMap = new HashMap<>();
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("projectId", projectId);
    paramMap.put("clientId", clientId);*/
    final String query = "SELECT a.id, a.model_id, name, a.config_id, a.user_id, "
        + "a.project_id, a.version, a.description, a.created_at, a.updated_at, "
        + "a.dataset_ids, a.model_type, a.accuracy, a.weightedFScore, "
            + "a.digital_hosted_url, a.speech_model_id, a.speech_config_id , a.vectorizer_type as vectorizer_id,"
            +" b.type as technology_type ,b.version as technology_version "
            + "FROM models as a LEFT JOIN vectorizer as b ON a.vectorizer_type=b.id  "
            +"WHERE a.project_id = :projectId ";
    Map<String, String> paramMap = new HashMap<>();
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("projectId", projectId);
    try {
      listOfModelForProject = this.jdbcTemplate.query(query, paramMap, new TFSModelRowMapper());
    } catch (Exception dae) {
      log.error("Failed fetching model with project id: {}", projectId, dae);
    }
    return listOfModelForProject;
  }

  @Override
  public void updateModelJobStatusByModelId(String modelId, String modelType, TFSModelJobState status) {
    Map<String, Object> paramMap = new HashMap<>();
    //https://247inc.atlassian.net/browse/NT-2747
      /*final StringBuilder query = new StringBuilder(
          "UPDATE model_job_queue m INNER JOIN client c ON m.cid = c.cid SET m.status = :status");
      modelId = StringEscapeUtils.escapeSql(modelId);
      paramMap.put("id", modelId);
      paramMap.put("clientId", clientId);
      paramMap.put("status", status.getStatus().toString());
      if (status.getEndedAt() != 0) {
        paramMap.put("endedAt", status.getEndedAt());
        query.append(", ended_at=:endedAt ");
      }
      query.append(" WHERE c.id= :clientId AND m.model_id= :id");

      this.jdbcTemplate.update(query.toString(), paramMap);*/
    final StringBuilder query = new StringBuilder("UPDATE model_job_queue SET status = :status ");
    modelId = StringEscapeUtils.escapeSql(modelId);
    paramMap.put("id", modelId);
    paramMap.put("status", status.getStatus().toString());
    paramMap.put("modelType", modelType);

    if (status.getEndedAt() != 0) {
      paramMap.put("endedAt", status.getEndedAt());
      query.append(", ended_at=:endedAt ");
    }
    query.append(" WHERE model_id = :id and model_type = :modelType");
    this.jdbcTemplate.update(query.toString(), paramMap);
  }

  @Override
  public void updateModelJobStatusModelTypeByModelIdModelType(String modelId, TFSModelJobState status, String currentModelType) {
    Map<String, Object> paramMap = new HashMap<>();
    final StringBuilder query = new StringBuilder("UPDATE model_job_queue SET status = :status ");
    modelId = StringEscapeUtils.escapeSql(modelId);
    paramMap.put("id", modelId);
    paramMap.put("status", status.getStatus().toString());
    paramMap.put("modelType", status.getModelType());
    paramMap.put("currentModelType", currentModelType);

    if (status.getEndedAt() != 0) {
      paramMap.put("endedAt", status.getEndedAt());
      query.append(", ended_at=:endedAt ");
    }
    query.append(", model_type=:modelType");
    query.append(" WHERE model_id = :id and model_type = :currentModelType");
    this.jdbcTemplate.update(query.toString(), paramMap);
  }

  @Override
  public TFSModelJobState getModelJobStatus(String id, String modelType) {
    TFSModelJobState job = null;

    //https://247inc.atlassian.net/browse/NT-2747
    /*
    Map<String, Object> paramMap = new HashMap<>();
    final String query = "SELECT m.id, m.model_id, m.token, m.status, m.started_at, "
        + "m.ended_at FROM model_job_queue m INNER JOIN client c ON m.cid = c.cid WHERE c.id= :clientId AND m.model_id = :id "
        + "ORDER BY started_at DESC LIMIT 1";
    id = StringEscapeUtils.escapeSql(id);
    paramMap.put("clientId", clientId);
    paramMap.put("id", id);*/
    Map<String, Object> paramMap = new HashMap<>();
    final String query = "SELECT id, model_id, model_type, token, status, started_at, "
        + "ended_at FROM model_job_queue WHERE model_id = :id and model_type = :modelType "
        + "ORDER BY started_at DESC LIMIT 1";
    id = StringEscapeUtils.escapeSql(id);
    paramMap.put("id", id);
    paramMap.put("modelType", modelType);

    try {
      job = this.jdbcTemplate.queryForObject(query, paramMap, new TFSModelJobRowMapper());

    } catch (Exception e) {
      log.debug("No jobs found for model id {}", id);
    }

    return job;
  }

  @Override
  public List<TFSModel> getModelsForModelIds(ArrayList<String> modelIds) {

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("modelIds", modelIds);

    final String modelsQuery = "SELECT id, model_id, name, config_id, user_id, "
        + "project_id, version, description, created_at, updated_at, "
        + " dataset_ids, model_type, accuracy, weightedFScore FROM models M "
        + "WHERE M.id in (:modelIds)";

    List<TFSModel> models = this.getModelsByIdQuery(modelsQuery, paramMap);

    return models;

  }

  private List<TFSModel> getModelsByIdQuery(final String query,
      final Map<String, ?> paramMap) {

    List<TFSModel> models = new LinkedList<TFSModel>();
    try {
      models = jdbcTemplate.query(query, paramMap, new TFSModelRowMapper());
    } catch (Exception e) {
      log.warn("models from query:{} and params: {} not found", query, paramMap);
    }
    return models;
  }

  public static class TFSModelRowMapper implements RowMapper<TFSModel> {

    @Override
    public TFSModel mapRow(ResultSet rs, int rowNum) throws SQLException {
      TFSModel tfsModel = new TFSModel();
      tfsModel.setId(String.valueOf(rs.getLong("id")));
      tfsModel.setName(rs.getString("name"));
      tfsModel.setModelId(rs.getString("model_id"));
      tfsModel.setUserId(rs.getString("user_id"));
      tfsModel.setConfigId(String.valueOf(rs.getInt("config_id")));
      tfsModel.setProjectId(String.valueOf(rs.getInt("project_id")));
      String datasetIdsStr = rs.getString("dataset_ids");
      tfsModel.setDatasetIds(Arrays.asList(datasetIdsStr.split(",")));
      tfsModel.setVersion(rs.getInt("version"));
      tfsModel.setCreatedAt(rs.getLong("created_at"));
      tfsModel.setUpdatedAt(rs.getLong("updated_at"));
      tfsModel.setDescription(rs.getString("description"));
      tfsModel.setModelType(rs.getString("model_type"));
      tfsModel.setVectorizer_id(rs.getInt("vectorizer_id"));
      tfsModel.setTechnology_type(rs.getString("technology_type"));
      tfsModel.setTechnology_version(rs.getString("technology_version"));
      tfsModel.setModelAccuracy(rs.getString("accuracy"));
      tfsModel.setSpeechConfigId(String.valueOf(rs.getInt("speech_config_id")));
      tfsModel.setModelWeightedFScore(rs.getString("weightedFScore"));
      tfsModel.setSpeechModelId(rs.getString("speech_model_id"));
      tfsModel.setDigitalHostedUrl(rs.getString("digital_hosted_url"));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(tfsModel);
      return tfsModel;
    }

  }

  public static class TFSModelJobRowMapper implements RowMapper<TFSModelJobState> {

    @Override
    public TFSModelJobState mapRow(ResultSet rs, int rowNum) throws SQLException {
      TFSModelJobState job = new TFSModelJobState();
      job.setId(String.valueOf(rs.getLong("id")));
      job.setModelId(rs.getString("model_id"));
      job.setModelType(rs.getString("model_type"));
      job.setToken(rs.getString("token"));
      job.setStartedAt(rs.getLong("started_at"));
      job.setEndedAt(rs.getLong("ended_at"));
      job.setStatus(TFSModelJobState.Status.lookup(rs.getString("status")));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(job);
      return job;
    }

  }

}
