package com.atlassian.core.filters.legacy;

import junit.framework.TestCase;
import com.atlassian.core.filters.StubEncodingFilter;
import com.atlassian.core.filters.ServletStubs;
import com.atlassian.core.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;
import java.util.HashMap;

public class TestWordCurlyQuotesRequestWrapper extends TestCase
{
    private static final String TEST_STRING = getUnicodePunctuationCharacters() + getLatin1PunctuationCharacters();
    private static final String ESCAPED_TEST_STRING_UTF_8 = StringUtils.escapeCP1252(TEST_STRING, "UTF-8");
    private static final String ESCAPED_TEST_STRING_WINDOWS_1252 = StringUtils.escapeCP1252(TEST_STRING, "Windows-1252");
    private static final String ESCAPED_TEST_STRING_LATIN_1 = StringUtils.escapeCP1252(TEST_STRING, "ISO-8859-1");

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

        assertEquals(ESCAPED_TEST_STRING_UTF_8, result.get("param"));
    }

    public void testPunctuationRemovedByGetParameterValues() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.doFilter(request, response, filterChain);

        String[] values = (String[]) result.get("paramValues");
        assertEquals(2, values.length);
        assertEquals(ESCAPED_TEST_STRING_UTF_8, values[0]);
        assertEquals(ESCAPED_TEST_STRING_UTF_8, values[1]);
    }

    public void testPunctuationRemovedByGetParameterMap() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.doFilter(request, response, filterChain);

        //noinspection unchecked
        Map<String, String[]> map = (Map<String, String[]>) result.get("paramMap");
        assertEquals(2, map.size());
        assertEquals(1, map.get("param").length);
        assertEquals(ESCAPED_TEST_STRING_UTF_8, map.get("param")[0]);
        assertEquals(2, map.get("paramValues").length);
        assertEquals(ESCAPED_TEST_STRING_UTF_8, map.get("paramValues")[0]);
        assertEquals(ESCAPED_TEST_STRING_UTF_8, map.get("paramValues")[1]);
    }

    public void testWindows1252PunctuationRemovedFromParametersInLatin1() throws Exception
    {
        encodingFilter.setEncoding("ISO-8859-1");
        encodingFilter.doFilter(request, response, filterChain);

        assertEquals(ESCAPED_TEST_STRING_LATIN_1, result.get("param"));
    }

    public void testUnicodePunctuationRemovedFromParametersInWindows1252() throws Exception
    {
        encodingFilter.setEncoding("Windows-1252");
        encodingFilter.doFilter(request, response, filterChain);

        assertEquals(ESCAPED_TEST_STRING_WINDOWS_1252, result.get("param"));
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
}
