/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.validator;

import static org.springframework.util.Assert.hasText;

import com.tfs.learningsystems.annotations.ValidDataType;
import com.tfs.learningsystems.ui.model.DataType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataTypeValidator implements ConstraintValidator<ValidDataType, Object> {

  @Override
  public void initialize(ValidDataType constraintAnnotation) {
    // do nothing
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    if (object == null) {
      return false;
    }
    try {
      hasText(object.toString());
      DataType dt = (DataType) object;
      DataType.valueOf(dt.getName());
    } catch (IllegalArgumentException e) {
      log.error("Empty value for object {}", e);
      return false;
    }
    return true;
  }
}
