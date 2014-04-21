package com.atlassian.crowd.embedded.impl;

import org.junit.Test;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.equalsInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.prepareIdentifierCompareLocale;
import static org.junit.Assert.assertEquals;

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
        runTestEqualsInLowerCase("tr", "THIS IS TEST", "this ıs test");
    }

    private void runTestToLowerCase(String language, String original, String lowercase)
    {
        final String oldLang = System.getProperty("crowd.identifier.language");
        System.setProperty("crowd.identifier.language", language);
        try
        {
            prepareIdentifierCompareLocale();
            assertEquals(lowercase, toLowerCase(original));
        }
        finally
        {
            if (oldLang == null)
            {
                System.clearProperty("crowd.identifier.language");
            }
            else
            {
                System.setProperty("crowd.identifier.language", oldLang);
            }
            prepareIdentifierCompareLocale();
        }
    }

    private void runTestCompareToInLowerCase(String language, String identifier1, String identifier2, int expectedResult)
    {
        final String oldLang = System.getProperty("crowd.identifier.language");
        System.setProperty("crowd.identifier.language", language);
        try
        {
            prepareIdentifierCompareLocale();
            assertEquals(expectedResult, compareToInLowerCase(identifier1, identifier2));
        }
        finally
        {
            if (oldLang == null)
            {
                System.clearProperty("crowd.identifier.language");
            }
            else
            {
                System.setProperty("crowd.identifier.language", oldLang);
            }
            prepareIdentifierCompareLocale();
        }
    }

    private void runTestEqualsInLowerCase(String language, String identifier1, String identifier2)
    {
        final String oldLang = System.getProperty("crowd.identifier.language");
        System.setProperty("crowd.identifier.language", language);
        try
        {
            prepareIdentifierCompareLocale();
            equalsInLowerCase(identifier1, identifier2);
        }
        finally
        {
            if (oldLang == null)
            {
                System.clearProperty("crowd.identifier.language");
            }
            else
            {
                System.setProperty("crowd.identifier.language", oldLang);
            }
            prepareIdentifierCompareLocale();
        }
    }
}
