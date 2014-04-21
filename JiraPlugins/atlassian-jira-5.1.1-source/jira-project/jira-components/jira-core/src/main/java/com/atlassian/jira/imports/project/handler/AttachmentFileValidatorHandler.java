package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.parser.AttachmentParserImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * This handler inspects attachment entries and if the user is importing attachments will check to see that the
 * attachment file exists for the corresponding database entry.
 *
 * Any attachments that are not found will cause a warning to be generated and placed into the MessageSet.
 *
 * @since v3.13
 */
public class AttachmentFileValidatorHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(AttachmentFileValidatorHandler.class);
    private static final int MAX_WARNINGS = 20;

    private MessageSet messageSet;
    private final BackupProject backupProject;
    private final ProjectImportOptions projectImportOptions;
    private final BackupSystemInformation backupSystemInformation;
    private final I18nHelper i18nHelper;
    private AttachmentParser attachmentParser;
    private boolean projectAttachmentDirExists;
    private boolean maxWarningsExceeded;
    private int validAttachmentCount = 0;

    public AttachmentFileValidatorHandler(final BackupProject backupProject, final ProjectImportOptions projectImportOptions, final BackupSystemInformation backupSystemInformation, final I18nHelper i18nHelper)
    {
        this.backupProject = backupProject;
        this.projectImportOptions = projectImportOptions;
        this.backupSystemInformation = backupSystemInformation;
        this.i18nHelper = i18nHelper;
        messageSet = new MessageSetImpl();
        projectAttachmentDirExists = true;
        maxWarningsExceeded = false;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // We only need to validate the attachment files if an attachment path has been provided
        // The service/manager should have short-circuited this handler anyway if there was no path, but lets be defensive.
        if (StringUtils.isNotEmpty(projectImportOptions.getAttachmentPath()))
        {
            if (AttachmentParser.ATTACHMENT_ENTITY_NAME.equals(entityName))
            {
                final ExternalAttachment externalAttachment = getAttachmentParser().parse(attributes);
                if ((externalAttachment != null) && backupProject.containsIssue(externalAttachment.getIssueId()))
                {
                    if (!projectAttachmentDirExists)
                    {
                        getValidationResults().addWarningMessage(
                            i18nHelper.getText("admin.project.import.attachment.project.directory.does.not.exist",
                                backupProject.getProject().getKey()));
                        getValidationResults().addWarningMessageInEnglish(
                            "The provided attachment path does not contain a sub-directory called '" + backupProject.getProject().getKey() + "'. If you proceed with the import attachments will not be included.");
                    }
                    else
                    {
                        final String fileAttachmentUrl = getAttachmentParser().getFileAttachmentUrl(externalAttachment,
                            projectImportOptions.getAttachmentPath(), backupProject.getProject().getKey(),
                            backupSystemInformation.getIssueKeyForId(externalAttachment.getIssueId()));
                        final File attachmentFile = new File(fileAttachmentUrl);
                        if (attachmentFile.exists())
                        {
                            validAttachmentCount++;
                        }
                        else
                        {
                            log.warn("The attachment '" + externalAttachment.getFileName() + "' does not exist at '" + fileAttachmentUrl + "'. It will not be imported.");
                            // We only want to add 20 warnings so as not to clutter the UI
                            if (getValidationResults().getWarningMessages().size() >= MAX_WARNINGS)
                            {
                                maxWarningsExceeded = true;
                                messageSet = new MessageSetImpl();
                                getValidationResults().addWarningMessage(i18nHelper.getText("admin.project.import.attachment.too.many.warnings"));
                            }
                            if (!maxWarningsExceeded)
                            {
                                // JRA-15914 Missing filename in XML file.
                                if (externalAttachment.getFileName() == null || externalAttachment.getFileName().length() == 0)
                                {
                                    getValidationResults().addWarningMessage(
                                        i18nHelper.getText("admin.project.import.attachment.missing.filename", externalAttachment.getId(),
                                            fileAttachmentUrl));
                                }
                                else
                                {
                                    getValidationResults().addWarningMessage(
                                        i18nHelper.getText("admin.project.import.attachment.does.not.exist", externalAttachment.getFileName(),
                                            fileAttachmentUrl));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public MessageSet getValidationResults()
    {
        return messageSet;
    }

    public int getValidAttachmentCount()
    {
        return validAttachmentCount;
    }

    public void startDocument()
    {
        // Do a check to see if the project directory exists in the attachment directory
        if (StringUtils.isNotEmpty(projectImportOptions.getAttachmentPath()))
        {
            final File attachmentDirectory = new File(projectImportOptions.getAttachmentPath() + File.separator + backupProject.getProject().getKey());
            if (!attachmentDirectory.exists())
            {
                // Set a flag to remember
                projectAttachmentDirExists = false;
            }
        }
    }

    ///CLOVER:OFF
    public AttachmentParser getAttachmentParser()
    {
        if (attachmentParser == null)
        {
            attachmentParser = new AttachmentParserImpl();
        }
        return attachmentParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {}

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final AttachmentFileValidatorHandler that = (AttachmentFileValidatorHandler) o;

        if (attachmentParser != null ? !attachmentParser.equals(that.attachmentParser) : that.attachmentParser != null)
        {
            return false;
        }
        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (backupSystemInformation != null ? !backupSystemInformation.equals(that.backupSystemInformation) : that.backupSystemInformation != null)
        {
            return false;
        }
        if (i18nHelper != null ? !i18nHelper.equals(that.i18nHelper) : that.i18nHelper != null)
        {
            return false;
        }
        if (messageSet != null ? !messageSet.equals(that.messageSet) : that.messageSet != null)
        {
            return false;
        }
        if (projectImportOptions != null ? !projectImportOptions.equals(that.projectImportOptions) : that.projectImportOptions != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (messageSet != null ? messageSet.hashCode() : 0);
        result = 31 * result + (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (projectImportOptions != null ? projectImportOptions.hashCode() : 0);
        result = 31 * result + (backupSystemInformation != null ? backupSystemInformation.hashCode() : 0);
        result = 31 * result + (i18nHelper != null ? i18nHelper.hashCode() : 0);
        result = 31 * result + (attachmentParser != null ? attachmentParser.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
