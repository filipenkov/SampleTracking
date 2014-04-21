package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

/**
 * Object that can deal with JIRA REST dates.
 *
 * @since v4.4
 */

import org.joda.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Locale;

/**
 * Adapter for converting LocalDate to String and vice-versa.
 *
 * @since v4.4
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate>
{
    /**
     * The ISO8601 date format used in the REST plugin.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public LocalDate unmarshal(String s) throws Exception
    {
        return s == null ? null : new LocalDate(s);
    }

    @Override
    public String marshal(LocalDate date) throws Exception
    {
        return date == null ? null : date.toString(DATE_FORMAT, Locale.US);
    }
}
