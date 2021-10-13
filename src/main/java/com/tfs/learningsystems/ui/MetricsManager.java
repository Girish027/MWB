/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.nlmodel.model.TFSAggregatedMetrics;
import com.tfs.learningsystems.ui.nlmodel.model.TFSTransactionalMetrics;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author girish.prajapati
**/
public interface MetricsManager {

  /** Get transactional metrics data for a specific model
   * @return Transactional metrics data for a specific model with date range
   */
  public List<TFSTransactionalMetrics> getTransactionalMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate);

  /**
   * Get aggregated metrics data with provided date range
   * @return Aggregated metrics data with provided date range
   */
  public List<TFSAggregatedMetrics> getAggregatedMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate);

}