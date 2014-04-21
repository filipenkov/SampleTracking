package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB bean for link issue requests.
 *
 * @since v4.3
 */
@XmlRootElement
public class LinkIssueRequestBean
{
    /**
     * The name of issue link type
     */
    @XmlElement
    private String linkType;

    /**
     * the issue key to link from.
     */
    @XmlElement
    private String fromIssueKey;

    /**
     * the issue key to create the link to.
     */
    @XmlElement
    private String toIssueKey;

    /**
     * An optional comment to add to the issue the link is created from.
     */
    @XmlElement
    private CommentBean comment;

    public LinkIssueRequestBean(){}

    public LinkIssueRequestBean(String fromIssueKey, String toIssueKey, String linkType, CommentBean comment)
    {
        this.fromIssueKey = fromIssueKey;
        this.toIssueKey = toIssueKey;
        this.linkType = linkType;
        this.comment = comment;
    }

    /**
     * Example representation for use in auto-generated docs.
     */
    public static final LinkIssueRequestBean DOC_EXAMPLE = new LinkIssueRequestBean("HSP-1", "MKY-1", "Duplicate", CommentBean.DOC_COMMENT_LINK_ISSUE_EXAMPLE);


    public String getLinkType()
    {
        return linkType;
    }

    public CommentBean getComment()
    {
        return comment;
    }

    public String getFromIssueKey()
    {
        return fromIssueKey;
    }

    public String getToIssueKey()
    {
        return toIssueKey;
    }
}
