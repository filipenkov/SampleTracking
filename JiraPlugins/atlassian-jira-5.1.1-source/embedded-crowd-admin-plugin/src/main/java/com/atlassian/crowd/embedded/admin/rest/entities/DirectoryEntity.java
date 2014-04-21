package com.atlassian.crowd.embedded.admin.rest.entities;

import com.atlassian.plugins.rest.common.Link;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An entity representing a user directory within embedded crowd.
 */
@XmlRootElement(name="directory")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectoryEntity
{
    @XmlAttribute
    private String name;

    @XmlElement(name="link")
    private List<Link> links = new ArrayList<Link>();

    @XmlElement(name="synchronisation")
    private DirectorySynchronisationInformationEntity sync;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Link> getLinks()
    {
        return links;
    }

    public void setLinks(List<Link> links)
    {
        this.links = links;
    }

    public DirectorySynchronisationInformationEntity getSync()
    {
        return sync;
    }

    public void setSync(DirectorySynchronisationInformationEntity sync)
    {
        this.sync = sync;
    }
}
