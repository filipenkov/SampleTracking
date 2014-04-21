package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.FileUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.AttachmentException;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.core.util.WebRequestUtils.MACOSX;
import static com.atlassian.core.util.WebRequestUtils.WINDOWS;
import static com.atlassian.core.util.WebRequestUtils.getBrowserOperationSystem;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultAttachmentManager implements AttachmentManager
{
    private static final Logger log = Logger.getLogger(DefaultAttachmentManager.class);
    private final IssueManager issueManager;
    private final OfBizDelegator ofBizDelegator;
    private final MimeManager mimeManager;
    private final ApplicationProperties applicationProperties;
    private final AttachmentPathManager attachmentPathManager;
    private final ComponentLocator componentLocator;
    private final I18nBean.BeanFactory i18nBeanFactory;

    public DefaultAttachmentManager(final IssueManager issueManager, final OfBizDelegator ofBizDelegator,
            final MimeManager mimeManager, final ApplicationProperties applicationProperties,
            final AttachmentPathManager attachmentPathManager, final ComponentLocator componentLocator,
            final I18nHelper.BeanFactory i18nBeanFactory)
    {
        this.issueManager = issueManager;
        this.ofBizDelegator = ofBizDelegator;
        this.mimeManager = mimeManager;
        this.applicationProperties = applicationProperties;
        this.attachmentPathManager = attachmentPathManager;
        this.componentLocator = componentLocator;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    /**
     * Get a single attachment by its id.
     * @return The attachment, or null if that id was not found.
     */
    public Attachment getAttachment(Long id)
    {
        GenericValue attachmentGV;
        try
        {
            attachmentGV = ofBizDelegator.findByPrimaryKey("FileAttachment", MapBuilder.build("id", id));
        }
        catch (DataAccessException e)
        {
            log.error("Unable to find a file attachment with id: " + id);
            throw e;
        }
        if (attachmentGV == null) throw new AttachmentNotFoundException(id);
        return new Attachment(issueManager, attachmentGV, OFBizPropertyUtils.getPropertySet(attachmentGV));
    }

    public List<Attachment> getAttachments(Issue issue)
    {
        return getAttachments(issue.getGenericValue());
    }

    public List<Attachment> getAttachments(GenericValue issue)
    {
        try
        {
            Collection<GenericValue> attachmentGvs = issue.getRelatedOrderBy("ChildFileAttachment", EasyList.build("filename ASC", "created DESC"));
            List<Attachment> attachments = new ArrayList<Attachment>(attachmentGvs.size());
            for (GenericValue attachmentGV : attachmentGvs)
            {
                attachments.add(new Attachment(issueManager, attachmentGV, OFBizPropertyUtils.getPropertySet(attachmentGV)));
            }
            return attachments;
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private List<Attachment> getAttachments(GenericValue issue, Comparator<? super Attachment> comparator)
    {
        List<Attachment> attachments = getAttachments(issue);
        Collections.sort(attachments, comparator);
        return attachments;
    }

    public List<Attachment> getAttachments(Issue issue, Comparator<? super Attachment> comparator)
    {
        return getAttachments(issue.getGenericValue(), comparator);
    }

    public Attachment createAttachmentCopySourceFile(final File file, final String filename, final String contentType, final String attachmentAuthor, final Issue issue, final Map<String, Object> attachmentProperties, final Date createdTime)
            throws AttachmentException
    {
        if (file == null)
        {
            log.warn("Cannot create attachment without a file (filename=" + filename + ").");
            return null;
        }
        else if (filename == null)
        {
            // Perhaps we should just use the temporary filename instead of losing the attachment? These are all hacks anyway. We need to properly support multipart/{related,inline}
            log.warn("Cannot create attachment without a filename - inline content? See http://jira.atlassian.com/browse/JRA-10825 (file=" + file.getName() + ").");
            return null;
        }
        AttachmentUtils.checkValidAttachmentDirectory(issue);

        //get sanitised version of the mimeType.
        String contentTypeFromFile = mimeManager.getSanitisedMimeType(contentType, filename);

        Attachment attachment;
        try
        {
            attachment = createAttachment(issue.getId(), attachmentAuthor, contentTypeFromFile, filename, file.length(), attachmentProperties, createdTime);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // create attachment on disk
        createAttachmentOnDiskCopySourceFile(attachment, file);
        return attachment;
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, com.opensymphony.user.User remoteUser, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException, GenericEntityException
    {
        // Old OSUser Object
        return createAttachment(file, filename, contentType, (User) remoteUser, issue, attachmentProperties, createdTime);
    }

    public Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime) throws GenericEntityException
    {
        return createAttachment(issue.getLong("id"), (author != null ? author.getName() : null), mimetype, filename, filesize, attachmentProperties, createdTime);
    }

    private Attachment createAttachment(Long issueId, String authorName, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime) throws GenericEntityException
    {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("issue", issueId);
        fields.put("author", authorName);
        fields.put("mimetype", mimetype);
        fields.put("filename", filename);
        fields.put("filesize", filesize);
        fields.put("created", createdTime);

        GenericValue attachmentGV = EntityUtils.createValue("FileAttachment", fields);

        if (attachmentProperties != null)
        {
            PropertySet propSet = createAttachmentPropertySet(attachmentGV, attachmentProperties);
            return new Attachment(issueManager, attachmentGV, propSet);
        }
        else
        {
            return new Attachment(issueManager, attachmentGV);
        }
    }

    public void deleteAttachment(Attachment attachment) throws RemoveException
    {
        try
        {
            if (deleteAttachmentFile(attachment))
            {
                ofBizDelegator.removeAll(CollectionBuilder.list(attachment.getGenericValue()));
            }
            else
            {
                throw new RemoveException("Could not delete attachment file");
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to delete attachment.", e);
            throw new DataAccessException(e);
        }
    }

    public void deleteAttachmentDirectory(Issue issue) throws RemoveException
    {
        deleteAttachmentDirectory(issue.getGenericValue());
    }

    IssueFactory getIssueFactory()
    {
        return componentLocator.getComponent(IssueFactory.class);
    }

    public void deleteAttachmentDirectory(GenericValue issue) throws RemoveException
    {
        if (issue != null && attachmentsAllowedAndDirectoryIsSet())
        {
            File attachmentDir = AttachmentUtils.getAttachmentDirectory(getIssueFactory().getIssue(issue));
            if (!attachmentDir.isDirectory()) throw new RemoveException("Attachment path '"+attachmentDir+"' is not a directory");
            if (!attachmentDir.canWrite()) throw new RemoveException("Can't write to attachment directory '"+attachmentDir+"'");

            // Remove the /thumbs/ subdirectory if required.
            final File thumbnailDirectory = new File(attachmentDir, AttachmentUtils.THUMBS_SUBDIR);
            if (thumbnailDirectory.exists())
            {
                // We want to delete it.
                if (thumbnailDirectory.listFiles().length == 0)
                {
                    boolean deleted = thumbnailDirectory.delete();
                    if (!deleted)
                    {
                        log.error("Unable to delete the issue attachment thumbnail directory '" + thumbnailDirectory + "'.");
                    }
                }
                else
                {
                    for (File file : thumbnailDirectory.listFiles())
                    {
                        System.out.println("file = " + file);
                    }
                    log.error("Unable to delete the issue attachment thumbnail directory '" + thumbnailDirectory + "' because it is not empty.");
                }
            }

            if (attachmentDir.listFiles().length == 0)
            {
                if (!attachmentDir.delete())
                {
                    log.error("Unable to delete the issue attachment directory '" + attachmentDir + "'.");
                }
            }
            else
            {
                log.error("Unable to delete the issue attachment directory '" + attachmentDir + "' because it is not empty.");
            }
        }
    }

    private boolean attachmentsAllowedAndDirectoryIsSet()
    {
        String attachmentDir = attachmentPathManager.getAttachmentPath();
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS) && StringUtils.isNotBlank(attachmentDir);
    }

    public boolean attachmentsEnabled()
    {
        boolean allowAttachments = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
        boolean attachmentPathSet = StringUtils.isNotBlank(attachmentPathManager.getAttachmentPath());
        return allowAttachments && attachmentPathSet;
    }

    public boolean isScreenshotAppletEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_ENABLED);
    }

    protected boolean isScreenshotAppletEnabledForLinux()
    {
        return applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_LINUX_ENABLED);
    }

    public boolean isScreenshotAppletSupportedByOS()
    {
        if (isScreenshotAppletEnabledForLinux())
        {
            // means all OS are supported so just return true.
            return true;
        }

        // Linux is still flakey
        int browserOS = getUsersOS();
        return (browserOS == WINDOWS || browserOS == MACOSX );
    }

    @Override
    public List<ChangeItemBean> convertTemporaryAttachments(com.opensymphony.user.User user, Issue issue, List<Long> selectedAttachments, TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException, GenericEntityException
    {
        // Old OSUser Object
        return convertTemporaryAttachments((User) user, issue, selectedAttachments, temporaryAttachmentsMonitor);
    }

    public List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments, final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor) throws AttachmentException, GenericEntityException
    {
        notNull("issue", issue);
        notNull("selectedAttachments", selectedAttachments);
        notNull("temporaryAttachmentsMonitor", temporaryAttachmentsMonitor);

        final List<ChangeItemBean> ret = new ArrayList<ChangeItemBean>();
        for (final Long selectedAttachment : selectedAttachments)
        {
            final TemporaryAttachment tempAttachment = temporaryAttachmentsMonitor.getById(selectedAttachment);
            final ChangeItemBean cib = createAttachment(tempAttachment.getFile(), tempAttachment.getFilename(), tempAttachment.getContentType(), user, issue.getGenericValue(), Collections.<String, Object>emptyMap(), UtilDateTime.nowTimestamp());
            if(cib != null)
            {
                ret.add(cib);
            }
        }
        
        //finally clear any other remaining temp attachments for this issue
        temporaryAttachmentsMonitor.clearEntriesForIssue(issue.getId());
        return ret;
    }

    int getUsersOS()
    {
        HttpServletRequest servletRequest = ExecutingHttpRequest.get();
        if (servletRequest == null)
        {
            servletRequest = ServletActionContext.getRequest();

        }
        return getBrowserOperationSystem(servletRequest);

    }

    private static boolean deleteAttachmentFile(Attachment attachment) throws GenericEntityException
    {
        File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
        File thumbnailFile = AttachmentUtils.getThumbnailFile(attachment);

        org.apache.commons.io.FileUtils.deleteQuietly(AttachmentUtils.getLegacyThumbnailFile(attachment));

        if (attachmentFile.exists() && thumbnailFile.exists())
        {
            return attachmentFile.delete() && thumbnailFile.delete();
        }
        else if (attachmentFile.exists())
        {
            return attachmentFile.delete();
        }
        else
        {
            log.warn("Trying to delete non-existent attachment: [" + attachmentFile.getAbsolutePath() + "] ..ignoring");
            return true;
        }
    }

    /**
     * @param contentType          The desired contentType.  This may be modified if a better alternative is suggested by {@link MimeManager#getSanitisedMimeType(String, String)}
     * @param attachmentProperties String -> Object property map
     */
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException, GenericEntityException
    {
        if (file == null)
        {
            log.warn("Cannot create attachment without a file (filename="+filename+").");
            return null;
        }
        else if (filename == null)
        {
            // Perhaps we should just use the temporary filename instead of losing the attachment? These are all hacks anyway. We need to properly support multipart/{related,inline}
            log.warn("Cannot create attachment without a filename - inline content? See http://jira.atlassian.com/browse/JRA-10825 (file="+file.getName()+").");
            return null;
        }
        AttachmentUtils.checkValidAttachmentDirectory(getIssueFactory().getIssue(issue));

        //get sanitised version of the mimeType.
        contentType = mimeManager.getSanitisedMimeType(contentType, filename);

        Attachment attachment = createAttachment(issue, remoteUser, contentType, filename, new Long(file.length()), attachmentProperties, createdTime);

        // create attachment on disk
        createAttachmentOnDisk(attachment, file, remoteUser);

        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Attachment", null, null, attachment.getId().toString(), filename);
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, com.opensymphony.user.User remoteUser, GenericValue issue)
            throws AttachmentException, GenericEntityException
    {
        // Old OSUser Object
        return createAttachment(file, filename, contentType, (User) remoteUser, issue);
    }

    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue) throws AttachmentException, GenericEntityException
    {
        return createAttachment(file, filename, contentType, remoteUser, issue, Collections.<String, Object>emptyMap(), UtilDateTime.nowTimestamp());
    }

    @Override
    public Attachment createAttachment(GenericValue issue, com.opensymphony.user.User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime)
            throws GenericEntityException
    {
        // Old OSUser Object
        return createAttachment(issue, (User) author, mimetype, filename, filesize, attachmentProperties, createdTime);
    }

    protected void createAttachmentOnDisk(Attachment attachment, File file, User user) throws AttachmentException
    {
        File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);

        boolean renameSucceded = file.renameTo(attachmentFile);

        //java cannot rename files across partitions
        if (!renameSucceded)
        {
            // may be trying to move across different file systems (JRA-839), try the old copy and delete
            try
            {
                FileUtils.copyFile(file, attachmentFile);
                if (!file.delete())
                {
                    throw new AttachmentException(i18nBeanFactory.getInstance(user).
                            getText("attachfile.error.delete", file.getAbsolutePath()));
                }
            }
            catch (Exception e)
            {
                final String message =
                        i18nBeanFactory.getInstance(user).
                                getText("attachfile.error.move",
                                        EasyList.build(file.getAbsolutePath(), attachmentFile.getAbsolutePath(), e));

                log.error(message, e);
                throw new AttachmentException(message);
            }
        }
    }

    protected void createAttachmentOnDiskCopySourceFile(Attachment attachment, File file) throws AttachmentException
    {
        File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);

        try
        {
            FileUtils.copyFile(file, attachmentFile);
        }
        catch (IOException e)
        {
            log.error("Could not copy attachment from '" + file.getAbsolutePath() + "' to '" + attachmentFile.getAbsolutePath() + "'.", e);
            throw new AttachmentException("Could not copy attachment from '" + file.getAbsolutePath() + "' to '" + attachmentFile.getAbsolutePath() + "'.");
        }
    }

    /**
     * Create attachment properties in the database.
     *
     * @param attachment the attachment whose property set we are meant to create.
     * @param attachmentProperties Map of String -> Object pairs
     * @return Returned map of {@link PropertySet}s.
     */
    private PropertySet createAttachmentPropertySet(GenericValue attachment, Map<String, Object> attachmentProperties)
    {
        PropertySet propSet = OFBizPropertyUtils.getPropertySet(attachment);
        for (Map.Entry<String, Object> entry : attachmentProperties.entrySet())
        {
            propSet.setAsActualType(entry.getKey(), entry.getValue());
        }
        return propSet;
    }
}
