/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.ValidationError;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Provider
@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  @Autowired
  Environment env;

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    ValidationError error = new ValidationError();
    error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
    error.setErrorCode("invalid_field");
    error.setMessage("Could not process request");

    List<ValidationError.FieldError> errors = new ArrayList<>();
    for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
      ValidationError.FieldError fieldError = new ValidationError.FieldError();
      fieldError.setField(constraintViolation.getPropertyPath().toString());
      fieldError.setError(constraintViolation.getMessage());
      errors.add(fieldError);
    }
    error.setErrors(errors);

    if (!env.acceptsProfiles("test")) {
      log.error(error.toString(), exception);
    }

    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }

}

