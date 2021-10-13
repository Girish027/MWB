/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import com.tfs.learningsystems.ui.model.TaskEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("taskEventDaoBean")
public class JdbcTaskEventDao implements TaskEventDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcTaskEventDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  @Override
  public Map<String, List<TaskEventBO>> getUnfinishedTaskEvents() {
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("queued", TaskEvent.Status.QUEUED.name());
    paramMap.put("running", TaskEvent.Status.RUNNING.name());
    paramMap.put("started", TaskEvent.Status.STARTED.name());
    final String query =
        "SELECT id, job_id, created_at, modified_at, records_processed, records_imported, "
            + "task, status, error_code, message "
            + "FROM taskevents WHERE status IN (:queued, :running, :started)";
    TaskEventRowByJobRowHandler rowHandler = new TaskEventRowByJobRowHandler();
    jdbcTemplate.query(query, paramMap, rowHandler);

    return rowHandler.results;
  }

  @Override
  public Map<String, List<TaskEventBO>> getFailedTaskEvents() {
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("cancelled", TaskEvent.Status.CANCELLED.name());
    paramMap.put("failed", TaskEvent.Status.FAILED.name());
    final String query =
        "SELECT id, job_id, created_at, modified_at, records_processed, records_imported, "
            + "task, status, error_code, message "
            + "FROM taskevents  WHERE status IN (:cancelled, :failed)";
    TaskEventRowByJobRowHandler rowHandler = new TaskEventRowByJobRowHandler();
    jdbcTemplate.query(query, paramMap, rowHandler);

    return rowHandler.results;
  }

  /**
   * This is an implementation of the Spring RowCallbackHandler for retrieving response values for
   * relevancy evaluation.
   */
  private static class TaskEventRowByJobRowHandler implements RowCallbackHandler {

    /**
     * Collation of response relevance values
     */
    Map<String, List<TaskEventBO>> results = new HashMap<>();

    private TaskEventRowByJobRowHandler() {
    }

    /**
     * Adds response ratings logged for the interface for the period given to a list.
     *
     * @param rs results of the query
     */
    @Override
    public void processRow(final ResultSet rs) throws SQLException {
      final TaskEventBO task = new TaskEventBO();
      task.setId(rs.getInt("id"));
      task.setJobId(rs.getInt("job_id"));
      task.setTask(TaskEventBO.TaskType.valueOf(rs.getString("task")));
      task.setCreatedAt(rs.getLong("created_at"));
      task.setModifiedAt(rs.getLong("modified_at"));
      task.setRecordsImported(rs.getLong("records_imported"));
      task.setRecordsProcessed(rs.getLong("records_processed"));
      task.setStatus(TaskEventBO.Status.valueOf(rs.getString("status")));
      String code = rs.getString("error_code");
      if (code != null) {
        task.setErrorCode(code);
      }
      task.setMessage(rs.getString("message"));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(task);

      List<TaskEventBO> tasks = results.get(task.getJobId());
      if (tasks == null) {
        tasks = new ArrayList<>();
      }
      tasks.add(task);

      results.put(Integer.toString(task.getJobId()), tasks);
    }
  }

  private static class TaskEventResultSetExtractor
      implements ResultSetExtractor<Map<String, TaskEventBO>> {

    @Override
    public Map<String, TaskEventBO> extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      Map<String, TaskEventBO> datasetTaskEventDetailMap = new LinkedHashMap<>();
      while (rs.next()) {
        TaskEventBO task = new TaskEventBO();
        task.setId(rs.getInt("id"));
        task.setJobId(rs.getInt("job_id"));
        task.setTask(TaskEventBO.TaskType.valueOf(rs.getString("task")));
        task.setCreatedAt(rs.getLong("created_at"));
        task.setModifiedAt(rs.getLong("modified_at"));
        task.setRecordsImported(rs.getLong("records_imported"));
        task.setRecordsProcessed(rs.getLong("records_processed"));
        task.setStatus(TaskEventBO.Status.valueOf(rs.getString("status")));
        String code = rs.getString("error_code");
        if (code != null) {
          task.setErrorCode(code);
        }
        task.setMessage(rs.getString("message"));
        String datasetId = rs.getString("dataset_id");
        //
        // prevent stored XSS, NT-2373
        //
        BusinessObject.sanitize(task);
        datasetTaskEventDetailMap.put(datasetId, task);
      }
      return datasetTaskEventDetailMap;
    }

  }

  @Override
  public DatasetTaskStatusResponse getLatestTaskEventStatusForDatasets(List<String> datasets) {
    int endIndex = 0;
    int startIndex = 0;
    int batchSize = 250; // sqlite craps out on more than 1024 in IN clause
    int mainListSize = datasets.size();
    final String query =
        "SELECT T.id as id, T.job_id as job_id, T.task as task, T.created_at as created_at, " +
            "T.modified_at as modified_at, T.status as status, T.records_imported as records_imported, "
            +
            "T.records_processed as records_processed, T.error_code as error_code, T.message as message, "
            +
            "MAX.dataset_id as dataset_id " +
            "FROM taskevents T " +
            "JOIN " +
            "(SELECT MAX(T.modified_at) as modified_at, ANY_VALUE(J.dataset_id) as dataset_id " +
            "FROM jobs J, taskevents T WHERE J.id = T.job_id AND J.dataset_id in (:datasetIdList) "
            +
            "GROUP BY T.job_id) MAX " +
            "ON T.modified_at = MAX.modified_at";
    Map<String, Object> paramsMap = new HashMap<>();
    DatasetTaskStatusResponse datasetTaskStatusResponse = new DatasetTaskStatusResponse();
    do {
      endIndex =
          ((endIndex + batchSize) > mainListSize) ? mainListSize : (endIndex + batchSize);
      List<String> subDatasetIdList = datasets.subList(startIndex, endIndex);
      if (subDatasetIdList.isEmpty()) {
        subDatasetIdList = null;
      }
      paramsMap.put("datasetIdList", subDatasetIdList);
      datasetTaskStatusResponse.putAll(
          jdbcTemplate.query(query, paramsMap, new TaskEventResultSetExtractor()));
      startIndex = endIndex;
    } while (endIndex < mainListSize);

    return datasetTaskStatusResponse;
  }

  @Override
  public ProjectTaskStatusResponse getLatestTaskEventStatusForProjects(
      Map<String, List<String>> projectDatasets) {
    ProjectTaskStatusResponse projectTaskStatusResponse = new ProjectTaskStatusResponse();
    for (Entry<String, List<String>> entry : projectDatasets.entrySet()) {
      int endIndex = 0;
      int startIndex = 0;
      int batchSize = 250; // sqlite craps out on more than 1024 in IN clause
      List<String> datasets = entry.getValue();
      String projectId = entry.getKey();
      int mainListSize = datasets.size();
      final String query =
          "SELECT T.id as id, T.job_id as job_id, T.task as task, T.created_at as created_at, " +
              "T.modified_at as modified_at, T.status as status, T.records_imported as records_imported, "
              +
              "T.records_processed as records_processed, T.error_code as error_code, T.message as message, "
              +
              "MAX.dataset_id as dataset_id, MAX.project_id as project_id " +
              "FROM taskevents T " +
              "JOIN " +
              "(SELECT MAX(T.modified_at) as modified_at, ANY_VALUE(J.dataset_id) as dataset_id, ANY_VALUE(J.project_id) as project_id "
              +
              "FROM jobs J, taskevents T WHERE J.id = T.job_id AND J.project_id = :projectId AND J.dataset_id in (:datasetIdList) "
              +
              "GROUP BY T.job_id) MAX " +
              "ON T.modified_at = MAX.modified_at";
      Map<String, Object> paramsMap = new HashMap<>();
      projectId = StringEscapeUtils.escapeSql(projectId);
      paramsMap.put("projectId", projectId);
      do {
        endIndex =
            ((endIndex + batchSize) > mainListSize) ? mainListSize : (endIndex + batchSize);
        List<String> subDatasetIdList = datasets.subList(startIndex, endIndex);
        if (subDatasetIdList.isEmpty()) {
          subDatasetIdList = null;
        }
        paramsMap.put("datasetIdList", subDatasetIdList);
        DatasetTaskStatusResponse datasetTaskStatusResponse = projectTaskStatusResponse
            .get(projectId);
        if (datasetTaskStatusResponse == null) {
          datasetTaskStatusResponse = new DatasetTaskStatusResponse();
        }
        datasetTaskStatusResponse
            .putAll(jdbcTemplate.query(query, paramsMap, new TaskEventResultSetExtractor()));
        projectTaskStatusResponse.put(projectId, datasetTaskStatusResponse);
        startIndex = endIndex;
      } while (endIndex < mainListSize);
    }

    return projectTaskStatusResponse;
  }
}
