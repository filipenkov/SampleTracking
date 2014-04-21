package com.atlassian.crowd.plugin.rest.service.controller;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement (name = "memberships")
public class MembershipsEntity
{
    @XmlElement(name = "membership")
    List<MembershipEntity> memberships = new ArrayList<MembershipEntity>();

    public String toString()
    {
        return "Memberships:" + memberships;
    }
}
