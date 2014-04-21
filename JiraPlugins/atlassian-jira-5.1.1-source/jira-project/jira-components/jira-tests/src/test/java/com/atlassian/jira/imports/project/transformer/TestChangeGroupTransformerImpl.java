package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

import java.util.Date;

/**
 * @since v3.13
 */
public class TestChangeGroupTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransformNoIssueMapping()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setAuthor("Fred");
        externalChangeGroup.setCreated(new Date(0));
        externalChangeGroup.setId("12");
        externalChangeGroup.setIssueId("100");

        ChangeGroupTransformerImpl changeGroupTransformer = new ChangeGroupTransformerImpl();
        assertNull(changeGroupTransformer.transform(projectImportMapper, externalChangeGroup).getIssueId());
    }
    
    @Test
    public void testTransform()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("100", "200");

        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setAuthor("Fred");
        externalChangeGroup.setCreated(new Date(0));
        externalChangeGroup.setId("12");
        externalChangeGroup.setIssueId("100");

        ChangeGroupTransformerImpl changeGroupTransformer = new ChangeGroupTransformerImpl();
        ExternalChangeGroup transformed = changeGroupTransformer.transform(projectImportMapper, externalChangeGroup);
        assertNull(transformed.getId());
        assertEquals("200", transformed.getIssueId());
        assertEquals(new Date(0), transformed.getCreated());
        assertEquals("Fred", transformed.getAuthor());
    }
}
