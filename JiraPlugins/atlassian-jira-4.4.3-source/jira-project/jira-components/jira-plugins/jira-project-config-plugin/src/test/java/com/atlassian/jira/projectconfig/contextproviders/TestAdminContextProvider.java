package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.easymock.IMocksControl;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.4
 */
public class TestAdminContextProvider
{
    @Test
    public void testExtractCurrentHeading()
    {
        final IMocksControl mockControl = createControl();

        final JiraAuthenticationContext mockContext = mockControl.createMock(JiraAuthenticationContext.class);
        final SimpleLinkManager mockSimpleLinkManager = mockControl.createMock(SimpleLinkManager.class);

        final MockUser admin = new MockUser("admin");
        expect(mockContext.getLoggedInUser()).andReturn(admin).anyTimes();

        expect(mockSimpleLinkManager.getLinksForSection("stuffed-menu", admin, null)).andReturn(Collections.<SimpleLink>emptyList());
        expect(mockSimpleLinkManager.getLinksForSection("project-menu", admin, null)).andReturn(CollectionBuilder.list(createSimpleLink("projects", "Projects")));
        expect(mockSimpleLinkManager.getLinksForSection("project-menu", admin, null)).andReturn(CollectionBuilder.list(createSimpleLink("projects", "Projects")));
        expect(mockSimpleLinkManager.getSectionsForLocation("options-menu", admin, null)).andReturn(Collections.<SimpleLinkSection>emptyList());
        expect(mockSimpleLinkManager.getLinksForSection("options-menu/issue-features", admin, null)).andReturn(CollectionBuilder.list(createSimpleLink("priorities", "Priorities")));
        expect(mockSimpleLinkManager.getSectionsForLocation("options-menu", admin, null)).andReturn(CollectionBuilder.list(createSimpleLinkSection("workflows", "Workflows Section")));
        expect(mockSimpleLinkManager.getSectionsForLocation("enum_options", admin, null)).andReturn(CollectionBuilder.list(createSimpleLinkSection("some-crap", "CRAP"), createSimpleLinkSection("issue_types", "Issue Types")));

        mockControl.replay();
        final AdminContextProvider provider = new AdminContextProvider(mockContext, mockSimpleLinkManager);

        Map<String, Object> context = MapBuilder.<String, Object>build("unrelated", "value");
        String heading = provider.extractHeading(context, null);
        assertNull(heading);

        //1 level deep with no active tab. Should never happen but hey
        context = MapBuilder.<String, Object>build("admin.active.section", "somesection");
        heading = provider.extractHeading(context, null);
        assertNull(heading);
        
        //1 level deep with with active tab but no matching section
        context = MapBuilder.<String, Object>build("admin.active.section", "stuffed-menu", "admin.active.tab", "projects");
        heading = provider.extractHeading(context, null);
        assertNull(heading);

        //1 level deep with with active tab matching section but stuffed tab
        context = MapBuilder.<String, Object>build("admin.active.section", "project-menu", "admin.active.tab", "stuffed tab");
        heading = provider.extractHeading(context, null);
        assertNull(heading);

        //1 level deep with with active tab matching section and matching tab
        context = MapBuilder.<String, Object>build("admin.active.section", "project-menu", "admin.active.tab", "projects");
        heading = provider.extractHeading(context, null);
        assertEquals("Projects", heading);

        //2 levels deep with with active tab. No match for section in the hierarchy. Should fall back to looking for matching links in the current hierarchy.
        context = MapBuilder.<String, Object>build("admin.active.section", "options-menu/issue-features", "admin.active.tab", "priorities");
        heading = provider.extractHeading(context, null);
        assertEquals("Priorities", heading);

        //2 levels deep with with active tab. Match for section in the hierarchy.
        context = MapBuilder.<String, Object>build("admin.active.section", "options-menu/workflows", "admin.active.tab", "workflow-schemes");
        heading = provider.extractHeading(context, null);
        assertEquals("Workflows Section", heading);

        //3 levels deep match in the hierarchy
        context = MapBuilder.<String, Object>build("admin.active.section", "options-menu/enum_options/issue_types", "admin.active.tab", "sub-tasks");
        heading = provider.extractHeading(context, null);
        assertEquals("Issue Types", heading);

        mockControl.verify();
    }

    private SimpleLinkSection createSimpleLinkSection(String id, String label)
    {
        return new SimpleLinkSectionImpl(id, label, null, null, null, null);
    }

    private SimpleLink createSimpleLink(String id, String label)
    {
        return new SimpleLinkImpl(id, label, null, null, null, "some/url", null);
    }
}
