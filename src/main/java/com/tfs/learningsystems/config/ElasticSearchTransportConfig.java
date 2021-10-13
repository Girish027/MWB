/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile({"production", "staging", "development", "dev_sv2", "qa_sv2", "psr_sv2", "stable_sv2",
    "staging_gslb", "prod_gslb",
    "integration"})
@Slf4j
public class ElasticSearchTransportConfig extends ElasticSearchBaseConfig {

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Bean
  @Scope("singleton")
  public Client elasticSearchClient() throws UnknownHostException {

    Settings settings = Settings.builder()
        .put("cluster.name", elasticSearchProps.getClusterName())
        .put("client.transport.ping_timeout", elasticSearchProps.getTransportPingTimeout())
        .put("client.transport.sniff", true).build();
    TransportClient transportClient = new PreBuiltTransportClient(settings);

    for (String host : elasticSearchProps.getHosts()) {
      transportClient.addTransportAddress(
          new InetSocketTransportAddress(
              InetAddress.getByName(host),
              elasticSearchProps.getTransportPort()));
    }
    searchClient = transportClient;
    List<IndexInfo> indicesInfo = new ArrayList<>();
    indicesInfo.add(new IndexInfo(elasticSearchProps.getNltoolsIndexName(),
        elasticSearchProps.getNltoolsIndexAlias(),
        elasticSearchProps.getIndexSourceLocation(),
        elasticSearchProps.getNltoolsIndexSchemaVersion(),
        elasticSearchProps.getDefaultDocumentIndexType()));
    indicesInfo.add(new IndexInfo(elasticSearchProps.getClassificationIndexName(),
        elasticSearchProps.getClassificationIndexAlias(),
        elasticSearchProps.getIndexSourceLocation(),
        null,
        elasticSearchProps.getDefaultDocumentIndexType()));
    initializeIndices(indicesInfo, log);
    return searchClient;
  }

  @PreDestroy
  public void closeSearchClient() {

    if (searchClient != null) {
      searchClient.close();
    }
  }
}
