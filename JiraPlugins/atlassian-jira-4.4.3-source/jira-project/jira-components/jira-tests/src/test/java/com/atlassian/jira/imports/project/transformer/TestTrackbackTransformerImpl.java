package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.external.beans.ExternalTrackback;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

import java.util.Date;

/**
 * @since v3.13
 */
public class TestTrackbackTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransformNoIssueMapping()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalTrackback externalTrackback = new ExternalTrackback("12", "100", "http://whatever", "BlogMe", "Why it sux", "It does", new Date(0));

        TrackbackTransformerImpl trackbackTransformer = new TrackbackTransformerImpl();
        assertNull(trackbackTransformer.transform(projectImportMapper, externalTrackback).getIssueId());
    }
    
    @Test
    public void testTransform()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("100", "200");

        ExternalTrackback externalTrackback = new ExternalTrackback("12", "100", "http://whatever", "BlogMe", "Why it sux", "It does", new Date(0));

        TrackbackTransformerImpl trackbackTransformer = new TrackbackTransformerImpl();
        ExternalTrackback transformed = trackbackTransformer.transform(projectImportMapper, externalTrackback);
        assertNull(transformed.getId());
        assertEquals("200", transformed.getIssueId());
        assertEquals("BlogMe", transformed.getBlogName());
        assertEquals(new Date(0), transformed.getCreated());
        assertEquals("Why it sux", transformed.getExcerpt());
        assertEquals("It does", transformed.getTitle());
        assertEquals("http://whatever", transformed.getUrl());
    }
}
