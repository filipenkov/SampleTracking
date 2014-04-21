package com.atlassian.crowd.plugin.rest.service.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.plugin.rest.entity.NamedEntity;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MembershipsControllerTest
{
    @Mock
    private ApplicationService applicationService;
    
    @Mock
    private ApplicationManager applicationManager;

    @Test
    public void emptyResultsAreWrittenAsWellFormedXml() throws Exception
    {
        MembershipsController mc = new MembershipsController(applicationService, applicationManager);

        StreamingOutput result = mc.searchGroups("test");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        result.write(baos);

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        Document doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
        
        assertEquals("memberships", doc.getDocumentElement().getTagName());
        assertEquals(0, doc.getDocumentElement().getElementsByTagName("*").getLength());
    }
    
    @Test
    public void emptyResultAreUnmarshalled() throws Exception
    {
        MembershipsController mc = new MembershipsController(applicationService, applicationManager);

        StreamingOutput result = mc.searchGroups("test");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        result.write(baos);

        JAXBContext context = JAXBContext.newInstance(MembershipsEntity.class);
        
        MembershipsEntity x = (MembershipsEntity) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(baos.toByteArray()));

        assertEquals(Collections.emptyList(), x.memberships);
    }
    
    private static MembershipsEntity unmarshal(byte[] ba) throws JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(MembershipsEntity.class);
        
        return (MembershipsEntity) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(ba));
    }
    
    @Test
    public void singleEmptyMembership() throws Exception
    {
        when(applicationService.searchGroups(Mockito.<Application>any(), Mockito.<EntityQuery<String>>any())).thenReturn(
                Arrays.asList("group"));
                
        MembershipsController mc = new MembershipsController(applicationService, applicationManager);

        StreamingOutput result = mc.searchGroups("test");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        result.write(baos);

        MembershipsEntity x = unmarshal(baos.toByteArray());

        MembershipEntity membership = x.memberships.get(0);
        
        assertEquals("group", membership.group);
        assertEquals(Collections.emptyList(), namesOf(membership.users));
        assertEquals(Collections.emptyList(), namesOf(membership.groups));
    }
    
    @Test
    public void groupWithASingleMemberAndChildGroup() throws Exception
    {
        when(applicationService.searchGroups(Mockito.<Application>any(), Mockito.<EntityQuery<String>>any())).thenReturn(
                Arrays.asList("group"));
        
        when(applicationService.searchDirectGroupRelationships(Mockito.<Application>any(), Mockito.<MembershipQuery<String>>any())).thenAnswer(
                new Answer<List<String>>()
                {
                    public List<String> answer(InvocationOnMock invocation) throws Throwable
                    {
                        MembershipQuery<?> q = (MembershipQuery<?>) invocation.getArguments()[1];
                        
                        switch (q.getEntityToReturn().getEntityType())
                        {
                            case USER:
                                return Arrays.asList("user");
                                
                            case GROUP:
                                return Arrays.asList("child-group");
                                
                            default:
                                fail("Unexpected query type in test: " + q.getEntityToReturn());
                                return null;
                        }
                    }
                });
                
        MembershipsController mc = new MembershipsController(applicationService, applicationManager);

        StreamingOutput result = mc.searchGroups("test");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        result.write(baos);

        MembershipsEntity x = unmarshal(baos.toByteArray());

        MembershipEntity membership = x.memberships.get(0);

        assertEquals("group", membership.group);
        assertEquals(Arrays.asList("user"), namesOf(membership.users));
        assertEquals(Arrays.asList("child-group"), namesOf(membership.groups));
    }
    
    private static List<String> namesOf(Iterable<? extends NamedEntity> entities)
    {
        List<String> names = new ArrayList<String>();
        
        for (NamedEntity e : entities)
        {
            names.add(e.getName());
        }
        
        return names;
    }
}
