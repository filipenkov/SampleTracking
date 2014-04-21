package com.atlassian.renderer.v2.functional;

import com.opensymphony.util.TextUtils;

import java.util.Properties;

import junit.framework.Assert;

public class FunctionalTest extends Assert
{
    public static String WIKI_NOTATION = "WIKI.NOTATION.";
    private static String XHTML_NOTATION = "XHTML.NOTATION.";
    private static final String XHTML_NOTATION_2 = "XHTML.V2.NOTATION.";
    public static final String DEFAULT_SPACE_KEY = "SPC";


    private String testName;
    private String htmlText;
    private String wikiText;
    private String spaceKey;
    private String pageTitle;

    public FunctionalTest(String testName, Properties testCases)
    {
        this.testName = testName;
        wikiText = testCases.getProperty(WIKI_NOTATION + testName);
        htmlText = testCases.getProperty(XHTML_NOTATION_2 + testName);
        spaceKey = DEFAULT_SPACE_KEY;
        pageTitle = testCases.getProperty("PAGE." + testName);

        if (TextUtils.stringSet(testCases.getProperty("SPACE." + testName)))
            spaceKey = testCases.getProperty("SPACE." + testName);

        if (!TextUtils.stringSet(htmlText))
            htmlText = testCases.getProperty(XHTML_NOTATION + testName);

    }

    public String getName()
    {
        return testName;
    }

    public void run(FunctionalTestSetup testSetup)
    {
        assertEquals(htmlText, testSetup.convertWikiToHtml(wikiText, spaceKey, pageTitle));
    }
}
