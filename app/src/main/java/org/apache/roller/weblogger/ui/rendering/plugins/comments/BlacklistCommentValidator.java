/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Validates comment if comment does not contain blacklisted words.
 */
public class BlacklistCommentValidator implements CommentValidator {
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");       

    public String getName() {
        return bundle.getString("comment.validator.blacklistName");
    }

    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        if (checkComment(comment)) {
            messages.addError("comment.validator.blacklistMessage");
            return 0;
        }
        return WebloggerCommon.PERCENT_100;
    }

    /**
     * Test comment, applying both site and weblog blacklists, if configured
     * @return True if comment matches a blacklist term
     */
    private boolean checkComment(WeblogEntryComment comment) {
        boolean isBlacklisted = false;
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Weblog weblog = comment.getWeblogEntry().getWeblog();
        Blacklist.populateSpamRules(
                weblog.getBlacklist(), WebloggerFactory.getWeblogger().getPropertiesManager().getStringProperty("spam.blacklist"),
                stringRules, regexRules
        );
        if (Blacklist.isBlacklisted(comment.getUrl(), stringRules, regexRules)
                || Blacklist.isBlacklisted(comment.getEmail(), stringRules, regexRules)
                || Blacklist.isBlacklisted(comment.getName(), stringRules, regexRules)
                || Blacklist.isBlacklisted(comment.getContent(), stringRules, regexRules)) {
            isBlacklisted = true;
        }
        return isBlacklisted;
    }
}
