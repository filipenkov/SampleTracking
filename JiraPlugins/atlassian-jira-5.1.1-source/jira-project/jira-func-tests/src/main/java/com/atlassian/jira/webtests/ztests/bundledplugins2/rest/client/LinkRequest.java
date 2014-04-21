package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.issue.ResourceRef;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since v4.3
 */
public class LinkRequest
{
    public static final String FIELD_NAME = "issuelinks";

    @JsonProperty
    private ResourceRef type;

    @JsonProperty
    private ResourceRef inwardIssue;

    @JsonProperty
    private ResourceRef outwardIssue;

    @JsonProperty
    private Comment comment;

    public LinkRequest()
    {
    }

    public LinkRequest(ResourceRef type, ResourceRef inwardIssue, ResourceRef outwardIssue, Comment comment)
    {
        this.comment = comment;
        this.type = type;
        this.inwardIssue = inwardIssue;
        this.outwardIssue = outwardIssue;
    }

    public ResourceRef type()
    {
        return this.type;
    }

    public LinkRequest type(ResourceRef linkType)
    {
        return new LinkRequest(linkType, inwardIssue, outwardIssue, comment);
    }

    public ResourceRef inwardIssue()
    {
        return this.inwardIssue;
    }

    public LinkRequest inwardIssue(ResourceRef inwardIssue)
    {
        return new LinkRequest(type, inwardIssue, outwardIssue, comment);
    }

    public ResourceRef outwardIssue()
    {
        return this.outwardIssue;
    }

    public LinkRequest outwardIssue(ResourceRef outwardIssue)
    {
        return new LinkRequest(type, inwardIssue, outwardIssue, comment);
    }

    public Comment comment()
    {
        return this.comment;
    }

    public LinkRequest comment(Comment comment)
    {
        return new LinkRequest(type, inwardIssue, outwardIssue, comment);
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
