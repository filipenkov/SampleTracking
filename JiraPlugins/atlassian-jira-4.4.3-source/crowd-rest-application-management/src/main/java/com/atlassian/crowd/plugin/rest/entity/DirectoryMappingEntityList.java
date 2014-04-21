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
 * Contains a list of <tt>DirectoryMappingEntity</tt>s.
 *
 * @since 2.2
 */
@XmlRootElement (name = "directory-mappings")
@XmlAccessorType (XmlAccessType.FIELD)
public class DirectoryMappingEntityList implements Iterable<DirectoryMappingEntity>
{
    @XmlElements (@XmlElement (name = "directory-mapping"))
    private final List<DirectoryMappingEntity> directoryMappings;

    @XmlElement (name = "link")
    private final Link link;

    /**
     * JAXB requires a no-arg constructor.
     */
    private DirectoryMappingEntityList()
    {
        directoryMappings = new ArrayList<DirectoryMappingEntity>();
        link = null;
    }

    public DirectoryMappingEntityList(final List<DirectoryMappingEntity> directoryMappings, final Link link)
    {
        this.directoryMappings = ImmutableList.copyOf(checkNotNull(directoryMappings));
        this.link = link;
    }

    public int size()
    {
        return directoryMappings.size();
    }

    public boolean isEmpty()
    {
        return directoryMappings.isEmpty();
    }

    public DirectoryMappingEntity get(final int index)
    {
        return directoryMappings.get(index);
    }

    public Iterator<DirectoryMappingEntity> iterator()
    {
        return directoryMappings.iterator();
    }

    public Link getLink()
    {
        return link;
    }
}
