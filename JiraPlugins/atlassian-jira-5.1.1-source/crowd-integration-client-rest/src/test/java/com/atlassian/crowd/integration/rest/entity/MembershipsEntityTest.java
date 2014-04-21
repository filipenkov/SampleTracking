package com.atlassian.crowd.integration.rest.entity;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.atlassian.crowd.model.group.Membership;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;

public class MembershipsEntityTest
{
    @Test
    public void unmarshallingGivesValidResults() throws JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(MembershipsEntity.class);
        
        InputStream in = getClass().getResourceAsStream("memberships-sample.xml");
        assertNotNull("Test resource required", in);
        
        MembershipsEntity memberships = (MembershipsEntity) context.createUnmarshaller().unmarshal(in);
        
        assertNotNull(memberships);
        List<? extends Membership> l = memberships.getList();
        assertEquals(1, l.size());
        
        assertEquals("group-name", l.get(0).getGroupName());
        assertEquals(Collections.singleton("user"), l.get(0).getUserNames());
        assertEquals(Collections.singleton("child-group"), l.get(0).getChildGroupNames());
    }
}
