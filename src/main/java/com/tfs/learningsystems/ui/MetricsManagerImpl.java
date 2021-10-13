/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.dao.MetricsDao;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSAggregatedMetrics;
import com.tfs.learningsystems.ui.nlmodel.model.TFSTransactionalMetrics;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author girish.prajapati
**/

@Slf4j
@Component
@Qualifier("metricsManagerBean")
public class MetricsManagerImpl implements MetricsManager {

    @Autowired
    @Qualifier("metricsDaoBean")
    private MetricsDao metricsDao;

    @Override
    public List<TFSTransactionalMetrics> getTransactionalMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate) {
        List<TFSTransactionalMetrics> result = null;
            try {
                if (startDate != null && endDate != null && startDate.getTime() <= endDate.getTime()) {
                    result = metricsDao.getTransactionalMetricsByClientModelDateRange(clientId, modelName, startDate, endDate);
                }
                if(result == null || result.isEmpty()){
                    log.error("Transactional metrics data for client: {}, model: {} and DateRange: {} to {} not found", clientId, modelName, startDate, endDate);
                    throw new InvalidRequestException(
                            new Error(Response.Status.BAD_REQUEST.getStatusCode(), "metrics_not_found",
                                    ErrorMessage.METRICS_NOT_FOUND));
                }
                return result;
            } catch (InvalidRequestException e) {
                log.error("Exception while fetching transactional metrics data", e);
                throw new InvalidRequestException(
                        new Error(Response.Status.BAD_REQUEST.getStatusCode(), "metrics_not_found",
                                ErrorMessage.METRICS_NOT_FOUND));
            }
    }

    @Override
    public List<TFSAggregatedMetrics> getAggregatedMetricsByClientModelDateRange(Integer clientId, String modelName, Timestamp startDate, Timestamp endDate){
        List<TFSAggregatedMetrics> result = null;
        try {
            if (startDate != null && endDate != null && startDate.getTime() <= endDate.getTime()) {
                result = metricsDao.getAggregatedMetricsByClientModelDateRange(clientId, modelName, startDate, endDate);
            }
            if(result == null || result.isEmpty()){
                log.error("Aggregated metrics data for client: {}, model: {} and DateRange: {} to {} not found", clientId, modelName, startDate, endDate);
                throw new InvalidRequestException(
                        new Error(Response.Status.BAD_REQUEST.getStatusCode(), "metrics_not_found",
                                ErrorMessage.METRICS_NOT_FOUND));
            }
            return result;
        } catch (InvalidRequestException e) {
            log.error("Exception while fetching aggregated metrics data", e);
            throw new InvalidRequestException(
                    new Error(Response.Status.BAD_REQUEST.getStatusCode(), "metrics_not_found",
                            ErrorMessage.METRICS_NOT_FOUND));
        }
    }
}