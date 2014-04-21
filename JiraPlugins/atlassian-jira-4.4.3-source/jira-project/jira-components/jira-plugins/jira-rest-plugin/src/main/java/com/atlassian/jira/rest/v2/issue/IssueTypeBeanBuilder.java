package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.issuetype.IssueType;

import java.net.URI;
import javax.ws.rs.core.UriInfo;

/**
 * Builder class for IssueType instances.
 *
 * @since v4.2
 */
public class IssueTypeBeanBuilder
{
    /**
     * The issue type that we want to convert.
     */
    private IssueType issueType;

    /**
     * The base URL.
     */
    private URI baseURI;

    /**
     * The context.
     */
    private UriInfo context;

    /**
     * Creates a new IssueTypeBeanBuilder.
     */
    public IssueTypeBeanBuilder()
    {
        // empty
    }

    /**
     * Sets the issue type.
     *
     * @param issueType an IssueType
     * @return this
     */
    public IssueTypeBeanBuilder issueType(IssueType issueType)
    {
        this.issueType = issueType;
        return this;
    }

    /**
     * Sets the base URI for JIRA.
     *
     * @param baseURI the base URI
     * @return this
     */
    public IssueTypeBeanBuilder baseURI(URI baseURI)
    {
        this.baseURI = baseURI;
        return this;
    }

    /**
     * Sets the request context.
     *
     * @param context a UriInfo
     * @return this
     */
    public IssueTypeBeanBuilder context(UriInfo context)
    {
        this.context = context;
        return this;
    }

    public IssueTypeBean build()
    {
        verifyPreconditions();
        return new IssueTypeBean(
                new ResourceUriBuilder().build(context, IssueTypeResource.class, issueType.getId()),
                issueType.getDescTranslation(),
                baseURI + issueType.getIconUrl(),
                issueType.getNameTranslation(),
                issueType.isSubTask()
        );
    }

    public IssueTypeBean buildShort()
    {
        verifyPreconditionsShort();
        return new IssueTypeBean(
                new ResourceUriBuilder().build(context, IssueTypeResource.class, issueType.getId()),
                null,
                null,
                issueType.getNameTranslation(),
                issueType.isSubTask()
        );
    }

    private void verifyPreconditions()
    {
        verifyPreconditionsShort();

        if (baseURI == null)
        {
            throw new IllegalStateException("baseURI not set");
        }
    }

    private void verifyPreconditionsShort()
    {
        if (issueType == null)
        {
            throw new IllegalStateException("issueType not set");
        }

        if (context == null)
        {
            throw new IllegalStateException("context not set");
        }
    }
}
