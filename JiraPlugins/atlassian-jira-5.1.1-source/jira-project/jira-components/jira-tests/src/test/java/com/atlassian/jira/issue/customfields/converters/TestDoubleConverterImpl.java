package com.atlassian.jira.issue.customfields.converters;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.mock.web.util.MockOutlookManager;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Locale;

public class TestDoubleConverterImpl extends ListeningTestCase
{
    @Test
    public void testGetDouble() {
        MockI18nHelper i18nHelper = new MockI18nHelper();
        i18nHelper.setLocale(Locale.ENGLISH);
        DoubleConverter converter = new DoubleConverterImpl(new MockAuthenticationContext(null, new MockOutlookManager(), i18nHelper));

        assertEquals(123.23d, converter.getDouble("123.23"), 0);
        assertEquals(123343.23d, converter.getDouble("123343.23"), 0);
        assertEquals(-123343.23d, converter.getDouble("-123343.23"), 0);

        i18nHelper.setLocale(new Locale("pl", "pl"));

        assertEquals(123.23d, converter.getDouble("123,23"), 0);
        assertEquals(123343.23d, converter.getDouble("123343,23"), 0);
        assertEquals(-123343.23d, converter.getDouble("-123343,23"), 0);
    }

    @Test
    public void testGetChangelogString() {
        MockI18nHelper i18nHelper = new MockI18nHelper();
        i18nHelper.setLocale(Locale.ENGLISH);
        DoubleConverter converter = new DoubleConverterImpl(new MockAuthenticationContext(null, new MockOutlookManager(), i18nHelper));

        assertEquals("123.23", converter.getStringForChangelog(123.23d));
        assertEquals("123343.23", converter.getStringForChangelog(123343.23d));
        assertEquals("-123343.23", converter.getStringForChangelog(-123343.23d));

        i18nHelper.setLocale(new Locale("pl", "pl"));

        assertEquals("123.23", converter.getStringForChangelog(123.23d));
        assertEquals("123343.23", converter.getStringForChangelog(123343.23d));
        assertEquals("-123343.23", converter.getStringForChangelog(-123343.23d));
    }
}
