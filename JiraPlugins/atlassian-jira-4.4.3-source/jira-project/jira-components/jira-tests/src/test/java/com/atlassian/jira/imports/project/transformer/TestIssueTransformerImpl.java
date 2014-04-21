package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;

import java.sql.Timestamp;

/**
 * @since v3.13
 */
public class TestIssueTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransformIssue()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getStatusMapper().mapValue("1", "2");
        projectImportMapper.getPriorityMapper().mapValue("3", "4");
        projectImportMapper.getResolutionMapper().mapValue("5", "6");
        projectImportMapper.getIssueTypeMapper().mapValue("7", "8");
        projectImportMapper.getIssueSecurityLevelMapper().mapValue("9", "10");
        projectImportMapper.getProjectMapper().mapValue("11", "12");

        ExternalIssue externalIssue = new ExternalIssueImpl();
        externalIssue.setStatus("1");
        externalIssue.setPriority("3");
        externalIssue.setResolution("5");
        externalIssue.setIssueType("7");
        externalIssue.setSecurityLevel("9");
        externalIssue.setProject("11");
        externalIssue.setDescription("I am desc");
        externalIssue.setKey("TST-1");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        externalIssue.setResolutionDate(now);

        IssueTransformerImpl issueTransformer = new IssueTransformerImpl();
        final ExternalIssue newIssue = issueTransformer.transform(projectImportMapper, externalIssue);
        assertEquals("2", newIssue.getStatus());
        assertEquals("4", newIssue.getPriority());
        assertEquals("6", newIssue.getResolution());
        assertEquals("8", newIssue.getIssueType());
        assertEquals("10", newIssue.getSecurityLevel());
        assertEquals("12", newIssue.getProject());
        assertEquals("I am desc", newIssue.getDescription());
        assertEquals("TST-1", newIssue.getKey());
        assertEquals(now, newIssue.getResolutionDate());
        assertNull(newIssue.getId());
    }

    @Test
    public void testTransformIssueNoResolutionDate()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getStatusMapper().mapValue("1", "2");
        projectImportMapper.getPriorityMapper().mapValue("3", "4");
        projectImportMapper.getResolutionMapper().mapValue("5", "6");
        projectImportMapper.getIssueTypeMapper().mapValue("7", "8");
        projectImportMapper.getIssueSecurityLevelMapper().mapValue("9", "10");
        projectImportMapper.getProjectMapper().mapValue("11", "12");

        ExternalIssue externalIssue = new ExternalIssueImpl();
        externalIssue.setStatus("1");
        externalIssue.setPriority("3");
        externalIssue.setResolution("5");
        externalIssue.setIssueType("7");
        externalIssue.setSecurityLevel("9");
        externalIssue.setProject("11");
        externalIssue.setDescription("I am desc");
        externalIssue.setKey("TST-1");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        externalIssue.setUpdated(now);

        IssueTransformerImpl issueTransformer = new IssueTransformerImpl();
        final ExternalIssue newIssue = issueTransformer.transform(projectImportMapper, externalIssue);
        assertEquals("2", newIssue.getStatus());
        assertEquals("4", newIssue.getPriority());
        assertEquals("6", newIssue.getResolution());
        assertEquals("8", newIssue.getIssueType());
        assertEquals("10", newIssue.getSecurityLevel());
        assertEquals("12", newIssue.getProject());
        assertEquals("I am desc", newIssue.getDescription());
        assertEquals("TST-1", newIssue.getKey());
        assertEquals(now, newIssue.getUpdated());
        //Resolution date should have been set to the last updated
        assertEquals(now, newIssue.getResolutionDate());
        assertNull(newIssue.getId());
    }
}
