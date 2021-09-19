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

import org.jsoup.safety.Safelist;

public final class HTMLSanitizer {

    private static final Safelist NONE_SAFELIST = Safelist.none();

    private static final Safelist LIMITED_SAFELIST = Safelist.simpleText().addTags("br", "p");

    private static final Safelist BASIC_SAFELIST = Safelist.basic();

    private static final Safelist BASIC_IMAGES_SAFELIST = Safelist.basicWithImages();

    private static final Safelist RELAXED_SAFELIST = Safelist.relaxed();

    private static final Safelist RELAXED_IFRAMES_SAFELIST = Safelist.relaxed()
            .addTags("iframe")
            .addAttributes("iframe", "width", "height", "src", "style", "allowfullscreen")
            .addProtocols("iframe", "src", "http", "https");

    public enum Level {
        NONE(0, "globalConfig.htmlsanitizer.none", NONE_SAFELIST),
        LIMITED(1, "globalConfig.htmlsanitizer.limited", LIMITED_SAFELIST),
        BASIC(2, "globalConfig.htmlsanitizer.basic", BASIC_SAFELIST),
        BASIC_IMAGES(3, "globalConfig.htmlsanitizer.basicimages", BASIC_IMAGES_SAFELIST),
        RELAXED(4, "globalConfig.htmlsanitizer.relaxed", RELAXED_SAFELIST),
        RELAXED_IFRAMES(5, "globalConfig.htmlsanitizer.relaxediframes", RELAXED_IFRAMES_SAFELIST),
        OFF(6, "globalConfig.htmlsanitizer.off", null);

        private String description;

        private int sanitizingLevel;

        private Safelist safelist;

        Level(int sanitizingLevel, String description, Safelist safelist) {
            this.sanitizingLevel = sanitizingLevel;
            this.description = description;
            this.safelist = safelist;
        }

        public String getDescription() {
            return description;
        }

        public int getSanitizingLevel() {
            return sanitizingLevel;
        }

        public Safelist getSafelist() {
            return safelist;
        }
    }

}
