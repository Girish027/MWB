/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Locale;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.util.CommonLib;
import java.sql.SQLException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ResourcesApiTest {

  private String basePath;
  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1";
  }

  @Before
  public void setUp() throws ScriptException, SQLException {

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

  }

  @Test
  public void testGetVerticals() {
    ResponseEntity<String[]> entity =
        this.restTemplate.getForEntity(basePath + "/resources/verticals", String[].class);
    String[] verticals = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(Vertical.values().length, verticals.length);
  }

  @Test
  public void testGetDataTypes() {

    ResponseEntity<String[]> entity =
        this.restTemplate.getForEntity(basePath + "/resources/datatypes", String[].class);
    String[] datatypes = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(DataType.values().length, datatypes.length);
  }

  @Test
  public void testGetLocales() {
    ResponseEntity<String[]> entity =
        this.restTemplate.getForEntity(basePath + "/resources/locales", String[].class);
    String[] locales = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(Locale.values().length, locales.length);
  }

  @Test
  public void testGetSingleConfigProperty() {
    //This will test retrieving single config property
    ResponseEntity<String> entity =
        this.restTemplate
            .getForEntity(basePath + "/configProperties?propertyName=kibanaLogURL", String.class);
    String propertyResponse = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertNotNull(propertyResponse);
  }

  @Test
  public void testGetMultipleConfigProperty() {
    //This will test retrieving multiple config property identified by property name and seperated
    //by comma
    ResponseEntity<String> entity =
        this.restTemplate
            .getForEntity(basePath + "/configProperties?propertyName=kibanaLogURL,orionURL",
                String.class);
    String propertyResponse = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertNotNull(propertyResponse);
  }

  @Test
  public void testGetPropertyForBlackListedProps() {
    // This will try to get config property which is blacklisted in BE code and hence
    // will be returned BAD request in response.
    ResponseEntity<String> entity =
        this.restTemplate
            .getForEntity(basePath + "/configProperties?propertyName=locationClassesFilename",
                String.class);
    String propertyResponse = entity.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertNotNull(propertyResponse);
  }

  @Test
  public void testGetPropertyForNonExistentProps() {
    // This will try to get config property which does not exists in config file
    ResponseEntity<String> entity =
        this.restTemplate
            .getForEntity(basePath + "/configProperties?propertyName=Bob", String.class);
    String propertyResponse = entity.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertNotNull(propertyResponse);
  }

  @Test
  public void testGetPropertyWitEmptyPropName() {
    // This will try to get config property with empty property name
    ResponseEntity<String> entity =
        this.restTemplate.getForEntity(basePath + "/configProperties?propertyName=", String.class);
    String propertyResponse = entity.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertNotNull(propertyResponse);
  }

  @Test
  public void testGetUserGroups() {

    // Download newly created config
    String url = basePath + "/userGroups";

    restTemplate.getRestTemplate().getMessageConverters().add(new ByteArrayHttpMessageConverter());

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate
        .exchange(url, HttpMethod.GET, httpEntity, String.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertThat(response.getHeaders().getContentLength(), greaterThan(0l));
    assertThat(response.getBody().length(), greaterThan(0));

  }

}
