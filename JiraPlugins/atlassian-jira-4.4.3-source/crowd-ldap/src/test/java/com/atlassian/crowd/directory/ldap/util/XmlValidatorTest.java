package com.atlassian.crowd.directory.ldap.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link XmlValidator}.
 */
public class XmlValidatorTest
{
    @Test
    public void simpleStringsAreValid()
    {
        assertTrue("The empty string is valid", XmlValidator.isSafe(""));
        assertTrue("An ASCII string is valid", XmlValidator.isSafe("Test string"));
    }

    @Test
    public void asciiNulIsNotValid()
    {
        assertFalse(XmlValidator.isSafe("\u0000"));
    }
    
    @Test
    public void supplementaryCharactersAreAccepted()
    {
        // A single character that is UTF-16-encoded as two Java chars
        String supplementaryCharacter = new String(Character.toChars(0x10400));

        assertEquals("Our string has two UTF-16 code points",
                2, supplementaryCharacter.length());
        
        assertTrue(XmlValidator.isSafe(supplementaryCharacter));
    }
}
