/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.util.Constants;
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
@Qualifier("mwbItsDaoBean")
@Slf4j
public class JdbcMwbItsClientMapDao implements MwbItsClientMapDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcMwbItsClientMapDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }


  @Override
  public MwbItsClientMapBO getClientByClientAppAccount(String clientName,
      String appName, String accountName) {

    MwbItsClientMapBO mwbItsClientMapBO = null;

    Map<String, Object> paramMap = new HashMap<>();

    accountName = StringEscapeUtils.escapeSql(accountName);

    paramMap.put(MwbItsClientMapBO.FLD_ITS_CLIENT_ID, clientName.toLowerCase().trim());
    paramMap.put(MwbItsClientMapBO.FLD_ITS_APP_ID, appName.toLowerCase().trim());
    paramMap.put(MwbItsClientMapBO.FLD_ITS_ACCOUNT_ID, accountName.toLowerCase().trim());

    StringBuilder itsClientQuery = new StringBuilder("SELECT id ");
    itsClientQuery = itsClientQuery.append("FROM mwb_its_client_map  where ")
        .append(Constants.LOWER).append(MwbItsClientMapBO.FLD_ITS_CLIENT_ID).append(Constants.CLOSE_BRACKETS)
        .append(MwbItsClientMapBO.FLD_ITS_CLIENT_ID)
        .append(" and ")
        .append(Constants.LOWER).append(MwbItsClientMapBO.FLD_ITS_APP_ID).append(Constants.CLOSE_BRACKETS)
        .append(MwbItsClientMapBO.FLD_ITS_APP_ID)
        .append(" and ")
        .append(Constants.LOWER).append(MwbItsClientMapBO.FLD_ITS_ACCOUNT_ID).append(Constants.CLOSE_BRACKETS)
        .append(MwbItsClientMapBO.FLD_ITS_ACCOUNT_ID);

    String query = itsClientQuery.toString();

    List<MwbItsClientMapBO> itsClients = new LinkedList<>();
    try {
      itsClients = jdbcTemplate
          .query(itsClientQuery.toString(), paramMap, new MWBClientRowMapper());
    } catch (Exception e) {
      log.warn("Datasets from query:{} and params: {} not found", query, paramMap);
    }

    if (itsClients != null && !itsClients.isEmpty()) {
      mwbItsClientMapBO = itsClients.get(0);
    }

    return mwbItsClientMapBO;
  }

  private class MWBClientRowMapper implements RowMapper<MwbItsClientMapBO> {

    @Override
    public MwbItsClientMapBO mapRow(ResultSet rs, int rowNum) throws SQLException {
      MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();
      mwbItsClientMapBO.setId(rs.getInt(Constants.DB_COLUMN_ID));
      mwbItsClientMapBO.setItsClientId(rs.getString(MwbItsClientMapBO.FLD_ITS_CLIENT_ID));
      mwbItsClientMapBO.setItsAccountId(rs.getString(MwbItsClientMapBO.FLD_ITS_ACCOUNT_ID));
      mwbItsClientMapBO.setItsAppId(rs.getString(MwbItsClientMapBO.FLD_ITS_APP_ID));
      mwbItsClientMapBO.setDescription(rs.getString("description"));
      mwbItsClientMapBO.setCreatedBy(rs.getInt("created_by"));
      mwbItsClientMapBO.setCreatedAt(rs.getLong("created_at"));
      mwbItsClientMapBO.setModifiedAt(rs.getLong("modified_at"));
      mwbItsClientMapBO.setModifiedBy(rs.getInt("modified_by"));

      BusinessObject.sanitize(mwbItsClientMapBO);

      return mwbItsClientMapBO;
    }

  }


}
