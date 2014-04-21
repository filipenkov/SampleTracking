package com.atlassian.jira.imports.project.handler;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestAttachmentFileValidatorHandler extends ListeningTestCase
{
    @Test
    public void testHandleNoAttachmentPath() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final String attachmentPath = null;
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nHelper());

        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertFalse(attachmentFileValidatorHandler.getValidationResults().hasAnyWarnings());
        mockBackupSystemInformationControl.verify();
    }

    @Test
    public void testHandleWrongEntity() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nHelper());

        attachmentFileValidatorHandler.handleEntity("SOME_ENTITY", null);

        assertFalse(attachmentFileValidatorHandler.getValidationResults().hasAnyWarnings());
        mockBackupSystemInformationControl.verify();
    }

    @Test
    public void testHandleNullAttachment() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(null);
        mockAttachmentParserControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nHelper())
        {
            public AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertFalse(attachmentFileValidatorHandler.getValidationResults().hasAnyWarnings());
        mockAttachmentParserControl.verify();
        mockBackupSystemInformationControl.verify();
    }

    @Test
    public void testHandleAttachmentNotInProject() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(14)));
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformationControl.replay();

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nHelper())
        {
            public AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertFalse(attachmentFileValidatorHandler.getValidationResults().hasAnyWarnings());
        mockAttachmentParserControl.verify();
        mockBackupSystemInformationControl.verify();
    }

    @Test
    public void testFileDoesNotExist() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParser.getFileAttachmentUrl(externalAttachment, attachmentPath, "TST", "TST-1");
        mockAttachmentParserControl.setReturnValue("/a/path/that/will/never/exist");
        mockAttachmentParserControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nBean())
        {
            public AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertTrue(attachmentFileValidatorHandler.getValidationResults().hasAnyWarnings());
        assertEquals("The attachment 'test.txt' does not exist at '/a/path/that/will/never/exist'. It will not be imported.", attachmentFileValidatorHandler.getValidationResults().getWarningMessages().iterator().next());
        mockAttachmentParserControl.verify();
        mockBackupSystemInformationControl.verify();
    }

    @Test
    public void testFileDoesNotExistNoMoreThan20Warnings() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final MockControl mockBackupSystemInformationControl = MockControl.createControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final MockControl mockAttachmentParserControl = MockControl.createControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParser.getFileAttachmentUrl(externalAttachment, attachmentPath, "TST", "TST-1");
        mockAttachmentParserControl.setReturnValue("/a/path/that/will/never/exist");
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParser.getFileAttachmentUrl(externalAttachment, attachmentPath, "TST", "TST-1");
        mockAttachmentParserControl.setReturnValue("/a/path/that/will/never/exist");
        mockAttachmentParserControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nBean())
        {
            public AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        // Add 20 warnings
        for (int i = 0; i < 20; i++)
        {
            attachmentFileValidatorHandler.getValidationResults().addWarningMessage(i + "");
        }

        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertEquals(1, attachmentFileValidatorHandler.getValidationResults().getWarningMessages().size());
        assertTrue(attachmentFileValidatorHandler.getValidationResults().getWarningMessages().contains("There are more than twenty attachment entries that do not exist in the attachment directory. See your logs for full details."));
        mockAttachmentParserControl.verify();
        mockBackupSystemInformationControl.verify();
    }

    @Test
    public void testHandleHappyPath() throws Exception
    {
        File dir = null;
        File tempFile = null;
        try
        {
            final ExternalProject project = new ExternalProject();
            project.setKey("TST");
            BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                    new ArrayList(), EasyList.build(new Long(12), new Long(14)));
            dir = new File(System.getProperty("java.io.tmpdir") + File.separator + "TST" + File.separator + "TST-1");
            dir.deleteOnExit();
            dir.mkdirs();
            tempFile = File.createTempFile("test", ".txt", dir);
            final String attachmentPath = System.getProperty("java.io.tmpdir");
            ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
            final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
            final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
            mockBackupSystemInformation.getIssueKeyForId("12");
            mockBackupSystemInformationControl.setReturnValue("TST-1");
            mockBackupSystemInformationControl.replay();

            ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
            final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
            final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
            mockAttachmentParser.parse(null);
            mockAttachmentParserControl.setReturnValue(externalAttachment);
            mockAttachmentParser.getFileAttachmentUrl(externalAttachment, attachmentPath, "TST", "TST-1");
            mockAttachmentParserControl.setReturnValue(tempFile.getAbsolutePath());
            mockAttachmentParserControl.replay();

            AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, mockBackupSystemInformation, new MockI18nHelper())
            {
                public AttachmentParser getAttachmentParser()
                {
                    return mockAttachmentParser;
                }
            };

            attachmentFileValidatorHandler.startDocument();
            attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

            assertFalse(attachmentFileValidatorHandler.getValidationResults().hasAnyWarnings());
            mockAttachmentParserControl.verify();
            mockBackupSystemInformationControl.verify();
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
            if (dir != null)
            {
                dir.delete();
            }
        }
    }

    @Test
    public void testProjectDirectoryDoesNotExist() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));

        final String attachmentPath = System.getProperty("java.io.tmpdir");

        // Delete the TST directory under /tmp if it exists
        File tstDirectory = new File(attachmentPath + File.separator + "TST");
        if (tstDirectory.exists())
        {
            tstDirectory.delete();
        }

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(null);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, null, new MockI18nBean())
        {
            public AttachmentParser getAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        MessageSetAssert.assert1WarningNoErrors(attachmentFileValidatorHandler.getValidationResults(), "The provided attachment path does not contain a sub-directory called 'TST'. If you proceed with the import attachments will not be included.");
        mockAttachmentParserControl.verify();
    }

    @Test
    public void testProjectDirectoryDoesNotExistNoAttachments() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));

        final String attachmentPath = System.getProperty("java.io.tmpdir");

        // Delete the TST directory under /tmp if it exists
        File tstDirectory = new File(attachmentPath + File.separator + "TST");
        if (tstDirectory.exists())
        {
            tstDirectory.delete();
        }

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(backupProject, projectImportOptions, null, new MockI18nBean());

        attachmentFileValidatorHandler.startDocument();

        assertFalse(attachmentFileValidatorHandler.getValidationResults().hasAnyMessages());
    }
    
}
