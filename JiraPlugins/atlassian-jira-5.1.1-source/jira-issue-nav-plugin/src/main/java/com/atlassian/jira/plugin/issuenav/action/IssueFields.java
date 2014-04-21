package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.plugin.issuenav.viewissue.webpanel.IssueWebPanelsBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Returns all fields required by kickass view refresh - issue edit fields, web panels and issue details
 *
 * @since v5.0.3
 */
@XmlRootElement
public class IssueFields extends EditFields
{
    @XmlElement (name = "issue")
    private IssueBean issue;

    @XmlElement (name = "panels")
    private IssueWebPanelsBean panels;

    private IssueFields() {}

    public IssueFields(final String atlToken, final ErrorCollection errorCollection)
    {
        super(atlToken, errorCollection);
    }

    public IssueFields(final List<FieldHtmlBean> fields, final String atlToken, final ErrorCollection errorCollection)
    {
        super(fields, atlToken, errorCollection);
    }

    public IssueFields(final List<FieldHtmlBean> fields, final String atlToken, final ErrorCollection errorCollection,
                       IssueBean issue, IssueWebPanelsBean panels)
    {
        super(fields, atlToken, errorCollection);
        this.issue = issue;
        this.panels = panels;
    }

    public IssueBean getIssue()
    {
        return issue;
    }

    public IssueWebPanelsBean getPanels()
    {
        return panels;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("fields", getFields()).
                append("errors", getErrorCollection()).
                append("issue", issue).
                append("panels", panels).
                toString();
    }
}
