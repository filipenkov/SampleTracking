package com.atlassian.jira.plugin.issuenav.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* Searcher
* @since v5.0
*/
@XmlRootElement
public class Searcher
{
    @XmlElement
    private String name;

    @XmlElement
    private String id;

    public Searcher()
    {
    }

    public Searcher(final String id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }
}
