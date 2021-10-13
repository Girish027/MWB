package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.model.ClientDetail;
import org.springframework.http.ResponseEntity;

import java.util.Map;


public interface ITSAPIManager {

  public Map<String, ClientDetail> findClientsByUserId(String userId);

}
