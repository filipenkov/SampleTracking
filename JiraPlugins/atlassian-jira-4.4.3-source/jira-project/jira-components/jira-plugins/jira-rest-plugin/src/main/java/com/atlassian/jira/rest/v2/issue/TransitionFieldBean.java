package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.IssueFieldConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since v4.2
 */
@XmlRootElement (name="availableField")
public class TransitionFieldBean
{
    @XmlElement
    private String id;

    @XmlElement
    private boolean required;

    @XmlElement
    private String type;

    TransitionFieldBean() {}

    public static TransitionFieldBean newBean()
    {
        return new TransitionFieldBean();
    }

    public TransitionFieldBean id(final String name)
    {
        this.id = name;
        return this;
    }

    public TransitionFieldBean required(final boolean required)
    {
        this.required = required;
        return this;
    }

    public TransitionFieldBean type(final String type)
    {
        this.type = type;
        return this;
    }

    static public final TransitionFieldBean DOC_EXAMPLE = newBean()
            .id(IssueFieldConstants.RESOLUTION)
            .required(true)
            .type(JiraDataTypes.getFieldType(IssueFieldConstants.RESOLUTION).asStrings().iterator().next());
}
