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
package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.business.URLStrategy;

/**
 * Paging through a collection of planet entries.
 */
public class PlanetEntriesPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(PlanetEntriesPager.class);
    
    private String feedURL = null;
    private String planetName = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // the collection for the pager
    private List<SubscriptionEntry> entries = null;
    
    // are there more items?
    private boolean more = false;

    private PlanetManager planetManager;
    
    public PlanetEntriesPager(
            PlanetManager  planetManager,
            URLStrategy    strat,
            String         feedURL,
            String         planetName,
            String         baseUrl,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);

        this.planetManager = planetManager;
        this.feedURL = feedURL;
        this.planetName = planetName;
        this.sinceDays = sinceDays;
        this.length = length;

        // initialize the collection
        getItems();
    }
    
    
    public List<SubscriptionEntry> getItems() {
        
        if (planetName == null) {
            return null;
        }

        if (entries == null) {
            // calculate offset
            int offset = getPage() * length;
            
            Date startDate = null;
            if (sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            List<SubscriptionEntry> results = new ArrayList<>();
            try {
                List<SubscriptionEntry> subEntries;
                if (feedURL != null) {
                    Planet planet = planetManager.getPlanet(planetName);
                    Subscription sub = planetManager.getSubscription(planet, feedURL);
                    subEntries = planetManager.getEntries(sub, offset, length+1);
                } else {
                    Planet group = planetManager.getPlanet(planetName);
                    subEntries = planetManager.getEntries(group, startDate, null, offset, length + 1);
                }
                
                // wrap 'em
                int count = 0;
                for (SubscriptionEntry entry : subEntries) {
                    if (count++ < length) {
                        results.add(entry);
                    } else {
                        more = true;
                    }
                }
                
            } catch (Exception e) {
                log.error("ERROR: get aggregation", e);
            }
            
            entries = results;
        }
        
        return entries;
    }
    
    
    public boolean hasMoreItems() {
        return more;
    }
}
