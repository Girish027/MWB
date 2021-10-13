/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.controllers;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class UI {

  @GetMapping(path = "/ui")
  public String uiIndex(Principal principal, HttpServletRequest request, Model model) {
    String nameIdValue = null;

    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      nameIdValue = (String) httpSession.getAttribute("User");
    } else {
      nameIdValue = "World";
    }
    model.addAttribute("name", nameIdValue);
    return "index";
  }

  @GetMapping(path = "/ui/upload")
  public String uploadFiles(Model model) {
    return "upload";
  }
}
