package com.tfs.learningsystems.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.tfs.learningsystems.ui.model.Error;
import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ErrorUtilTest {

    @Test
    public void createErrorWithBadRequest() {
        int httpCode = 400;
        String errorCode = "invalid_modules";
        String message = "Cannot publish invalid models.";
        Error errorMessage = ErrorUtil.createError(httpCode, errorCode, message);
        assertEquals("class Error {\n" +
                "    code: 400\n" +
                "    errorCode: invalid_modules\n" +
                "    message: Cannot publish invalid models.\n" +
                "}", errorMessage.toString());
    }

    @Test
    public void createErrorWithUnAuthorized() {
        int httpCode = 401;
        String errorCode = "UNAUTHORIZED";
        String message = "Not authorized to access model.";
        Error errorMessage = ErrorUtil.createError(httpCode, errorCode, message);
        assertEquals("class Error {\n" +
                "    code: 401\n" +
                "    errorCode: UNAUTHORIZED\n" +
                "    message: Not authorized to access model.\n" +
                "}", errorMessage.toString());
    }

    @Test
    public void notFoundError() {
        int httpCode = 404;
        String missingItem = "model";
        String id = "1";
        Error errorMessage = ErrorUtil.notFoundError(httpCode, missingItem, id);
        assertEquals("class Error {\n" +
                "    code: 404\n" +
                "    errorCode: model_not_found\n" +
                "    message: model '1' not found\n" +
                "}", errorMessage.toString());
    }
}