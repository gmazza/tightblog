/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.bloggerui.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tightblog.config.DynamicProperties;
import org.tightblog.service.LuceneIndexer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Walk user through install process.
 */
@Controller
@RequestMapping(path = "/tb-ui/install")
public class InstallerController {

    private static final Logger LOG = LoggerFactory.getLogger(InstallerController.class);

    private final DynamicProperties dynamicProperties;
    private final Environment environment;
    private final LuceneIndexer luceneIndexer;

    @Autowired
    public InstallerController(LuceneIndexer luceneIndexer, DynamicProperties dynamicProperties,
                               Environment environment) {
        this.luceneIndexer = luceneIndexer;
        this.dynamicProperties = dynamicProperties;
        this.environment = environment;
    }

    public enum StartupStatus {
        bootstrapError(true, "installer.bootstrappingError");

        final boolean error;

        final String descriptionKey;

        public boolean isError() {
            return error;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }

        StartupStatus(boolean error, String descriptionKey) {
            this.error = error;
            this.descriptionKey = descriptionKey;
        }
    }

    @RequestMapping(value = "/install")
    public ModelAndView install(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (dynamicProperties.isDatabaseReady()) {
            response.sendRedirect(request.getContextPath() + "/");
            return null;
        }

        return bootstrap(request, response);
    }

    @RequestMapping(value = "/bootstrap")
    public ModelAndView bootstrap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (dynamicProperties.isDatabaseReady()) {
            response.sendRedirect(request.getContextPath() + "/");
            return null;
        }

        Map<String, Object> map = initializeMap();

        try {
            // trigger bootstrapping process
            dynamicProperties.setDatabaseReady(true);
            luceneIndexer.initialize();

            LOG.info("TightBlog Weblogger (Version: {}, Revision {}) startup successful",
                environment.getProperty("weblogger.version", "Unknown"),
                environment.getProperty("weblogger.revision", "Unknown"));

            // get-default-blog endpoint handles first user registration
            response.sendRedirect(request.getContextPath() + "/tb-ui/app/get-default-blog");
            return null;
        } catch (Exception e) {
            LOG.error("Exception", e);
            map.put("rootCauseException", e);
            map.put("rootCauseStackTrace", getRootCauseStackTrace(e));
        }

        map.put("status", StartupStatus.bootstrapError);
        return new ModelAndView("standard", map);
    }

    private Map<String, Object> initializeMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("pageToUse", "install.jsp");
        map.put("pageTitleKey", "install.pageTitle");
        return map;
    }

    private String getRootCauseStackTrace(Throwable rootCauseException) {
        String stackTrace = "";
        if (rootCauseException != null) {
            StringWriter sw = new StringWriter();
            rootCauseException.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString().trim();
        }
        return stackTrace;
    }

}
