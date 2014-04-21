package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapper;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import com.google.common.collect.ImmutableList;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains a list of <tt>GroupEntity</tt>s.
 *
 * @since v2.1
 */
@XmlRootElement (name = "groups")
@XmlAccessorType (XmlAccessType.FIELD)
public class GroupEntityList implements Iterable<GroupEntity>
{
    /**
     * Name of the group list field.
     */
    public static final String GROUP_LIST_FIELD_NAME = "groups";

    @SuppressWarnings("unused")
    @XmlAttribute
    private String expand;

    @Expandable("group")
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
        this.groups = ImmutableList.copyOf(checkNotNull(groups));
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
