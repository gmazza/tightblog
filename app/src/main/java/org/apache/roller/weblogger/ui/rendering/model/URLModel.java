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
package org.apache.roller.weblogger.ui.rendering.model;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;

/**
 * Provides access to URL building functionality.
 *
 * NOTE: we purposely go against the standard getter/setter bean standard
 * for methods that take arguments so that users get a consistent way to
 * access those methods in their templates. i.e.
 *
 * $url.category("foo") instead of $url.getCategory("foo")
 */
public class URLModel implements Model {
    
    private static Log log = LogFactory.getLog(URLModel.class);
    
    protected Weblog weblog;

    private WeblogEntryManager weblogEntryManager;

    protected URLStrategy urlStrategy;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    public URLModel() {}

    @Override
    public String getModelName() {
        return "url";
    }

    /** Init page model, requires a WeblogRequest object. */
    @Override
    public void init(Map initData) throws WebloggerException {
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if (weblogRequest == null) {
            throw new WebloggerException("Expected 'weblogRequest' init param!");
        }
        
        this.weblog = weblogRequest.getWeblog();
    }
    
    
    /** Relative URL of weblogger, e.g. /tightblog */
    public String getSite() {
        return WebloggerStaticConfig.getRelativeContextURL();
    }
    
    
    /** Absolute URL of weblogger, e.g. http://localhost:8080/tightblog */
    public String getAbsoluteSite() {
        return WebloggerStaticConfig.getAbsoluteContextURL();
    }

    /** URL for logging in */  
    public String getLogin() {
        return urlStrategy.getLoginURL(false);
    }


    /** URL for logging out */
    public String getLogout() {
        return urlStrategy.getLogoutURL(false);
    }
    
    /** URL for registering */
    public String getRegister() {
        return urlStrategy.getRegisterURL(false);
    }
    
    
    /** URL for a specific UI action */
    public String action(String action, String namespace) {
        if(namespace != null) {
            if("/tb-ui".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, null, null, true);
            } else if("/tb-ui/authoring".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, weblog.getHandle(), null, true);
            } else if("/tb-ui/admin".equals(namespace)) {
                return urlStrategy.getActionURL(action, namespace, null, null, true);
            }
        }
        return null;
    }
    
    public String getCommentAuthenticator() {
        return getSite()+"/tb-ui/rendering/comment/authform";
    }

    public String themeResource(String theme, String filePath) {
        return getSite()+"/themes/"+theme+"/"+filePath;
    }
        
    public String getHome() {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, null, -1, true);
    }
    
    public String entry(String anchor) {
        return urlStrategy.getWeblogEntryURL(weblog, anchor, true);
    }

    public String comment(String anchor, String timeStamp) {
        return urlStrategy.getWeblogCommentURL(weblog, anchor, timeStamp, true);
    }


    public String comments(String anchor) {
        return urlStrategy.getWeblogCommentsURL(weblog, anchor, true);
    }


    public String trackback(String anchor) {
        return urlStrategy.getWeblogEntryURL(weblog, anchor, true);
    }

    
    public String category(String catName) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, null, null, -1, true);
    }
    
    public String tag(String tag) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, tag, -1, true);
    }

    public String getSearch() {
        return urlStrategy.getWeblogSearchURL(weblog, null, null, -1, false);
    }
    
    public String page(String theme, String pageLink) {
        return urlStrategy.getWeblogPageURL(weblog, theme, pageLink, null, null, null, null, -1, true);
    }

    public FeedURLS getFeed() {
        return new FeedURLS();
    }

    /** URL for editing a weblog entry */
    public String editEntry(String anchor) {
        try {
            // need to determine entryId from anchor
            WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, anchor);
            if(entry != null) {
                return urlStrategy.getEntryEditURL(weblog.getHandle(), entry.getId(), false);
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up entry by anchor - "+anchor, ex);
        }
        return null;
    } 

    /** URL for creating a new weblog entry */
    public String getCreateEntry() {
        return urlStrategy.getEntryAddURL(weblog.getHandle(), false);
    }

    /** URL for editing weblog settings */
    public String getEditSettings() {
        return urlStrategy.getWeblogConfigURL(weblog.getHandle(), false);
    }

    ///////  Inner Classes  ///////
    public class FeedURLS {
        public EntryFeedURLS getEntries() {
            return new EntryFeedURLS();
        }
        public CommentFeedURLS getComments() {
            return new CommentFeedURLS();
        }
    }
    
    public class EntryFeedURLS {
        
        public String getRss() {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "rss", null, null);
        }
        
        public String rss(String catName) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "rss", catName, null);
        }
        
        public String rssByTag(String tag) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "rss", null, tag);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", null, null);
        }
        
        public String atom(String catName) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", catName, null);
        }
        
        public String atomByTag(String tag) {
            return urlStrategy.getWeblogFeedURL(weblog, "entries", "atom", null, tag);
        }
    }
    
    public class CommentFeedURLS {
        
        public String getRss() {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "rss", null, null);
        }
        
        public String rss(String catName) {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "rss", catName, null);
        }
        
        public String getAtom() {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "atom", null, null);
        }
        
        public String atom(String catName) {
            return urlStrategy.getWeblogFeedURL(weblog, "comments", "atom", catName, null);
        }
        
    }

}
