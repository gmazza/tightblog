package org.tightblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@PropertySources({
    @PropertySource(value = "classpath:tightblog.properties"),
    @PropertySource(value = "classpath:tightblog-custom.properties", ignoreResourceNotFound = true)
})
// https://stackoverflow.com/a/32087621
@EnableWebSecurity
public class TightblogApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TightblogApplication.class, args);
    }

}
