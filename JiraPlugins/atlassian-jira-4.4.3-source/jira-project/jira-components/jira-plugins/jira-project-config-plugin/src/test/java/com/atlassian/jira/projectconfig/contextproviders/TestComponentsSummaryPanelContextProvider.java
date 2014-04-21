package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.atlassian.jira.projectconfig.contextproviders.ComponentsSummaryPanelContextProvider.SimpleComponent;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestComponentsSummaryPanelContextProvider
{
    private static final String COMPONENT_LEAD_ID = "component-summary-panel-lead";
    private static final String URL = "something";

    private IMocksControl control;
    private ContextProviderUtils utils;
    private ProjectComponentService pcs;
    private UserFormats ufm;
    private UserFormat userFormat;
    private TabUrlFactory tabUrlFactory;

    @Before
    public void setup()
    {
        control = createControl();
        utils = control.createMock(ContextProviderUtils.class);
        pcs = control.createMock(ProjectComponentService.class);
        ufm = control.createMock(UserFormats.class);
        userFormat = control.createMock(UserFormat.class);
        tabUrlFactory = control.createMock(TabUrlFactory.class);
    }

    @After
    public void teardown()
    {
        control = null;
        utils = null;
        pcs = null;
        ufm = null;
        userFormat = null;
        tabUrlFactory = null;
    }

    @Test
    public void testGetContextMapNone() throws Exception
    {
        final Map<String,Object> arguments = MapBuilder.<String, Object>build("argument", true);
        final Project mockProject = new MockProject(181818L, "KEY");

        expect(utils.getProject()).andReturn(mockProject);
        expect(pcs.findAllForProject(EasyMock.<ErrorCollection>notNull(), eq(mockProject.getId()))).andReturn(Collections.<ProjectComponent>emptyList());
        expect(ufm.forType(ProfileLinkUserFormat.TYPE)).andReturn(userFormat);
        expect(utils.flattenErrors(new SimpleErrorCollection())).andReturn(Collections.<String>emptySet());
        expect(tabUrlFactory.forComponents()).andReturn(URL);

        control.replay();

        final ComponentsSummaryPanelContextProvider testing = new ComponentsSummaryPanelContextProvider(utils, pcs, ufm, tabUrlFactory);
        final Map<String, Object> actualContext = testing.getContextMap(arguments);

        final MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder("components", Collections.<Object>emptyList(),
                "errors", Collections.<String>emptySet(),
                "totalSize", 0,
                "actualSize", 0);
        expectedContext.add("manageUrl", URL);
        expectedContext.addAll(arguments);

        assertEquals(expectedContext.toMap(), actualContext);
        control.verify();
    }

    @Test
    public void testGetContextMapAll() throws Exception
    {
        final String leaderFormatted = "<user>leader</user>";
        final Map<String,Object> arguments = MapBuilder.<String, Object>build("argument", true);
        final Project mockProject = new MockProject(181818L, "KEY");
        final MockProjectComponent projectComponent1 = new MockProjectComponent(67L, "name1").setLead("leader");
        final MockProjectComponent projectComponent2 = new MockProjectComponent(56L, "name2");
        final List<ProjectComponent> components = Lists.<ProjectComponent>newArrayList(projectComponent1, projectComponent2);

        expect(utils.getProject()).andReturn(mockProject);
        expect(pcs.findAllForProject(EasyMock.<ErrorCollection>notNull(), eq(mockProject.getId()))).andReturn(components);
        expect(ufm.forType(ProfileLinkUserFormat.TYPE)).andReturn(userFormat);
        expect(utils.flattenErrors(new SimpleErrorCollection())).andReturn(Collections.<String>emptySet());
        expect(tabUrlFactory.forComponents()).andReturn(URL);

        expect(userFormat.format(projectComponent1.getLead(), COMPONENT_LEAD_ID)).andReturn(leaderFormatted);

        control.replay();

        final ComponentsSummaryPanelContextProvider testing = new ComponentsSummaryPanelContextProvider(utils, pcs, ufm, tabUrlFactory);
        final Map<String, Object> actualContext = testing.getContextMap(arguments);

        final List<SimpleComponent> simpleComponents = Lists.newArrayList(
                new SimpleComponent(projectComponent1.getName(), leaderFormatted),
                new SimpleComponent(projectComponent2.getName(), null));

        final MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder("components", simpleComponents,
                "errors", Collections.<String>emptySet(),
                "totalSize", 2,
                "actualSize", 2);
        expectedContext.add("manageUrl", URL);
        expectedContext.addAll(arguments);

        assertEquals(expectedContext.toMap(), actualContext);
        control.verify();
    }

    @Test
    public void testGetContextMapSome() throws Exception
    {
        final Map<String,Object> arguments = MapBuilder.<String, Object>build("argument", true);
        final Project mockProject = new MockProject(181818L, "KEY");
        final List<ProjectComponent> components = Lists.newArrayList();

        for (int i = 0; i < 20; i++)
        {
            String user = i % 2 == 0 ? String.format("User-%d", i) : null;
            components.add(new MockProjectComponent((long)i, String.format("Component-%d", i)).setLead(user));
        }

        expect(utils.getProject()).andReturn(mockProject);
        expect(pcs.findAllForProject(EasyMock.<ErrorCollection>notNull(), eq(mockProject.getId()))).andReturn(components);
        expect(ufm.forType(ProfileLinkUserFormat.TYPE)).andReturn(userFormat);
        expect(utils.flattenErrors(new SimpleErrorCollection())).andReturn(Collections.<String>emptySet());
        expect(tabUrlFactory.forComponents()).andReturn(URL);

        List<SimpleComponent> expectedComponents = Lists.newArrayList();

        for (ListIterator<ProjectComponent> iterator = components.listIterator(); iterator.hasNext() && iterator.nextIndex() < 5;)
        {
            ProjectComponent next = iterator.next();
            String user = null;
            if (next.getLead() != null)
            {
                user = String.format("<user>%s</user>", next.getLead());
                expect(userFormat.format(next.getLead(), COMPONENT_LEAD_ID))
                        .andReturn(user);
            }
            expectedComponents.add(new SimpleComponent(next.getName(), user));
        }

        control.replay();

        final ComponentsSummaryPanelContextProvider testing = new ComponentsSummaryPanelContextProvider(utils, pcs, ufm, tabUrlFactory);
        final Map<String, Object> actualContext = testing.getContextMap(arguments);

        final MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder("components", expectedComponents,
                "errors", Collections.<String>emptySet(),
                "totalSize", 20,
                "actualSize", 5);
        expectedContext.add("manageUrl", URL);
        expectedContext.addAll(arguments);

        assertEquals(expectedContext.toMap(), actualContext);
        control.verify();
    }

    @Test
    public void testGetContextMapError() throws Exception
    {
        final Map<String,Object> arguments = MapBuilder.<String, Object>build("argument", true);
        final Project mockProject = new MockProject(181818L, "KEY");
        final ProjectComponent component = new MockProjectComponent(78L, "Name");
        final List<ProjectComponent> components = Lists.newArrayList(component);
        final String error1 = "Error1";
        final String error2 = "Error1";

        expect(utils.getProject()).andReturn(mockProject);
        expect(ufm.forType(ProfileLinkUserFormat.TYPE)).andReturn(userFormat);
        expect(pcs.findAllForProject(EasyMock.<ErrorCollection>notNull(), eq(mockProject.getId()))).andReturn(components);
        expect(utils.flattenErrors(EasyMock.<ErrorCollection>notNull()))
                .andReturn(Sets.<String>newLinkedHashSet(Arrays.asList(error1, error2)));
        expect(tabUrlFactory.forComponents()).andReturn(URL);;

        final List<SimpleComponent> expectedComponents = Lists.newArrayList(new SimpleComponent(component.getName(), null));

        control.replay();

        final ComponentsSummaryPanelContextProvider testing = new ComponentsSummaryPanelContextProvider(utils, pcs, ufm, tabUrlFactory);
        final Map<String, Object> actualContext = testing.getContextMap(arguments);

        final MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder("components", expectedComponents,
                "errors", Sets.<Object>newLinkedHashSet(Arrays.asList(error1, error2)),
                "totalSize", 1,
                "actualSize", 1);
        expectedContext.add("manageUrl", URL);
        expectedContext.addAll(arguments);

        assertEquals(expectedContext.toMap(), actualContext);
        control.verify();
    }
}
