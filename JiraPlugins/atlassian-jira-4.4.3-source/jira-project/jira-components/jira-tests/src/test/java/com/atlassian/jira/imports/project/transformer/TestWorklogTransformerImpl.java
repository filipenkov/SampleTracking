package com.atlassian.jira.imports.project.transformer;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Date;

/**
 * @since v3.13
 */
public class TestWorklogTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransform()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");
        projectImportMapper.getProjectRoleMapper().mapValue("3", "103");

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setComment("I worklog on stuff.");
        externalWorklog.setGroupLevel("dudes");
        externalWorklog.setId("1");
        externalWorklog.setIssueId("2");
        externalWorklog.setRoleLevelId(new Long(3));
        final Date timePerformed = new Date();
        externalWorklog.setCreated(timePerformed);
        externalWorklog.setUpdateAuthor("someone");
        externalWorklog.setUpdated(new Date(3));
        externalWorklog.setAuthor("fred");
        externalWorklog.setTimeSpent(new Long(123));

        WorklogTransformerImpl worklogTransformer = new WorklogTransformerImpl();
        final ExternalWorklog newWorklog = worklogTransformer.transform(projectImportMapper, externalWorklog);
        assertEquals("I worklog on stuff.", newWorklog.getComment());
        assertEquals("dudes", newWorklog.getGroupLevel());
        assertEquals("102", newWorklog.getIssueId());
        assertEquals(new Long(103), newWorklog.getRoleLevelId());
        assertEquals(timePerformed, newWorklog.getCreated());
        assertEquals("someone", newWorklog.getUpdateAuthor());
        assertEquals(new Date(3), newWorklog.getUpdated());
        assertEquals("fred", newWorklog.getAuthor());
        assertEquals(new Long(123), newWorklog.getTimeSpent());
        assertNull(newWorklog.getId());
    }

    @Test
    public void testTransformWithNulls()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");
        projectImportMapper.getProjectRoleMapper().mapValue("3", "103");

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setComment("I worklog on stuff.");
        externalWorklog.setGroupLevel(null);
        externalWorklog.setId("1");
        externalWorklog.setIssueId("2");
        externalWorklog.setRoleLevelId(null);
        externalWorklog.setCreated(null);
        externalWorklog.setUpdateAuthor(null);
        externalWorklog.setUpdated(null);
        externalWorklog.setAuthor("fred");
        externalWorklog.setTimeSpent(new Long(123));


        WorklogTransformerImpl worklogTransformer = new WorklogTransformerImpl();
        final ExternalWorklog newWorklog = worklogTransformer.transform(projectImportMapper, externalWorklog);
        assertEquals("I worklog on stuff.", newWorklog.getComment());
        assertNull(newWorklog.getGroupLevel());
        assertEquals("102", newWorklog.getIssueId());
        assertNull(newWorklog.getRoleLevelId());
        assertNull(newWorklog.getCreated());
        assertNull(newWorklog.getUpdateAuthor());
        assertNull(newWorklog.getUpdated());
        assertEquals("fred", newWorklog.getAuthor());
        assertEquals(new Long(123), newWorklog.getTimeSpent());
        assertNull(newWorklog.getId());
    }

}
