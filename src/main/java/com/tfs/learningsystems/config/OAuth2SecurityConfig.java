package com.tfs.learningsystems.config;

import com.tfs.learningsystems.auth.filter.AuthValidationFilter;
import com.tfs.learningsystems.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableOAuth2Sso
@Configuration
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private AuthValidationFilter authValidationFilter;


  @Override
  public void configure(HttpSecurity http) throws Exception {

    http.csrf().disable();

    http
        .authorizeRequests()
        .requestMatchers(EndpointRequest.to("health", "flyway")).permitAll()
         .antMatchers("/favicon.ico", "/images/**", "/css/**", "/dist/**", "index.html",
            "/actuator/**", "/error").permitAll()
        .antMatchers("/nltools/private/**").fullyAuthenticated()
        .anyRequest().authenticated()
        .and()
        .logout()
        .deleteCookies("SESSION", Constants.SESSION_CLIENTID_FIELD,
            Constants.SESSION_ITS_CLIENTID_FIELD, Constants.SESSION_ITS_APP_ID_FIELD,
            Constants.SESSION_USERID_FIELD, Constants.SESSION_USERNAME_FIELD, Constants.SESSION_USER_DETAILS, Constants.ITS_GROUP_NAME)
        .invalidateHttpSession(true)
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutSuccessUrl("/");

    http.addFilterBefore(authValidationFilter, FilterSecurityInterceptor.class);

  }


}

