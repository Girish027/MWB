/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
*******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.ui.nlmodel.model.TFSAggregatedMetrics;
import com.tfs.learningsystems.ui.nlmodel.model.TFSTransactionalMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author girish.prajapati
**/

@Repository
@Qualifier("metricsDaoBean")
@Slf4j
public class JdbcMetricsDao implements MetricsDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcMetricsDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  public List<TFSTransactionalMetrics> getTransactionalMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate){
    Map<String, Object> paramMap = new HashMap<>();
    List<TFSTransactionalMetrics> getTransactionalData = new LinkedList<>();
    paramMap.put("clientId", clientId);
    paramMap.put("modelName", modelName);
    paramMap.put("startDate", startDate);
    paramMap.put("endDate", endDate);
    String query = "SELECT client_id, model_name, node_name, metric_date, volume, escalation, version, created_at, modified_at FROM metrics WHERE client_id = :clientId AND model_name = :modelName AND metric_date BETWEEN :startDate AND :endDate";
    try {
      getTransactionalData = jdbcTemplate.query(query, paramMap, new TFSTransactionalMetricsRowMapper());
    }catch (Exception e) {
      log.error("Transactional metrics data for the query:{} and params: {} not found", query, paramMap, e);
    }
    return getTransactionalData;
  }

  public List<TFSAggregatedMetrics> getAggregatedMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate){
    Map<String, Object> paramMap = new HashMap<>();
    List<TFSAggregatedMetrics> getAggregatedData = new LinkedList<>();
    paramMap.put("clientId", clientId);
    paramMap.put("modelName", modelName);
    paramMap.put("startDate", startDate);
    paramMap.put("endDate", endDate);
    String query = "SELECT client_id, model_name, SUM(volume) AS volume, SUM(escalation) AS escalation FROM metrics WHERE client_id = :clientId AND model_name = :modelName AND metric_date BETWEEN :startDate AND :endDate GROUP BY client_id, model_name";
    try {
      getAggregatedData = jdbcTemplate.query(query, paramMap, new TFSAggregatedMetricsRowMapper());
    }catch (Exception e) {
      log.error("Aggregated metrics data for the query:{} and params: {} not found", query, paramMap, e);
    }
    return getAggregatedData;
  }

  public static class TFSTransactionalMetricsRowMapper implements RowMapper<TFSTransactionalMetrics> {
    @Override
    public TFSTransactionalMetrics mapRow(ResultSet rs, int rowNum) throws SQLException {
      TFSTransactionalMetrics tfsTransactionalMetrics = new TFSTransactionalMetrics();
      tfsTransactionalMetrics.setClientId(rs.getInt("client_id"));
      tfsTransactionalMetrics.setModelName(rs.getString("model_name"));
      tfsTransactionalMetrics.setNodeName(rs.getString("node_name"));
      tfsTransactionalMetrics.setMetricDate(rs.getTimestamp("metric_date"));
      tfsTransactionalMetrics.setEscalation(rs.getInt("escalation"));
      tfsTransactionalMetrics.setVolume(rs.getInt("volume"));
      tfsTransactionalMetrics.setVersion(rs.getInt("version"));
      tfsTransactionalMetrics.setCreatedAt(rs.getLong("created_at"));
      tfsTransactionalMetrics.setModifiedAt(rs.getLong("modified_at"));
      BusinessObject.sanitize(tfsTransactionalMetrics);
      return tfsTransactionalMetrics;
    }
  }

  public static class TFSAggregatedMetricsRowMapper implements RowMapper<TFSAggregatedMetrics> {
    @Override
    public TFSAggregatedMetrics mapRow(ResultSet rs, int rowNum) throws SQLException {
      TFSAggregatedMetrics tfsAggregatedMetrics = new TFSAggregatedMetrics();
      tfsAggregatedMetrics.setClientId(rs.getInt("client_id"));
      tfsAggregatedMetrics.setModelName(rs.getString("model_name"));
      tfsAggregatedMetrics.setEscalation(rs.getInt("escalation"));
      tfsAggregatedMetrics.setVolume(rs.getInt("volume"));
      BusinessObject.sanitize(tfsAggregatedMetrics);
      return tfsAggregatedMetrics;
    }
  }
}
