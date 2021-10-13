/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.ui.nlmodel.model.TFSAggregatedMetrics;
import com.tfs.learningsystems.ui.nlmodel.model.TFSTransactionalMetrics;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author girish.prajapati
**/

public interface MetricsDao {

  public List<TFSTransactionalMetrics> getTransactionalMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate);

  public List<TFSAggregatedMetrics> getAggregatedMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate);

}