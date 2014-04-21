package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

import java.io.File;
import java.util.Date;

/**
 * @since v3.13
 */
public class TestAttachmentTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransform()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");

        Date attachDate = new Date();
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setId("1");
        externalAttachment.setIssueId("2");
        externalAttachment.setAttachedDate(attachDate);
        externalAttachment.setAttacher("admin");
        externalAttachment.setAttachedFile(new File("/tmp"));
        externalAttachment.setFileName("test.txt");

        AttachmentTransformerImpl attachmentTransformer = new AttachmentTransformerImpl();
        final ExternalAttachment transformedAttachment = attachmentTransformer.transform(projectImportMapper, externalAttachment);
        assertNull(transformedAttachment.getId());
        assertEquals("102", transformedAttachment.getIssueId());
        assertEquals(attachDate, transformedAttachment.getAttachedDate());
        assertEquals(externalAttachment.getAttacher(), transformedAttachment.getAttacher());
        assertEquals(externalAttachment.getAttachedFile(), transformedAttachment.getAttachedFile());
        assertEquals(externalAttachment.getFileName(), transformedAttachment.getFileName());
    }

}
