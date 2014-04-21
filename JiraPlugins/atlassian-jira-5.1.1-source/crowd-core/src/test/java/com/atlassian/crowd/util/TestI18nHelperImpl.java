package com.atlassian.crowd.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TestI18nHelperImpl extends TestCase
{
    private I18nHelperImpl i18nHelper = null;
    private static final String LICENSE_RESOURCE_LIMIT_TEXT = "license.resource.limit.text";

    protected void setUp() throws Exception
    {
        super.setUp();
        i18nHelper = new I18nHelperImpl(
                new I18nHelperConfiguration()
                {

                    public Locale getLocale()
                    {
                        return Locale.getDefault();
                    }

                    public List<String> getBundleLocations()
                    {
                        return Arrays.asList(TestI18nHelperImpl.class.getName());
                    }
                });
    }

    public void testGetUnescapedText()
    {
        String text = i18nHelper.getUnescapedText(LICENSE_RESOURCE_LIMIT_TEXT);
        assertNotNull(text);
        assertTrue(text.length() > 0);
        assertEquals(text, "Some random test text {0}");
    }

    public void testTextWithParam()
    {
        String text = i18nHelper.getText(LICENSE_RESOURCE_LIMIT_TEXT, "Crowd");
        assertNotNull(text);
        assertTrue(text.length() > 0);
        assertEquals(text, "Some random test text Crowd");

    }

    protected void tearDown() throws Exception
    {
        i18nHelper = null;
        super.tearDown();
    }
}
