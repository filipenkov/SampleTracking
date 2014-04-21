package com.atlassian.jira.issue.attachment;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;

public class Attachment extends AbstractOfBizValueWrapper
{
    private static final Logger log = Logger.getLogger(Attachment.class);

    private final IssueManager issueManager;
    private PropertySet attachmentProperties;

    /**
     * Construct an Attachment, with properties.
     * @param issueManager
     * @param genericValue FileAttachment GenericValue
     * @param attachmentProperties Properties of the attachment.
     */
    public Attachment(IssueManager issueManager, GenericValue genericValue, PropertySet attachmentProperties)
    {
        super(genericValue);
        this.issueManager = issueManager;
        this.attachmentProperties = attachmentProperties;
    }
    /**
     * Construct an Attachment.
     * @param issueManager
     * @param genericValue FileAttachment GenericValue
     */
    public Attachment(IssueManager issueManager, GenericValue genericValue)
    {
        this(issueManager, genericValue, null);
    }

    /**
     * Returns the Issue that this file is attached to.
     * @return the Issue that this file is attached to.
     */
    public Issue getIssueObject()
    {
        return issueManager.getIssueObject(genericValue.getLong("issue"));
    }

    /**
     * Returns the Issue that this file is attached to.
     * @return the Issue that this file is attached to.
     *
     * @deprecated Use {@link #getIssueObject()} instead. Since v4.4.
     */
    public GenericValue getIssue()
    {
        return issueManager.getIssue(genericValue.getLong("issue"));
    }

    public Long getId()
    {
        return genericValue.getLong("id");
    }

    public String getMimetype()
    {
        return genericValue.getString("mimetype");
    }

    public String getFilename()
    {
        return genericValue.getString("filename");
    }

    public Timestamp getCreated()
    {
        return genericValue.getTimestamp("created");
    }

    public Long getFilesize()
    {
        return genericValue.getLong("filesize");
    }

    public String getAuthor()
    {
        return genericValue.getString("author");
    }

    /** Get attachment properties
     * @return A Map of key -> {@link com.opensymphony.module.propertyset.PropertySet}s
     */
    public PropertySet getProperties()
    {
        return attachmentProperties;
    }
}