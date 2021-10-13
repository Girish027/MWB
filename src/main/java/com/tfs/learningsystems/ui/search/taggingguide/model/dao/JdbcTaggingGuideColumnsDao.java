/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingModel;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("taggingGuideColumnsDao")
@Slf4j
public class JdbcTaggingGuideColumnsDao implements TaggingGuideColumnsDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcTaggingGuideColumnsDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  private static class TaggingGuideColumnMappingBaseExtractor implements
      ResultSetExtractor<Map<String, String>> {

    @Override
    public Map<String, String> extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      Map<String, String> nameToIdMap = new HashMap<String, String>();
      while (rs.next()) {
        nameToIdMap.put(rs.getString("name"), String.valueOf(rs.getInt("id")));
      }
      return nameToIdMap;
    }
  }

  private static class TaggingGuideColumnMappingExtractor implements
      ResultSetExtractor<TaggingGuideColumnMappingSelectionList> {

    @Override
    public TaggingGuideColumnMappingSelectionList extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      TaggingGuideColumnMappingSelectionList columnMappings = new TaggingGuideColumnMappingSelectionList();
      while (rs.next()) {
        TaggingGuideColumnMappingSelection mapping = new TaggingGuideColumnMappingSelection();
        mapping.setColumnName(rs.getString("name"));
        mapping.setId(String.valueOf(rs.getInt("id")));
        mapping.setDisplayName(rs.getString("display_name"));
        mapping.setUserId(rs.getString("user_id"));
        mapping.setColumnIndex(String.valueOf(rs.getInt("column_index")));
        //
        // prevent stored XSS, NT-2373
        //
        BusinessObject.sanitize(mapping);
        columnMappings.add(mapping);
      }
      return columnMappings;
    }
  }


  @Override
  public void addMappings(String projectId,
      final TaggingGuideColumnMappingSelectionList columnMappings) {

    log.info("SelectionList: {}", columnMappings);
    final String getColumnIdQuery = "SELECT id, name from tagging_guide_columns";
    final String addColumnMappingQuery = "REPLACE INTO tagging_guide_column_mappings"
        + "(user_id, column_id, project_id, column_index, display_name) "
        + "VALUES (:userId, :columnId, :projectId, :columnIndex, :displayName)";

    Map<String, String> nameIdMap = jdbcTemplate
        .query(getColumnIdQuery, new TaggingGuideColumnMappingBaseExtractor());
    List<TaggingGuideColumnMappingModel> serializableColumnMappingList = new ArrayList<>(
        columnMappings.size());

    for (TaggingGuideColumnMappingSelection mapping : columnMappings) {
      String columnName = mapping.getColumnName();
      String columnId = nameIdMap.get(columnName);
      TaggingGuideColumnMappingModel mappingModel = new TaggingGuideColumnMappingModel();
      mappingModel.setColumnId(columnId);
      mappingModel.setUserId(mapping.getUserId());
      projectId = StringEscapeUtils.escapeSql(projectId);
      mappingModel.setProjectId(projectId);
      mappingModel.setDisplayName(mapping.getDisplayName());
      mappingModel.setColumnIndex(Integer.valueOf(mapping.getColumnIndex()));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(mappingModel);
      serializableColumnMappingList.add(mappingModel);
    }

    SqlParameterSource[] batch = SqlParameterSourceUtils
        .createBatch(serializableColumnMappingList.toArray());
    jdbcTemplate.batchUpdate(addColumnMappingQuery, batch);
  }

  @Override
  public TaggingGuideColumnMappingSelectionList getMappings(String userId, String projectId) {
    final String columnMappingQuery = "SELECT CM.id as id, CM.user_id as user_id, "
        + "C.name as name, CM.column_index as column_index, CM.display_name as display_name "
        + "FROM tagging_guide_column_mappings CM, tagging_guide_columns C "
        + "WHERE CM.column_id = C.id AND CM.user_id = :userId AND CM.project_id = :projectId";
    Map<String, String> paramMap = new HashMap<>();
    userId = StringEscapeUtils.escapeSql(userId);
    projectId = StringEscapeUtils.escapeSql(projectId);
    paramMap.put("userId", userId);
    paramMap.put("projectId", projectId);
    return this.jdbcTemplate
        .query(columnMappingQuery, paramMap, new TaggingGuideColumnMappingExtractor());
  }
}
