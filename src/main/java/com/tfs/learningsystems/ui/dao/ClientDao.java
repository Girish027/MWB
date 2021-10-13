/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.ClientBO;

public interface ClientDao {

  public ClientBO getClientByName(String clientName);

}
