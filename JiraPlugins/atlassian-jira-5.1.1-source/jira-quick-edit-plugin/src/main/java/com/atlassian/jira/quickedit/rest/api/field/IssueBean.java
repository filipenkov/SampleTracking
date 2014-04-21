package com.atlassian.jira.quickedit.rest.api.field;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since v5.0
 */
@XmlRootElement(name = "issue")
public class IssueBean {

    @XmlElement(name = "issueKey")
    private String issueKey;

    @XmlElement(name = "issueId")
    private Long issueId;

    private IssueBean() {} // needed for jersy magic

    public IssueBean(String issueKey, Long issueId)
    {
        this.issueKey = issueKey;
        this.issueId = issueId;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public Long getIssueId() {
        return issueId;
    }
}
