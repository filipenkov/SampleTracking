package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This bean holds the information that is reported for each issue link.
 *
 * @since v4.2
 */
@XmlRootElement (name = "issueLinks")
public class IssueLinkBean
{
    @XmlElement (name = "issueKey")
    private String key;

    @XmlElement
    private URI issue;

    @XmlElement (name = "type")
    private LinkedIssueTypeBean type;

    private IssueLinkBean()
    {
        // needed for JAXB
    }

    public IssueLinkBean(String key, URI selfUri, LinkedIssueTypeBean linkedIssueType)
    {
        this.key = key;
        this.issue = selfUri;
        this.type =  linkedIssueType;
    }

    public String getKey()
    {
        return key;
    }
}
