/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.db.DatasetBO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

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

/**
 * @author jkarpala
 */
@Repository
@Qualifier("projectDatasetDaoBean")
@Slf4j
public class JdbcProjectDatasetDao implements ProjectDatasetDao {

  private static class DatasetRowMapper implements RowMapper<DatasetBO> {

    @Override
    public DatasetBO mapRow(ResultSet rs, int rowNum) throws SQLException {
      DatasetBO dataset = new DatasetBO();
      dataset.setId(rs.getInt("id"));
      dataset.setName(rs.getString("name"));
      dataset.setClientId(String.valueOf(rs.getLong("client_id")));
      dataset.setDataType(rs.getString("data_type"));
      dataset.setDescription(rs.getString("description"));
      dataset.setUri(rs.getString("uri"));
      dataset.setCreatedBy(rs.getString("created_by"));
      dataset.setModifiedBy(rs.getString("modified_by"));
      dataset.setCreatedAt(rs.getLong("created_at"));
      dataset.setReceivedAt(rs.getLong("received_at"));
      dataset.setModifiedAt(rs.getLong("modified_at"));
      dataset.setLocale(rs.getString("locale"));
      dataset.setSource(DatasetBO.Source.valueOf(rs.getString("source")));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(dataset);

      return dataset;
    }
  }

  public static class DatasetIdsExtractor implements ResultSetExtractor<List<Integer>> {

    @Override
    public List<Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
      List<Integer> datasetIds = new ArrayList<>();
      while (rs.next()) {
        int datasetId = rs.getInt("id");
        datasetIds.add(datasetId);
      }
      return datasetIds;
    }
  }

  private DataSource datasource;

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcProjectDatasetDao(DataSource datasource) {
    this.datasource = datasource;
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  @Override
  public Integer countDatasets(String projectId) {

    Map<String, Object> paramMap = new HashMap<>();
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("projectId", projectId);

    final String query =
            "SELECT COUNT(id) FROM datasets WHERE datasets.id IN "
                    + "(SELECT datasets_projects.dataset_id FROM datasets_projects "
                    + "WHERE datasets_projects.project_id = :projectId) ";

    return jdbcTemplate.queryForObject(query, paramMap, Integer.class);
  }

  @Override
  public List<Integer> getDatasetIds(String projectId) {

    Map<String, Object> paramMap = new HashMap<>();
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("projectId", projectId);

    final String query =
            "SELECT id FROM datasets WHERE datasets.id IN "
                    + "(SELECT datasets_projects.dataset_id FROM datasets_projects "
                    + "WHERE datasets_projects.project_id = :projectId) ";

    try {
      return this.jdbcTemplate.query(query, paramMap, new DatasetIdsExtractor());
    } catch (Exception e) {
      log.warn("Dataset IDs from query:{} and params: {} not found", query, paramMap);
    }
    return (new LinkedList());
  }

  @Override
  public List<DatasetBO> getDatasets(String projectId, Integer startIndex, Integer limit,
                                     String filter, String sortBy, String sortOrder) {

    Map<String, Object> paramMap = new HashMap<>();
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("projectId", projectId);
    paramMap.put("startIndex", startIndex);
    paramMap.put("count", limit);
    filter = StringEscapeUtils.escapeSql(filter);
    paramMap.put("filter", filter);
    paramMap.put("sortBy", sortBy);
    paramMap.put("sortOrder", sortOrder);

    final String query =
            "SELECT id, name, client_id, " + "data_type, description, uri, locale, "
                    + "created_by, modified_by, source, "
                    + "created_at, modified_at, received_at "
                    + "FROM datasets WHERE datasets.id IN "
                    + "(SELECT datasets_projects.dataset_id FROM datasets_projects "
                    + "WHERE datasets_projects.project_id = :projectId) "
                    + "ORDER BY " + sortBy + " " + sortOrder + " LIMIT :count OFFSET :startIndex";

    return this.getDatasetsByQuery(query, paramMap);
  }

  private List<DatasetBO> getDatasetsByQuery(final String query,
                                             final Map<String, ?> paramMap) {

    List<DatasetBO> datasets = new LinkedList<>();
    try {
      datasets = jdbcTemplate.query(query, paramMap, new DatasetRowMapper());
    } catch (Exception e) {
      log.warn("Datasets from query:{} and params: {} not found", query, paramMap);
    }
    return datasets;
  }
}

