package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultSimpleLinkManager
{
    @Mock private JiraWebInterfaceManager jiraWebInterfaceManager;
    @Mock private SimpleLinkFactoryModuleDescriptors simpleLinkFactoryModuleDescriptors;
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private VelocityRequestContextFactory velocityRequestContextFactory;
    @Mock private User user;
    private JiraHelper jiraHelper;
    private DefaultSimpleLinkManager simpleLinkManager;

    @Before
    public void setUp() throws Exception
    {
        simpleLinkManager = new DefaultSimpleLinkManager(jiraWebInterfaceManager, simpleLinkFactoryModuleDescriptors,
                jiraAuthenticationContext, velocityRequestContextFactory);
        jiraHelper = new JiraHelper();
    }

    @Test
    public void testGetLinksWithNoIdUsesKey() throws Exception
    {
        JiraWebItemModuleDescriptor webItem = mock(JiraWebItemModuleDescriptor.class);
        JiraWebLink link = mock(JiraWebLink.class);
        when(link.getRenderedUrl(user, jiraHelper)).thenReturn("http://url");
        when(webItem.getLink()).thenReturn(link);
        when(webItem.getKey()).thenReturn("mykey");
        when(jiraWebInterfaceManager.getDisplayableItems("section", user, jiraHelper)).thenReturn(Arrays.asList(webItem));
        when(simpleLinkFactoryModuleDescriptors.get()).thenReturn(Collections.<SimpleLinkFactoryModuleDescriptor>emptyList());

        List<SimpleLink> links = simpleLinkManager.getLinksForSection("section", user, jiraHelper);
        assertEquals(1, links.size());
        assertEquals("You have broken something Studio depends on (JRADEV-6587)", "mykey", links.get(0).getId());
    }

    @Test
    public void testGetLinksWithId() throws Exception
    {
        JiraWebItemModuleDescriptor webItem = mock(JiraWebItemModuleDescriptor.class);
        JiraWebLink link = mock(JiraWebLink.class);
        when(link.getRenderedUrl(user, jiraHelper)).thenReturn("http://url");
        when(link.getId()).thenReturn("myid");
        when(webItem.getLink()).thenReturn(link);
        when(webItem.getKey()).thenReturn("mykey");
        when(jiraWebInterfaceManager.getDisplayableItems("section", user, jiraHelper)).thenReturn(Arrays.asList(webItem));
        when(simpleLinkFactoryModuleDescriptors.get()).thenReturn(Collections.<SimpleLinkFactoryModuleDescriptor>emptyList());

        List<SimpleLink> links = simpleLinkManager.getLinksForSection("section", user, jiraHelper);
        assertEquals(1, links.size());
        assertEquals("myid", links.get(0).getId());
    }

}
