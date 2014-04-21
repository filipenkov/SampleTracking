/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.util.AttachmentException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Manages all attachment related tasks in JIRA, which involves retrieving an attachment,
 * creating an attachment and deleting an attachment.
 */
public interface AttachmentManager
{
    /**
     * Get a single attachment by its ID.
     *
     * @param id the Attachment ID
     * @return the Attachment
     * @throws DataAccessException if there is a problem accessing the database.
     */
    Attachment getAttachment(Long id) throws DataAccessException;

    /**
     * Get a list of all attachments for a certain issue.
     *
     * @param issue the Issue
     * @return a list of {@link Attachment} objects for the given issue.
     * @throws DataAccessException if there is a problem accessing the database.
     *
     * @deprecated use #getAttachments(Issue) instead. Since v4.0
     */
    List<Attachment> getAttachments(GenericValue issue) throws DataAccessException;

    /**
     * Get a list of all attachments for a certain issue.
     *
     * @param issue the Issue
     * @return a list of {@link Attachment} objects
     * @throws DataAccessException if there is a problem accessing the database.
     */
    List<Attachment> getAttachments(Issue issue) throws DataAccessException;

    /**
     * Get a list of all attachments for a certain issue, sorted according to the specified comparator.
     *
     * @param issue the Issue
     * @param comparator used for sorting
     * @return a list of {@link Attachment} objects
     * @throws DataAccessException if there is a problem accessing the database.
     */
    List<Attachment> getAttachments(Issue issue, Comparator<? super Attachment> comparator) throws DataAccessException;

    /**
     * Create an attachment both on disk, and in the database by copying the provided file instead of moving it.
     *
     * @param file                 A file on a locally accessible filesystem, this will be copied, not moved.
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param attachmentAuthor     The username of the user who created this attachment, this is not validated so it must be a valid username
     * @param issue                The id of the issue that this attachment is attached to
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime          when the attachment was created
     *
     * @return the Attachment
     * @throws com.atlassian.jira.web.util.AttachmentException if any errors occur.
     */
    Attachment createAttachmentCopySourceFile(File file, String filename, String contentType, String attachmentAuthor, Issue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException;

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param file                 A file on a locally accessible filesystem
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param remoteUser           The use who created this attachment
     * @param issue                The issue that this attachment is attached to
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime the created time
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     * @throws org.ofbiz.core.entity.GenericEntityException if there is an error in creating the DB record for the attachment
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, com.opensymphony.user.User remoteUser, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException, GenericEntityException;

    /**
     * Create an attachment both on disk, and in the database.
     *
     * @param file                 A file on a locally accessible filesystem
     * @param filename             The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType          The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param remoteUser           The use who created this attachment
     * @param issue                The issue that this attachment is attached to
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     *                             and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.  Pass null to set no properties
     * @param createdTime the created time
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     * @throws org.ofbiz.core.entity.GenericEntityException if there is an error in creating the DB record for the attachment
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime) throws AttachmentException, GenericEntityException;

    /**
     * Same as the {@link #createAttachment(java.io.File, String, String, com.opensymphony.user.User, org.ofbiz.core.entity.GenericValue, java.util.Map, java.util.Date)} method, except it
     * submits no attachmentProperties and uses now() for the created time.
     *
     * @param file        A file on a locally accessible filesystem
     * @param filename    The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param remoteUser  The use who created this attachment
     * @param issue       The issue that this attachment is attached to
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     * @throws org.ofbiz.core.entity.GenericEntityException if there is an error in creating the DB record for the attachment
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, com.opensymphony.user.User remoteUser, GenericValue issue) throws AttachmentException, GenericEntityException;

    /**
     * Same as the {@link #createAttachment(java.io.File, String, String, User, org.ofbiz.core.entity.GenericValue, java.util.Map, java.util.Date)} method, except it
     * submits no attachmentProperties and uses now() for the created time.
     *
     * @param file        A file on a locally accessible filesystem
     * @param filename    The desired filename for this attachment.  This may be different to the filename on disk (for example with temp files used in file uploads)
     * @param contentType The desired contentType.  Implementations of this interface can choose to override this value as appropriate
     * @param remoteUser  The use who created this attachment
     * @param issue       The issue that this attachment is attached to
     * @return A {@link ChangeItemBean} with all the changes to the issue.
     *
     * @throws com.atlassian.jira.web.util.AttachmentException if an error occurs while attempting to copy the file
     * @throws org.ofbiz.core.entity.GenericEntityException if there is an error in creating the DB record for the attachment
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue) throws AttachmentException, GenericEntityException;

    /**
     * Create an attachment in the database.  Note that this does not create it on disk, nor does it create a change item.
     *
     * @param issue                the issue that this attachment is attached to
     * @param author               The user who created this attachment
     * @param mimetype             mimetype
     * @param filename             The desired filename for this attachment.
     * @param filesize             filesize
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).
     * @param createdTime          when the attachment was created
     *
     * @return the Attachment
     * @throws org.ofbiz.core.entity.GenericEntityException if there is an error in creating the DB record for the attachment
     */
    Attachment createAttachment(GenericValue issue, com.opensymphony.user.User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime) throws GenericEntityException;

    /**
     * Create an attachment in the database.  Note that this does not create it on disk, nor does it create a change item.
     *
     * @param issue                the issue that this attachment is attached to
     * @param author               The user who created this attachment
     * @param mimetype             mimetype
     * @param filename             The desired filename for this attachment.
     * @param filesize             filesize
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).
     * @param createdTime          when the attachment was created
     *
     * @return the Attachment
     * @throws org.ofbiz.core.entity.GenericEntityException if there is an error in creating the DB record for the attachment
     */
    Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime) throws GenericEntityException;

    /**
     * Delete an attachment from the database and from disk.
     *
     * @param attachment the Attachment
     * @throws RemoveException if the attachment cannot be removed from the disk
     */
    void deleteAttachment(Attachment attachment) throws RemoveException;

    /**
     * Delete the attachment directory from disk if the directory is empty.
     *
     * @param issue the issue whose attachment directory we wish to delete.
     * @throws RemoveException if the directory can not be removed or is not empty.
     * @deprecated Please use {@link #deleteAttachmentDirectory(Issue)} instead. Deprecated since v4.2
     */
    void deleteAttachmentDirectory(GenericValue issue) throws RemoveException;

    /**
     * Delete the attachment directory from disk if the directory is empty.
     *
     * @param issue the issue whose attachment directory we wish to delete.
     * @throws RemoveException if the directory can not be removed or is not empty.
     */
    void deleteAttachmentDirectory(Issue issue) throws RemoveException;

    /**
     * Determine if attachments have been enabled in JIRA and if the attachments directory exists.
     * @return true if enabled, false otherwise
     */
    boolean attachmentsEnabled();

    /**
     * Determine if screenshot applet has been enabled in JIRA.
     * @return true if enabled, false otherwise
     */
    boolean isScreenshotAppletEnabled();

    /**
     * Determine if the screenshot applet is supported by the user's operating system.
     *
     * Note. This always returns true now as we support screenshots on all our supported platforms
     *
     * @return true if applet is supported by the user's OS, false otherwise
     */
    boolean isScreenshotAppletSupportedByOS();

    /**
     * Converts a set of provided temporary attachments to real attachments attached to an issue.  This method will
     * also clean up any temporary attachments still linked to the issue via the TemporaryAttachmentsMonitor.
     *
     * @param user The user performing the action
     * @param issue The issue attachments should be linked to
     * @param selectedAttachments The temporary attachment ids to convert as selected by the user
     * @param temporaryAttachmentsMonitor TemporaryAttachmentsMonitor containing information about all temporary attachments
     * @return A list of ChangeItemBeans for any attachments that got created
     * @throws AttachmentException If there were problems with the Attachment itself
     * @throws GenericEntityException if there is an error in creating the DB record for the attachment
     */
    List<ChangeItemBean> convertTemporaryAttachments(final com.opensymphony.user.User user, final Issue issue, final List<Long> selectedAttachments,
            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor) throws AttachmentException, GenericEntityException;

    /**
     * Converts a set of provided temporary attachments to real attachments attached to an issue.  This method will
     * also clean up any temporary attachments still linked to the issue via the TemporaryAttachmentsMonitor.
     *
     * @param user The user performing the action
     * @param issue The issue attachments should be linked to
     * @param selectedAttachments The temporary attachment ids to convert as selected by the user
     * @param temporaryAttachmentsMonitor TemporaryAttachmentsMonitor containing information about all temporary attachments
     * @return A list of ChangeItemBeans for any attachments that got created
     * @throws AttachmentException If there were problems with the Attachment itself
     * @throws GenericEntityException if there is an error in creating the DB record for the attachment
     */
    List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments,
            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor) throws AttachmentException, GenericEntityException;
}
