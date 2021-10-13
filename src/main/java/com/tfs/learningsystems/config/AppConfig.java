/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tfs")
public class AppConfig {



  @Value("${tfs.rest.url-prefix}")
  private String restUrlPrefix;

  @Value("${tfs.app.file-upload-repo-root}")
  private String fileUploadRepositoryRoot;

  @Value("${tfs.app.shared-repo-root}")
  private String sharedRepositoryRoot;

  @Value("${tfs.app.resource-file-location}")
  private String resourceFileLocation;

  @Value("${tfs.app.file-poll-refresh-interval-ms}")
  private int filePollRefreshInterval;

  @Value("${tfs.logstash.exec-timeout}")
  private long logstashExecTimeout;

  @Value("${tfs.logstash.check-exec-timeout}")
  private long checkLogstashExecTimeout;

  @Value("${tfs.csv.original-transcription-col-name}")
  private String csvOriginalTranscriptionColumnName;

  @Value("${tfs.csv.normalized-transcription-col-name}")
  private String csvNormalizedTranscriptionColumnName;

  @Value("${tfs.csv.transcription-entity-col-name}")
  private String csvTranscriptionEntityColumnName;

  @Value("${tfs.auto-categorizer.categorize.url}")
  private String autoCategorizerUrl;

  @Value("${tfs.auto-categorizer.categorize.columns}")
  private String autoCategorizerColumns;

  @Value("${tfs.okta.url}")
  private String oktaURL;

  @Value("${tfs.auth.cookie-max-age}")
  private int cookieMaxAge;

  @Value("${spring.categorizerConnectionRequestTimeout}")
  private int categorizerConnectionRequestTimeout;

  @Value("${spring.categorizerConnectTimeout}")
  private int categorizerConnectTimeout;

  @Value("${spring.categorizerReadTimeout}")
  private int categorizerReadTimeout;

  @Value("${server.session.cookie.domain}")
  private String serverDomain;

  @Value("${tfs.orion.url}")
  private String orionURL;

  @Value("${tfs.model.config.defualt.english.archive.old}")
  private String oldEnglishConfigArchiveFilename;

  @Value("${tfs.model.config.defualt.english.archive}")
  private String englishConfigArchiveFilename;

  @Value("${tfs.kibana.url}")
  private String kibanaURL;

  @Value("${tfs.web2nl.url}")
  private String web2nlUrl;

  @Value("${tfs.web2nl.api.key}")
  private String web2nlApiKey;

  @Value("${tfs.webreco.url}")
  private String webrecoUrl;

  @Value("${tfs.webreco.client}")
  private String webrecoClient;

  @Value("${tfs.webreco.clientSecret}")
  private String webrecoClientSecret;

  @Value("${tfs.redis.host}")
  private String redisHost;

  @Value("${tfs.redis.port}")
  private int redisPort;

  @Value("${tfs.is.test}")
  private Boolean isTest;

  @Value("${tfs.its.url}")
  private String itsURL;

  @Value("${tfs.its.api.url}")
  private String itsApiURL;

  @Value("${tfs.ufp.url}")
  private String ufpURL;

  @Value("${tfs.kibana.log.url}")
  private String kibanaLogURL;

  @Value("${tfs.kibana.log.index}")
  private String kibanaLogIndex;

  @Value("${tfs.test.timeout}")
  private long testCaseTimeout;

  @Value("${tfs.external.user.denied.resources}")
  private String externalDeniedResources;

  @Value("${tfs.test.longtimeout}")
  private long testCaseLongerTimeout;

  @Value("${okta.oauth2.clientSecret}")
  private String clientSecret;

  @Value("${okta.oauth2.clientId}")
  private String oauth2ClientId;

  @Value("${okta.oauth2.scope}")
  private String scope;

  @Value("${okta.oauth2.localTokenValidation}")
  private String localTokenValidation;

  @Value("${tfs.test.intEnv}")
  private String intEnv;

  @Value("${tfs.test.intAccessToken}")
  private String intAccessToken;

  @Value("${tfs.environment}")
  private String environment;
  @Value("${tfs.test.intPollingCount}")
  private int intPollingCount;

  @Value("${okta.oauth2.logoutURL}")
  private String oAuthLogoutURL;

  @Value("${tfs.external.api.retry.attempts}")
  private int externalApiRetryCount;

  @Value("${tfs.external.api.retry.wait.time.ms}")
  private long externalApiRetryWaitTime;

  @Value("${tfs.user.sessions.max}")
  private int maxPerUserSessions;

  @Value("${tfs.user.max.inactive.interval}")
  private long maxPerUserMaxInactiveInterval;

  @Value("${tfs.github.base.url}")
  private String gitHubBaseURL;

  @Value("${tfs.github.mwb.org}")
  private String gitHubOrg;

  @Value("${tfs.github.mwb.org.user.name}")
  private String gitHubUserName;

  @Value("${tfs.github.mwb.org.user.token}")
  private String gitHubUserToken;

  @Value("${tfs.github.mwb.org.retry.timeout}")
  private long gitHubRetryTimeout;

  @Value("${tfs.github.mwb.org.retry.count}")
  private int gitHubRetryCount;

  @Value("${tfs.github.server}")
  private String gitHubServer;

  @Value("${tfs.mwb.user.name}")
  private String mwbServiceACUserName;

  @Value("${tfs.mwb.user.pwd}")
  private String mwbServiceACUserPwd;

  @Value("${tfs.github.mwb.content.deployment.url}")
  private String deploy2APIURl;

  @Value("${amplitude.api.key}")
  private String amplitudeApiKey;

  @Value("${tfs.apigee.api.access.key}")
  private String apigeeApiAccessKey;

  @Value("${tfs.ui.logout.warning.timeout}")
  private long logoutWarningTimeout;

  @Value("${tfs.ui.logout.timeout}")
  private long logoutTimeout;

  @Value("${tfs.ui.speech.test.timeout}")
  private long speechTestTimeout;

  @Value("${tfs.okta.user.account.url}")
  private String userAccountLink;

  @Value("${tfs.mwb.internal.support.url}")
  private String internalSupportLink;

  @Value("${tfs.mwb.external.support.url}")
  private String externalSupportLink;

  @Value("${tfs.mwb.documentation.url}")
  private String documentationLink;

  @Value("${tfs.model.build.timeout}")
  private String modelBuildTimeout;

  @Value("${defaultModelConfig}")
  private Boolean defModelConfig;

  public final Map<String, Object> toPropertiesMap() {
    Map<String, Object> hashMap = new HashMap<String, Object>();
    try {
      Class<? extends Object> c = this.getClass();
      Method m[] = c.getMethods();
      for (int i = 0; i < m.length; i++) {
        if (m[i].getName().indexOf("get") == 0) {
          String name = m[i].getName().toLowerCase().substring(3, 4) + m[i].getName().substring(4);
          hashMap.put(name, m[i].invoke(this, new Object[0]));
        }
      }
    } catch (Throwable e) {
      throw new RuntimeException("Failed while parsing application configuration property file", e);
    }
    return hashMap;
  }

}
