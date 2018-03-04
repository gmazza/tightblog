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
package org.tightblog.rendering.generators;

import org.apache.commons.lang3.StringUtils;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.CalendarData;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.Utilities;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Data generator for a blog calendar containing links to days with blog entries,
 * and optionally links to individual blog entries themselves.
 */
public class WeblogCalendar {

    private WeblogPageRequest pageRequest = null;
    private Map<LocalDate, String> monthMap;
    private Map<LocalDate, List<WeblogEntry>> monthToEntryMap;
    private boolean includeBlogEntryData;
    private LocalDate dayInMonth = null;
    private LocalDate prevMonth = null;
    private LocalDate nextMonth = null;
    private static final DateTimeFormatter sixCharDateFormat = DateTimeFormatter.ofPattern(Utilities.FORMAT_6CHARS);
    private static final DateTimeFormatter eightCharDateFormat = DateTimeFormatter.ofPattern(Utilities.FORMAT_8CHARS);

    protected WeblogEntryManager weblogEntryManager;
    protected URLStrategy urlStrategy;
    protected Weblog weblog = null;
    protected String cat = null;

    public WeblogCalendar(WeblogEntryManager wem, URLStrategy urlStrategy, WeblogPageRequest pRequest, boolean includeBlogEntryData) {
        this.weblogEntryManager = wem;
        this.urlStrategy = urlStrategy;
        this.pageRequest = pRequest;
        this.includeBlogEntryData = includeBlogEntryData;
        weblog = pageRequest.getWeblog();

        if (pageRequest.getWeblogCategoryName() != null) {
            cat = pageRequest.getWeblogCategoryName();
        }
        dayInMonth = parseWeblogURLDateString(pageRequest.getWeblogDate());
        initModel();
    }

    private void initModel() {
        LocalDateTime startTime = dayInMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endTime = dayInMonth.withDayOfMonth(dayInMonth.lengthOfMonth()).atStartOfDay().plusDays(1).minusNanos(1);

        // determine if we should have next and prev month links, and if so, the months for them to point to
        WeblogEntry temp = weblogEntryManager.findNearestWeblogEntry(weblog, cat, startTime.minusNanos(1), false);
        prevMonth = firstDayOfMonthOfWeblogEntry(temp);

        temp = weblogEntryManager.findNearestWeblogEntry(weblog, cat, endTime.plusNanos(1), true);
        nextMonth = firstDayOfMonthOfWeblogEntry(temp);

        // retrieve the entries for this month
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(weblog);
        wesc.setStartDate(startTime.atZone(ZoneId.systemDefault()).toInstant());
        wesc.setEndDate(endTime.atZone(ZoneId.systemDefault()).toInstant());
        wesc.setCategoryName(cat);
        wesc.setStatus(PubStatus.PUBLISHED);
        monthMap = weblogEntryManager.getWeblogEntryStringMap(wesc);
        if (includeBlogEntryData) {
            monthToEntryMap = weblogEntryManager.getWeblogEntryObjectMap(wesc);
        }
    }

    private LocalDate firstDayOfMonthOfWeblogEntry(WeblogEntry entry) {
        return (entry == null) ? null : entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
    }

    /**
     * Create previous or next month URLs for the calendar, that normally appear to the left and right of the
     * month name at the top of the calendar.
     *
     * The URL to return is commonly of two types:
     * (1) Usually for the small calendar that is displayed along with the blog entries, a month
     *     paginator that will show the blog entries of a different month, i.e., getWeblogCollectionURL.
     * (2) For the large calendar that might sit on a custom "archive" page, the previous and next month links
     *     should point to that same archive page, albeit giving the calendar for a different month, i.e.,
     *     getCustomPageURL.
     *
     * @param day       Day for URL or null if no entries on that day
     * @return URL for day, or null if no weblog entry on that day
     */
    private String computeUrl(LocalDate day) {
        String dateString = monthMap.get(day);
        if (dateString == null) {
            dateString = sixCharDateFormat.format(day);
        }
        if (pageRequest.getCustomPageName() != null) {
            return urlStrategy.getCustomPageURL(weblog, pageRequest.getCustomPageName(), dateString, false);
        } else {
            return urlStrategy.getWeblogCollectionURL(weblog, cat, dateString, null, -1, false);
        }
    }

    /**
     * Parse date as either 6-char or 8-char format.  Use current date if date not provided
     * in URL (e.g., a permalink)
     */
    private LocalDate parseWeblogURLDateString(String dateString) {
        LocalDate ret = null;

        if (dateString != null && StringUtils.isNumeric(dateString)) {
            if (dateString.length() == 8) {
                ret = LocalDate.parse(dateString, eightCharDateFormat);
            } else if (dateString.length() == 6) {
                YearMonth tmp = YearMonth.parse(dateString, sixCharDateFormat);
                ret = tmp.atDay(1);
            }
        }

        // make sure the requested date is not in the future
        if (ret == null || ret.isAfter(LocalDate.now())) {
            ret = LocalDate.now();
        }

        return ret;
    }

    public CalendarData getCalendarData() {
        Locale locale = pageRequest.getWeblog().getLocaleInstance();

        // Allows for different formatting for today's date
        LocalDate todaysDate = LocalDate.now(weblog.getZoneId());

        // get Resource Bundle
        ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources", locale);
        // formatter for Month-Year title of calendar
        DateTimeFormatter formatTitle = DateTimeFormatter.ofPattern(bundle.getString("calendar.dateFormat"), locale)
                .withZone(weblog.getZoneId());

        CalendarData data = new CalendarData();
        data.setCalendarTitle(formatTitle.format(dayInMonth));
        data.setDayOfWeekNames(buildDayNames(locale));

        if (prevMonth != null) {
            data.setPrevMonthLink(computeUrl(prevMonth));
        }
        if (nextMonth != null) {
            data.setNextMonthLink(computeUrl(nextMonth));
        }

        data.setHomeLink(urlStrategy.getWeblogCollectionURL(weblog, cat, null, null, -1, false));

        YearMonth monthToDisplay = YearMonth.from(dayInMonth);

        // Weeks start on different days depending on locale (Sat, Sun, Mon)
        DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();

        // dayPointer will serve as the iterator, going through not just the days of the
        // desired month but the few days before and/or after it to fill a 6 week grid.
        // start with the first day of the week containing the first day of the month
        LocalDate dayPointer = dayInMonth.withDayOfMonth(1).with(TemporalAdjusters.previousOrSame(firstDayOfWeek));

        for (int w = 0; w < 6; w++) {
            CalendarData.Week weekIter = new CalendarData.Week();
            data.getWeeks()[w] = weekIter;

            for (int d = 0; d < 7; d++) {
                CalendarData.Day dayIter = new CalendarData.Day();
                weekIter.getDays()[d] = dayIter;

                // don't store info if dayPointer outside monthToDisplay
                if (YearMonth.from(dayPointer).equals(monthToDisplay)) {
                    // is day today?
                    if (dayPointer.equals(todaysDate)) {
                        data.getWeeks()[w].getDays()[d].setToday(true);
                    }
                    dayIter.setDayNum(Integer.toString(dayPointer.getDayOfMonth()));

                    String dateString = monthMap.get(dayPointer);
                    if (dateString != null) {
                        String link = urlStrategy.getWeblogCollectionURL(weblog, cat, dateString, null, -1, false);
                        dayIter.setLink(link);
                        if (includeBlogEntryData) {
                            dayIter.setEntries(getCalendarEntries(dayPointer));
                        }
                    }
                }

                // increment calendar by one day
                dayPointer = dayPointer.plusDays(1);
            }
        }

        return data;
    }

    private List<CalendarData.BlogEntry> getCalendarEntries(LocalDate day) {
        List<CalendarData.BlogEntry> calendarEntries = new ArrayList<>();

        List<WeblogEntry> entries = monthToEntryMap.get(day);
        if (entries != null) {
            for (WeblogEntry entry : entries) {
                CalendarData.BlogEntry newEntry = new CalendarData.BlogEntry();
                newEntry.setLink(urlStrategy.getWeblogEntryURL(entry, true));

                String title = entry.getTitle().trim();
                if (title.length() > 43) {
                    title = title.substring(0, 40) + "...";
                }
                newEntry.setTitle(title);
                calendarEntries.add(newEntry);
            }
        }
        return calendarEntries;
    }

    /**
     * Helper method to build the names of the weekdays (Sun, Mon, Tue, etc.), with locale-specific
     * names and ordering
     */
    private String[] buildDayNames(Locale locale) {
        String[] dayNames = new String[7];
        DayOfWeek dayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();
        LocalDate localDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(dayOfWeek));
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", locale);

        for (int dnum = 0; dnum < 7; dnum++) {
            dayNames[dnum] = dayFormatter.format(localDate.getDayOfWeek());
            localDate = localDate.plusDays(1);
        }
        return dayNames;
    }

}
