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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.tightblog.security.CsrfSecurityRequestMatcher;
import org.tightblog.security.CustomAuthenticationSuccessHandler;
import org.tightblog.security.CustomWebAuthenticationDetailsSource;
import org.tightblog.security.MultiFactorAuthenticationProvider;

import java.util.LinkedHashMap;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private final MultiFactorAuthenticationProvider multiFactorAuthenticationProvider;
    private final CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final PathPatternParser parser = new PathPatternParser();

    @Autowired
    public WebSecurityConfiguration(CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource,
                                    CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                                    MultiFactorAuthenticationProvider multiFactorAuthenticationProvider) {
        this.customWebAuthenticationDetailsSource = customWebAuthenticationDetailsSource;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.multiFactorAuthenticationProvider = multiFactorAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(multiFactorAuthenticationProvider);
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Required authorities listed are defined in class GlobalRole.
        http.authorizeHttpRequests(auth -> auth
                // API Calls
                .requestMatchers("/tb-ui/register/**").permitAll()
                .requestMatchers("/tb-ui/install/**").permitAll()
                .requestMatchers("/tb-ui/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/tb-ui/authoring/**").hasAnyAuthority("ADMIN", "BLOGCREATOR", "BLOGGER")
                .requestMatchers("/tb-ui/newuser/**").hasAnyAuthority("MISSING_MFA_SECRET")
                .requestMatchers("/tb-ui/app/login-redirect")
                        .hasAnyAuthority("ADMIN", "BLOGCREATOR", "BLOGGER", "MISSING_MFA_SECRET")
                .requestMatchers("/tb-ui/app/unsubscribeNotifications").permitAll()
                // UI Calls
                .requestMatchers("/tb-ui/app/register").permitAll()
                .requestMatchers("/tb-ui/app/login").permitAll()
                .requestMatchers("/tb-ui/app/relogin").permitAll()
                .requestMatchers("/tb-ui/app/logout").permitAll()
                .requestMatchers("/tb-ui/styles/*.css").permitAll()
                .requestMatchers("/tb-ui/*.ico").permitAll()
                .requestMatchers("/tb-ui/assets/**").permitAll()
                // .requestMatchers("/styles/*.css").permitAll()
                // below are UI calls that require authentication
                .requestMatchers("/tb-ui/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/tb-ui/app/createWeblog").hasAnyAuthority("ADMIN", "BLOGCREATOR")
                .requestMatchers("/tb-ui/app/scanCode").hasAnyAuthority("MISSING_MFA_SECRET")
                .requestMatchers("/tb-ui/app/any/sessioninfo").hasAnyAuthority("ADMIN",
                        "BLOGCREATOR", "BLOGGER", "MISSING_MFA_SECRET")
                .requestMatchers("/tb-ui/app/authoring/startupconfig").hasAnyAuthority("ADMIN",
                        "BLOGCREATOR", "BLOGGER", "MISSING_MFA_SECRET")
                .requestMatchers("/tb-ui/app/**").hasAnyAuthority("ADMIN", "BLOGCREATOR", "BLOGGER")
                .requestMatchers("/tb-ui/**").hasAnyAuthority("ADMIN", "BLOGCREATOR", "BLOGGER") // default (= myBlogs on UI)
                // All remaining, everyone can see
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/tb-ui/app/login")
                .failureForwardUrl("/tb-ui/app/login?error=true")
                .authenticationDetailsSource(customWebAuthenticationDetailsSource)
                .loginProcessingUrl("/tb_j_security_check")
                .successHandler(customAuthenticationSuccessHandler)
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .requireCsrfProtectionMatcher(csrfSecurityRequestMatcher())
            )
            // if unauthorized, go to delegatingEntryPoint to determine login-redirect or 401 status code.
            .exceptionHandling(exceptions ->
                    exceptions.authenticationEntryPoint(delegatingEntryPoint())
            );
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint delegatingEntryPoint() {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> map = new LinkedHashMap<>();
        // UI endpoints (also with defaultEntryPoint below), return login form if unauthorized
        map.put(createPathPatternRequestMatcher("/"), new LoginUrlAuthenticationEntryPoint("/tb-ui/app/login"));
        // REST endpoints, want to return 401 if unauthorized
        map.put(createPathPatternRequestMatcher("/tb-ui/admin/**"), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        map.put(createPathPatternRequestMatcher("/tb-ui/authoring/**"), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        final DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(map);
        entryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/tb-ui/app/login"));
        return entryPoint;
    }

    private PathPatternRequestMatcher createPathPatternRequestMatcher(String pattern) {
        return PathPatternRequestMatcher.withPathPatternParser(parser).matcher(pattern);
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
