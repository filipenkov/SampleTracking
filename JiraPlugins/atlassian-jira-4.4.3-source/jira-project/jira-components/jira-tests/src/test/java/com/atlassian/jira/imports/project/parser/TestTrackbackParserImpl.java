package com.atlassian.jira.imports.project.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalTrackback;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.local.ListeningTestCase;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * @since v3.13
 */
public class TestTrackbackParserImpl extends ListeningTestCase
{
    TrackbackParserImpl trackbackParser = new TrackbackParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            trackbackParser.parse(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseMissingId()
    {
        try
        {
            //<TrackbackPing id="10000" issue="10001" url="http://localhost:8090/jira/browse/TRACKBACK-1" title="[TRACKBACK-1] This is ticket TRACKBACK-1 that tracks back a peer issue" blogname="JIRA: TRACKBACK" excerpt="null http://127.0.0.1:8090/jira/browse/TRACKBACK-2 ok  " created="2008-01-02 23:44:24.573"/>
            final Map attributes = EasyMap.build("issue", "10000", "url", "http://localhost:8090/jira/browse/TRACKBACK-1", "title", "[TRACKBACK-1]", "blogname", "JIRA: TRACKBACK", "excerpt", "blah", "created", "2008-01-02 23:44:24.573");
            trackbackParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingIssueId()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10001", "url", "http://localhost:8090/jira/browse/TRACKBACK-1", "title", "[TRACKBACK-1]", "blogname", "JIRA: TRACKBACK", "excerpt", "blah", "created", "2008-01-02 23:44:24.573");
            trackbackParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingCreatedDate()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "url", "http://localhost:8090/jira/browse/TRACKBACK-1", "title", "[TRACKBACK-1]", "blogname", "JIRA: TRACKBACK", "excerpt", "blah");
            trackbackParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMinimal() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "created", "2008-01-02 23:44:24.573");
        ExternalTrackback trackback = trackbackParser.parse(attributes);
        assertEquals("10001", trackback.getId());
        assertEquals("10000", trackback.getIssueId());
        assertEquals(new Date(Timestamp.valueOf("2008-01-02 23:44:24.573").getTime()), trackback.getCreated());
        assertNull(trackback.getBlogName());
        assertNull(trackback.getExcerpt());
        assertNull(trackback.getTitle());
        assertNull(trackback.getUrl());
    }

    @Test
    public void testParseFull() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "url", "http://localhost:8090/jira/browse/TRACKBACK-1", "title", "[TRACKBACK-1]", "blogname", "JIRA: TRACKBACK", "excerpt", "blah", "created", "2008-01-02 23:44:24.573");
        ExternalTrackback trackback = trackbackParser.parse(attributes);
        assertEquals("10001", trackback.getId());
        assertEquals("10000", trackback.getIssueId());
        assertEquals(new Date(Timestamp.valueOf("2008-01-02 23:44:24.573").getTime()), trackback.getCreated());
        assertEquals("JIRA: TRACKBACK", trackback.getBlogName());
        assertEquals("[TRACKBACK-1]", trackback.getTitle());
        assertEquals("blah", trackback.getExcerpt());
        assertEquals("http://localhost:8090/jira/browse/TRACKBACK-1", trackback.getUrl());
    }

    @Test
    public void testGetEntityRepresentationNullCreatedDate()
    {
        final Date created = new Date();
        ExternalTrackback trackback = new ExternalTrackback("10001", "10000", "http://localhost:8090/jira/browse/TRACKBACK-1", "JIRA: TRACKBACK", "blah", "[TRACKBACK-1]", null);

        final EntityRepresentation representation = trackbackParser.getEntityRepresentation(trackback);
        assertNotNull(representation);
        assertEquals(TrackbackParser.TRACKBACK_ENTITY_NAME, representation.getEntityName());
        assertEquals("10001", representation.getEntityValues().get("id"));
        assertEquals("10000", representation.getEntityValues().get("issue"));
        assertEquals("http://localhost:8090/jira/browse/TRACKBACK-1", representation.getEntityValues().get("url"));
        assertEquals("[TRACKBACK-1]", representation.getEntityValues().get("title"));
        assertEquals("JIRA: TRACKBACK", representation.getEntityValues().get("blogname"));
        assertEquals("blah", representation.getEntityValues().get("excerpt"));
        assertEquals(null, representation.getEntityValues().get("created"));
    }

    @Test
    public void testGetEntityRepresentation()
    {
        final Date created = new Date();
        ExternalTrackback trackback = new ExternalTrackback("10001", "10000", "http://localhost:8090/jira/browse/TRACKBACK-1", "JIRA: TRACKBACK", "blah", "[TRACKBACK-1]", created);

        final EntityRepresentation representation = trackbackParser.getEntityRepresentation(trackback);
        assertNotNull(representation);
        assertEquals(TrackbackParser.TRACKBACK_ENTITY_NAME, representation.getEntityName());
        assertEquals("10001", representation.getEntityValues().get("id"));
        assertEquals("10000", representation.getEntityValues().get("issue"));
        assertEquals("http://localhost:8090/jira/browse/TRACKBACK-1", representation.getEntityValues().get("url"));
        assertEquals("[TRACKBACK-1]", representation.getEntityValues().get("title"));
        assertEquals("JIRA: TRACKBACK", representation.getEntityValues().get("blogname"));
        assertEquals("blah", representation.getEntityValues().get("excerpt"));
        assertEquals(new Timestamp(created.getTime()).toString(), representation.getEntityValues().get("created"));
    }

}
