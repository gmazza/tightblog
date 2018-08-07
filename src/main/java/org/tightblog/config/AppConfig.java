package org.tightblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ImportResource({ "classpath:spring-beans.xml", "classpath*:tightblog-custom.xml" })
public class AppConfig {

    @Bean
    ThreadPoolTaskScheduler blogTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(6);
        return scheduler;
    }

}
