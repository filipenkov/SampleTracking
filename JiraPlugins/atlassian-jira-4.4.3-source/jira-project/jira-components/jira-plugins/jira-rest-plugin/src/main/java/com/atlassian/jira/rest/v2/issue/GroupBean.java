package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class for a user group.
 *
 * @since v4.2
 */
@XmlRootElement
public class GroupBean
{
    /**
     * The group name.
     */
    @XmlElement
    private String name;

    /**
     * Creates a new GroupBean for the group with the given name.
     *
     * @param name a String containing the group name
     */
    public GroupBean(String name)
    {
        this.name = name;
    }
}
