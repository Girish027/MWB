/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.validator;

import static org.springframework.util.Assert.hasText;

import com.tfs.learningsystems.annotations.NotEmptyObject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NonEmptyObjectValidator implements ConstraintValidator<NotEmptyObject, Object> {

  @Override
  public void initialize(NotEmptyObject constraintAnnotation) {
    // do nothing
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    if (object == null) {
      return false;
    }
    try {
      hasText(object.toString());
    } catch (IllegalArgumentException e) {
      log.error("Empty value for object {}", e);
      return false;
    }
    return true;
  }
}
