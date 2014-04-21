package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This bean holds information about issue linkedIssue types.
 *
 * @since v4.2
 */
@XmlRootElement (name = "linkType")
public class LinkedIssueTypeBean
{
    @XmlElement (name = "name")
    private String name;

    @XmlElement (name = "direction")
    private Direction direction;

    @XmlElement (name = "description")
    private String description;

    public enum Direction
    {
        INBOUND,
        OUTBOUND
    }
    
    public LinkedIssueTypeBean()
    {
    }

    static Builder instance()
    {
        return new Builder();
    }

    static class Builder
    {
        final private LinkedIssueTypeBean linkedIssue;

        public Builder()
        {
            linkedIssue = new LinkedIssueTypeBean();
        }

        public Builder name(final String name)
        {
            linkedIssue.name = name;
            return this;
        }

        public Builder direction(final Direction direction)
        {
            linkedIssue.direction = direction;
            return this;
        }

        public Builder description(final String description)
        {
            linkedIssue.description = description;
            return this;
        }

        public LinkedIssueTypeBean build()
        {
            if (linkedIssue.name == null)
            {
                throw new IllegalStateException("name of linkedIssue is not set");
            }
            if (linkedIssue.direction == null)
            {
                throw new IllegalStateException("direction of linkedIssue is not set");
            }
            return linkedIssue;
        }

    }
}