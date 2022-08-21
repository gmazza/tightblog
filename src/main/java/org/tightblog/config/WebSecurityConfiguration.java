/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.tightblog.security.CsrfSecurityRequestMatcher;
import org.tightblog.security.CustomAuthenticationSuccessHandler;
import org.tightblog.security.CustomWebAuthenticationDetailsSource;
import org.tightblog.security.MultiFactorAuthenticationProvider;

import java.util.LinkedHashMap;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final MultiFactorAuthenticationProvider multiFactorAuthenticationProvider;
    private final CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    public WebSecurityConfiguration(CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource,
                                    CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                                    MultiFactorAuthenticationProvider multiFactorAuthenticationProvider) {
        this.customWebAuthenticationDetailsSource = customWebAuthenticationDetailsSource;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.multiFactorAuthenticationProvider = multiFactorAuthenticationProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(multiFactorAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Required authorities listed are defined in class GlobalRole.
        http.authorizeRequests()
                // API Calls
                .antMatchers("/tb-ui/admin/**").hasAuthority("ADMIN")
                .antMatchers("/tb-ui/authoring/**").hasAnyAuthority("ADMIN", "BLOGCREATOR", "BLOGGER")
                // UI Calls
                .antMatchers("/tb-ui2/**").hasAuthority("ADMIN") // needed to trigger login
                .antMatchers("/tb-ui/app/login-redirect")
                    .permitAll() // hasAnyAuthority("ADMIN", "BLOGCREATOR", "BLOGGER", "MISSING_MFA_SECRET")
                .antMatchers("/tb-ui/app/scanCode").hasAuthority("MISSING_MFA_SECRET")
                // All remaining, everyone can see
                .anyRequest().permitAll()
                .and()
            .formLogin()
                .loginPage("/tb-ui/app/login")
                .failureForwardUrl("/tb-ui/app/login?error=true")
                .authenticationDetailsSource(customWebAuthenticationDetailsSource)
                .loginProcessingUrl("/tb_j_security_check")
                .successHandler(customAuthenticationSuccessHandler)
                .and()
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .requireCsrfProtectionMatcher(csrfSecurityRequestMatcher())
                .and()
            // if unauthorized, go to delegatingEntryPoint to determine login-redirect or 401 status code.
            .exceptionHandling().authenticationEntryPoint(delegatingEntryPoint());
    }

    @Bean
    public AuthenticationEntryPoint delegatingEntryPoint() {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> map = new LinkedHashMap<>();
        // UI endpoints (also with defaultEntryPoint below), return login form if unauthorized
        map.put(new AntPathRequestMatcher("/"), new LoginUrlAuthenticationEntryPoint("/tb-ui/app/login"));
//        map.put(new AntPathRequestMatcher("/"), new LoginUrlAuthenticationEntryPoint("/tb-ui2/#/app/login2/"));
        // REST endpoints, want to return 401 if unauthorized
        map.put(new AntPathRequestMatcher("/tb-ui/admin/**"), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        map.put(new AntPathRequestMatcher("/tb-ui/authoring/**"), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        final DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(map);
        entryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/tb-ui/app/login"));
//      entryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/tb-ui2/#/app/login2/"));
        return entryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CsrfSecurityRequestMatcher csrfSecurityRequestMatcher() {
        return new CsrfSecurityRequestMatcher();
    }

}
