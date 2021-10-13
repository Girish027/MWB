package com.tfs.learningsystems.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Slf4j
public class DbIdTest {

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
  public void testGeneralTest() {
    DbId id;

    id = new DbId("");
    assertFalse(DbId.isValid(id));

    id = new DbId("00000000000000000000");
    assertFalse(DbId.isValid(id));
    assertTrue(DbId.isEmpty(id));

    id = DbId.GenerateId(new ModelBO(), 3);
    assertFalse(DbId.isEmpty(id));
    assertTrue(DbId.isValid(id));
    assertEquals((new ModelBO()).getDbPrefix(), DbId.GetKeyPrefix(id));

  }

  @Test
  public void testCaseSafe() {
    DbId id;

    id = new DbId("008GGXXXXXYYYYYZZZZ1");
    assertEquals("008GGXXXXXYYYYYZZZZ_1", DbId.GetCaseSafeString(id));

    id = new DbId("008GGXXXXXYYYYYZZZZa");
    assertEquals("008GGXXXXXYYYYYZZZZ_a", DbId.GetCaseSafeString(id));

    id = new DbId("008GGaXXXXXYYYYYZZZZ");
    assertEquals("008GG_aXXXXXYYYYYZZZZ", DbId.GetCaseSafeString(id));

    id = new DbId("008GGXXXXXaaaaaZZZZZ");
    assertEquals("008GGXXXXX_aaaaaZZZZZ", DbId.GetCaseSafeString(id));
  }

  @Test
  public void testCalculateSequence() {
    DbId id;

    id = new DbId("008AAXXXXXYYYYYZZZZ3");
    assertEquals(3L, id.calculateSequence());

    id = new DbId("008GGXXXXXYYYYYZZZZa");
    assertEquals(10L, id.calculateSequence());
  }

}
