/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.LuceneUtils.DateParsingException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

import com.atlassian.jira.local.ListeningTestCase;

public final class TestLuceneUtils extends ListeningTestCase
{
    @Test
    public void testParse1digit()
    {
        assertParseFailed("2");
    }

    @Test
    public void testParse2digit()
    {
        assertParseFailed("20");
    }

    @Test
    public void testParse3digit()
    {
        assertParseFailed("200");
    }

    @Test
    public void testParseYear()
    {
        assertEquals(date(2007, 01, 01), parse("2007"));
    }

    @Test
    public void testParse5digit()
    {
        assertParseFailed("20071");
    }

    @Test
    public void testParseMonth()
    {
        assertEquals(date(2007, 03, 01), parse("200703"));
    }

    @Test
    public void testParse7digit()
    {
        assertParseFailed("2007031");
    }

    @Test
    public void testParseDay()
    {
        assertEquals(date(2007, 03, 27), parse("20070327"));
    }

    @Test
    public void testParse9digit()
    {
        assertParseFailed("200703271");
    }

    @Test
    public void testParseHour()
    {
        assertEquals(date(2007, 03, 27, 13, 0, 0), parse("2007032713"));
    }

    @Test
    public void testParse11digit()
    {
        assertParseFailed("20070327131");
    }

    @Test
    public void testParseMinute()
    {
        assertEquals(date(2007, 03, 27, 13, 12, 0), parse("200703271312"));
    }

    @Test
    public void testParse13digit()
    {
        assertParseFailed("2007032713125");
    }

    @Test
    public void testParseSecond()
    {
        assertEquals(date(2007, 03, 27, 13, 12, 52), parse("20070327131252"));
    }

    @Test
    public void testParse15digit()
    {
        assertParseFailed("200703271312521");
    }

    @Test
    public void testParse16digit()
    {
        assertParseFailed("2007032713125214");
    }

    @Test
    public void testParseMillisecond()
    {
        assertEquals(date(2007, 03, 27, 13, 12, 52, 145), parse("20070327131252145"));
    }

    @Test
    public void testParse18digit()
    {
        assertParseFailed("200703271312521456");
    }

    @Test
    public void testParse19digit()
    {
        assertParseFailed("2007032713125214567");
    }

    @Test
    public void testParse20digit()
    {
        assertParseFailed("20070327131252145678");
    }

    private void assertParseFailed(final String string)
    {
        try
        {
            parse(string);
            fail("DateParsingException expected");
        }
        catch (final DateParsingException expected)
        {}
    }

    private Date parse(final String string) throws DateParsingException
    {
        return new LuceneUtils.DateFormatter().stringToDate(string);
    }

    static Date date(final int year, final int month, final int date)
    {
        return date(year, month, date, 0, 0, 0);
    }

    static Date date(final int year, final int month, final int date, final int hrs, final int min, final int sec)
    {
        return date(year, month, date, hrs, min, sec, 0);
    }

    static Date date(final int year, final int month, final int date, final int hrs, final int min, final int sec, final int milli)
    {
        return new DateTime(year, month, date, hrs, min, sec, milli, DateTimeZone.UTC).toDate();
    }
}
