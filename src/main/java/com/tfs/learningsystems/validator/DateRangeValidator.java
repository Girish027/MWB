/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.validator;

import com.tfs.learningsystems.annotations.ValidDateRange;
import com.tfs.learningsystems.ui.model.DateRange;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

  @Override
  public void initialize(ValidDateRange constraintAnnotation) {
    // do nothing
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    if (object == null) {
      return false;
    }
    DateRange dateRange = (DateRange) object;
    Long startDate = dateRange.getStartDate();
    Long endDate = dateRange.getEndDate();

    if (startDate != null && startDate < 0) {
      log.error("Invalid date range: startDate less than 0");
      return false;
    }

    if (endDate != null && endDate < 0) {
      log.error("Invalid date range: endDate less than 0");
      return false;
    }

    if (startDate != null && endDate != null && startDate > endDate) {
      log.error("Invalid date range: startDate later than endDate");
      return false;
    }

    return true;
  }
}
