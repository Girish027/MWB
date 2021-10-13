package com.tfs.learningsystems.db;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * A common place to hold all handlers (a.k.a Spring Data repositories). At least for simple CRUD
 * operations, we won't have to write raw SQLs. Also, by centralize and hide these from application,
 * it enables us to enforce additional security checks.
 */

@Component
public class RepositoryUtil implements ApplicationContextAware, InitializingBean {

  protected static Map<Class, JpaSpecificationExecutor> specExecutorMap;
  protected static Map<Class, CrudRepository> repositoryMap;

  private ApplicationContext context;

  @Override
  public void afterPropertiesSet() throws Exception {
    Map<Class, JpaSpecificationExecutor> execMap = new HashMap<>();
    execMap.put(ClientBO.class, context.getBean(ClientRepository.class));
    execMap.put(DatasetBO.class, context.getBean(DatasetRepository.class));
    execMap.put(DatasetIntentInheritanceBO.class,
        context.getBean(DatasetIntentInheritanceRepository.class));
    execMap.put(FileBO.class, context.getBean(FileRepository.class));
    execMap.put(FileColumnBO.class, context.getBean(FileColumnRepository.class));
    execMap.put(FileColumnMappingBO.class, context.getBean(FileColumnMappingRepository.class));
    execMap.put(JobBO.class, context.getBean(JobRepository.class));
    execMap.put(ModelBO.class, context.getBean(ModelRepository.class));
    execMap.put(ModelConfigBO.class, context.getBean(ModelConfigRepository.class));
    execMap.put(ModelJobQueueBO.class, context.getBean(ModelJobQueueRepository.class));
    execMap.put(ModelDeploymentDetailsBO.class, context.getBean(ModelDeploymentDetailsRepository.class));
    execMap.put(ModelDeploymentMapBO.class, context.getBean(ModelDeploymentMapRepository.class));
    execMap.put(ModelTestBatchBO.class, context.getBean(ModelTestBatchRepository.class));
    execMap.put(MwbItsClientMapBO.class, context.getBean(MwbItsClientMapRepository.class));
    execMap.put(PreferencesBO.class, context.getBean(PreferencesRepository.class));
    execMap.put(VectorizerBO.class, context.getBean(VectorizerRepository.class));
    execMap.put(MetricsBO.class, context.getBean(MetricsRepository.class));
    execMap.put(ProjectBO.class, context.getBean(ProjectRepository.class));
    execMap.put(ProjectDatasetBO.class, context.getBean(ProjectDatasetRepository.class));
    execMap.put(TaggingGuideColumnBO.class, context.getBean(TaggingGuideColumnRepository.class));
    execMap.put(TaggingGuideColumnMappingBO.class,
        context.getBean(TaggingGuideColumnMappingRepository.class));
    execMap.put(TaggingGuideImportStatBO.class,
        context.getBean(TaggingGuideImportStatRepository.class));
    execMap.put(TaskEventBO.class, context.getBean(TaskEventRepository.class));
    Map<Class, CrudRepository> tmpMap = new HashMap<>();
    tmpMap.put(ClientBO.class, context.getBean(ClientRepository.class));
    tmpMap.put(DatasetBO.class, context.getBean(DatasetRepository.class));
    tmpMap.put(DatasetIntentInheritanceBO.class,
        context.getBean(DatasetIntentInheritanceRepository.class));
    tmpMap.put(FileBO.class, context.getBean(FileRepository.class));
    tmpMap.put(FileColumnBO.class, context.getBean(FileColumnRepository.class));
    tmpMap.put(FileColumnMappingBO.class, context.getBean(FileColumnMappingRepository.class));
    tmpMap.put(JobBO.class, context.getBean(JobRepository.class));
    tmpMap.put(ModelBO.class, context.getBean(ModelRepository.class));
    tmpMap.put(ModelConfigBO.class, context.getBean(ModelConfigRepository.class));
    tmpMap.put(ModelDeploymentDetailsBO.class, context.getBean(ModelDeploymentDetailsRepository.class));
    tmpMap.put(ModelDeploymentMapBO.class, context.getBean(ModelDeploymentMapRepository.class));
    tmpMap.put(ModelJobQueueBO.class, context.getBean(ModelJobQueueRepository.class));
    tmpMap.put(ModelTestBatchBO.class, context.getBean(ModelTestBatchRepository.class));
    tmpMap.put(MwbItsClientMapBO.class, context.getBean(MwbItsClientMapRepository.class));
    tmpMap.put(PreferencesBO.class, context.getBean(PreferencesRepository.class));
    tmpMap.put(MetricsBO.class, context.getBean(MetricsRepository.class));
    tmpMap.put(VectorizerBO.class, context.getBean(VectorizerRepository.class));
    tmpMap.put(ProjectBO.class, context.getBean(ProjectRepository.class));
    tmpMap.put(ProjectDatasetBO.class, context.getBean(ProjectDatasetRepository.class));
    tmpMap.put(TaggingGuideColumnBO.class, context.getBean(TaggingGuideColumnRepository.class));
    tmpMap.put(TaggingGuideColumnMappingBO.class,
        context.getBean(TaggingGuideColumnMappingRepository.class));
    tmpMap.put(TaggingGuideImportStatBO.class,
        context.getBean(TaggingGuideImportStatRepository.class));
    tmpMap.put(TaskEventBO.class, context.getBean(TaskEventRepository.class));

    repositoryMap = tmpMap;
    specExecutorMap = execMap;
  }

  @Override
  public void setApplicationContext(final ApplicationContext ctx) throws BeansException {
    context = ctx;
  }

  public static JpaSpecificationExecutor getSpecExecutor(Class cls) {
    return (specExecutorMap.get(cls));
  }

  public static CrudRepository getRepository(Class cls) {
    return (repositoryMap.get(cls));
  }

}
