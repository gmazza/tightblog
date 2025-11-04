/*
 * Copyright 2024
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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test to verify Flyway configuration and migration setup.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false"
})
public class FlywayConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testFlywayBeanConfiguration() {
        // Verify Flyway bean configuration when disabled
        // Since flyway.enabled=false, Flyway should not auto-run migrations
        boolean hasFlywayBean = applicationContext.containsBean("flyway");
        
        // With spring.flyway.enabled=false, Spring Boot won't create the Flyway bean
        // When Flyway is disabled, the bean won't be created, which is expected
        assertFalse(hasFlywayBean, "Flyway bean should not be created when disabled");
    }
}
