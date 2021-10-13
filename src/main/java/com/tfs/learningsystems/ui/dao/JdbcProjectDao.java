/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.model.DatasetTaskStatus;
import com.tfs.learningsystems.ui.model.Project;
import com.tfs.learningsystems.ui.model.ProjectDetailDatasetTaskStatus;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("projectDaoBean")
@Slf4j
public class JdbcProjectDao implements ProjectDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  private static final String PROJECT_ID = "project_id";
  private static final String PROJECT_ID_LIST = "projectIdList";

  @Autowired
  public JdbcProjectDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  private List<ProjectDetailDatasetTaskStatus> getProjectsByQuery(final String query,
      final Map<String, ?> paramMap) {

    List<ProjectDetailDatasetTaskStatus> projects = new LinkedList<>();
    try {
      projects = jdbcTemplate.query(query, paramMap, new ProjectDetailDatasetRowMapper());
    } catch (Exception e) {
      log.error("Error while fetch list of projects:", e);
      log.warn("Projects from query:{} and params: {} not found", query, paramMap);
    }
    return projects;
  }

  private Map<String, Integer> getProjectModelCountByQuery(final String query) {

    final Map<String, Integer> projectModelMap = new HashMap<>();
    try {
      jdbcTemplate.query(query, (ResultSet rs) -> {
        while (rs.next()) {
          projectModelMap.put(rs.getString(PROJECT_ID), rs.getInt("model_count"));
        }
        return projectModelMap;
      });
    } catch (Exception e) {
      log.warn("Projects from query:{} not found", query);
    }
    return projectModelMap;
  }

  private List<ProjectBO> getProjectsByIdQuery(final String query,
      final Map<String, ?> paramMap) {

    List<ProjectBO> projects = new LinkedList<>();
    try {
      projects = jdbcTemplate.query(query, paramMap, new ProjectRowMapper());
    } catch (Exception e) {
      log.warn("Projects from Id query:{} and params: {} not found", query, paramMap);

    }
    return projects;
  }

  private Map<String, List<DatasetTaskStatus>> getProjectDatasetTaskStatusByQuery(
      final String query,
      final Map<String, ?> paramMap) {

    Map<String, List<DatasetTaskStatus>> projectDatasetMap = new HashMap<>();
    try {
      projectDatasetMap = jdbcTemplate
          .query(query, paramMap, new ProjectDatasetTaskStatusExtractor());
    } catch (Exception e) {
      log.error("Error while fetching dataset entires", e);
      log.warn("Projects from query:{} and params: {} not found", query, paramMap);
    }
    return projectDatasetMap;
  }

  @Override
  public List<ProjectDetailDatasetTaskStatus> getProjects(int startIndex, int count, String filter,
      String sortBy, String sortOrder, boolean showDeleted, String filterClientId, List<Integer> projectIdsIntList) {

    List<ProjectDetailDatasetTaskStatus> projects;
    String projectIdCondition;

    Map<String, String> filterMap = new HashMap<>();
    if (!showDeleted) {
      filterMap.put("state", "'" + ProjectBO.State.ENABLED.toString() + "'");
    }

    filterClientId = StringEscapeUtils.escapeSql(filterClientId);
    if (!(filterClientId == null || filterClientId.isEmpty())) {
      filterMap.put("client_id", filterClientId);
    }

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("startIndex", startIndex);
    paramMap.put("count", count);
    filter = StringEscapeUtils.escapeSql(filter);
    paramMap.put("filter", filter);
    paramMap.put("sortBy", sortBy);

    Map<String, Object> subParamMap = new HashMap<>();

    if(projectIdsIntList != null) {
      projectIdCondition = " AND id in (:projectIdList) ";
      paramMap.put(PROJECT_ID_LIST, projectIdsIntList);
    }else {
      projectIdCondition = "";
    }

    final String projectsQuery = "SELECT id, name, client_id, owner_id, "
        + "group_id, vertical, description, locale, created_at, "
        + "modified_at, modified_by, start_at, end_at, state, type, original_name, deployable_model_id, preview_model_id, live_model_id "
        + "FROM projects "
        + formatWhereString(filterMap)
        + projectIdCondition
        + "ORDER BY :sortBy " + sortOrder + " LIMIT :count OFFSET :startIndex";

    final String datasetsQuery = "SELECT DP.project_id as project_id, "
        + "D.id as dataset_id, D.name as dataset_name, "
        + "T.task as transformation_task, T.status as transformation_status "
        + "FROM datasets_projects DP "
        + "LEFT JOIN datasets D ON D.id = DP.dataset_id "
        + "LEFT JOIN jobs J ON J.dataset_id = D.id "
        + "LEFT JOIN taskevents T ON T.id = (SELECT id FROM taskevents WHERE job_id = J.id ORDER BY modified_at DESC LIMIT 1) "
        + "WHERE DP.project_id in (:projectIdList)";

    final String modelQuery = "SELECT project_id, count(project_id) as model_count FROM models group by project_id";

    projects = this.getProjectsByQuery(projectsQuery, paramMap);

    List<String> projectIdList = projects.stream().map(ProjectDetailDatasetTaskStatus::getId).collect(Collectors.toList());
    if (projectIdList.isEmpty()) {
      projectIdList = null;
    }

    subParamMap.put(PROJECT_ID_LIST, projectIdList);

    Map<String, List<DatasetTaskStatus>> projectDatasetMap =
        this.getProjectDatasetTaskStatusByQuery(datasetsQuery, subParamMap);

    Map<String, Integer> projectModelCountMap =
            this.getProjectModelCountByQuery(modelQuery);

    for (ProjectDetailDatasetTaskStatus pdetail : projects) {
      List<DatasetTaskStatus> datasetStatusList = projectDatasetMap.get(pdetail.getId());
      pdetail.setDatasetTaskStatusList(datasetStatusList);
      Integer modelCount = 0;
      if( projectModelCountMap != null && projectModelCountMap.size() != 0 && projectModelCountMap.get(pdetail.getId())!= null){
        modelCount = projectModelCountMap.get(pdetail.getId());
      }
      pdetail.setModelCount(modelCount);
    }

    return projects;
  }

  @Override
  public List<ProjectDetailDatasetTaskStatus> getProjectDetailsByProjectIDs(List<Integer> projectIdsIntList) {

    List<String> projectIds = projectIdsIntList.stream()
            .map(String::valueOf)
            .collect(Collectors.toList());

    Map<String, Object> paramMap = new HashMap<>();

    paramMap.put(PROJECT_ID_LIST, projectIds);

    final String projectsQuery = "SELECT id, name, client_id, owner_id, "
            + "group_id, vertical, description, locale, created_at, "
            + "modified_at, modified_by, start_at, end_at, state, type, original_name "
            + "FROM projects P "
            + "WHERE P.id in (:projectIdList)";

    return this.getProjectsByQuery(projectsQuery, paramMap);
  }

  @Override
  public List<ProjectBO> getProjectsByProjectIDs(List<Integer> projectIdsIntList) {

    List<String> projectIds = projectIdsIntList.stream()
            .map(String::valueOf)
            .collect(Collectors.toList());

    Map<String, Object> paramMap = new HashMap<>();

    paramMap.put(PROJECT_ID_LIST, projectIds);

    final String projectsQuery = "SELECT P.id as project_id, "
            + "P.name as project_name "
            + "FROM projects P "
            + "WHERE P.id in (:projectIdList)";

    return this.getProjectsByIdQuery(projectsQuery, paramMap);
  }

  private String formatWhereString(Map<String, String> fields) {

    if (fields.isEmpty()) {
      return "";
    }

    return "WHERE " + fields.entrySet().stream()
        .map(Object::toString)
        .collect(Collectors.joining(" AND ")) + " ";
  }

  private class ProjectDetailDatasetRowMapper implements RowMapper<ProjectDetailDatasetTaskStatus> {

    @Override
    public ProjectDetailDatasetTaskStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
      ProjectDetailDatasetTaskStatus project = new ProjectDetailDatasetTaskStatus();
      project.setId(String.valueOf(rs.getLong("id")));
      project.setName(rs.getString("name"));
      project.setClientId(String.valueOf(rs.getLong("client_id")));
      project.setOwnerId(rs.getString("owner_id"));
      project.setGroupId(String.valueOf(rs.getLong("group_id")));
      project.setVertical(Vertical.valueOf(rs.getString("vertical")));
      project.setDescription(rs.getString("description"));
      project.setLocale(String.valueOf(rs.getString("locale")));
      project.setCreatedAt(rs.getLong("created_at"));
      project.setModifiedAt(rs.getLong("modified_at"));
      project.setModifiedBy(rs.getString("modified_by"));
      project.setStartAt(rs.getLong("start_at"));
      project.setEndAt(rs.getLong("end_at"));
      project.setState(Project.StateEnum.GetState(rs.getString("state")));
      project.setType(Project.TypeEnum.GetType(rs.getString("type")));
      project.setOriginalName(rs.getString("original_name"));
      project.setDeployableModelId(rs.getInt("deployable_model_id")==0?null:rs.getInt("deployable_model_id"));
      project.setPreviewModelId(rs.getString("preview_model_id"));
      project.setLiveModelId(rs.getString("live_model_id"));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(project);

      return project;
    }

  }

  private class ProjectRowMapper implements RowMapper<ProjectBO> {

    @Override
    public ProjectBO mapRow(ResultSet rs, int rowNum) throws SQLException {
      ProjectBO project = new ProjectBO();
      project.setId(rs.getInt(PROJECT_ID));
      project.setName(rs.getString("project_name"));

      BusinessObject.sanitize(project);

      return project;
    }

  }

  private class ProjectDatasetTaskStatusExtractor implements
      ResultSetExtractor<Map<String, List<DatasetTaskStatus>>> {

    @Override
    public Map<String, List<DatasetTaskStatus>> extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      final Map<String, List<DatasetTaskStatus>> projectDatasetMap = new LinkedHashMap<>();
      while (rs.next()) {
        String projectId = rs.getString(PROJECT_ID);
        List<DatasetTaskStatus> datasets = projectDatasetMap.get(projectId);
        if (datasets == null) {
          datasets = new ArrayList<>();
        }
        DatasetTaskStatus datasetTaskStatus = new DatasetTaskStatus();
        datasetTaskStatus.setId(rs.getString("dataset_id"));
        datasetTaskStatus.setName(rs.getString("dataset_name"));
        datasetTaskStatus.setTask(rs.getString("transformation_task"));
        datasetTaskStatus.setStatus(rs.getString("transformation_status"));
        //
        // prevent stored XSS, NT-2373
        //
        BusinessObject.sanitize(datasetTaskStatus);
        datasets.add(datasetTaskStatus);
        projectDatasetMap.put(projectId, datasets);
      }
      return projectDatasetMap;
    }

  }
}
