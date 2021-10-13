/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ElasticSearchBaseConfig {

  protected Client searchClient;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @lombok.Data
  public static class IndexInfo {

    private String indexName;
    private String indexAlias;
    private String indexSourcePath;
    private String indexSchemaVersion;
    private String indexTypeName;

    public IndexInfo(String indexName, String indexSourcePath) {
      this(indexName, null, indexSourcePath, null, "_default_");
    }

    public IndexInfo(String indexName, String indexAlias, String indexSourcePath) {
      this(indexName, indexAlias, indexSourcePath, null, "_default_");
    }

    public IndexInfo(String indexName, String indexAlias, String indexSourcePath,
        String indexSchemaVersion) {
      this(indexName, indexAlias, indexSourcePath, indexSchemaVersion, "_default_");
    }

    public IndexInfo(String indexName, String indexAlias, String indexSourcePath,
        String indexSchemaVersion, String indexTypeName) {
      this.indexName = indexName;
      this.indexAlias = indexAlias;
      this.indexSourcePath = indexSourcePath;
      this.indexSchemaVersion = indexSchemaVersion;
      this.indexTypeName = indexTypeName;
    }
  }

  public void initializeIndices(List<IndexInfo> indicesInfo, Logger log) {
    try {
      for (IndexInfo indexEntry : indicesInfo) {
        String indexName = indexEntry.getIndexName();
        String aliasName = indexEntry.getIndexAlias();
        String indexSourcePath = indexEntry.getIndexSourcePath();
        String indexTypeName = indexEntry.getIndexTypeName();
        String indexSchemaVersion = indexEntry.getIndexSchemaVersion();

        if (!hasIndex(indexName, log)) {
          addIndex(indexName, aliasName, indexSourcePath, log);
        } else if (!hasLatestMappingSchema(aliasName, indexName, indexSchemaVersion, indexTypeName,
            log)) {
          updateIndex(indexName, indexSourcePath, indexSchemaVersion, indexTypeName, log);
        }
      }
    } catch (NoNodeAvailableException ex) {
      String deployProfile = System.getProperty("spring.profiles.active");
      log.debug("[ELASTICSEARCH] deploy.profile: {}", deployProfile);
      if ("production".equals(deployProfile)) {
        throw ex;
      } else {
        log.warn("[ELASTICSEARCH] Supressed: NoNodeAvailableException");
      }
    }
  }

  public boolean hasIndex(String indexName, Logger log) {

    IndicesExistsResponse response = searchClient.admin().indices().prepareExists(indexName).get();
    if (response.isExists()) {
      log.info("[ELASTICSEARCH] Index found for " + indexName);
      return true;
    }

    log.info("[ELASTICSEARCH] Index not found for " + indexName);
    return false;
  }

  public void addIndex(String indexName, String aliasName,
      String indexSourcePath, Logger log) {

    log.info("[ELASTICSEARCH] Creating index for " + indexName);
    try {
      searchClient.admin().indices().prepareCreate(indexName).setSource(
          new String(Files.readAllBytes(
              Paths.get(indexSourcePath + indexName + ".json")),
              StandardCharsets.UTF_8), XContentType.JSON).get();
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    if (aliasName != null && !aliasName.isEmpty()) {
      IndicesAdminClient indicesAdminClient = searchClient.admin().indices();

      IndicesAliasesRequestBuilder aliasesRequestBuilder =
          indicesAdminClient.prepareAliases();

      ImmutableOpenMap<String, List<AliasMetaData>> aliasDataMap =
          indicesAdminClient.prepareGetAliases(aliasName).get().getAliases();

      for (ObjectCursor<String> iname : aliasDataMap.keys()) {
        aliasesRequestBuilder.removeAlias(iname.value, aliasName);
      }

      aliasesRequestBuilder.addAlias(indexName, aliasName).execute().actionGet();
    }
  }

  public void updateIndex(String indexName, String indexSourcePath, String schemaVersion,
      String typeName, Logger log) {

    log.info("[ELASTICSEARCH] Index mapping schema out of date, updating index {}", indexName);
    try {

      String mapping =
          new String(
              Files.readAllBytes(Paths.get(indexSourcePath
                  + indexName + "." + schemaVersion + ".json")),
              StandardCharsets.UTF_8);

      searchClient.admin().indices().preparePutMapping(indexName)
          .setSource(mapping, XContentType.JSON)
          .setType(typeName).execute().actionGet();

    } catch (Exception e) {
      log.debug("[ELASTICSEARCH] failed to update {} schema", indexName);
      log.error(e.getMessage());
    }


  }

  public boolean hasLatestMappingSchema(String alias, String indexName, String schemaVersion,
      String typeName, Logger log) {

    log.info("[ELASTICSEARCH] Validating index {} mapping schema version", alias);

    if (schemaVersion == null) {
      return true;
    }

    final GetMappingsResponse response = searchClient.admin().indices().prepareGetMappings(alias)
        .setTypes(typeName).execute().actionGet();

    try {
      final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings =
          response.mappings();
      final ImmutableOpenMap<String, MappingMetaData> indexMappings = mappings.get(indexName);
      final MappingMetaData mappingMetadata = indexMappings.get(typeName);

      // Get mapping content for the type
      final Map<String, Object> source = mappingMetadata.sourceAsMap();
      if (!source.containsKey("_meta")) {
        return false;
      }

      @SuppressWarnings("unchecked") final Map<String, Object> customMetadata = (Map<String, Object>) source
          .get("_meta");

      String schemaVersionLabel = elasticSearchProps.getNltoolsIndexSchemaVersionLabel();

      String deployedSchemaVersion = customMetadata.containsKey(schemaVersionLabel)
          ? customMetadata.get(schemaVersionLabel).toString() : null;
      return schemaVersion.equalsIgnoreCase(deployedSchemaVersion);
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }


  }
}
