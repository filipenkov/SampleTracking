package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains a list of <tt>GroupEntity</tt>s.
 *
 * @since v2.1
 */
@XmlRootElement (name = "groups")
@XmlAccessorType (XmlAccessType.FIELD)
public class GroupEntityList implements Iterable<GroupEntity>
{
    @XmlElements (@XmlElement (name = "group"))
    private final List<GroupEntity> groups;

    /**
     * JAXB requires a no-arg constructor.
     */
    private GroupEntityList()
    {
        groups = new ArrayList<GroupEntity>();
    }

    public GroupEntityList(final List<GroupEntity> groups)
    {
        this.groups = new ArrayList<GroupEntity>(groups);
    }

    public int size()
    {
        return groups.size();
    }

    public boolean isEmpty()
    {
        return groups.isEmpty();
    }

    public GroupEntity get(final int index)
    {
        return groups.get(index);
    }

    public Iterator<GroupEntity> iterator()
    {
        return groups.iterator();
    }
}
