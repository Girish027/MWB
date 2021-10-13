package com.tfs.learningsystems.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@Slf4j
public class RepositoryUtilTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private RepositoryUtil repositoryUtil;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(
        "START EXECUTING TEST CLASS [" + Thread.currentThread().getStackTrace()[1].getClassName()
            + "]");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    log.info("END EXECUTING TEST CLASS [" + Thread.currentThread().getStackTrace()[1].getClassName()
        + "]");
  }

  @Test
  public void sanitizationTest() {
    String testStr1 = "hükan's dataset";
    String expStr1 = "hükan&#39;s dataset";

    String testStr2 = "www <href a=\"xyz\"/> <javascript>var b=0;</javascript>";
    String expStr2 = "www  var b&#61;0;";

    String testStr3 = "bb $(document).ready(function() {echo 'aa'}";
    String expStr3 = "bb $(document).ready(function() {echo &#39;aa&#39;}";

    DatasetBO dataset = new DatasetBO();
    dataset.setName(testStr1);
    dataset.setDescription(testStr2);

    BusinessObject.sanitize(dataset);
    assertEquals(expStr1, dataset.getName());
    assertEquals(expStr2, dataset.getDescription());

    dataset = new DatasetBO();
    dataset.setName(testStr3);

    BusinessObject.sanitize(dataset);
    assertEquals(expStr3, dataset.getName());

  }

  @Test
  public void prefixTest() {
    try {
      this.repositoryUtil.setApplicationContext(this.applicationContext);
      this.repositoryUtil.afterPropertiesSet();
    } catch (Exception e) {
      fail("Failed to initialize RepositoryUtil - " + e.getMessage());
    }
    // every BO should be registered in both map
    assertEquals(RepositoryUtil.repositoryMap.size(), RepositoryUtil.specExecutorMap.size());

    Set<String> prefixes = new HashSet<>();
    for (Class boCls : RepositoryUtil.repositoryMap.keySet()) {
      try {
        BusinessObject boInstance = (BusinessObject) boCls.newInstance();
        String prefix = boInstance.getDbPrefix();

        // each BO should define a prefix
        assertFalse(StringUtils.isEmpty(prefix));

        // each prefix should be 3 letters, required be DbId
        assertEquals(DbId.TYPE_SIZE, prefix.length());

        // no duplication
        assertFalse(prefixes.contains(prefix));

        prefixes.add(prefix);
      } catch (Exception e) {
        fail("Failed to exam BO in RepositoryUtil - " + e.getMessage());
      }
    }

  }

  @Test
  public void crudClientTest() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = new ClientBO();
    client.setName(name);
    client.setState(ClientBO.State.DISABLED);
    client.create();


    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();
    mwbItsClientMapBO.setId(client.getId());
    mwbItsClientMapBO.setItsAccountId("defaultac");
    mwbItsClientMapBO.setItsAppId("default");
    mwbItsClientMapBO.setItsClientId(name);
    mwbItsClientMapBO.create();

    Integer id = client.getId();
    assertNotNull("Should be able to create Client - " + name, id);

    mwbItsClientMapBO.findOne(client.getId().toString());

    assertNotNull("Should be able to create ITS Client - " + name, id);

    client = client.findOne(id.toString());
    assertNotNull("Should be able to retrieve Client - " + id, client);
    assertTrue("This client should be created with state to 'DISABLED' - " + id,
        client.isDisabled());

    client.setState(ClientBO.State.ENABLED);
    client.update();
    client = client.findOne(id.toString());
    assertNotNull("Should be able to retrieve Client after update - " + id, client);
    assertFalse("This client should be updated with state to 'ENABLED' - " + id,
        client.isDisabled());

    try {
      ClientBO clientDup = new ClientBO();
      clientDup.setName(name);
      clientDup.create();
      fail("Should not be able create duplicate client");
    } catch (Exception e) {
      // e.printStackTrace();
    }
  }

  @Test
  public void createBOTest() {
    try {
      this.repositoryUtil.setApplicationContext(this.applicationContext);
      this.repositoryUtil.afterPropertiesSet();
    } catch (Exception e) {
      fail("Failed to initialize RepositoryUtil - " + e.getMessage());
    }
    // every BO should be registered in both map
    assertEquals(RepositoryUtil.repositoryMap.size(), RepositoryUtil.specExecutorMap.size());

    Set<String> prefixes = new HashSet<>();
    for (Class boCls : RepositoryUtil.repositoryMap.keySet()) {
      BusinessObject boInstance = null;
      try {
        boInstance = (BusinessObject) boCls.newInstance();
        fillObject(boInstance);

        if(boInstance instanceof MwbItsClientMapBO) {
          ((MwbItsClientMapBO) boInstance).setId(1);
        }

        if(boInstance instanceof MetricsBO) {
          ((MetricsBO) boInstance).setClientId(1);
        }
        boInstance.create();

        if (!boInstance.hasId()) {
          continue;
        }
        Object id = boInstance.getObjId();
        assertTrue("failed to create - " + boCls.getName(), id != null);
        boInstance = boInstance.findOne(id.toString());
        assertNotNull("Couldn't locate " + boCls.getName() + " after creation with id - " + id,
            boInstance);

        if (boInstance.hasDbId()) {
          String dbIdStr = boInstance.getDbIdValue();
          assertTrue(DbId.isValid(dbIdStr));

          String prefix = boInstance.getDbPrefix();
          assertTrue(dbIdStr.startsWith(prefix));
        }

      } catch (Exception e) {
        fail("Failed to create BO in RepositoryUtil - " + boCls.getName() + " - " + e.getMessage());
      }
    }

  }

  private void fillObject(Object obj) {
    Class<? extends Object> clazz = obj.getClass();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);

      if (BusinessObject.OBJ_ID.equals(field.getName()) ||
          BusinessObject.DB_ID.equals(field.getName()) ||
          BusinessObject.CLIENT_CID.equals(field.getName()) ||
          BusinessObject.MODIFIED_AT.equals(field.getName()) ||
          BusinessObject.MODIFIED_BY.equals(field.getName())) {
        continue;
      }
      if (!Modifier.isProtected(field.getModifiers())) {
        // our BO use protected for all its fields. We only care about them.
        continue;
      }

      if (Modifier.isFinal(field.getModifiers())) {
        continue;
      }

      try {
        if (field.get(obj) != null) {
          // already have default value
          continue;
        }
        Object val = generateRandomValue(field.getType());
        if (val == null) {
          // some fields are hard to fill. But they might have default value that we don't want to override
          continue;
        }
        field.set(obj, val);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private Object generateRandomValue(Class<?> fieldType) {
    if (fieldType.isAssignableFrom(String.class)) {
      // todo: need to be smarter about how long the string can be
      return (UUID.randomUUID().toString().substring(0, 15));
    } else if (fieldType.isAssignableFrom(Date.class)) {
      return new Date(System.currentTimeMillis());
    } else if (fieldType.isAssignableFrom(Long.class)) {
      return (ThreadLocalRandom.current().nextLong(1, 10000 + 1));
    } else if (fieldType.isAssignableFrom(Integer.class)) {
      return (ThreadLocalRandom.current().nextInt(1, 10000 + 1));
    } else {
      // don't care;
    }
    return (null);
  }


}
