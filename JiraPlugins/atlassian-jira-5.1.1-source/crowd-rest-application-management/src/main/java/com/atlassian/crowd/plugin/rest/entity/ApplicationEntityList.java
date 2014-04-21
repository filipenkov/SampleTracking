package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains a list of <tt>ApplicationEntity</tt>s.
 *
 * @since 2.2
 */
@XmlRootElement (name = "applications")
@XmlAccessorType (XmlAccessType.FIELD)
@SuppressWarnings("unused")
public class ApplicationEntityList implements Iterable<ApplicationEntity>
{
    @XmlElements (@XmlElement (name = "application"))
    private final List<ApplicationEntity> applications;

    @XmlElement (name = "link")
    private final Link link;

    /**
     * JAXB requires a no-arg constructor.
     */
    private ApplicationEntityList()
    {
        applications = new ArrayList<ApplicationEntity>();
        link = null;
    }

    public ApplicationEntityList(final List<ApplicationEntity> applications, final Link link)
    {
        this.applications = ImmutableList.copyOf(checkNotNull(applications));
        this.link = link;
    }

    public int size()
    {
        return applications.size();
    }

    public boolean isEmpty()
    {
        return applications.isEmpty();
    }

    public ApplicationEntity get(final int index)
    {
        return applications.get(index);
    }

    public Iterator<ApplicationEntity> iterator()
    {
        return applications.iterator();
    }

    public Link getLink()
    {
        return link;
    }
}
