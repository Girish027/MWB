/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("fileDaoBean")
@Slf4j
public class JdbcFileDao implements FileDao {

  private DataSource datasource;

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcFileDao(DataSource datasource) {
    this.datasource = datasource;
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  private FileEntryDetail getFileByQuery(final String query, final Map<String, ?> paramMap) {
    FileEntryDetail fileEntry = null;
    try {
      fileEntry = jdbcTemplate.queryForObject(query, paramMap, new FileRowMapper());
    } catch (Exception e) {
      log.warn("File from query:{} and params: {} not found", query, paramMap);
    }
    return fileEntry;
  }

  private List<FileEntryDetail> getFilesByQuery(final String query, final Map<String, ?> paramMap) {

    List<FileEntryDetail> files = new LinkedList<>();
    try {
      files = jdbcTemplate.query(query, paramMap, new FileRowMapper());
    } catch (Exception e) {
      log.warn("Files from query:{} and params: {} not found", query, paramMap);
    }
    return files;
  }

  @Override
  public void deleteFileById(String fileId) {
    final String query = "DELETE FROM files where file_id = :fileId";
    Map<String, String> paramMap = new HashMap<>();
    fileId = StringEscapeUtils.escapeSql(fileId);
    paramMap.put("fileId", fileId);
    this.jdbcTemplate.update(query, paramMap);
  }

  @Override
  public FileEntryDetail getFileById(String fileId) {
    final String query = "SELECT id, file_id, name, system_name, user, created_at, "
        + "modified_at FROM files WHERE file_id = :fileId";
    Map<String, String> paramMap = new HashMap<>();
    fileId = StringEscapeUtils.escapeSql(fileId);
    paramMap.put("fileId", fileId);
    return this.getFileByQuery(query, paramMap);
  }

  private class FileRowMapper implements RowMapper<FileEntryDetail> {

    @Override
    public FileEntryDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
      FileEntryDetail fileEntry = new FileEntryDetail();
      fileEntry.setId(String.valueOf(rs.getLong("id")));
      fileEntry.setFileId(rs.getString("file_id"));
      fileEntry.setName(rs.getString("name"));
      fileEntry.setSystemName(rs.getString("system_name"));
      fileEntry.setUser(rs.getString("user"));
      fileEntry.setCreatedAt(rs.getLong("created_at"));
      fileEntry.setModifiedAt(rs.getLong("modified_at"));
      //
      // prevent stored XSS, NT-2373
      //
      BusinessObject.sanitize(fileEntry);
      return fileEntry;
    }

  }
}
