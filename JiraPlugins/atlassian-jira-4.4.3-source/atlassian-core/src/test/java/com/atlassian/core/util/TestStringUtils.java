package com.atlassian.core.util;

import com.atlassian.core.util.StringUtils;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TestStringUtils extends TestCase
{
    private final String UTF8_TEST_STRING = "" + (char) 8216 + "singlequotes" + (char) 8217 + " " +
            (char) 8220 + "doublequotes" + (char) 8221 + " Elipsis: " + (char) 8230 + " Em-dash: " +
            (char) 8211 + " bullet: " + (char) 183;

    private final String ISO88591_TEST_STRING = "" + (char) 145 + "singlequotes" + (char) 146 + " " +
            (char) 147 + "doublequotes" + (char) 148 + " Elipsis: " + (char) 133 + " Em-dash: " +
            (char) 150 + " bullet: " + (char) 183;

    private final String SIMPLIFIED_TEST_STRING = "'singlequotes' \"doublequotes\" Elipsis: ... Em-dash: - bullet: - ";

    public TestStringUtils(String s)
    {
        super(s);
    }

    public void testUTF()
    {
        String escaped = StringUtils.escapeCP1252(UTF8_TEST_STRING, "UTF-8");
        assertEquals(SIMPLIFIED_TEST_STRING, escaped);
    }

    public void testISO8859()
    {
        String escaped = StringUtils.escapeCP1252(ISO88591_TEST_STRING, "ISO-8859-1");
        assertEquals(SIMPLIFIED_TEST_STRING, escaped);
    }

    public void testNormalString()
    {
        String escaped = StringUtils.escapeCP1252("normal string", "UTF-8");
        assertEquals("normal string", escaped);
    }

    public void testNull()
    {
        String escaped = StringUtils.escapeCP1252(null, "UTF-8");
        assertEquals(null, escaped);
    }

    public void testHighRangeStrings()
    {
        String highEndString = new String("This is a " + UTF8_TEST_STRING + " test String '" + ((char) 25628) + "' does it work?");
        StringUtils.escapeCP1252(highEndString, "UTF-8");
        StringUtils.escapeCP1252(highEndString, "ISO-8859-1");
    }

    public void testInvalidControlCharacters()
    {
        String s = "start";
        for (int i = 0; i <= 30; i++)
        {
            if (i == 9 || i == 10 || i == 13)
            {
                continue; // 9, 10, 13 are line feed and carriage return chars
            }
            s += (char) i;
        }
        s += "end";
        assertEquals("startend", StringUtils.escapeCP1252(s, "UTF-8"));
        assertEquals("startend", StringUtils.escapeCP1252(s, "ISO-8859-1"));
    }

    public void testOnlyControlCharacters()
    {
        String s = "";
        for (int i = 0; i <= 30; i++)
        {
            if (i == 9 || i == 10 || i == 13)
            {
                continue; // 9, 10, 13 are line feed and carriage return chars
            }
            s += (char) i;
        }
        assertEquals("", StringUtils.escapeCP1252(s, "UTF-8"));
        assertEquals("", StringUtils.escapeCP1252(s, "ISO-8859-1"));
    }

    public void testReplaceAll()
    {
        assertEquals("no change", StringUtils.replaceAll("no change", "foo", "bar"));
        assertEquals("no change", StringUtils.replaceAll("no change", "", ""));
        assertEquals("", StringUtils.replaceAll("deleteme", "deleteme", ""));
        assertEquals(null, StringUtils.replaceAll(null, "&", "X"));
        assertEquals("", StringUtils.replaceAll("", "&", "X"));
        assertEquals("no change", StringUtils.replaceAll("no change", null, ""));
        assertEquals("hello%26world%26sad", StringUtils.replaceAll("hello&world&sad", "&", "%26"));
        assertEquals("Xhello", StringUtils.replaceAll("&hello", "&", "X"));
        assertEquals("helloX", StringUtils.replaceAll("hello&", "&", "X"));
        assertEquals("XhelloXworld", StringUtils.replaceAll("&%hello&%world", "&%", "X"));
        assertEquals("goodbye world", StringUtils.replaceAll("hello world", "hello", "goodbye"));
        assertEquals("doo dum dum diddly dum", StringUtils.replaceAll("doo da da diddly da", "da", "dum"));
        assertEquals("normal", StringUtils.replaceAll("normal", "&", "X"));
    }

    public void testReplaceAllDips()
    {
        String str = "hello'world";
        String str2 = "'foo'bar'";

        assertEquals("hello''world", StringUtils.replaceAll(str, "\'", "\'\'"));
        assertEquals("''foo''bar''", StringUtils.replaceAll(str2, "\'", "\'\'"));
    }

    public void testStringAllASCII()
    {
        assertTrue(StringUtils.isStringAllASCII(null));
        assertTrue(StringUtils.isStringAllASCII(""));
        assertTrue(StringUtils.isStringAllASCII("abc"));
        assertTrue(StringUtils.isStringAllASCII("abc\tgr"));
        assertTrue(StringUtils.isStringAllASCII("?/\\@#$"));
        assertFalse(StringUtils.isStringAllASCII("?/\u00fd#"));
        assertFalse(StringUtils.isStringAllASCII("\\@\u00ff$"));
        assertFalse(StringUtils.isStringAllASCII("a\u0129ngr"));
    }

    public void testStringISO_8859_1()
    {
        assertTrue(StringUtils.isStringISO_8859_1(null));
        assertTrue(StringUtils.isStringISO_8859_1(""));
        assertTrue(StringUtils.isStringISO_8859_1("abc"));
        assertTrue(StringUtils.isStringISO_8859_1("abc\tgr"));
        assertTrue(StringUtils.isStringISO_8859_1("?/\\@#$"));
        assertTrue(StringUtils.isStringISO_8859_1("?/\u00fd#"));
        assertTrue(StringUtils.isStringISO_8859_1("\\@\u00ff$"));
        assertFalse(StringUtils.isStringISO_8859_1("a\u0129ngr"));
    }

    public void testEqualsIgnoreLineTerminators()
    {
        // Test That null and empty string is treated as equal
        assertTrue(StringUtils.equalsIgnoreLineTerminators(null, null));
        assertTrue(StringUtils.equalsIgnoreLineTerminators(null, ""));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("", null));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("", ""));

        // Test that comparing a string to no string
        assertFalse(StringUtils.equalsIgnoreLineTerminators(null, "a"));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("", "a"));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("a", null));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("a", ""));

        // Test that line terminators are not ignored (they do matter)
        assertFalse(StringUtils.equalsIgnoreLineTerminators("a\na", "aa"));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("aa", "a\ra"));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("aa", "a\r\na"));

        // Check that all line terminators are treated equaly
        assertTrue(StringUtils.equalsIgnoreLineTerminators("a\na", "a\na"));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("a\r\na", "a\r\na"));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("a\na", "a\r\na"));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("a\na", "a\ra"));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("a\ra", "a\ra"));

        // Same more random tests
        assertTrue(StringUtils.equalsIgnoreLineTerminators("abc", "abc"));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("a\na", "a\na\n"));
        assertFalse(StringUtils.equalsIgnoreLineTerminators("a\ra", "a\na\r"));
        assertTrue(StringUtils.equalsIgnoreLineTerminators("a\ra\r\n", "a\na\r"));
    }

    public void testSplitCommaSeperatedString()
    {
        String[] namesExpected = new String[]{"bob","fred","john"};
        String[] names;
        
        // Test a simple String
        names = StringUtils.splitCommaSeparatedString("bob,fred,john");
        assertArrayContentEquals(names, namesExpected);

        // Test String with spaces
        names = StringUtils.splitCommaSeparatedString("bob, fred, john");
        assertArrayContentEquals(names, namesExpected);

        // Test empty String
        names = StringUtils.splitCommaSeparatedString("");
        assertEquals(names.length, 1);
    }

    public void testCreateCommaSeperatedString()
    {
        String[] namesExpected = new String[]{"bob","fred","john"};
        Collection nameList = Arrays.asList(namesExpected);

        String nameString = "bob,fred,john";

        String names;

        names = StringUtils.createCommaSeperatedString(nameList);
        assertEquals(names, nameString);

        names = StringUtils.createCommaSeperatedString(null);
        assertNull(names);

        names = StringUtils.createCommaSeperatedString(Collections.EMPTY_LIST);
        assertEquals(names, "");
    }




    private void assertArrayContentEquals(String[] array1, String[] array2)
    {
        assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++)
        {
            String s1 = array1[i];
            String s2 = array1[i];
            assertEquals(s1, s2);
        }
    }
}
