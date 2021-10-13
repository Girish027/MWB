/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.util.Constants;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("clientDaoBean")
public class JdbcClientDao implements ClientDao {

  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public JdbcClientDao(DataSource datasource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  @Override
  public ClientBO getClientByName(String clientName) {

    ClientBO client = new ClientBO();
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("name", clientName);

    final String query =
            "select id, name, is_vertical, description, address, created_at, modified_at, cid, state from client where name = :name";
    ClientRowByJobRowHandler rowHandler = new ClientRowByJobRowHandler();
    jdbcTemplate.query(query, paramMap, rowHandler);
    return client;
  }


  /**
   * This is an implementation of the Spring RowCallbackHandler for retrieving response values for
   * relevancy evaluation.
   */
  private static class ClientRowByJobRowHandler implements RowCallbackHandler {

    /**
     * Collation of response relevance values
     */
    Map<String, List<ClientBO>> results = new HashMap<>();

    private ClientRowByJobRowHandler() {
    }

    /**
     * Adds response ratings logged for the interface for the period given to a list.
     *
     * @param rs results of the query
     */
    @Override
    public void processRow(final ResultSet rs) throws SQLException {
      final ClientBO client = new ClientBO();
      client.setId(rs.getInt(Constants.DB_COLUMN_ID));
      client.setName(rs.getString(Constants.DB_COLUMN_NAME));
      client.setIsVertical(rs.getBoolean(Constants.DB_COLUMN_VERTICAL));
      client.setDescription(rs.getString(Constants.DB_COLUMN_DESCRIPTION));
      client.setAddress(rs.getString(Constants.DB_COLUMN_ADDRESS));
      client.setCreatedAt(rs.getLong(Constants.DB_COLUMN_CREATED_AT));
      client.setModifiedAt(rs.getLong(Constants.DB_COLUMN_MODIFIED_AT));
      client.setCid(rs.getString(Constants.DB_COLUMN_CID));
      client.setState(ClientBO.State.valueOf(rs.getString(Constants.DB_COLUMN_STATE)));

      BusinessObject.sanitize(client);

      List<ClientBO> clients = results.get(client.getId());
      if (clients == null) {
        clients = new ArrayList<>();
      }
      clients.add(client);

      results.put(Integer.toString(client.getId()), clients);
    }


  }

  private static class ClientResultSetExtractor
      implements ResultSetExtractor<Map<String, ClientBO>> {

    @Override
    public Map<String, ClientBO> extractData(ResultSet rs)
        throws SQLException, DataAccessException {
      Map<String, ClientBO> clientMap = new LinkedHashMap<>();
      while (rs.next()) {
        ClientBO client = new ClientBO();
        client.setId(rs.getInt(Constants.DB_COLUMN_ID));
        client.setName(rs.getString(Constants.DB_COLUMN_NAME));
        client.setIsVertical(rs.getBoolean(Constants.DB_COLUMN_VERTICAL));
        client.setDescription(rs.getString(Constants.DB_COLUMN_DESCRIPTION));
        client.setAddress(rs.getString(Constants.DB_COLUMN_ADDRESS));
        client.setCreatedAt(rs.getLong(Constants.DB_COLUMN_CREATED_AT));
        client.setModifiedAt(rs.getLong(Constants.DB_COLUMN_MODIFIED_AT));
        client.setCid(rs.getString(Constants.DB_COLUMN_CID));
        client.setState(ClientBO.State.valueOf(rs.getString(Constants.DB_COLUMN_STATE)));
        BusinessObject.sanitize(client);
        clientMap.put(client.getCid(), client);
      }
      return clientMap;
    }

  }


}
