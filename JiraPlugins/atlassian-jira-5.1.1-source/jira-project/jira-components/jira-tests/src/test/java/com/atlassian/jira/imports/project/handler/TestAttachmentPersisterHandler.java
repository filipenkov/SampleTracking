package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.transformer.AttachmentTransformer;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestAttachmentPersisterHandler extends ListeningTestCase
{
    @Test
    public void testHandle() throws Exception
    {
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setIssueId("12");

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/", "/");

        ExternalProject oldProject = new ExternalProject();
        oldProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(oldProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockAttachmentControl = MockClassControl.createControl(Attachment.class);
        final Attachment mockAttachment = (Attachment) mockAttachmentControl.getMock();
        mockAttachmentControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createAttachment(externalAttachment);
        mockProjectImportPersisterControl.setReturnValue(mockAttachment);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final MockControl mockAttachmentTransformerControl = MockControl.createStrictControl(AttachmentTransformer.class);
        final AttachmentTransformer mockAttachmentTransformer = (AttachmentTransformer) mockAttachmentTransformerControl.getMock();
        mockAttachmentTransformer.transform(projectImportMapper, externalAttachment);
        mockAttachmentTransformerControl.setReturnValue(externalAttachment);
        mockAttachmentTransformerControl.replay();

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParser.getFileAttachmentUrl(externalAttachment, "/", "TST", "TST-1");
        mockAttachmentParserControl.setReturnValue("/some/file.txt");
        mockAttachmentParserControl.replay();

        ProjectImportResults projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());

        AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(mockProjectImportPersister, projectImportOptions, projectImportMapper, backupProject, mockBackupSystemInformation, projectImportResults, new ExecutorForTests())
        {
            AttachmentTransformer getAttachmentTransformer()
            {
                return mockAttachmentTransformer;
            }

            AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }

            boolean fileExists(final File attachedFile)
            {
                return true;
            }
        };
            
        attachmentPersisterHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        attachmentPersisterHandler.handleEntity("SOME_ENTITY", null);

        assertEquals(1, projectImportResults.getAttachmentsCreatedCount());
        assertTrue(projectImportResults.getErrors().isEmpty());

        assertEquals(0, projectImportResults.getErrors().size());
        mockAttachmentParserControl.verify();
        mockAttachmentTransformerControl.verify();
        mockBackupSystemInformationControl.verify();
        mockProjectImportPersisterControl.verify();
        mockAttachmentControl.verify();
    }

    @Test
    public void testHandleAttachmentNotCreated() throws Exception
    {
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setIssueId("12");
        externalAttachment.setFileName("my.file");

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/", "/");

        ExternalProject oldProject = new ExternalProject();
        oldProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(oldProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createAttachment(externalAttachment);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final MockControl mockAttachmentTransformerControl = MockControl.createStrictControl(AttachmentTransformer.class);
        final AttachmentTransformer mockAttachmentTransformer = (AttachmentTransformer) mockAttachmentTransformerControl.getMock();
        mockAttachmentTransformer.transform(projectImportMapper, externalAttachment);
        mockAttachmentTransformerControl.setReturnValue(externalAttachment);
        mockAttachmentTransformerControl.replay();

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParser.getFileAttachmentUrl(externalAttachment, "/", "TST", "TST-1");
        mockAttachmentParserControl.setReturnValue("/some/file.txt");
        mockAttachmentParserControl.replay();

        ProjectImportResults projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());

        AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(mockProjectImportPersister, projectImportOptions, projectImportMapper, backupProject, mockBackupSystemInformation, projectImportResults, new ExecutorForTests())
        {
            AttachmentTransformer getAttachmentTransformer()
            {
                return mockAttachmentTransformer;
            }

            AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }

            boolean fileExists(final File attachedFile)
            {
                return true;
            }
        };

        attachmentPersisterHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        attachmentPersisterHandler.handleEntity("SOME_ENTITY", null);

        assertEquals(0, projectImportResults.getAttachmentsCreatedCount());
        assertEquals(1, projectImportResults.getErrors().size());
        assertEquals("There was a problem saving attachment 'my.file' for issue 'TST-1'.", projectImportResults.getErrors().iterator().next());

        mockAttachmentParserControl.verify();
        mockAttachmentTransformerControl.verify();
        mockBackupSystemInformationControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleIssueNotMapped() throws Exception
    {
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setIssueId(null);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/", "/");

        ExternalProject oldProject = new ExternalProject();
        oldProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(oldProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        final MockControl mockAttachmentTransformerControl = MockControl.createStrictControl(AttachmentTransformer.class);
        final AttachmentTransformer mockAttachmentTransformer = (AttachmentTransformer) mockAttachmentTransformerControl.getMock();
        mockAttachmentTransformerControl.replay();

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        ProjectImportResults projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());

        AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(mockProjectImportPersister, projectImportOptions, projectImportMapper, backupProject, mockBackupSystemInformation, projectImportResults, null)
        {
            AttachmentTransformer getAttachmentTransformer()
            {
                return mockAttachmentTransformer;
            }

            AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }

            boolean fileExists(final File attachedFile)
            {
                return true;
            }
        };

        attachmentPersisterHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        attachmentPersisterHandler.handleEntity("SOME_ENTITY", null);

        assertEquals(0, projectImportResults.getAttachmentsCreatedCount());
        assertTrue(projectImportResults.getErrors().isEmpty());

        mockAttachmentParserControl.verify();
        mockAttachmentTransformerControl.verify();
        mockBackupSystemInformationControl.verify();
        mockProjectImportPersisterControl.verify();
    }
    
    @Test
    public void testHandleNoAttachmentPathSpecified() throws Exception
    {
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setIssueId("12");

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/", null);

        ExternalProject oldProject = new ExternalProject();
        oldProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(oldProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(mockProjectImportPersister, projectImportOptions, projectImportMapper, backupProject, mockBackupSystemInformation, null, null);

        attachmentPersisterHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        mockBackupSystemInformationControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleWrongEntityType() throws Exception
    {
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setIssueId("12");

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/", "/");

        ExternalProject oldProject = new ExternalProject();
        oldProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(oldProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(mockProjectImportPersister, projectImportOptions, projectImportMapper, backupProject, mockBackupSystemInformation, null, null);

        attachmentPersisterHandler.handleEntity("SOME_ENTITY", null);

        mockBackupSystemInformationControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleFileDoesNotExist() throws Exception
    {
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setIssueId("12");

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/", "/");

        ExternalProject oldProject = new ExternalProject();
        oldProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(oldProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final MockControl mockAttachmentTransformerControl = MockControl.createStrictControl(AttachmentTransformer.class);
        final AttachmentTransformer mockAttachmentTransformer = (AttachmentTransformer) mockAttachmentTransformerControl.getMock();
        mockAttachmentTransformerControl.replay();

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParser.getFileAttachmentUrl(externalAttachment, "/", "TST", "TST-1");
        mockAttachmentParserControl.setReturnValue("/some/file.txt");
        mockAttachmentParserControl.replay();

        ProjectImportResults projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(mockProjectImportPersister, projectImportOptions, projectImportMapper, backupProject, mockBackupSystemInformation, projectImportResults, null)
        {
            AttachmentTransformer getAttachmentTransformer()
            {
                return mockAttachmentTransformer;
            }

            AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }

            boolean fileExists(final File attachedFile)
            {
                return false;
            }
        };
            
        attachmentPersisterHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertEquals(0, projectImportResults.getErrors().size());

        mockAttachmentParserControl.verify();
        mockAttachmentTransformerControl.verify();
        mockBackupSystemInformationControl.verify();
        mockProjectImportPersisterControl.verify();
    }

}
