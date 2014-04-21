package com.atlassian.crowd.integration.rest.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains a list of <tt>UserEntity</tt>s.
 *
 * @since v2.1
 */
@XmlRootElement (name = "users")
@XmlAccessorType (XmlAccessType.FIELD)
public class UserEntityList implements Iterable<UserEntity>
{
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
        this.users = new ArrayList<UserEntity>(users);
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

