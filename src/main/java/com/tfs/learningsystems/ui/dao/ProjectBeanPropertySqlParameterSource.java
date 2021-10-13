/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.ui.model.ProjectDetail;
import java.sql.Types;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

public class ProjectBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {

  public ProjectBeanPropertySqlParameterSource(ProjectDetail project) {
    super(project);
    this.registerSqlType("dataType", Types.VARCHAR);
    this.registerSqlType("vertical", Types.VARCHAR);
    this.registerSqlType("state", Types.VARCHAR);
    this.registerSqlType("ownerId", Types.INTEGER);
    this.registerSqlType("groupId", Types.INTEGER);
    this.registerSqlType("clientId", Types.INTEGER);
  }

}
