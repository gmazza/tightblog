package org.tightblog.config;

import tools.jackson.datatype.hibernate7.Hibernate7Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Hibernate7Module hibernate7Module() {
        return new Hibernate7Module();
    }
}
