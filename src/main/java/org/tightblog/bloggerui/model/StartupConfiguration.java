/*
 * Copyright 2019 the original author or authors.
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
package org.tightblog.bloggerui.model;

public class StartupConfiguration {
    private boolean showMediaFileTab;
    private boolean mfaEnabled;
    private String tightblogVersion;
    private String tightblogRevision;
    private boolean searchEnabled;
    private String absoluteSiteURL;

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public boolean isShowMediaFileTab() {
        return showMediaFileTab;
    }

    public void setShowMediaFileTab(boolean showMediaFileTab) {
        this.showMediaFileTab = showMediaFileTab;
    }

    public String getTightblogVersion() {
        return tightblogVersion;
    }

    public void setTightblogVersion(String tightblogVersion) {
        this.tightblogVersion = tightblogVersion;
    }

    public String getTightblogRevision() {
        return tightblogRevision;
    }

    public void setTightblogRevision(String tightblogRevision) {
        this.tightblogRevision = tightblogRevision;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public String getAbsoluteSiteURL() {
        return absoluteSiteURL;
    }

    public void setAbsoluteSiteURL(String absoluteSiteURL) {
        this.absoluteSiteURL = absoluteSiteURL;
    }
}
