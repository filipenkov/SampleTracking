package com.atlassian.core.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;

public class TestAbstractEncodingFilterPunctuationReplacement extends TestCase
{
    private static final String TEST_STRING = getUnicodePunctuationCharacters() + getLatin1PunctuationCharacters();
    private static final String TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED = replaceUnicodePunctuationWithAscii(TEST_STRING);
    private static final String TEST_STRING_WITH_WINDOWS_1252_PUNCTUATION_REPLACED = replaceWindows1252PunctuationWithAscii(TEST_STRING);

    private StubEncodingFilter encodingFilter;
    private ServletStubs.Request request;
    private ServletStubs.Response response;
    private FilterChain filterChain;
    private Map<String, Object> result;

    protected void setUp() throws Exception
    {
        super.setUp();

        request = ServletStubs.getRequestInstance();
        request.setParameter("param", TEST_STRING);
        request.addParameter("paramValues", TEST_STRING);
        request.addParameter("paramValues", TEST_STRING);
        request.setParameterMap(new HashMap<String, String[]>() {{
            put("param", new String[]{ "not used -- filter calls getParameterValues" });
            put("paramValues", new String[]{ "not used -- filter calls getParameterValues" });
        }});

        response = ServletStubs.getResponseInstance();

        encodingFilter = new StubEncodingFilter();

        result = new HashMap<String, Object>();
        filterChain = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response)
            {
                result.put("param", request.getParameter("param"));
                result.put("paramValues", request.getParameterValues("paramValues"));
                result.put("paramMap", request.getParameterMap());
            }
        };
    }

    public void testUnicodePunctuationRemovedFromParametersInUtf8() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.doFilter(request, response, filterChain);

        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, result.get("param"));
    }

    public void testPunctuationRemovedByGetParameterValues() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.doFilter(request, response, filterChain);

        String[] values = (String[]) result.get("paramValues");
        assertEquals(2, values.length);
        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, values[0]);
        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, values[1]);
    }

    public void testPunctuationRemovedByGetParameterMap() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.doFilter(request, response, filterChain);

        //noinspection unchecked
        Map<String, String[]> map = (Map<String, String[]>) result.get("paramMap");
        assertEquals(2, map.size());
        assertEquals(1, map.get("param").length);
        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, map.get("param")[0]);
        assertEquals(2, map.get("paramValues").length);
        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, map.get("paramValues")[0]);
        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, map.get("paramValues")[1]);
    }

    public void testWindows1252PunctuationRemovedFromParametersInLatin1() throws Exception
    {
        encodingFilter.setEncoding("ISO-8859-1");
        encodingFilter.doFilter(request, response, filterChain);

        assertEquals(TEST_STRING_WITH_WINDOWS_1252_PUNCTUATION_REPLACED, result.get("param"));
    }

    public void testUnicodePunctuationRemovedFromParametersInWindows1252() throws Exception
    {
        encodingFilter.setEncoding("Windows-1252");
        encodingFilter.doFilter(request, response, filterChain);

        assertEquals(TEST_STRING_WITH_UNICODE_PUNCTUATION_REPLACED, result.get("param"));
    }

    public void testParametersUnchangedInUtf16() throws Exception
    {
        encodingFilter.setEncoding("UTF-16");
        encodingFilter.doFilter(request, response, filterChain);

        assertEquals(TEST_STRING, result.get("param"));
    }

    /**
     * Returns the "General Punctuation" range of characters in Unicode as a String.
     */
    private static String getUnicodePunctuationCharacters()
    {
        StringBuffer result = new StringBuffer(200);
        for (char c = '\u2000'; c < '\u2070'; c++) // General Punctuation range
            result.append(c);
        return result.toString();
    }

    /**
     * Returns punctuation characters in Latin-1 and Windows-1252 as a String.
     */
    private static String getLatin1PunctuationCharacters()
    {
        StringBuffer result = new StringBuffer(200);
        for (char c = '\u0020'; c <= '\u0040'; c++) // ASCII punctuation and digits
            result.append(c);
        for (char c = '\u005b'; c <= '\u0060'; c++) // more ASCII punctuation
            result.append(c);
        for (char c = '\u007b'; c <= '\u007e'; c++) // more ASCII punctuation
            result.append(c);
        for (char c = '\u0080'; c < '\u0100'; c++) // high-bit Latin-1, includes Windows-1252 punctuation
            result.append(c);
        return result.toString();
    }

    /**
     * Replaces the Unicode punctuation with a rough ASCII equivalent. The same replacements
     * as the encoding filter, but not as fast.
     */
    private static String replaceUnicodePunctuationWithAscii(String input)
    {
        StringBuffer output = new StringBuffer(input.length() + 50); // room for ellipsis
        for (int i=0; i<input.length(); i++)
        {
            char c = input.charAt(i);
            switch (c) {
                case '\u00b7': // MIDDLE DOT
                    output.append("- ");
                    break;
                case '\u2013': // EN DASH
                    output.append("-");
                    break;
                case '\u2018': // LEFT SINGLE QUOTATION MARK
                case '\u2019': // RIGHT SINGLE QUOTATION MARK
                    output.append('\'');
                    break;
                case '\u201c': // LEFT DOUBLE QUOTATION MARK
                case '\u201d': // RIGHT DOUBLE QUOTATION MARK
                    output.append('"');
                    break;
                case '\u2026': // HORIZONTAL ELLIPSIS
                    output.append("...");
                    break;
                default:
                    output.append(c);
            }
        }
        return output.toString();
    }

    /**
     * Replaces the Windows-1252 punctuation with a rough ASCII equivalent. The same replacements
     * as the encoding filter, but not as fast.
     */
    private static String replaceWindows1252PunctuationWithAscii(String input)
    {
        StringBuffer output = new StringBuffer(input.length() + 50); // room for ellipsis
        for (int i=0; i<input.length(); i++)
        {
            char c = input.charAt(i);
            switch (c) {
                case 133: // HORIZONTAL ELLIPSIS
                    output.append("...");
                    break;
                case 145: // LEFT SINGLE QUOTATION MARK
                case 146: // RIGHT SINGLE QUOTATION MARK
                    output.append('\'');
                    break;
                case 147: // LEFT DOUBLE QUOTATION MARK
                case 148: // RIGHT DOUBLE QUOTATION MARK
                    output.append('"');
                    break;
                case 150: // EN DASH
                    output.append("-");
                    break;
                case 183: // MIDDLE DOT
                    output.append("- ");
                    break;
                default:
                    output.append(c);
            }
        }
        return output.toString();
    }
}
