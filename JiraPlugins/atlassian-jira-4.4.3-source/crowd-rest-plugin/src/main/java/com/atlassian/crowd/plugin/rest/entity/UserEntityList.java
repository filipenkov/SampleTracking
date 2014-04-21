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
 * Contains a list of <tt>UserEntity</tt>s.
 *
 * @since v2.1
 */
@XmlRootElement (name = "users")
@XmlAccessorType (XmlAccessType.FIELD)
public class UserEntityList implements Iterable<UserEntity>
{
    /**
     * Name of the user list field.
     */
    public static final String USER_LIST_FIELD_NAME = "users";

    @SuppressWarnings("unused")
    @XmlAttribute
    private String expand;

    @Expandable ("user")
    @XmlElements (@XmlElement (name = "user"))
    private final List<UserEntity> users;

    /**
     * JAXB requires a no-arg constructor.
     */
    private UserEntityList()
    {
        users = new ArrayList<UserEntity>();
    }

    public UserEntityList(final List<UserEntity> users)
    {
        this.users = ImmutableList.copyOf(checkNotNull(users));
    }

    public int size()
    {
        return users.size();
    }

    public boolean isEmpty()
    {
        return users.isEmpty();
    }

    public UserEntity get(final int index)
    {
        return users.get(index);
    }

    public Iterator<UserEntity> iterator()
    {
        return users.iterator();
    }
}
