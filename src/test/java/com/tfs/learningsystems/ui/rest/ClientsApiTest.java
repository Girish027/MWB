/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.VectorizerBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.util.CommonLib;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ClientsApiTest {

  private String basePath;

  private String modelTechnology = "use_large";

  @Autowired
  private TestRestTemplate restTemplate;
  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1";
  }

  @Before
  public void setUp() throws ScriptException, SQLException {
    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

  }

  @Test
  public void testGetPostPatchClient() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    String accountId =
        methodName + "Ac" + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    String appId = methodName + "App" + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    Client client = ModelUtils.getTestClient(name, name, appId, accountId, true);

    VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
    testVectorizer.create();

    ResponseEntity<ClientDetail> createEntity = this.restTemplate
        .postForEntity(basePath + "/clients", client,
            ClientDetail.class);
    assertEquals(HttpStatus.CREATED, createEntity.getStatusCode());

    ClientDetail createdClient = createEntity.getBody();

    String id = createdClient.getId();

    assertNotNull(createdClient.getId());

    assertEquals(id.toString(), createdClient.getId());
    assertEquals(name, createdClient.getName());
    assertEquals(client.getItsAccountId(), createdClient.getItsAccountId());
    assertEquals(client.getItsAppId(), createdClient.getItsAppId());
    assertEquals(client.getItsClientId(), createdClient.getItsClientId());
    assertEquals(client.getAddress(), createdClient.getAddress());
    assertEquals(client.getDescription(), createdClient.getDescription());

    ResponseEntity<ClientDetail> getEntity =
        this.restTemplate.getForEntity(basePath + "/clients/" + id, ClientDetail.class);
    ClientDetail returnedClient = getEntity.getBody();
    assertEquals(HttpStatus.OK, getEntity.getStatusCode());
    assertEquals(id, returnedClient.getId());
    assertEquals(name, returnedClient.getName());
    assertEquals(client.getItsAccountId(), returnedClient.getItsAccountId());
    assertEquals(client.getItsAppId(), returnedClient.getItsAppId());
    assertEquals(client.getItsClientId(), returnedClient.getItsClientId());
    assertEquals(client.getAddress(), returnedClient.getAddress());
    assertEquals(client.getDescription(), returnedClient.getDescription());

    String newAddress = "new" + createdClient.getAddress();

    String newName = createdClient.getName() + "_new";

    String newItsAppId = createdClient.getItsAppId() + "_new";

    String newAccountId = createdClient.getItsAccountId() + "_new";

    String newDesc = "new" + createdClient.getDescription();

    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/name");
    patchDocument.setValue(newName);
    patchRequest.add(patchDocument);

    patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/itsAppId");
    patchDocument.setValue(newItsAppId);
    patchRequest.add(patchDocument);

    patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/itsAccountId");
    patchDocument.setValue(newAccountId);
    patchRequest.add(patchDocument);

    patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/description");
    patchDocument.setValue(newDesc);
    patchRequest.add(patchDocument);

    patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/address");
    patchDocument.setValue(newAddress);
    patchRequest.add(patchDocument);

    ClientDetail patchedClient = null;

    ObjectMapper mapper = new ObjectMapper();
    try {
      String jsonPatch = mapper.writeValueAsString(patchRequest);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.valueOf("application/json-patch+json"));
      HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);

      String url = basePath + "/clients/" + createdClient.getId();
      ResponseEntity<ClientDetail> patchEntity =
          this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ClientDetail.class);
      patchedClient = patchEntity.getBody();

      String patchedId = patchedClient.getId();

      assertNotNull(patchedId);

      assertEquals(id.toString(), patchedClient.getId());
      assertEquals(newName, patchedClient.getName());
      assertEquals(newAccountId, patchedClient.getItsAccountId());
      assertEquals(newItsAppId, patchedClient.getItsAppId());
      assertEquals(newAddress, patchedClient.getAddress());
      assertEquals(newDesc, patchedClient.getDescription());


    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    String deleteurl = basePath + "/clients/" + patchedClient.getId();
    ResponseEntity<ClientDetail> deletedEntity = this.restTemplate
        .exchange(deleteurl, HttpMethod.DELETE, null, ClientDetail.class);

    assertEquals(HttpStatus.OK, deletedEntity.getStatusCode());
    testVectorizer.delete();
  }

  @Test
  public void createAndPatchClient() {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    String accountId =
        methodName + "Ac" + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    String appId = methodName + "App" + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    Client client = ModelUtils.getTestClient(name, name, appId, accountId, true);

    VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
    testVectorizer.create();

    ResponseEntity<ClientDetail> createEntity = this.restTemplate
        .postForEntity(basePath + "/clients", client,
            ClientDetail.class);
    assertEquals(HttpStatus.CREATED, createEntity.getStatusCode());

    ClientDetail createdClient = createEntity.getBody();
    testVectorizer.delete();

  }


  @Test
  public void testListClients() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO clientVertical = ModelUtils.getTestVerticalClientObject(name);
    clientVertical.create();

    ResponseEntity<ClientsDetail> entity =
        this.restTemplate.getForEntity(basePath + "/clients", ClientsDetail.class);
    ClientsDetail clients = entity.getBody();
    assertTrue(clients.size() >= 2);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    client.delete();
    clientVertical.delete();
  }

  @Test
  public void testListNonVerticalClients() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String verticalName = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO clientVertical = ModelUtils.getTestVerticalClientObject(verticalName);
    clientVertical.create();

    ResponseEntity<ClientsDetail> entity =
        this.restTemplate
            .getForEntity(basePath + "/clients?showVerticals=false", ClientsDetail.class);
    ClientsDetail clients = entity.getBody();
    assertTrue(clients.size() >= 1);
    assertEquals(HttpStatus.OK, entity.getStatusCode());

    boolean found = false;
    int id = client.getId();
    for (ClientDetail returnedClient : clients) {
      //
      // only has records with 'isVertical' == false
      //
      assertEquals(returnedClient.getIsVertical(), false);
      if (id == Integer.valueOf(returnedClient.getId())) {
        assertEquals(returnedClient.getName(), name);
        found = true;
        break;
      }
    }
    if (!found &&
        clients.size() < 100) {
      //
      // the API has a pagination size. Depends on the test run, the newly created record might
      // not be in the returned list.
      // Somehow, the "X-Total-Count" doesn't reflect the total counts. Otherwise, we can check if
      // it is greater than the 'clients.size()'
      //
      fail("Not find the non-vertical client");
    }
    client.delete();
    clientVertical.delete();
  }

  @Test
  public void testListVerticalClients() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String verticalName = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO clientVertical = ModelUtils.getTestVerticalClientObject(verticalName);
    clientVertical.create();

    ResponseEntity<ClientsDetail> entity = this.restTemplate
        .getForEntity(basePath + "/clients?showVerticals=true", ClientsDetail.class);
    ClientsDetail clients = entity.getBody();
    assertTrue(clients.size() >= 1);
    assertEquals(HttpStatus.OK, entity.getStatusCode());

    boolean found = false;
    int id = clientVertical.getId();
    for (ClientDetail returnedClient : clients) {
      if (id != Integer.valueOf(returnedClient.getId())) {
        continue;
      }
      assertEquals(returnedClient.getIsVertical(), true);
      assertEquals(returnedClient.getName(), verticalName);
      found = true;
      break;
    }
    if (!found) {
      fail("Could not find the vertical client");
    }
    client.delete();
    clientVertical.delete();
  }

//    @Test
//    public void testAddUnicodeClient() {
//        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
//        String name = methodName+ "_" + Long.toString(System.currentTimeMillis() % 10000000);
//        ClientBO client =  ModelUtils.getTestClientObject(name);
//        client.setName("hükan");
//        client.create();
//
//        ResponseEntity<ClientDetail> entity =
//                this.restTemplate.postForEntity(basePath + "/clients", client, ClientDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//        ClientDetail createdClient = entity.getBody();
//        assertNotNull(createdClient);
//        assertEquals(createdClient.getName(), client.getName());
//        assertEquals(createdClient.getDescription(), client.getDescription());
//    }

//    @Test
//    public void testDuplicateClient() {
//
//        Client client1 = new Client();
//        client1.setInternalId("TEST01");
//        client1.setName("company");
//        client1.setDescription("bad company");
//        ResponseEntity<ClientDetail> entity =
//                this.restTemplate.postForEntity(basePath + "/clients", client1, ClientDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//
//        Client client2 = new Client();
//        client2.setInternalId("TEST01");
//        client2.setName("company");
//        client2.setDescription("bad company");
//        ResponseEntity<Error> exceptionEntity =
//                this.restTemplate.postForEntity(basePath + "/clients", client2, Error.class);
//
//        assertEquals(HttpStatus.CONFLICT, exceptionEntity.getStatusCode());
//    }

//    @Test
//    public void testRequiredClientFields() {
//        Client client = new Client();
//        client.setDescription("bad company");
//        client.setAddress("123 fake street");
//
//        ResponseEntity<Error> entity =
//                this.restTemplate.postForEntity(basePath + "/clients", client, Error.class);
//        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
//    }
//


  @Test
  public void testGetClientErrorConditions() {
    ResponseEntity<Error> entity =
        this.restTemplate
            .getForEntity(basePath + "/clients/" + (Integer.MAX_VALUE - 10), Error.class);
    assertNotNull(entity);
    assertThat(entity.getStatusCode(), (equalTo(HttpStatus.NOT_FOUND)));

    ResponseEntity<Error> deletedEntity =
        this.restTemplate
            .getForEntity(basePath + "/clients/" + (Integer.MAX_VALUE - 10), Error.class);
    assertNotNull(deletedEntity);
    assertThat(deletedEntity.getStatusCode(), (equalTo(HttpStatus.NOT_FOUND)));
  }

  @Test
  public void testPaging() {
    String offset = "0";
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    client = ModelUtils.getTestClientObject(name);
    client.create();

    ResponseEntity<ClientsDetail> readEntity =
        this.restTemplate
            .getForEntity(basePath + "/clients?limit=1&startIndex=0", ClientsDetail.class);
    ClientsDetail clients = readEntity.getBody();
    assertEquals(clients.size(), 1);
    assertEquals(HttpStatus.OK, readEntity.getStatusCode());
    //
    // we cannot check this. we no long 'reinit' table anymore
    //
    // assertEquals(clients.get(0).getName(), client1.getName());
    assertEquals(offset, readEntity.getHeaders().getFirst("X-Offset"));
    // assertEquals(totalCount, readEntity.getHeaders().getFirst("X-Total-Count"));

    offset = "1";

    readEntity =
        this.restTemplate.getForEntity(readEntity.getHeaders().getLocation(), ClientsDetail.class);
    clients = readEntity.getBody();
    assertEquals(clients.size(), 1);
    assertEquals(HttpStatus.OK, readEntity.getStatusCode());

    //
    // we cannot check this. we no long 'reinit' table anymore
    //
    // assertEquals(clients.get(0).getName(), client2.getName());
    // assertEquals(totalCount, readEntity.getHeaders().getFirst("X-Total-Count"));

    // Handle error case
    ResponseEntity<Error> read =
        this.restTemplate
            .getForEntity(basePath + "/clients?limit=10&startIndex=" + (Integer.MAX_VALUE - 100),
                Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, read.getStatusCode());
    client.delete();
  }

//    @Test
//    public void testClientEquivalence(){
//        Client client1 = new Client();
//        client1.name("Company Inc.").description("Big Company").address("123 Fake St. Campbell CA");
//
//        Client client2 = new Client();
//        client2.name("Company Inc.").description("Big Company").address("123 Fake St. Campbell CA");
//
//        assert client1.equals(client2);
//
//        client2.name("Company Inc.").description("Big Company").address("123 Fake St. Toronto ON");
//        assert !client1.equals(client2);
//    }
//
//    @Test
//    public void testClientDetailEquivalence(){
//        ClientDetail client1 = new ClientDetail();
//        client1.createdAt(0L).modifiedAt(0L).name("Company Inc.").description("Big Company").address("123 Fake St. Campbell CA");
//
//        ClientDetail client2 = new ClientDetail();
//        client2.createdAt(0L).modifiedAt(0L).name("Company Inc.").description("Big Company").address("123 Fake St. Campbell CA");
//
//        assert client1.equals(client2);
//
//        client2.name("Company Inc.").description("Big Company").address("123 Fake St. Toronto ON");
//
//        assert !client1.equals(client2);
//        assert client1.hashCode() != client2.hashCode();
//
//        assert !client1.equals(null);
//
//        assert client1.equals(client1);
//    }

  @Test
  public void testPrettyPrint() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String stringResponse = this.restTemplate
        .getForObject(basePath + "/clients/" + client.getId(), String.class);
    String prettyStringResponse = this.restTemplate
        .getForObject(basePath + "/clients/" + client.getId() + "?pretty", String.class);
    assertNotEquals(stringResponse, prettyStringResponse);
    assertThat(prettyStringResponse, containsString("\n"));
    client.delete();
  }


}
