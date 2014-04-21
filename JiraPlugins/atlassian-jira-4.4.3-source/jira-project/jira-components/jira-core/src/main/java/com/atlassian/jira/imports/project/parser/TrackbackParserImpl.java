package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalTrackback;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class TrackbackParserImpl implements TrackbackParser
{
    private static final String ID = "id";
    private static final String ISSUE = "issue";
    private static final String URL = "url";
    private static final String TITLE = "title";
    private static final String CREATED = "created";
    private static final String EXCERPT = "excerpt";
    private static final String BLOGNAME = "blogname";

    public ExternalTrackback parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        //<TrackbackPing id="10000" issue="10001" url="http://localhost:8090/jira/browse/TRACKBACK-1" title="[TRACKBACK-1] This is ticket TRACKBACK-1 that tracks back a peer issue" blogname="JIRA: TRACKBACK" excerpt="null http://127.0.0.1:8090/jira/browse/TRACKBACK-2 ok  " created="2008-01-02 23:44:24.573"/>
        final String id = (String) attributes.get(ID);
        final String issueId = (String) attributes.get(ISSUE);
        final String url = (String) attributes.get(URL);
        final String excerpt = (String) attributes.get(EXCERPT);
        final String title = (String) attributes.get(TITLE);
        final String blogName = (String) attributes.get(BLOGNAME);
        final String created = (String) attributes.get(CREATED);

        //Validate the values
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("A file attachment must have an id specified.");
        }
        if (StringUtils.isEmpty(issueId))
        {
            throw new ParseException("A file attachment with id '" + id + "' must have an issue id specified.");
        }
        if (StringUtils.isEmpty(created))
        {
            throw new ParseException("A file attachment with id '" + id + "' must have a create date specified.");
        }

        final Date createdDate = java.sql.Timestamp.valueOf(created);

        return new ExternalTrackback(id, issueId, url, blogName, excerpt, title, createdDate);
    }

    public EntityRepresentation getEntityRepresentation(final ExternalTrackback trackback)
    {
        final Map attributes = new HashMap();
        attributes.put(ID, trackback.getId());
        attributes.put(ISSUE, trackback.getIssueId());
        attributes.put(URL, trackback.getUrl());
        attributes.put(EXCERPT, trackback.getExcerpt());
        attributes.put(TITLE, trackback.getTitle());
        attributes.put(BLOGNAME, trackback.getBlogName());

        if (trackback.getCreated() != null)
        {
            attributes.put(CREATED, new Timestamp(trackback.getCreated().getTime()).toString());
        }

        return new EntityRepresentationImpl(TRACKBACK_ENTITY_NAME, attributes);
    }
}
