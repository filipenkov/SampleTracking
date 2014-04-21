package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.sun.jersey.api.client.WebResource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.REST;

/**
 * Test the broad shape of an issue (the right objects/properties appear, and are the right value)
 * This does not use any JSON bean deserialization, so we avoid any bugs that mask themselves
 * (e.g. dates being output then parsed as longs instead of strings)
 */
@WebTest ( { FUNC_TEST, REST })
public class TestIssueShape extends RestFuncTest
{

    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    public void testGeneralShape()
    {
        administration.restoreData("TestIssueShape.xml");

        WebResource resource = issueClient.issueResource("TST-1");
        Object json = resource.get(Object.class);
        Map<String, ?> fields = asObject(getAt(json, "fields"));

        assertEquals("A critical bug", asString(getAt(fields, "summary")));
        assertEquals("blah", asString(getAt(fields, "description")));

        assertEquals("1", asString(getAt(fields, "issuetype", "id")));
        assertEndsWithEquals("/rest/api/2/issuetype/1", asString(getAt(fields, "issuetype", "self")));
        assertEquals("A problem which impairs or prevents the functions of the product.", asString(getAt(fields, "issuetype", "description")));
        assertEndsWithEquals("/images/icons/bug.gif", asString(getAt(fields, "issuetype", "iconUrl")));
        assertEquals("Bug", asString(getAt(fields, "issuetype", "name")));
        assertEquals(false, asBoolean(getAt(fields, "issuetype", "subtask")));

        assertEndsWithEquals("/rest/api/2/issue/TST-1/votes", asString(getAt(fields, "votes", "self")));
        assertEquals(0, asNumber(getAt(fields, "votes", "votes")));
        assertEquals(false, asBoolean(getAt(fields, "votes", "hasVoted")));

        assertEndsWithEquals("/rest/api/2/securitylevel/10001", asString(getAt(fields, "security", "self")));
        assertEquals("10001", asString(getAt(fields, "security", "id")));
        assertEquals("", asString(getAt(fields, "security", "description")));
        assertEquals("lvl2", asString(getAt(fields, "security", "name")));

        assertEndsWithEquals("/rest/api/2/version/10001", asString(getAt(fields, "fixVersions", 0, "self")));
        assertEquals("10001", asString(getAt(fields, "fixVersions", 0, "id")));
        assertEquals("", asString(getAt(fields, "fixVersions", 0, "description")));
        assertEquals("v2", asString(getAt(fields, "fixVersions", 0, "name")));
        assertEquals("2011-09-23", asDateString(getAt(fields, "fixVersions", 0, "releaseDate")));
        assertEquals(false, asBoolean(getAt(fields, "fixVersions", 0, "archived")));
        assertEquals(false, asBoolean(getAt(fields, "fixVersions", 0, "released")));

        assertEndsWithEquals("/rest/api/2/resolution/1", asString(getAt(fields, "resolution", "self")));
        assertEquals("1", asString(getAt(fields, "resolution", "id")));
        assertEquals("A fix for this issue is checked into the tree and tested.", asString(getAt(fields, "resolution", "description")));
        assertEquals("Fixed", asString(getAt(fields, "resolution", "name")));

        assertEquals(asDateTime("2011-07-27T12:56:01.847+1000"), asDateTime(getAt(fields, "resolutiondate")));
        assertEquals(asDateTime("2011-06-29T16:40:56.287+1000"), asDateTime(getAt(fields, "created")));
        assertEquals(asDateTime("2011-09-23T10:33:34.794+1000"), asDateTime(getAt(fields, "updated")));
        assertEquals("2012-02-29", asDateString(getAt(fields, "duedate")));

        assertEndsWithEquals("/rest/api/2/user?username=fry", asString(getAt(fields, "reporter", "self")));
        assertEquals("fry", asString(getAt(fields, "reporter", "name")));
        assertEquals("fry@example.com", asString(getAt(fields, "reporter", "emailAddress")));
        assertEndsWithEquals("/secure/useravatar?size=small&avatarId=10062", asString(getAt(fields, "reporter", "avatarUrls", "16x16")));
        assertEndsWithEquals("/secure/useravatar?avatarId=10062", asString(getAt(fields, "reporter", "avatarUrls", "48x48")));
        assertEquals("Phillip J. Fry", asString(getAt(fields, "reporter", "displayName")));
        assertEquals(true, asBoolean(getAt(fields, "reporter", "active")));

        assertEquals("bar", asString(getAt(fields, "labels", 0)));
        assertEquals("foo", asString(getAt(fields, "labels", 1)));

        assertEquals(0, asNumber(getAt(fields, "comment", "startAt")));
        assertEquals(1, asNumber(getAt(fields, "comment", "maxResults")));
        assertEquals(1, asNumber(getAt(fields, "comment", "total")));
        assertEndsWithEquals("/rest/api/2/issue/10000/comment/10000", asString(getAt(fields, "comment", "comments", 0, "self")));
        assertEquals("10000", asString(getAt(fields, "comment", "comments", 0, "id")));
        assertEquals("foo", asString(getAt(fields, "comment", "comments", 0, "body")));
        assertEquals(asDateTime("2011-09-23T10:33:16.309+1000"), asDateTime(getAt(fields, "comment", "comments", 0, "created")));
        assertEquals(asDateTime("2011-09-23T10:33:16.309+1000"), asDateTime(getAt(fields, "comment", "comments", 0, "updated")));
        assertEquals("admin", asString(getAt(fields, "comment", "comments", 0, "author", "name")));
        assertEquals("admin", asString(getAt(fields, "comment", "comments", 0, "updateAuthor", "name")));

    }

    private void assertEndsWithEquals(String expected, String actual)
    {
        assertNotNull(actual);
        assertTrue(actual + " ends with '" + expected + "'", actual.endsWith(expected));
    }

    private Object getAt(Object o, Object... path)
    {
        for (Object key : path)
        {
            if (key instanceof String)
            {
                String s = (String) key;
                Map<String, ?> obj = asObject(o);
                assertTrue(s + " in " + obj, obj.containsKey(s));
                o = obj.get(s);
            }
            else if (key instanceof Integer)
            {
                int i = (Integer) key;
                List<?> array = asArray(o);
                assertTrue(i + " indexed into " + array, 0 <= i && i < array.size());
                o = array.get(i);
            }
            else
            {
                fail("unknow key type " + key);
            }
        }
        return o;

    }

    private Map<String, ?> asObject(Object o)
    {
        assertTrue(o instanceof Map);
        return (Map<String, ?>) o;
    }
    private List<?> asArray(Object o)
    {
        assertTrue(o instanceof List);
        return (List<?>) o;
    }
    private String asString(Object o)
    {
        assertTrue(o instanceof String);
        return (String) o;
    }
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d\\d\\d\\d-\\d\\d-\\d\\d$");
    private String asDateString(Object o)
    {
        String s = asString(o);
        assertTrue(s, DATE_PATTERN.matcher(s).matches());
        return s;
    }
    private static final Pattern DATETIME_PATTERN = Pattern.compile("^\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d[+-]\\d\\d\\d\\d$");
    private Date asDateTime(Object o)
    {
        String s = asString(o);
        assertTrue(s, DATETIME_PATTERN.matcher(s).matches());
        try
        {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(s);
        }
        catch (ParseException e)
        {
            fail(e.getMessage());
            return null;
        }
    }
    private boolean asBoolean(Object o)
    {
        assertTrue(o instanceof Boolean);
        return ((Boolean) o).booleanValue();
    }
    private Number asNumber(Object o)
    {
        assertTrue(o instanceof Number);
        return (Number) o;
    }
}
