package com.atlassian.jira.dashboard;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletAccessManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;

public class TestJiraApplicationGadgetSpecProvider extends MockControllerTestCase
{
    @Test
    public void testEntries()
    {
        final Portlet mockPortlet = mockController.getMock(Portlet.class);
        mockPortlet.getId();
        mockController.setReturnValue("com.atlassian.jira:projects", 2);

        final Portlet mockPortlet2 = mockController.getMock(Portlet.class);
        mockPortlet2.getId();
        mockController.setReturnValue("com.atlassian.jira:introduction", 2);

        //this one should be stripped away!
        final Portlet mockSystemPortlet = mockController.getMock(Portlet.class);
        mockPortlet.getId();
        mockController.setReturnValue("com.atlassian.jira.plugin.system.portlets:introduction", 1);

        final PortletAccessManager mockPortletAccessManager = mockController.getMock(PortletAccessManager.class);
        mockPortletAccessManager.getAllPortlets();
        mockController.setReturnValue(CollectionBuilder.newBuilder(mockPortlet, mockPortlet2, mockSystemPortlet).asList());

        final LegacyGadgetUrlProvider mockLegacyGadgetUrlProvider = mockController.getMock(LegacyGadgetUrlProvider.class);
        mockLegacyGadgetUrlProvider.getLegacyURI("com.atlassian.jira:projects");
        mockController.setReturnValue(URI.create("http://localhost:8090/jira/rest/spec/projects.xml"));
        mockLegacyGadgetUrlProvider.getLegacyURI("com.atlassian.jira:introduction");
        mockController.setReturnValue(URI.create("http://localhost:8090/jira/rest/spec/introduction.xml"));

        final JiraApplicationGadgetSpecProvider gadgetSpecStore = mockController.instantiateAndReplay(JiraApplicationGadgetSpecProvider.class);
        final Iterable<URI> iterable = gadgetSpecStore.entries();
        final Iterator<URI> uriIterator = iterable.iterator();
        assertEquals(URI.create("http://localhost:8090/jira/rest/spec/projects.xml"), uriIterator.next());
        assertEquals(URI.create("http://localhost:8090/jira/rest/spec/introduction.xml"), uriIterator.next());
        assertFalse(uriIterator.hasNext());
    }

    @Test
    public void testNoEntries()
    {
        final PortletAccessManager mockPortletAccessManager = mockController.getMock(PortletAccessManager.class);
        mockPortletAccessManager.getAllPortlets();
        mockController.setReturnValue(Collections.emptyList());

        final JiraApplicationGadgetSpecProvider gadgetSpecStore = mockController.instantiate(JiraApplicationGadgetSpecProvider.class);
        final Iterable<URI> iterable = gadgetSpecStore.entries();
        final Iterator<URI> uriIterator = iterable.iterator();
        assertFalse(uriIterator.hasNext());
    }

    @Test
    public void testContains()
    {
        final Portlet mockPortlet = mockController.getMock(Portlet.class);
        mockPortlet.getId();
        mockController.setDefaultReturnValue("com.atlassian.jira:projects");

        final Portlet mockPortlet2 = mockController.getMock(Portlet.class);
        mockPortlet2.getId();
        mockController.setDefaultReturnValue("com.atlassian.jira:introduction");

        final PortletAccessManager mockPortletAccessManager = mockController.getMock(PortletAccessManager.class);
        mockPortletAccessManager.getAllPortlets();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder(mockPortlet, mockPortlet2).asList());

        final LegacyGadgetUrlProvider mockLegacyGadgetUrlProvider = mockController.getMock(LegacyGadgetUrlProvider.class);
        mockLegacyGadgetUrlProvider.getLegacyURI("com.atlassian.jira:projects");
        mockController.setDefaultReturnValue(URI.create("http://localhost:8090/jira/rest/spec/projects.xml"));
        mockLegacyGadgetUrlProvider.getLegacyURI("com.atlassian.jira:introduction");
        mockController.setDefaultReturnValue(URI.create("http://localhost:8090/jira/rest/spec/introduction.xml"));

        final JiraApplicationGadgetSpecProvider gadgetSpecStore = mockController.instantiateAndReplay(JiraApplicationGadgetSpecProvider.class);
        assertTrue(gadgetSpecStore.contains(URI.create("http://localhost:8090/jira/rest/spec/projects.xml")));
        assertFalse(gadgetSpecStore.contains(URI.create("http://www.google.com/ig/somepsec.xml")));
    }
}
