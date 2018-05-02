/*
   Copyright 2018 the original author or authors.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.generators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.util.Utilities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeblogListGenerator {

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    public List<WeblogData> getHotWeblogs(int sinceDays, int length) {
        List<WeblogData> weblogDataList = new ArrayList<>();
        List<Weblog> weblogs = weblogManager.getHotWeblogs(sinceDays, 0, length);
        for (Weblog weblog : weblogs) {
            weblogDataList.add(weblogToWeblogData(weblog));
        }
        return weblogDataList;
    }

    public WeblogListData getWeblogsByLetter(String baseUrl, Character letter, int pageNum, int length) {
        WeblogListData weblogListData = new WeblogListData();

        List<Weblog> rawWeblogs;
        if (letter == null) {
            rawWeblogs = weblogManager.getWeblogs(Boolean.TRUE, pageNum * length, length + 1);
        } else {
            rawWeblogs = weblogManager.getWeblogsByLetter(letter, pageNum * length, length + 1);
        }

        List<WeblogData> weblogList = weblogListData.getWeblogs();
        for (Weblog weblog : rawWeblogs) {
            weblogList.add(weblogToWeblogData(weblog));
            if (weblogList.size() >= length) {
                break;
            }
        }

        if (pageNum > 0) {
            Map<String, String> params = new HashMap<>();
            params.put("page", "" + (pageNum - 1));
            if (letter != null) {
                params.put("letter", String.valueOf(letter));
            }
            weblogListData.setPrevLink(createURL(baseUrl, params));
        }

        if (rawWeblogs.size() > weblogListData.getWeblogs().size()) {
            Map<String, String> params = new HashMap<>();
            params.put("page", "" + (pageNum + 1));
            if (letter != null) {
                params.put("letter", String.valueOf(letter));
            }
            weblogListData.setNextLink(createURL(baseUrl, params));
        }

        return weblogListData;
    }

    private String createURL(String urlToCreate, Map<String, String> params) {
        return urlToCreate + Utilities.getQueryString(params);
    }

    private WeblogData weblogToWeblogData(Weblog weblog) {
        WeblogData wd = new WeblogData();
        wd.setName(weblog.getName());
        wd.setHandle(weblog.getHandle());
        wd.setAbout(weblog.getAbout());
        wd.setCreatorScreenName(weblog.getCreator().getScreenName());
        wd.setLastModified(weblog.getLastModified());
        wd.setHitsToday(weblog.getHitsToday());
        return wd;
    }

    public static class WeblogListData {
        private String nextLink;
        private String prevLink;
        private List<WeblogData> weblogs = new ArrayList<>();

        public String getNextLink() {
            return nextLink;
        }

        public void setNextLink(String nextLink) {
            this.nextLink = nextLink;
        }

        public String getPrevLink() {
            return prevLink;
        }

        public void setPrevLink(String prevLink) {
            this.prevLink = prevLink;
        }

        public List<WeblogData> getWeblogs() {
            return weblogs;
        }
    }

    public static class WeblogData {
        private String name;
        private String handle;
        private String about;
        private String creatorScreenName;
        private Instant lastModified;
        private int hitsToday;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHandle() {
            return handle;
        }

        public void setHandle(String handle) {
            this.handle = handle;
        }

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public String getCreatorScreenName() {
            return creatorScreenName;
        }

        public void setCreatorScreenName(String creatorScreenName) {
            this.creatorScreenName = creatorScreenName;
        }

        public Instant getLastModified() {
            return lastModified;
        }

        public void setLastModified(Instant lastModified) {
            this.lastModified = lastModified;
        }

        public int getHitsToday() {
            return hitsToday;
        }

        public void setHitsToday(int hitsToday) {
            this.hitsToday = hitsToday;
        }
    }
}