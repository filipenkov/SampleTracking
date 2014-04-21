package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.external.beans.ExternalComment;

import java.util.Date;

/**
 * @since v3.13
 */
public class TestCommentTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransform()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");
        projectImportMapper.getProjectRoleMapper().mapValue("3", "103");

        ExternalComment externalComment = new ExternalComment();
        externalComment.setBody("I comment on stuff.");
        externalComment.setGroupLevel("dudes");
        externalComment.setId("1");
        externalComment.setIssueId("2");
        externalComment.setRoleLevelId(new Long(3));
        final Date timePerformed = new Date();
        externalComment.setTimePerformed(timePerformed);
        externalComment.setUpdateAuthor("someone");
        externalComment.setUpdated(new Date(3));
        externalComment.setUsername("fred");

        CommentTransformerImpl commentTransformer = new CommentTransformerImpl();
        final ExternalComment newComment = commentTransformer.transform(projectImportMapper, externalComment);
        assertEquals("I comment on stuff.", newComment.getBody());
        assertEquals("dudes", newComment.getGroupLevel());
        assertEquals("102", newComment.getIssueId());
        assertEquals(new Long(103), newComment.getRoleLevelId());
        assertEquals(timePerformed, newComment.getTimePerformed());
        assertEquals("someone", newComment.getUpdateAuthor());
        assertEquals(new Date(3), newComment.getUpdated());
        assertEquals("fred", newComment.getUsername());
        assertNull(newComment.getId());
    }

    @Test
    public void testTransformWithNulls()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");
        projectImportMapper.getProjectRoleMapper().mapValue("3", "103");

        ExternalComment externalComment = new ExternalComment();
        externalComment.setBody("I comment on stuff.");
        externalComment.setGroupLevel(null);
        externalComment.setId("1");
        externalComment.setIssueId("2");
        externalComment.setRoleLevelId(null);
        externalComment.setTimePerformed(null);
        externalComment.setUpdateAuthor(null);
        externalComment.setUpdated(null);
        externalComment.setUsername("fred");

        CommentTransformerImpl commentTransformer = new CommentTransformerImpl();
        final ExternalComment newComment = commentTransformer.transform(projectImportMapper, externalComment);
        assertEquals("I comment on stuff.", newComment.getBody());
        assertNull(newComment.getGroupLevel());
        assertEquals("102", newComment.getIssueId());
        assertNull(newComment.getRoleLevelId());
        assertNull(newComment.getTimePerformed());
        assertNull(newComment.getUpdateAuthor());
        assertNull(newComment.getUpdated());
        assertEquals("fred", newComment.getUsername());
        assertNull(newComment.getId());
    }

}
