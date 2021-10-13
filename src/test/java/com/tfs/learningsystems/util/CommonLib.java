package com.tfs.learningsystems.util;

import java.util.Collections;
import org.springframework.web.client.RestTemplate;

public class CommonLib {

  public static class Auth {

    private static String AUTHHEADER = "Authorization";
    private static String accessToken = "Bearer eyJraWQiOiJGN0hBbXFOQ3Y3eGRmVUVqUDVVZFZVS3dRS1ZtLS1PcktNem1FOFpaT1JJIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIwMHVlNXF4emQ2M0pRUkhWYzBoNyIsIm5hbWUiOiJNYW5hc2kgS2FydmF0IiwiZW1haWwiOiJNYW5hc2kuS2FydmF0QDI0Ny5haSIsInZlciI6MSwiaXNzIjoiaHR0cHM6Ly9zc28tMjQ3LWluYy5va3RhcHJldmlldy5jb20vb2F1dGgyL2F1c2Y1OTV2czF1dThFV0psMGg3IiwiYXVkIjoiMG9hZjU0b21nb3c3NkJxMmcwaDciLCJpYXQiOjE1MzkxMTMzMjUsImV4cCI6MTUzOTExNjkyNSwianRpIjoiSUQucTY1RHhabVl6eldYOTRIb3hBQjVYc1ZiYVZrRnF5cWRqOGFsWWU0bnFZTSIsImFtciI6WyJwd2QiXSwiaWRwIjoiMDBvYng0cWszekQxOW9wSGgwaDciLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJNYW5hc2kuS2FydmF0QDI0Ny5haSIsImF1dGhfdGltZSI6MTUzOTExMzMxNCwiYXRfaGFzaCI6IkRUV1kySHBLQjZKNjY4ZEw3ZlpIMFEifQ.VNkEndr3MO2VVfJfNSDyfZBsRPBAnkkCyskT48P8LDiuCFlfaA96n0acmclWvrpI2BiaLhXDdTrLc8RhZOzSm8zY_KTt_hse_4QIc2ZxOjVbGnC7JHI4rMoqSzdtJ5sDd9C2NOInKB7WVgxKNUv1U-4Ml81y2Zj8xkXFJGiUKTZ1DTdShVMoq_ZtzCqYlY-ym2twKGbQ4BWSmAg9n0wbkqArIFyhHxljhX2ezr3tP5bT8uSOdciHAH0Su8L-KUWCGFTnurv7rKMY9hEJF2UGM0SakP1JrPrHolt4DlF47AtI5AoMBiMGIzzmapsj1BOpWHmkX4C_X3yiccg77vCy0A";

    public static void addAccessToken(RestTemplate rt) {

      rt.setInterceptors(Collections.singletonList((request, body, execution) -> {
        request.getHeaders().add(AUTHHEADER, accessToken);
        return execution.execute(request, body);
      }));

    }

    public static String getAUTHHEADERNAME() {
      return AUTHHEADER;
    }

    public static String getAccessToken() {
      return accessToken;
    }

  }

}
