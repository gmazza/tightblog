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
package org.apache.roller.weblogger.business.jpa;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.time.DateUtils;

import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Planet Roller objects and entry aggregations in a database.
 */
public class JPAPlanetManagerImpl implements PlanetManager {

    private static Logger log = LoggerFactory.getLogger(JPAPlanetManagerImpl.class);

    private JPAPersistenceStrategy strategy;

    protected JPAPlanetManagerImpl() {}

    public void setStrategy(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void savePlanet(Planet group) {
        strategy.store(group);
    }

    @Override
    public void saveSubscription(Subscription sub) {
        Subscription existing = getSubscription(sub.getPlanet(), sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        } else {
            throw new IllegalStateException("ERROR: duplicate feed URLs not allowed");
        }
    }

    @Override
    public void deletePlanet(Planet group) {
        strategy.remove(group);
    }

    @Override
    public void deleteSubscription(Subscription sub) {
        strategy.remove(sub);
    }

    @Override
    public Subscription getSubscription(Planet planet, String feedUrl) {
        TypedQuery<Subscription> q = strategy.getNamedQuery("Subscription.getByPlanetAndFeedURL", Subscription.class);
        q.setParameter(1, planet);
        q.setParameter(2, feedUrl);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Subscription getSubscription(String id) {
        return strategy.load(Subscription.class, id);
    }


    @Override
    public List<Planet> getPlanets() {
        TypedQuery<Planet> q = strategy.getNamedQuery("Planet.getAll", Planet.class);
        return q.getResultList();
    }

    @Override
    public Planet getPlanetByHandle(String handle) {
        TypedQuery<Planet> q = strategy.getNamedQuery("Planet.getByHandle", Planet.class);
        q.setParameter(1, handle);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Planet getPlanet(String id) {
        return strategy.load(Planet.class, id);
    }

    @Override
    public SubscriptionEntry getEntryById(String id) {
        return strategy.load(SubscriptionEntry.class, id);
    }

    @Override
    public List<SubscriptionEntry> getEntries(Planet group, Instant startDate, int offset, int len) {

        if (group == null) {
            throw new IllegalArgumentException("group cannot be null or empty");
        }

        List<SubscriptionEntry> ret;

        long startTime = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int size = 0;
        sb.append("SELECT e FROM SubscriptionEntry e ");

        params.add(size++, group.getHandle());
        sb.append("WHERE e.subscription.planet.handle = ?").append(size);

        if (startDate != null) {
            params.add(size++, startDate);
            sb.append(" AND e.pubTime > ?").append(size);
        }
        sb.append(" ORDER BY e.pubTime DESC");

        TypedQuery<SubscriptionEntry> query = strategy.getDynamicQuery(sb.toString(), SubscriptionEntry.class);
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        if (offset - 1 > 0) {
            query.setFirstResult((offset - 1) * len);
        }
        if (len != -1) {
            query.setMaxResults(len);
        }

        ret = query.getResultList();

        long endTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Generated aggregation of {} in {} seconds", ret.size(),
                    (endTime - startTime) / DateUtils.MILLIS_PER_SECOND);
        }

        return ret;
    }

    @Override
    public List<Subscription> getSubscriptions() {
        TypedQuery<Subscription> q = strategy.getNamedQuery("Subscription.getAll", Subscription.class);
        return q.getResultList();
    }

}
