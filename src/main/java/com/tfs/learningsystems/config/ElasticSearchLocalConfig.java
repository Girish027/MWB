/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
@Slf4j
public class ElasticSearchLocalConfig extends ElasticSearchBaseConfig {


  private Node node;

  private Path homeDirPath;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Bean
  public Client elasticSearchClient() throws UnknownHostException, IOException {

    try {
      this.homeDirPath = Files.createTempDirectory("esData-", new FileAttribute[]{});
      Settings settings = Settings.builder()
          .put("client.transport.ping_timeout", elasticSearchProps.getTransportPingTimeout())
          .put("cluster.name", elasticSearchProps.getClusterName())
          .build();

      TransportClient transportClient = new PreBuiltTransportClient(settings);

      for (String host : elasticSearchProps.getHosts()) {
        transportClient.addTransportAddress(
            new InetSocketTransportAddress(
                InetAddress.getByName(host),
                elasticSearchProps.getTransportPort()));
      }

      searchClient = transportClient;
      searchClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
    } catch (Exception e) {
      log.error("Cannot start local instance", e);
      throw (e);

    }

    return searchClient;
  }

  public void deletePathHomeDirectory() {
    try {
      FileUtils.deleteDirectory(new File(this.homeDirPath.toString()));
    } catch (IOException e) {
      log.error("Could not delete data directory of embedded elasticsearch server", e);
      throw new RuntimeException("Could not delete data directory of embedded elasticsearch server",
          e);
    }
  }

  @PreDestroy
  public void closeSearchClient() {
    if (searchClient != null) {
      searchClient.close();
    }
    this.deletePathHomeDirectory();
  }
}
