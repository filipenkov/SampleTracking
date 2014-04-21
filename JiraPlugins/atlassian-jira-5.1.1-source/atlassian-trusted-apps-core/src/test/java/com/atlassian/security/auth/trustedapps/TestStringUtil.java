package com.atlassian.security.auth.trustedapps;

import java.util.Arrays;

import junit.framework.TestCase;

public class TestStringUtil extends TestCase
{
    public void testSplitSimple() throws Exception
    {
        String[] result = StringUtil.split("[\"one\",\"four\", \"another\", \"six\"]");
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("one", result[0]);
        assertEquals("four", result[1]);
        assertEquals("another", result[2]);
        assertEquals("six", result[3]);
    }

    public void testSplitSimpleNoTrailingCloseBrace() throws Exception
    {
        String[] result = StringUtil.split("[\"one\",\"four\", \"another\", \"six\"");
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("one", result[0]);
        assertEquals("four", result[1]);
        assertEquals("another", result[2]);
        assertEquals("six", result[3]);
    }

    public void testSplitWithCommaValues() throws Exception
    {
        String[] result = StringUtil.split("[\"one\",\"four\", \"ano,ther\", \"s,',',ix\"]");
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("one", result[0]);
        assertEquals("four", result[1]);
        assertEquals("ano,ther", result[2]);
        assertEquals("s,',',ix", result[3]);
    }

    public void testSplitWithNulls() throws Exception
    {
        String[] result = StringUtil.split("[\"one\",,, \"six\"]");
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("one", result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertEquals("six", result[3]);
    }

    public void testSplitWithTrailingComma() throws Exception
    {
        String[] result = StringUtil.split("[\"wun\",]");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("wun", result[0]);
    }

    public void testSplitWithTrailingCommaNoTrailingBrace() throws Exception
    {
        String[] result = StringUtil.split("[\"wun\",");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("wun", result[0]);
    }

    public void testSplitEmpty() throws Exception
    {
        String[] result = StringUtil.split("[]");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testSplitPartialEmpty() throws Exception
    {
        String[] result = StringUtil.split("[");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testSplitThrowsNoLeadingBrace() throws Exception
    {
        try
        {
            StringUtil.split("one");
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException expected)
        {
        }
    }

    public void testArrayToString() throws Exception
    {
        String encoded = StringUtil.toString(new String[] {"one", "two", "three"});
        assertEquals("[\"one\",\"two\",\"three\"]", encoded);
    }

    public void testArrayToStringEncodesQuotes() throws Exception
    {
        String encoded = StringUtil.toString(new String[] {"one", "tw\"o", "three"});
        assertEquals("[\"one\",\"tw\\\"o\",\"three\"]", encoded);
    }

    public void testArrayToStringEncodesSemiTab() throws Exception
    {
        String encoded = StringUtil.toString(new String[] {"one", "tw;\to", "three"});
        assertEquals("[\"one\",\"tw;\\to\",\"three\"]", encoded);
    }

    public void testRoundTripWithQuotes() throws Exception
    {
        final String[] input = new String[] {"one", "tw\"o", "three"};
        final String encoded = StringUtil.toString(input);
        assertEquals("[\"one\",\"tw\\\"o\",\"three\"]", encoded);
        final String[] output = StringUtil.split(encoded);
        assertTrue(Arrays.equals(input, output));
    }

    public void testRoundTripWithTabs() throws Exception
    {
        final String[] input = new String[] {"one", "tw;\to", "three"};
        final String encoded = StringUtil.toString(input);
        assertEquals("[\"one\",\"tw;\\to\",\"three\"]", encoded);
        final String[] output = StringUtil.split(encoded);
        assertTrue(Arrays.equals(input, output));
    }
}