/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.ui.model.DatasetDetail;
import java.sql.Types;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

public class DatasetBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {

  public DatasetBeanPropertySqlParameterSource(DatasetDetail dataset) {
    super(dataset);
    this.registerSqlType("dataType", Types.VARCHAR);
    this.registerSqlType("clientId", Types.INTEGER);
  }

}
