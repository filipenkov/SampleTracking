package com.atlassian.applinks.core.rest.model;

import com.atlassian.plugins.rest.common.Link;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for entities. Provides links.
 */
@XmlSeeAlso({ApplicationLinkEntity.class})
public class LinkedEntity
{
    @XmlElement(name="link")
    private List<Link> links;

    public void addLink(Link link)
    {
        if(links==null)
            links = new ArrayList<Link>();
        links.add(link);
    }
}

