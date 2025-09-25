/*
 * Copyright 2018-2021 the original author or authors.
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
package org.tightblog.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InstantConverterTest {

    @Test
    public void convertToDatabaseColumn() {
        Instant now = Instant.now(Clock.systemUTC());
        LocalDateTime ldt = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));

        InstantConverter converter = new InstantConverter();
        LocalDateTime test = converter.convertToDatabaseColumn(now);
        assertEquals(ldt, test);
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void convertToEntityAttribute() {
        Instant now = Instant.now(Clock.systemUTC());
        LocalDateTime ldt = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));

        InstantConverter converter = new InstantConverter();
        Instant test = converter.convertToEntityAttribute(ldt);
        assertEquals(now, test);
        assertNull(converter.convertToEntityAttribute(null));
    }
}
