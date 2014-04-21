package com.atlassian.crowd.integration.rest.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.crowd.model.group.Membership;


@XmlRootElement (name = "memberships")
public class MembershipsEntity
{
    @XmlElement(name = "membership")
    private List<MembershipEntity> memberships;

    public MembershipsEntity()
    {
        this(new ArrayList<MembershipEntity>());
    }
    
    public MembershipsEntity(List<MembershipEntity> list)
    {
        this.memberships = list;
    }

    public List<? extends Membership> getList()
    {
        return memberships;
    }
    
    public String toString()
    {
        return "Memberships:" + memberships;
    }
}
