package com.atlassian.crowd.plugin.rest.service.controller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.atlassian.crowd.plugin.rest.entity.GroupEntityList;
import com.atlassian.crowd.plugin.rest.entity.UserEntityList;


public class MembershipEntity
{
    @XmlAttribute
    String group;
    
    @XmlElement
    UserEntityList users;
    
    @XmlElement
    GroupEntityList groups;

    @Override
    public String toString()
    {
        return group + "={users:" + users + ",groups:" + groups + "}";
    }
}
