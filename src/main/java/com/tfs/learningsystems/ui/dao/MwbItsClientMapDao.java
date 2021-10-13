/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.MwbItsClientMapBO;

public interface MwbItsClientMapDao {

  public MwbItsClientMapBO getClientByClientAppAccount(String clientName,
      String appName,String accountName);

}
