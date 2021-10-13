package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.ModelTestBatchBO;

import com.tfs.learningsystems.db.RepositoryUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("modelTestBatchDaoBean")
@Slf4j
public class JdbcModelTestBatch implements ModelTestBatchDao {

  @Override
  public void deleteBatchesByIds(List<ModelTestBatchBO> batchTestResults) {
    RepositoryUtil.getRepository(ModelTestBatchBO.class).delete(batchTestResults);
  }

}
