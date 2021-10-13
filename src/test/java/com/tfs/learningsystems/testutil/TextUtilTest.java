/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tfs.learningsystems.util.TextUtil;
import org.junit.Test;

public class TextUtilTest {

  @Test
  public void testToCanonical() {
    assertEquals("green.goblib", TextUtil.toCanonical("Green.Goblib"));
    assertEquals(null, TextUtil.toCanonical(null));
    assertEquals("", TextUtil.toCanonical(""));
  }

  @Test
  public void testIsIntentValid() {
    assertFalse(TextUtil.isIntentValid(""));
    assertFalse(TextUtil.isIntentValid(null));
    assertTrue(TextUtil.isIntentValid("password-reset"));
    assertTrue(TextUtil.isIntentValid("res-match"));
    assertTrue(TextUtil.isIntentValid("ın23-test"));
    assertTrue(TextUtil.isIntentValid("ın-test"));
    assertTrue(TextUtil.isIntentValid("漢字-test"));
    assertFalse(TextUtil.isIntentValid("漢字"));
    assertFalse(TextUtil.isIntentValid("reservation-"));
    assertFalse(TextUtil.isIntentValid("-make"));
    assertTrue(TextUtil.isIntentValid("côte-côte"));
    assertFalse(TextUtil.isIntentValid("ABCDBDNFHJFFLSKASSDDDSFDKFGFGERGERGERGERGERGERGERGERGERGERGERGERGREGERGERGERGERGERGEGERGRGERGERGERGEGEGEGERGREGERREG"));
  }
}