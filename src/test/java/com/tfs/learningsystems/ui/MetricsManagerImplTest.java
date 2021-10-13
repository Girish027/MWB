package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.MetricsBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSTransactionalMetrics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MetricsManagerImplTest extends AuthValidationBaseTest {

    private ClientBO testClient;
    private MetricsBO testMetrics1, testMetrics2;

    Timestamp startDate = Timestamp.valueOf("2021-07-01 00:00:00");
    Timestamp endDate = Timestamp.valueOf("2021-07-02 00:00:00");

    @Autowired
    @Qualifier("metricsManagerBean")
    private MetricsManager metricsManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
        String name = clsName + "_" + System.currentTimeMillis() % 10000000;
        String modelName = "model1";
        String nodeName = "node1";

        testClient = ModelUtils.getTestClientObject(name);
        testClient.create();

        testMetrics1 = ModelUtils.getTestMetricsObject(testClient.getId(), modelName, nodeName, startDate, 100, 60, 10);
        testMetrics1.create();

        testMetrics2 = ModelUtils.getTestMetricsObject(testClient.getId(), modelName, nodeName, endDate, 200, 120, 11);
        testMetrics2.create();
    }

    @Test (expected = Test.None.class)
    public void testGetTransactionalMetricsByClientModelDateRange() {
        metricsManager.getTransactionalMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetTransactionalMetricsByInvalidClientModelDateRange() {
        metricsManager.getTransactionalMetricsByClientModelDateRange(testClient.getId()+1, testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetTransactionalMetricsByClientInvalidModelDateRange() {
        metricsManager.getTransactionalMetricsByClientModelDateRange(testClient.getId(), "model2", startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetTransactionalMetricsByClientModelInvalidDateRange() {
        Timestamp startDate = Timestamp.valueOf("2021-07-05 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2021-07-06 00:00:00");
        metricsManager.getTransactionalMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetTransactionalMetricsByNullClientModelDateRange() {
        metricsManager.getTransactionalMetricsByClientModelDateRange(null, testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetTransactionalMetricsByClientModelNullDateRange() {
        metricsManager.getTransactionalMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), null, null);
    }

    @Test
    public void testGetTransactionalMetricsDataCheckByClientModelDateRange() {
        List<TFSTransactionalMetrics> result = metricsManager.getTransactionalMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), startDate, endDate);
        assertEquals(2, result.size());
    }

    @Test (expected = Test.None.class)
    public void testGetAggregatedMetricsByClientModelDateRange() {
        metricsManager.getAggregatedMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = Test.None.class)
    public void testGetAggregatedMetricsByClientModelDateRange1() {
        metricsManager.getAggregatedMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetAggregatedMetricsByInvalidClientModelDateRange() {
        metricsManager.getAggregatedMetricsByClientModelDateRange(testClient.getId()+1, testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetAggregatedMetricsByClientInvalidModelDateRange() {
        metricsManager.getAggregatedMetricsByClientModelDateRange(testClient.getId(), "model2", startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetAggregatedMetricsByClientModelInvalidDateRange() {
        Timestamp startDate = Timestamp.valueOf("2021-07-05 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2021-07-06 00:00:00");
        metricsManager.getAggregatedMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetAggregatedMetricsByNullClientModelDateRange() {
        metricsManager.getAggregatedMetricsByClientModelDateRange(null, testMetrics1.getModelName(), startDate, endDate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetAggregatedMetricsByClientModelNullDateRange() {
        metricsManager.getAggregatedMetricsByClientModelDateRange(testClient.getId(), testMetrics1.getModelName(), null, null);
    }
}