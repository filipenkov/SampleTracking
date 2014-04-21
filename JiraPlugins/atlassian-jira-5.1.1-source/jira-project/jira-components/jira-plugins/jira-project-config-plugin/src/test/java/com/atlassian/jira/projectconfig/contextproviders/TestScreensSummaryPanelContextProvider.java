package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.mock.issue.fields.screen.issuetype.MockIssueTypeScreenScheme;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.projectconfig.contextproviders.ScreensSummaryPanelContextProvider.SimpleIssueTypeScreenScheme;
import com.atlassian.jira.projectconfig.contextproviders.ScreensSummaryPanelContextProvider.SimpleScreenScheme;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link ScreensSummaryPanelContextProvider}
 *
 * @since v4.4
 */
public class TestScreensSummaryPanelContextProvider
{
    private static final String SCREEN_SCHEME_NAME = "Screen Scheme Name";
    private static final String SCREEN_SCHEME_DESCRIPTION = "Screen Scheme Description";
    private static final String OTHER_SCREEN_SCHEME_NAME = "zScreen Scheme Name";
    private static final String OTHER_SCREEN_SCHEME_DESCRIPTION = "zScreen Scheme Description";

    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private IMocksControl control;
    private GenericValue projectGV;
    private IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity;
    private IssueTypeScreenSchemeEntity otherIssueTypeScreenSchemeEntity;
    private FieldScreenScheme fieldScreenScheme;
    private FieldScreenScheme otherFieldScreenScheme;
    private ContextProviderUtils contextProviderUtils;
    private TabUrlFactory tabUrlFactory;
    private MockComparatorFactory comparatorFactory;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        issueTypeScreenSchemeManager = control.createMock(IssueTypeScreenSchemeManager.class);
        projectGV = control.createMock(GenericValue.class);
        contextProviderUtils = control.createMock(ContextProviderUtils.class);

        issueTypeScreenSchemeEntity = control.createMock(IssueTypeScreenSchemeEntity.class);
        fieldScreenScheme = control.createMock(FieldScreenScheme.class);

        otherIssueTypeScreenSchemeEntity = control.createMock(IssueTypeScreenSchemeEntity.class);
        otherFieldScreenScheme = control.createMock(FieldScreenScheme.class);

        tabUrlFactory = control.createMock(TabUrlFactory.class);

        comparatorFactory = new MockComparatorFactory();
    }

    @After
    public void tearDown() throws Exception
    {
        control = null;
        issueTypeScreenSchemeManager = null;
        projectGV = null;
        contextProviderUtils = null;

        issueTypeScreenSchemeEntity = null;
        fieldScreenScheme = null;

        otherIssueTypeScreenSchemeEntity = null;
        otherFieldScreenScheme = null;

        tabUrlFactory = null;
        comparatorFactory = null;
    }

    @Test
    public void testGetContextMap() throws Exception
    {

        final MockProject mockProject = new MockProject(888L, null, null, projectGV);
        mockProject.setIssueTypes("type a", "type b");

        final Map<String,Object> testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, mockProject
        );

        final MockIssueTypeScreenScheme mockIssueTypeScreenScheme = new MockIssueTypeScreenScheme(5858L, "screen scheme", "screen scheme description")
                .setEntities(MapBuilder.build(
                        null, issueTypeScreenSchemeEntity,
                        "type a", issueTypeScreenSchemeEntity,
                        "type b", otherIssueTypeScreenSchemeEntity
                    )
                );
        expect(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(eq(projectGV))).andReturn(mockIssueTypeScreenScheme);

        expect(issueTypeScreenSchemeEntity.getFieldScreenScheme()).andReturn(fieldScreenScheme).atLeastOnce();
        expect(otherIssueTypeScreenSchemeEntity.getFieldScreenScheme()).andReturn(otherFieldScreenScheme).atLeastOnce();


        expect(fieldScreenScheme.getId()).andReturn(4848L).atLeastOnce();
        expect(fieldScreenScheme.getName()).andReturn(SCREEN_SCHEME_NAME);
        expect(fieldScreenScheme.getDescription()).andReturn(SCREEN_SCHEME_DESCRIPTION);

        expect(otherFieldScreenScheme.getId()).andReturn(3838L).atLeastOnce();
        expect(otherFieldScreenScheme.getName()).andReturn(OTHER_SCREEN_SCHEME_NAME);
        expect(otherFieldScreenScheme.getDescription()).andReturn(OTHER_SCREEN_SCHEME_DESCRIPTION);

        control.replay();

        final ScreensSummaryPanelContextProvider screensSummaryPanelContextProvider =
                getScreensSummarypanelContextProviderUnderTest(issueTypeScreenSchemeManager, contextProviderUtils);

        final Map<String, Object> contextMap = screensSummaryPanelContextProvider.getContextMap(testContext);

        final List<SimpleScreenScheme> expectedScreenSchemes =
                Lists.newArrayList(
                        new SimpleScreenScheme(4848L, SCREEN_SCHEME_NAME, SCREEN_SCHEME_DESCRIPTION, getScreenUrl(4848L), true),
                        new SimpleScreenScheme(3838L, OTHER_SCREEN_SCHEME_NAME, OTHER_SCREEN_SCHEME_DESCRIPTION, getScreenUrl(3838L), false)
                );

        final SimpleIssueTypeScreenScheme expectedSimpleIssueTypeScreenScheme =
                new SimpleIssueTypeScreenScheme("screen scheme", "screen scheme description", getChangeUrl(888L), getEditUrl());

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(ScreensSummaryPanelContextProvider.CONTEXT_ISSUE_TYPE_SCREEN_SCHEME_KEY, expectedSimpleIssueTypeScreenScheme)
                .add(ScreensSummaryPanelContextProvider.CONTEXT_SCREEN_SCHEMES_KEY, expectedScreenSchemes)
                .toMap();

        assertEquals(contextMap, expectedContextMap);

        control.verify();

    }

    @Test
    public void testGetContextMapWithNoDefaultScreenSchemes() throws Exception
    {

        final MockProject mockProject = new MockProject(888L, null, null, projectGV);
        mockProject.setIssueTypes("type a", "type b");

        final Map<String,Object> testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, mockProject
        );

        final MockIssueTypeScreenScheme mockIssueTypeScreenScheme = new MockIssueTypeScreenScheme(5858L, "screen scheme", null)
                .setEntities(MapBuilder.build(
                        null, otherIssueTypeScreenSchemeEntity,
                        "type a", issueTypeScreenSchemeEntity,
                        "type b", issueTypeScreenSchemeEntity
                    )
                );
        expect(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(eq(projectGV))).andReturn(mockIssueTypeScreenScheme);

        expect(issueTypeScreenSchemeEntity.getFieldScreenScheme()).andReturn(fieldScreenScheme).atLeastOnce();
        expect(otherIssueTypeScreenSchemeEntity.getFieldScreenScheme()).andReturn(otherFieldScreenScheme).atLeastOnce();

        expect(fieldScreenScheme.getId()).andReturn(4848L).anyTimes();
        expect(fieldScreenScheme.getName()).andReturn(SCREEN_SCHEME_NAME).anyTimes();
        expect(fieldScreenScheme.getDescription()).andReturn(SCREEN_SCHEME_DESCRIPTION).anyTimes();

        expect(otherFieldScreenScheme.getId()).andReturn(3838L).anyTimes();
        expect(otherFieldScreenScheme.getName()).andReturn(OTHER_SCREEN_SCHEME_NAME).anyTimes();
        expect(otherFieldScreenScheme.getDescription()).andReturn(OTHER_SCREEN_SCHEME_DESCRIPTION).anyTimes();

        control.replay();

        final ScreensSummaryPanelContextProvider screensSummaryPanelContextProvider =
                getScreensSummarypanelContextProviderUnderTest(issueTypeScreenSchemeManager, contextProviderUtils);

        final Map<String, Object> contextMap = screensSummaryPanelContextProvider.getContextMap(testContext);

        final List<SimpleScreenScheme> expectedScreenSchemes =
                Lists.newArrayList(
                        new SimpleScreenScheme(4848L, SCREEN_SCHEME_NAME, SCREEN_SCHEME_DESCRIPTION, getScreenUrl(4848L), false)
                );

        final SimpleIssueTypeScreenScheme expectedSimpleIssueTypeScreenScheme =
                new SimpleIssueTypeScreenScheme("screen scheme", null, getChangeUrl(888L), getEditUrl());

        final Map<String, Object> expectedContextMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(ScreensSummaryPanelContextProvider.CONTEXT_ISSUE_TYPE_SCREEN_SCHEME_KEY, expectedSimpleIssueTypeScreenScheme)
                .add(ScreensSummaryPanelContextProvider.CONTEXT_SCREEN_SCHEMES_KEY, expectedScreenSchemes)
                .toMap();

        assertEquals(contextMap, expectedContextMap);

        control.verify();
    }

    @Test
    public void testScreenSchemeUrl()
    {
        final long screenSchemeId = 4L;
        expect(contextProviderUtils.createUrlBuilder("/secure/admin/ConfigureFieldScreenScheme.jspa"))
                .andReturn(new UrlBuilder("screenScheme", "UTF-8", false));

        control.replay();

        ScreensSummaryPanelContextProvider provider = new ScreensSummaryPanelContextProvider(issueTypeScreenSchemeManager,
                contextProviderUtils, tabUrlFactory, comparatorFactory);
        assertEquals("screenScheme?id=" + screenSchemeId, provider.getScreenSchemeUrl(screenSchemeId));

        control.verify();
    }

    @Test
    public void testGetChangeSchemeUrl()
    {
        final long projectId = 5L;
        expect(contextProviderUtils.createUrlBuilder("/secure/admin/SelectIssueTypeScreenScheme!default.jspa"))
                .andReturn(new UrlBuilder("screenScheme", "UTF-8", false));

        control.replay();

        ScreensSummaryPanelContextProvider provider =
                new ScreensSummaryPanelContextProvider(issueTypeScreenSchemeManager, contextProviderUtils, tabUrlFactory, comparatorFactory);
        assertEquals("screenScheme?projectId=" + projectId, provider.getChangeSchemeUrl(projectId));

        control.verify();
    }

    @Test
    public void testGetEditSchemeUrl()
    {
        expect(tabUrlFactory.forScreens()).andStubReturn("screens");

        control.replay();

        ScreensSummaryPanelContextProvider provider =
                new ScreensSummaryPanelContextProvider(issueTypeScreenSchemeManager, contextProviderUtils, tabUrlFactory, comparatorFactory);
        assertEquals("screens", provider.getEditSchemeUrl());

        control.verify();
    }

    private ScreensSummaryPanelContextProvider getScreensSummarypanelContextProviderUnderTest(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, ContextProviderUtils contextProviderUtils)
    {
        return new ScreensSummaryPanelContextProvider(issueTypeScreenSchemeManager, contextProviderUtils, tabUrlFactory, comparatorFactory){
            @Override
            String getChangeSchemeUrl(final Long id)
            {
                return getChangeUrl(id);
            }

            @Override
            String getEditSchemeUrl()
            {
                return getEditUrl();
            }

            @Override
            String getScreenSchemeUrl(final Long id)
            {
                return getScreenUrl(id);
            }
        };
    }

    private static String getScreenUrl(Long id)
    {
        return "screenSchemeUrl" + id;
    }

    private static String getEditUrl()
    {
        return "editUrl";
    }

    private static String getChangeUrl(Long id)
    {
        return "changeUrl" + id;
    }

}
