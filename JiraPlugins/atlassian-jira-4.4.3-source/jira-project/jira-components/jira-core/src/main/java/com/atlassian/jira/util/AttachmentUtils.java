package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.AttachmentException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utilities for working with the attachment files and their directories.
 */
public class AttachmentUtils
{
    private static final Logger log = Logger.getLogger(AttachmentUtils.class);

    /**
     * Infix for generated thumbnail images.
     */
    private static final String THUMBNAIL_DESIGNATION = "_thumb_";
    private static final String TMP_ATTACHMENTS = "tmp_attachments";
    public static final String THUMBS_SUBDIR = "thumbs";

    /**
     * Returns the physical directory of the thumbnails for the given issue, creating if necessary.
     *
     * @param issue the issue whose thumbnail directory you want.
     * @return The issue's thumbnail directory.
     */
    public static File getThumbnailDirectory(Issue issue)
    {
        final File thumbDir = new File(getAttachmentDirectory(issue), THUMBS_SUBDIR);
        if (!thumbDir.exists() && !thumbDir.mkdirs())
        {
            log.warn("Unable to make thumbnail directory " + thumbDir.getAbsolutePath());
        }
        return thumbDir;
    }

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want.
     * @return The issue's attachment directory.
     */
    public static File getAttachmentDirectory(Issue issue)
    {
        return getAttachmentDirectory(issue, true);
    }

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want.
     * @param createDirectory If true, and the directory does not currently exist, then the directory is created.
     * @return The issue's attachment directory.
     */
    public static File getAttachmentDirectory(Issue issue, final boolean createDirectory)
    {
        Project project = issue.getProjectObject();
        return getAttachmentDirectory(project.getKey(), issue.getKey(), createDirectory);
    }

    public static File getTemporaryAttachmentDirectory()
    {
        final File cachesDirectory = ComponentAccessor.getComponent(JiraHome.class).getCachesDirectory();
        final File tempDirectory = new File(cachesDirectory, TMP_ATTACHMENTS);
        if (!tempDirectory.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            tempDirectory.mkdirs();
        }
        return tempDirectory;
    }

    /**
     * Get the attachment directory for the given project key and issue key.
     *
     * @param projectKey the project key the issue belongs to
     * @param issueKey the issue key for the issue
     * @param createDirectory whether the directory should be created if it doesn't exist
     * @return the directory attachments for this issue live in
     */
    private static File getAttachmentDirectory(final String projectKey, final String issueKey, final boolean createDirectory)
    {
        final File directory = getAttachmentDirectory(getAttachmentDirName(), projectKey, issueKey);
        if (createDirectory)
        {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * Get the attachment directory for the given attachment base directory, project key, and issue key.
     * <p/>
     * The idea is to encapsulate all of the path-joinery magic to make future refactoring easier if we ever decide to
     * move away from attachment-base/project-key/issue-ket
     *
     * @param attachmentDirectory base of attachments
     * @param projectKey the project key the issue belongs to
     * @param issueKey the issue key for the issue
     * @return the directory attachments for this issue live in
     */
    public static File getAttachmentDirectory(final String attachmentDirectory, final String projectKey, final String issueKey)
    {
        final File projectDirectory = new File(attachmentDirectory, projectKey);
        return new File(projectDirectory, issueKey);
    }

    /**
     * Returns the physical File for the given Attachment.
     *
     * @param attachment the attachment.
     * @return the file.
     * @throws DataAccessException on failure getting required attachment info.
     */
    public static File getAttachmentFile(Attachment attachment) throws DataAccessException
    {
        final File attachmentDir = getAttachmentDirectory(attachment.getIssueObject());
        return getAttachmentFile(AttachmentAdapter.fromAttachment(attachment), attachmentDir);
    }

    /**
     * This is intended for cases where you want more control over where the attachment actually lives and you just want
     * something to handle the look up logic for the various possible filenames an attachment can have.
     * <p/>
     * In practice, this is just used during Project Import
     *
     * @param attachment it's not an attachment but it acts like one for our purposes.
     * @param attachmentDir the directory the attachments live in. This is different that the system-wide attachment
     * directory. i.e. this would "attachments/MKY/MKY-1" and not just "attachments"
     * @return the actual attachment
     */
    public static File getAttachmentFile(final AttachmentAdapter attachment, final File attachmentDir)
    {
        //First try a direct lookup which is fast.
        final File legacyFile = getLegacyAttachmentFile(attachment, attachmentDir);
        final File defaultFile = getDefaultAttachmentFile(attachment, attachmentDir);
        if (defaultFile.exists())
        {
            return defaultFile;
        }
        else if (legacyFile.exists())
        {
            return legacyFile;
        }
        else
        {
            //Now lets try a slower lookup by ID if this fails.
            final File legacyAttachmentById = findLegacyAttachmentById(attachment, attachmentDir);

            // If *that* didn't work then we fall back to just returning whatever the default should be.
            // The most common case for this is when a new attachment gets uploaded, though other error cases could
            // also result in it (like if someone manually deleted the file from underneath JIRA).
            if (legacyAttachmentById == null)
            {
                return defaultFile;
            }
            else
            {
                return legacyAttachmentById;
            }
        }
    }

    /**
     * Just like the attachments themselves, thumbnails can succumb to file system encoding problems. However we are
     * going to regenerate thumbnails by only using the new naming scheme and not the legacy one.  We cant do this for
     * attachments but we can for thumbnails since they are epheral objects anyway.
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attacment in play
     * @return the full thumbnail file name
     */
    public static File getThumbnailFile(Attachment attachment)
    {
        final AttachmentAdapter attachmentAdapter = AttachmentAdapter.fromAttachment(attachment);
        final File thumbDir = getThumbnailDirectory(attachment.getIssueObject());
        return getThumbnailFile(attachmentAdapter, thumbDir);
    }

    /**
     * Returns the old legacy file name for thumbnails
     *
     * http://jira.atlassian.com/browse/JRA-23311
     *
     * @param  attachment the attacment in play
     * @return the full legacy thumbnail file name
     */
    public static File getLegacyThumbnailFile(Attachment attachment)
    {
        final AttachmentAdapter attachmentAdapter = AttachmentAdapter.fromAttachment(attachment);
        final File thumbDir = getThumbnailDirectory(attachment.getIssueObject());
        return getLegacyThumbnailFile(attachmentAdapter, thumbDir);
    }

    /**
     * Checks that the Attachment directory of the given issue is right to go - writable, accessible etc. Will create it
     * if necessary.
     *
     * @param issue the issue whose attachment directory to check.
     * @throws AttachmentException if the directory is not writable or missing and cannot be created.
     */
    public static void checkValidAttachmentDirectory(Issue issue) throws AttachmentException
    {
        // check that we can write to the attachment directory
        try
        {
            File directory = AttachmentUtils.getAttachmentDirectory(issue);

            if (!directory.canWrite())
            {
                throw new AttachmentException(new I18nBean().getText("attachfile.error.writeerror", directory.getAbsolutePath()));
            }
            checkValidTemporaryAttachmentDirectory();
        }
        catch (Exception e)
        {
            throw new AttachmentException(new I18nBean().getText("attachfile.error.exception", e.toString()), e);
        }
    }

    public static void checkValidTemporaryAttachmentDirectory() throws AttachmentException
    {
        final File tempDirectory = getTemporaryAttachmentDirectory();
        if (!tempDirectory.canWrite())
        {
            throw new AttachmentException(new I18nBean().getText("attachfile.error.temp.writeerror", tempDirectory.getAbsolutePath()));
        }
    }

    private static String getAttachmentDirName()
    {
        return ComponentAccessor.getAttachmentPathManager().getAttachmentPath();
    }

    /**
     * We need some of this utility to code to work for both Attachments and ExternalAttachments (from Project Import).
     * All we really need to work is the id and the filename so this provides an adapter so that we can reuse code
     * here.
     */
    public static class AttachmentAdapter
    {
        final private Long id;
        final private String name;

        public AttachmentAdapter(final Long id, final String name)
        {
            this.id = id;
            this.name = name;
        }

        public Long getId()
        {
            return id;
        }

        public String getFilename()
        {
            return name;
        }

        static AttachmentAdapter fromAttachment(final Attachment attachment)
        {
            return new AttachmentAdapter(attachment.getId(), attachment.getFilename());
        }
    }

    // For a brief period of time we thought that doing this was sufficient to avoid Encoding issues. However, see
    // http://jira.atlassian.com/browse/JRA-23311 for an explanation of how this can fail. However, we still need
    // to do this to find any attachments that were stored under the old scheme.
    private static File findLegacyAttachmentById(final AttachmentAdapter attachment, final File attachmentDir)
    {
        //Find all the files that start with "attachment.id_"
        final Pattern allFilesPattern = Pattern.compile("^" + attachment.getId() + "_.+");
        final File[] list = attachmentDir.listFiles(new FilenameFilter()
        {
            public boolean accept(final File dir, final String name)
            {
                Matcher m = allFilesPattern.matcher(name);
                return m.matches();
            }
        });

        if (list == null || list.length == 0)
        {
            return null;
        }
        else if (list.length > 1)
        {
            //More than 1 file found, the list could contain the thumbnail version of the attachment, search the
            //list excluding anything starting with ID_thumb_
            final Pattern thumbnailExcludingPattern = Pattern.compile("^" + attachment.getId() + "_(?!thumb_).+");
            File firstFile = null;
            int matchCount = 0;
            for (File file : list)
            {
                if (thumbnailExcludingPattern.matcher(file.getName()).matches())
                {
                    matchCount++;
                    if (firstFile == null)
                    {
                        firstFile = file;
                    }
                }
            }

            //If only one match is found then lets use it. Otherwise lets return the first match.
            if (matchCount == 1)
            {
                return firstFile;
            }

            log.warn("More than one file found for attachment id " + attachment.getId() + " in " +
                    attachmentDir + ". The first entry will be returned.");
        }
        return list[0];
    }

    /*
     Prosecution : Your honour, this is hard coding the format name.  Hard coding is always bad!
     Defense     : There are benefits in knowing that a file on disk is in fact a png.
     Prosection  : What if we change this thumbnail format.
     Defense     : We will never change from png any time soon
     Judge       : I will allow it!
     */
    private static File getThumbnailFile(final AttachmentAdapter attachment, final File attachmentDirectory)
    {
        return new File(attachmentDirectory, THUMBNAIL_DESIGNATION + attachment.getId() + ".png");
    }

    private static File getLegacyThumbnailFile(final AttachmentAdapter attachment, final File attachmentDirectory)
    {
        return new File(attachmentDirectory, attachment.getId() + THUMBNAIL_DESIGNATION + attachment.getFilename());
    }

    private static File getDefaultAttachmentFile(final AttachmentAdapter attachment, final File attachmentDirectory)
    {
        return new File(attachmentDirectory, attachment.getId().toString());
    }

    private static File getLegacyAttachmentFile(final AttachmentAdapter attachment, final File attachmentDir)
    {
        return new File(attachmentDir, attachment.getId() + "_" + attachment.getFilename());
    }
}
