/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.ssl.HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
@Slf4j
public class HttpConfig {

  public static final HostnameVerifier nullHostnameVerifier = new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return hostname != null;
    }

    @Override
    public void check(String host, SSLSocket ssl) throws IOException {
    }

    @Override
    public void check(String host, X509Certificate cert) throws SSLException {
    }

    @Override
    public void check(String host, String[] cns, String[] subjectAlts)
        throws SSLException {
    }

    @Override
    public void check(String[] hosts, SSLSocket ssl) throws IOException {
    }

    @Override
    public void check(String[] hosts, X509Certificate cert) throws SSLException {
    }

    @Override
    public void check(String[] hosts, String[] cns, String[] subjectAlts)
        throws SSLException {
    }
  };

  public static SSLContext createContext() {
    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      @Override
      public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
          String authType) throws CertificateException  {
      }

      @Override
      public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
          String authType) throws CertificateException {
      }
    }};

    try {
      SSLContext sc = SSLContext.getInstance("TLSv1.2");
      sc.init(null, trustAllCerts, null);
      SSLContext.setDefault(sc);
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier);
      return sc;

    } catch (Exception e) {
      log.error("Exception while creating context {}", e);
    }
    return null;
  }

  public static HttpComponentsClientHttpRequestFactory createSecureTransport() {
    CloseableHttpClient client =
        HttpClientBuilder.create().setSSLHostnameVerifier(nullHostnameVerifier)
            .setSSLContext(createContext()).build();
    return new HttpComponentsClientHttpRequestFactory(client);
  }

}
