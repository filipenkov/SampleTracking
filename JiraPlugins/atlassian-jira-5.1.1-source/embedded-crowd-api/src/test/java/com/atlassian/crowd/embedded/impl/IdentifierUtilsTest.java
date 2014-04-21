package com.atlassian.crowd.embedded.impl;

import org.junit.Test;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.equalsInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdentifierUtilsTest
{
    @Test
    public void testToLowerCaseEqualInEnglish()
    {
        runTestToLowerCase("en", "THIS IS TEST", "this is test");
    }

    @Test
    public void testToLowerCaseEqualInTurkish()
    {
        runTestToLowerCase("tr", "THIS IS TEST", "thıs ıs test");
    }

    @Test
    public void testCompareToInLowerCaseInEnglish()
    {
        runTestCompareToInLowerCase("en", "THIS IS TESs", "this is test", -1);
    }

    @Test
    public void testCompareToInLowerCaseInTurkish()
    {
        runTestCompareToInLowerCase("tr", "THIS IS TESs", "this is test", 200);
    }

    @Test
    public void testEqualsInLowerCaseInEnglish()
    {
        runTestEqualsInLowerCase("en", "THIS IS TEST", "this is test");
    }

    @Test
    public void testEqualsInLowerCaseInTurkish()
    {
        runTestEqualsInLowerCase("tr", "THIS IS TEST", "thıs ıs test");
    }

    private static void withCrowdIdentifierLanguage(String lang, Runnable r)
    {
        String before = System.getProperty("crowd.identifier.language");
        try
        {
            System.setProperty("crowd.identifier.language", lang);
            IdentifierUtils.prepareIdentifierCompareLocale();
            r.run();
        }
        finally
        {
            if (before != null)
            {
                System.setProperty("crowd.identifier.language", before);
            }
            else
            {
                System.clearProperty("crowd.identifier.language");
            }
            IdentifierUtils.prepareIdentifierCompareLocale();
        }
    }

    private void runTestToLowerCase(String language, final String original, final String lowercase)
    {
        withCrowdIdentifierLanguage(language, new Runnable() {
            @Override
            public void run()
            {
                assertEquals(lowercase, toLowerCase(original));
            }
        });
    }

    private void runTestCompareToInLowerCase(String language, final String identifier1, final String identifier2, final int expectedResult)
    {
        withCrowdIdentifierLanguage(language, new Runnable() {
            @Override
            public void run()
            {
                assertEquals(expectedResult, compareToInLowerCase(identifier1, identifier2));
            }
        });
    }

    private void runTestEqualsInLowerCase(String language, final String identifier1, final String identifier2)
    {
        withCrowdIdentifierLanguage(language, new Runnable() {
            @Override
            public void run()
            {
                assertTrue(equalsInLowerCase(identifier1, identifier2));
            }
        });
    }
}
