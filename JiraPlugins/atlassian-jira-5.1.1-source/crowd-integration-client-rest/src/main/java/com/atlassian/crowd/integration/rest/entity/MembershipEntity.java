package com.atlassian.crowd.integration.rest.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Membership;


public class MembershipEntity implements Membership
{
    private String group;
    
    private UserEntityList users;
    
    private GroupEntityList groups;

    public MembershipEntity()
    {
        this(new UserEntityList(Collections.<UserEntity>emptyList()), new GroupEntityList(Collections.<GroupEntity>emptyList()));
    }
    
    public MembershipEntity(UserEntityList users, GroupEntityList groups)
    {
        this.users = users;
        this.groups = groups;
    }

    @Override
    public String toString()
    {
        return group + "={users:" + users + ",groups:" + groups + "}";
    }

    @XmlAttribute(name = "group")
    public String getGroupName()
    {
        return group;
    }
    
    public void setGroupName(String name)
    {
        this.group = name;
    }
    
    @XmlElement(name="users")
    public UserEntityList getUsers()
    {
        return users;
    }
    
    @XmlElement(name="groups")
    public GroupEntityList getGroups()
    {
        return groups;
    }

    public void setUsers(UserEntityList users)
    {
        this.users = users;
    }

    public void setGroups(GroupEntityList childGroups)
    {
        this.groups = childGroups;
    }
    
    public Set<String> getUserNames()
    {
        return namesOf(users);
    }
    
    public Set<String> getChildGroupNames()
    {
        return namesOf(groups);
    }
    
    private static Set<String> namesOf(Iterable<? extends DirectoryEntity> entities)
    {
        Set<String> names = new HashSet<String>();
        
        for (DirectoryEntity e : entities)
        {
            names.add(e.getName());
        }
        
        return names;
    }
}
