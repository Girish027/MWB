package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.ModelTestBatchBO;
import java.util.List;

public interface ModelTestBatchDao {

  public void deleteBatchesByIds(List<ModelTestBatchBO> batchTestResults);

}
