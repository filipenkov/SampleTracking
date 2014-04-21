package com.atlassian.crowd.plugin.rest.service.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import com.megginson.sax.XMLWriter;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class MembershipsController extends AbstractResourceController
{
    public MembershipsController(ApplicationService applicationService, ApplicationManager applicationManager)
    {
        super(applicationService, applicationManager);
    }

    public StreamingOutput searchGroups(final String applicationName)
    {
        return new OutputAsXml(applicationName);
    }
    
    /**
     * This method streams the output so the server doesn't need to hold all the memberships in
     * memory.
     */
    void writeXmlToStream(String applicationName, OutputStream output) throws IOException, SAXException
    {
        final Application application = getApplication(applicationName);

        OutputStreamWriter w = new OutputStreamWriter(output, "utf-8");
        XMLWriter ch = new XMLWriter(w);
        
        ch.startDocument();
        
        AttributesImpl attrs = new AttributesImpl();
        
        ch.startElement("", "memberships", "memberships", attrs);
        ch.characters("\n");
        
        SearchRestriction searchRestriction = NullRestrictionImpl.INSTANCE;
        
        final EntityQuery<String> entityQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(searchRestriction).startingAt(0).returningAtMost(EntityQuery.ALL_RESULTS);
        
        for (String groupName : applicationService.searchGroups(application, entityQuery))
        {
            ch.characters(" ");
            attrs.clear();
            attrs.addAttribute("", "group", "group", "CDATA", groupName);
            ch.startElement("", "membership", "membership", attrs);
            ch.characters("\n");
            
            ch.characters("  ");
            attrs.clear();
            ch.startElement("", "users", "users", attrs);
            ch.characters("\n");
            
            /* Direct user members */
            MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).startingAt(0).returningAtMost(EntityQuery.ALL_RESULTS);
            for (String user : applicationService.searchDirectGroupRelationships(application, query))
            {
                ch.characters("   ");
                attrs.clear();
                attrs.addAttribute("", "name", "name", "CDATA", user);
                ch.emptyElement("", "user", "user", attrs);
                ch.characters("\n");
            }

            ch.characters("  ");
            ch.endElement("users");
            ch.characters("\n");
            
            ch.characters("  ");
            attrs.clear();
            ch.startElement("", "groups", "groups", attrs);
            ch.characters("\n");
            
            /* Child groups */
            MembershipQuery<String> query2 = QueryBuilder.createMembershipQuery(EntityQuery.ALL_RESULTS, 0, true, EntityDescriptor.group(), String.class, EntityDescriptor.group(), groupName);
            for (String childGroup : applicationService.searchDirectGroupRelationships(application, query2))
            {
                ch.characters("   ");
                attrs.clear();
                attrs.addAttribute("", "name", "name", "CDATA", childGroup);
                ch.emptyElement("", "group", "group", attrs);
                ch.characters("\n");
            }


            ch.characters("  ");
            ch.endElement("groups");
            ch.characters("\n");

            ch.characters(" ");
            ch.endElement("", "membership", "membership");
            ch.characters("\n");
        }
        
        ch.endElement("", "memberships", "memberships");
        ch.endDocument();
    }
    
    class OutputAsXml implements StreamingOutput
    {
        private final String applicationName;
        
        public OutputAsXml(String applicationName)
        {
            this.applicationName = applicationName;
        }
        
        public void write(OutputStream output) throws IOException, WebApplicationException
        {
            try
            {
                writeXmlToStream(applicationName, output);
            }
            catch (SAXException e)
            {
                throw new WebApplicationException(e);
            }
        }
    }
}
