/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.exceptions.ApplicationException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This class is only for bulk ingestion of original documents.
 */

@Component
@Slf4j
public class BulkProcessing {
  
    @Autowired
    @Qualifier("jsonObjectMapper")
    private ObjectMapper jsonObjectMapper;

    @Autowired
    private ElasticSearchPropertyConfig elasticSearchProps;

    @Autowired
    @Qualifier("elasticSearchClient")
    private org.elasticsearch.client.Client elasticSearchClient;

    public IndexRequest buildIndexRequest(String indexName, String indexType, Map<String, Object> objectAsMap) {
        return new IndexRequest(indexName, indexType).source(objectAsMap);

    }

    public BulkProcessor buildBulkProcessor() throws ApplicationException {

        return BulkProcessor
                .builder(elasticSearchClient, new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {

                        log.info("Executing total actions: {}", request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

                        if (response.hasFailures()) {
                            log.error("Bulk response error: {}", response.buildFailureMessage());
                        }
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

                        log.error("Bulk operation failed with error", failure);
                    }
                }).setBulkActions(elasticSearchProps.getTotalBulkProcessingActions())
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .setFlushInterval(
                        TimeValue.timeValueSeconds(elasticSearchProps.getBulkProcessingFlushInterval()))
                .setConcurrentRequests(elasticSearchProps.getBulkProcessingConcurrentRequests())
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue
                                        .timeValueMillis(elasticSearchProps.getBulkProcessingBackoffInitialDelay()),
                                elasticSearchProps.getBulkProcessingBackoffTotalRetries())).build();
    }

    void closeBulkProcessor(BulkProcessor bulkProcessor){
        try {

            bulkProcessor
                    .awaitClose(elasticSearchProps.getBulkProcessingAwaitCloseTime(), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            String message = String
                    .format("Interrupted while waiting for bulk processing of ingest documents to finish: %s", e.getMessage());
            log.error(message);
            Thread.currentThread().interrupt();
            throw new ApplicationException(message, e);
        } catch (Exception e){
            String message = String
                    .format("Unexpected error occured while waiting for bulk processing of original documents to finish: %s", e.getMessage());
            log.equals(message);
            throw new ApplicationException(message,e);
        }

    }
}
