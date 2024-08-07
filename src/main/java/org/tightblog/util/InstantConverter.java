/*
 * Copyright 2016 the original author or authors.
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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneOffset;

@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, LocalDateTime> {

    @Override
    public LocalDateTime convertToDatabaseColumn(Instant attribute) {
        return attribute == null ? null : LocalDateTime.from(attribute.atOffset(ZoneOffset.UTC));
    }

    @Override
    public Instant convertToEntityAttribute(LocalDateTime dbData) {
        return dbData == null ? null : dbData.toInstant(ZoneOffset.UTC);
    }
}
