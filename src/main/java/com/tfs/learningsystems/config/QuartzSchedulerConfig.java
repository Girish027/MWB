/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import com.tfs.learningsystems.job.DeleteFileJob;
import com.tfs.learningsystems.job.IngestionJobListener;
import com.tfs.learningsystems.job.IngestionRecoveryJob;
import com.tfs.learningsystems.job.IngestionSchedulerListener;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.util.QuartzJobUtil;
import java.io.IOException;
import java.util.Properties;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class QuartzSchedulerConfig {

  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  private IngestionPropertyConfig ingestionProps;


  @Autowired
  private Environment env;

  @Bean
  public JobFactory jobFactory(ApplicationContext applicationContext) {
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);
    return jobFactory;
  }

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory,
      @Qualifier("recoveryJobTrigger") Trigger recoveryJobTrigger) throws IOException {
    SchedulerFactoryBean factory = new SchedulerFactoryBean();
    // this allows to update triggers in DB when updating settings in config file:
    IngestionJobListener ingestionJobListener = new IngestionJobListener();
    ingestionJobListener.setJobManager(jobManager);
    ingestionJobListener.setIngestionGroup(elasticSearchProps.getQuartzJobGroup());
    factory.setJobFactory(jobFactory);
    factory.setGlobalJobListeners(ingestionJobListener);
    factory.setSchedulerListeners(new IngestionSchedulerListener());
    factory.setQuartzProperties(quartzProperties());
    factory.setWaitForJobsToCompleteOnShutdown(true);

    // Don't initiate recovery job during unit tests
    if (!env.acceptsProfiles("test")) {
      factory.setTriggers(recoveryJobTrigger);
    }

    return factory;
  }

  @Bean
  public Properties quartzProperties() throws IOException {
    PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
    propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));
    propertiesFactoryBean.afterPropertiesSet();
    return propertiesFactoryBean.getObject();
  }

  @Bean
  public JobDetailFactoryBean recoveryJobDetail() {
    return createJobDetail(IngestionRecoveryJob.class);
  }

  @Bean
  public JobDetailFactoryBean deleteJobDetail() {
    return createJobDetail(DeleteFileJob.class);
  }

  @Bean(name = "recoveryJobTrigger")
  public SimpleTriggerFactoryBean recoveryJobTrigger(
      @Qualifier("recoveryJobDetail") JobDetail jobDetail) {
    return createTrigger(jobDetail);
  }

  @Bean(name = "deleteJobTrigger")
  public SimpleTriggerFactoryBean deleteJobTrigger(
      @Qualifier("deleteJobDetail") JobDetail jobDetail) {
    return createTrigger(jobDetail);
  }

  private JobDetailFactoryBean createJobDetail(Class<?> jobClass) {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass((Class<? extends Job>) jobClass);
    // job has to be durable to be stored in DB:
    factoryBean.setDurability(true);
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT, ingestionProps.getStartingRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT, ingestionProps.getMaxRetryCount());
    factoryBean.setJobDataMap(jobDataMap);
    return factoryBean;
  }

  private SimpleTriggerFactoryBean createTrigger(
      JobDetail jobDetail) {
    SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
    factoryBean.setJobDetail(jobDetail);
    factoryBean.setStartDelay(ingestionProps.getStartingDelayMs());
    factoryBean.setRepeatCount(ingestionProps.getRepeatCount());
    // in case of misfire, ignore all missed triggers and continue :
    factoryBean.setMisfireInstruction(
        SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    return factoryBean;
  }

}
