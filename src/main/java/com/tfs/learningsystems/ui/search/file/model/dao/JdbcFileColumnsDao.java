/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.file.model.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.ui.search.file.model.FileColumn;
import com.tfs.learningsystems.ui.search.file.model.FileColumnList;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingModel;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelection;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.util.Constants;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("fileColumnsDao")
@Slf4j
public class JdbcFileColumnsDao implements FileColumnsDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcFileColumnsDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  @Override
  public List<String> getColumnNames() {
    final String query = "SELECT name from file_columns";
    return jdbcTemplate.queryForList(query, new HashMap<String, String>(), String.class);
  }

  @Override
  public FileColumnList getColumns() {
    final StringBuilder query = new StringBuilder("SELECT").append(Constants.EMPTY_STRING)
        .append(Constants.DB_COLUMN_ID).append(Constants.COMMA)
        .append(Constants.DB_COLUMN_NAME).append(Constants.COMMA)
        .append(Constants.DB_COLUMN_REQUIRED).append(Constants.COMMA)
        .append(Constants.DB_COLUMN_DISPLAY_NAME).append(" from file_columns");
    return jdbcTemplate.query(query.toString(), new DatasetColumnExtractor());
  }

  @Override
  public void addMappings(String userEmail, final FileColumnMappingSelectionList columnMappings) {

    log.info("SelectionList: {}", columnMappings);
    final String getColumnIdQuery = "SELECT id, name from file_columns";
    final String addColumnMappingQuery = "REPLACE INTO file_column_mappings"
        + "(user_id, column_id, column_index, display_name) "
        + "VALUES (:userId, :columnId, :columnIndex, :displayName)";

    Map<String, String> nameIdMap = jdbcTemplate
        .query(getColumnIdQuery, new DatasetColumnMappingBaseExtractor());
    Map<String, String> fileColumnMappings = fileColumnMappingMapByUserEmail(userEmail);
    List<FileColumnMappingModel> serializableColumnMappingList = new ArrayList<>(
        columnMappings.size());

    for (FileColumnMappingSelection mapping : columnMappings) {
      String columnName = mapping.getColumnName();
      String columnId = nameIdMap.get(columnName);
      String userId = mapping.getUserId();
      String columnIndex = mapping.getColumnIndex();
      String displayName = mapping.getDisplayName();
      String key = columnId + Constants.COLON + columnIndex;
      if(fileColumnMappings == null
              || fileColumnMappings.isEmpty()
              || fileColumnMappings.get(key) == null
              || !fileColumnMappings.get(key).equals(displayName)) {
        FileColumnMappingModel mappingModel = new FileColumnMappingModel();
        mappingModel.setColumnId(columnId);
        mappingModel.setUserId(userId);
        mappingModel.setDisplayName(displayName);
        mappingModel.setColumnIndex(Integer.parseInt(columnIndex));
        //
        // prevent stored XSS, NT-2373
        //
        BusinessObject.sanitize(mappingModel);
        serializableColumnMappingList.add(mappingModel);
      }
    }

    SqlParameterSource[] batch = SqlParameterSourceUtils
        .createBatch(serializableColumnMappingList.toArray());
    jdbcTemplate.batchUpdate(addColumnMappingQuery, batch);
  }

  public Map<String, String> fileColumnMappingMapByUserEmail(String userId) {
    final StringBuilder query = new StringBuilder("SELECT").append(Constants.EMPTY_STRING)
            .append(Constants.STAR).append(" from file_column_mappings where ")
            .append(Constants.DB_COLUMN_USER_ID + " = :userId");
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("userId", userId);
    return jdbcTemplate.query(query.toString(), paramMap, new DatasetColumnMappingExtractor());
  }

  private static class DatasetColumnMappingBaseExtractor implements
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

  private static class DatasetColumnMappingExtractor implements
      ResultSetExtractor<Map<String, String>> {

    @Override
    public Map<String, String> extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      Map<String, String> serializableColumnMappingList = new HashMap<>();
      while (rs.next()) {
        String key = rs.getString(Constants.DB_COLUMN_COLUMN_ID) + Constants.COLON + rs.getInt(Constants.DB_COLUMN_COLUMN_INDEX);
        String displayName = rs.getString(Constants.DB_COLUMN_DISPLAY_NAME);
        serializableColumnMappingList.put(key, displayName);
      }
      return serializableColumnMappingList;
    }
  }

  private static class DatasetColumnExtractor implements ResultSetExtractor<FileColumnList> {

    @Override
    public FileColumnList extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      FileColumnList columnList = new FileColumnList();
      while (rs.next()) {
        FileColumn column = new FileColumn();
        column.setId(String.valueOf(rs.getInt(Constants.DB_COLUMN_ID)));
        column.setName(rs.getString(Constants.DB_COLUMN_NAME));
        column.setRequired(rs.getBoolean(Constants.DB_COLUMN_REQUIRED));
        column.setDisplayName(rs.getString(Constants.DB_COLUMN_DISPLAY_NAME));
        //
        // prevent stored XSS, NT-2373
        //
        BusinessObject.sanitize(column);
        columnList.add(column);
      }
      return columnList;
    }

  }

}
