package com.atlassian.jira.rest.v2.admin;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Transport for getting the key-value for an application property modification via REST
 *
 * @since v4.4
 */

@XmlRootElement(name = "applicationProperty")
public class ApplicationPropertyBean
{
    @XmlElement
    private String id;

    @XmlElement
    private String value;

    public ApplicationPropertyBean()
    {}

    public ApplicationPropertyBean(final String id, final String value)
    {
        this.id = id;
        this.value = value;
    }

    public String getId()
    {
        return id;
    }

    public String getValue()
    {
        return value;
    }
}
