/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritance;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritanceStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("datasetIntentInheritanceDao")
@Slf4j
public class JdbcDatasetIntentInheritanceDao implements DatasetIntentInheritanceDao {

  private DataSource datasource;

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcDatasetIntentInheritanceDao(DataSource datasource) {
    this.datasource = datasource;
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  @Override
  public DatasetIntentInheritance getInheritanceById(final String id) {
    final String query = "SELECT id, dataset_id, project_id, requested_at, "
        + "requested_by, total_tagged, unique_tagged, total_tagged_multiple_intents, "
        + "unique_tagged_multiple_intents, inherited_from_dataset_ids, updated_at, "
        + "status FROM dataset_intent_inheritance WHERE id = :id";
    Map<String, String> paramMap = new HashMap<String, String>();
    String idEscaped = StringEscapeUtils.escapeSql(id);
    paramMap.put("id", idEscaped);
    DatasetIntentInheritance inheritanceInfo = null;
    try {
      inheritanceInfo = this.jdbcTemplate
          .queryForObject(query, paramMap, new DatasetIntentInheritanceRowMapper());
    } catch (Exception dae) {
      log.error("Could not find intent inheritance info for the id: {}", id);
    }
    return inheritanceInfo;
  }

  @Override
  public List<DatasetIntentInheritance> getInheritaceForDataset(String datasetId) {
    final String query = "SELECT id, dataset_id, project_id, requested_at, "
        + "requested_by, total_tagged, unique_tagged, total_tagged_multiple_intents, "
        + "unique_tagged_multiple_intents, inherited_from_dataset_ids, updated_at, "
        + "status FROM dataset_intent_inheritance WHERE dataset_id = :datasetId";
    Map<String, String> paramMap = new HashMap<String, String>();
    datasetId = StringEscapeUtils.escapeSql(datasetId);
    paramMap.put("datasetId", datasetId);
    List<DatasetIntentInheritance> inheritanceHistory = null;
    try {
      inheritanceHistory = this.jdbcTemplate.queryForList(query, paramMap,
          DatasetIntentInheritance.class);
    } catch (Exception dae) {
      inheritanceHistory = new ArrayList<>();
      log.error("Could not find any intent auto-tagging inheritance info for the dataset: {}",
          datasetId);
    }
    return inheritanceHistory;
  }

  private class DatasetIntentInheritanceRowMapper implements RowMapper<DatasetIntentInheritance> {

    @Override
    public DatasetIntentInheritance mapRow(ResultSet rs, int rowNum) throws SQLException {
      List<String> inheritedFromDatasetIds = null;
      DatasetIntentInheritance inheritance = new DatasetIntentInheritance();
      inheritance.setId(String.valueOf(rs.getLong("id")));
      inheritance.setDatasetId((String.valueOf(rs.getLong("dataset_id"))));
      inheritance.setProjectId((String.valueOf(rs.getLong("project_id"))));
      inheritance.setRequestedAt(rs.getLong("requested_at"));
      inheritance.setUpdatedAt(rs.getLong("requested_at"));
      inheritance.setRequestedBy(rs.getString("requested_by"));
      inheritance.setTotalTagged(rs.getLong("total_tagged"));
      inheritance.setUniqueTagged(rs.getLong("unique_tagged"));
      inheritance.setTotalTaggedMulipleIntents(rs.getLong("total_tagged_multiple_intents"));
      inheritance.setUniqueTaggedMulipleIntents(rs.getLong("unique_tagged_multiple_intents"));
      inheritance.setStatus(DatasetIntentInheritanceStatus.lookup(rs.getString("status")));
      String inheritedFromDatasetIdsString = rs.getString("inherited_from_dataset_ids");
      if (inheritedFromDatasetIdsString != null && !"".equals(inheritedFromDatasetIdsString)) {
        inheritedFromDatasetIds = Arrays.asList(inheritedFromDatasetIdsString.split(","));
      } else {
        inheritedFromDatasetIds = new ArrayList<String>();
      }
      inheritance.setInheritedFromDatasetIds(inheritedFromDatasetIds);
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(inheritance);
      return inheritance;
    }

  }

  @Override
  public List<DatasetIntentInheritance> getInheritaceForProject(String projectId) {
    final String query = "SELECT id, dataset_id, project_id, requested_at, "
        + "requested_by, total_tagged, unique_tagged, total_tagged_multiple_intents, "
        + "unique_tagged_multiple_intents, inherited_from_dataset_ids, updated_at, "
        + "status FROM dataset_intent_inheritance WHERE project_id = :projectId";
    Map<String, String> paramMap = new HashMap<>();
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("projectId", projectId);
    List<DatasetIntentInheritance> inheritanceHistory = null;
    try {
      inheritanceHistory = this.jdbcTemplate.queryForList(query, paramMap,
          DatasetIntentInheritance.class);
    } catch (Exception dae) {
      inheritanceHistory = new ArrayList<>();
      log.error("Could not find any intent auto-tagging inheritance info for the project: {}",
          projectId);
    }
    return inheritanceHistory;
  }

  @Override
  public void updateStatus(String id, DatasetIntentInheritanceStatus status) {
    final String query = "UPDATE dataset_intent_inheritance SET status = :status WHERE id = :id";
    Map<String, String> paramMap = new HashMap<>();
    id = StringEscapeUtils.escapeSql(id);
    paramMap.put("id", id);
    paramMap.put("status", status.status());
    this.jdbcTemplate.update(query, paramMap);
  }

  @Override
  public DatasetIntentInheritance getLastPendingInheritaceForDataset(String datasetId) {
    final String query = "SELECT id, dataset_id, project_id, requested_at, "
        + "requested_by, total_tagged, unique_tagged, total_tagged_multiple_intents, "
        + "unique_tagged_multiple_intents, inherited_from_dataset_ids, updated_at, "
        + "status FROM dataset_intent_inheritance WHERE dataset_id = :datasetId "
        + "AND status = 'PENDING'";
    Map<String, String> paramMap = new HashMap<String, String>();
    datasetId = StringEscapeUtils.escapeSql(datasetId);
    paramMap.put("datasetId", datasetId);
    DatasetIntentInheritance intentInheritance = null;
    try {
      intentInheritance = this.jdbcTemplate.queryForObject(query, paramMap,
          new DatasetIntentInheritanceRowMapper());
    } catch (Exception dae) {
      log.debug("No auto-tagging is scheduled for the dataset {}", datasetId);
    }
    return intentInheritance;
  }

}
