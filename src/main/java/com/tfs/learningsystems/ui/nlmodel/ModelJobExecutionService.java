package com.tfs.learningsystems.ui.nlmodel;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobResult;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class ModelJobExecutionService {

  private ListeningExecutorService service;
  private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(50);

  public ModelJobExecutionService() {
    this.service = MoreExecutors.listeningDecorator(
        new ThreadPoolExecutor(3, 3,
            5000L, TimeUnit.MILLISECONDS,
            queue, new ThreadPoolExecutor.CallerRunsPolicy()));
  }

  public ListenableFuture<?> submit(Runnable job) {
    return this.service.submit(job);
  }

  public ListenableFuture<TFSModelJobResult> submit(Callable<TFSModelJobResult> task) {
    return this.service.submit(task);
  }
}
