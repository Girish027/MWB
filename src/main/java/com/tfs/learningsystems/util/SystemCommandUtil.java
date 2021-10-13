/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemCommandUtil {

  @Data
  public static class SystemCommandResult {

    int exitValue;
    Throwable exception;
    StringBuilder stdOut;
    StringBuilder stdError;
  }

  public static class AsyncStreamHandler implements Runnable {

    private InputStream inputStream;

    public AsyncStreamHandler(InputStream inputStream) {
      this.inputStream = inputStream;
    }

    @Override
    public void run() {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
          log.debug("[AsyncStreamHandler] {}", line);
        }
      } catch (IOException ioe) {
        log.error("Error reading the input stream", ioe);
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            log.error("Error closing the reader", e);
          }
        }
      }
    }
  }

  public static void readFromStreamAndWriteToStream(InputStream inputStream,
      OutputStream outputStream) throws IOException {
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    try {
      String line = null;
      while ((line = reader.readLine()) != null) {
        writer.write(line);
      }
      writer.flush();
    } finally {
      writer.close();
      reader.close();
    }
  }

  public static SystemCommandResult executeCommand(
      final List<String> command,
      final String workingDirectory,
      final Map<String, String> environment,
      final File inputFile,
      final InputStream inputStream,
      final boolean redirectErrorStream,
      final long timeout) {

    Process process;
    SystemCommandResult commandResult = new SystemCommandResult();
    commandResult.setExitValue(1);

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    if (workingDirectory != null) {
      processBuilder.directory(new File(workingDirectory));
    }

    if (environment != null) {
      Map<String, String> processEnvironment = processBuilder.environment();
      processEnvironment.putAll(environment);
    }

    if (inputFile != null) {
      assert inputStream == null;
      processBuilder.redirectInput(inputFile);
    }
    processBuilder.redirectErrorStream(redirectErrorStream);

    String commandString = String.join(" ", command);
    log.info("Executing system command {}", commandString);

    try {
      Future<?> futureErrStreamBuilder = null;
      Future<?> futureStdoutStreamBuilder;
      Runnable stdOutInputStreamHandler;
      Runnable stdErrInputStreamHandler;
      ExecutorService executorService = Executors.newFixedThreadPool(2);

      log.debug("Starting new subprocess...");
      process = processBuilder.start();

      if (inputStream != null) {
        assert inputFile == null;
        OutputStream stdin = process.getOutputStream();
        readFromStreamAndWriteToStream(inputStream, stdin);
      }

      InputStream stdOutInputStream = process.getInputStream();
      stdOutInputStreamHandler = new AsyncStreamHandler(stdOutInputStream);

      log.debug("Asynchronously fetching stdout...");
      futureStdoutStreamBuilder = executorService.submit(stdOutInputStreamHandler);

      if (!redirectErrorStream) {
        InputStream stdErrInputStream = process.getErrorStream();
        stdErrInputStreamHandler = new AsyncStreamHandler(stdErrInputStream);
        log.debug("Asynchronously fetching stderr...");
        futureErrStreamBuilder = executorService.submit(stdErrInputStreamHandler);
      }

      try {
        log.debug("Initiating shutdown of async stream handlers for the subprocess");
        executorService.shutdown();
        executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        futureStdoutStreamBuilder.get(timeout, TimeUnit.MILLISECONDS);
        if (futureErrStreamBuilder != null) {
          futureErrStreamBuilder.get(timeout, TimeUnit.MILLISECONDS);
        }
      } catch (InterruptedException | ExecutionException ee) {
        commandResult.setException(ee);
        log.error("Error fetching stdout and stderr streams from the system command execution", ee);
      }

      try {
        log.debug("Waiting for {} milliseconds before giving up", timeout);
        process.waitFor(timeout, TimeUnit.MILLISECONDS);
        log.debug("Subprocess successfully ended");
        commandResult.setExitValue(process.exitValue());
      } catch (InterruptedException e1) {
        commandResult.setException(e1);
        log.error("The subprocess did not finish within your expected time", e1);
      }
    } catch (Exception e) {
      commandResult.setException(e);
      log.error("Error executing systemCommand {}", String.join(" ", command));
    }

    log.info("Done executing system command {} with result {}", commandString, commandResult);
    return commandResult;
  }

}
