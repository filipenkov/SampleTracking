package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link IssueTypesSummaryPanelContextProvider}
 *
 * @since v4.4
 */
public class TestIssueTypesSummaryPanelContextProvider
{
    private IMocksControl mockControl;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private MockProject project;
    private MockGenericValue projectGV;
    private FieldConfigScheme issueTypeScheme;
    private Collection<IssueType> issueTypes;
    private IssueType issueType;
    private I18nHelper i18nHelper;
    private Map<String, Object> testContext;
    private List<SimpleIssueType> simpleIssueTypes;
    private SimpleIssueType simpleIssueType;
    private TabUrlFactory tabUrlFactory;
    private MockComparatorFactory comparatorFactory;

    @Before
    public void setUp()
    {
        mockControl = EasyMock.createControl();

        issueTypeSchemeManager = mockControl.createMock(IssueTypeSchemeManager.class);
        issueTypeScheme = mockControl.createMock(FieldConfigScheme.class);
        projectGV = new MockGenericValue("foo");
        project = new MockProject(null, null, null, projectGV);
        issueType = mockControl.createMock(IssueType.class);
        i18nHelper = mockControl.createMock(I18nHelper.class);
        tabUrlFactory = mockControl.createMock(TabUrlFactory.class);
        issueTypes = Lists
                .newArrayList(issueType);
        testContext = MapBuilder.<String, Object>newBuilder()
                .add("a", "aValue")
                .add("b", "bValue")
                .add("c", new Object())
                .add(ContextProviderUtils.CONTEXT_PROJECT_KEY, project)
                .add(ContextProviderUtils.CONTEXT_I18N_KEY, i18nHelper)
                .toMap();
        comparatorFactory = new MockComparatorFactory();
    }

    @After
    public void tearDown()
    {
        mockControl = null;
        issueTypeSchemeManager = null;
        issueTypeScheme = null;
        projectGV = null;
        project = null;
        issueType = null;
        i18nHelper = null;
        issueTypes = null;
        testContext = null;
        comparatorFactory = null;
    }

    @Test
    public void testGetContextMap() throws Exception
    {
        expect(issueTypeSchemeManager.getConfigScheme(eq(project))).andReturn(issueTypeScheme);
        expect(issueTypeSchemeManager.getIssueTypesForProject(eq(project))).andReturn(issueTypes);
        expect(issueTypeSchemeManager.getDefaultValue(eq(projectGV))).andReturn(null);
        expect(tabUrlFactory.forIssueTypes()).andReturn("");

        expect(issueType.getId()).andReturn("1818");
        expect(issueType.getNameTranslation()).andReturn("issueType");
        expect(issueType.getDescTranslation()).andReturn(null);
        expect(issueType.getIconUrl()).andReturn("url");
        expect(issueType.isSubTask()).andReturn(false);

        simpleIssueType =
                new SimpleIssueTypeImpl("1818", "issueType", "", "url", false, false);
        simpleIssueTypes = Lists
                .newArrayList(simpleIssueType);

        mockControl.replay();

        final IssueTypesSummaryPanelContextProvider issueTypesSummaryPanelContextProvider =
                new IssueTypesSummaryPanelContextProvider(issueTypeSchemeManager, tabUrlFactory, comparatorFactory);
        final Map<String, Object> contextMap = issueTypesSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.newBuilder(testContext)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPES_KEY, simpleIssueTypes)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPE_SCHEME_KEY, issueTypeScheme)
                .add(IssueTypesSummaryPanelContextProvider.MANAGE_URL, "")
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ERRORS_KEY, Collections.EMPTY_LIST)
                .toMap();

        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }
    @Test
    public void testGetContextMapWithMultipleIssueTypes() throws Exception
    {
        IssueType issueType = new MockIssueType("1818", "issueType", false);
        IssueType issueType2 = new MockIssueType("2222", "subTask z", true);
        IssueType issueType3 = new MockIssueType("3333", "SubTask A", true);
        IssueType issueType4 = new MockIssueType("4444", "an Issue Type", false);
        IssueType issueType5 = new MockIssueType("5555", "another Issue Type", false);

        expect(issueTypeSchemeManager.getConfigScheme(eq(project))).andReturn(issueTypeScheme);
        expect(issueTypeSchemeManager.getIssueTypesForProject(eq(project))).andReturn(Lists.<IssueType>newArrayList(issueType, issueType2, issueType3, issueType4, issueType5));
        expect(issueTypeSchemeManager.getDefaultValue(eq(projectGV))).andReturn(issueType4);
        expect(tabUrlFactory.forIssueTypes()).andReturn("");


        simpleIssueTypes = Lists.<SimpleIssueType>newArrayList(new SimpleIssueTypeImpl(issueType4, true), new SimpleIssueTypeImpl(issueType5, false), new SimpleIssueTypeImpl(issueType, false), new SimpleIssueTypeImpl(issueType3, false), new SimpleIssueTypeImpl(issueType2, false));

        mockControl.replay();

        final IssueTypesSummaryPanelContextProvider issueTypesSummaryPanelContextProvider =
                new IssueTypesSummaryPanelContextProvider(issueTypeSchemeManager, tabUrlFactory, comparatorFactory);
        final Map<String, Object> contextMap = issueTypesSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.newBuilder(testContext)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPES_KEY, simpleIssueTypes)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPE_SCHEME_KEY, issueTypeScheme)
                .add(IssueTypesSummaryPanelContextProvider.MANAGE_URL, "")
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ERRORS_KEY, Collections.EMPTY_LIST)
                .toMap();

        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWithDefaultIssueType() throws Exception
    {
        expect(issueTypeSchemeManager.getConfigScheme(eq(project))).andReturn(issueTypeScheme);
        expect(issueTypeSchemeManager.getIssueTypesForProject(eq(project))).andReturn(issueTypes);
        expect(issueTypeSchemeManager.getDefaultValue(eq(projectGV))).andReturn(issueType);
        expect(tabUrlFactory.forIssueTypes()).andReturn("");

        expect(issueType.getId()).andReturn("1818");
        expect(issueType.getNameTranslation()).andReturn("issueType");
        expect(issueType.getDescTranslation()).andReturn(null);
        expect(issueType.getIconUrl()).andReturn("url");
        expect(issueType.isSubTask()).andReturn(false);

        simpleIssueType =
                new SimpleIssueTypeImpl("1818", "issueType", "", "url", false, true);
        simpleIssueTypes = Lists
                .newArrayList(simpleIssueType);

        mockControl.replay();

        final IssueTypesSummaryPanelContextProvider issueTypesSummaryPanelContextProvider =
                new IssueTypesSummaryPanelContextProvider(issueTypeSchemeManager, tabUrlFactory, comparatorFactory);
        final Map<String, Object> contextMap = issueTypesSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.newBuilder(testContext)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPES_KEY, simpleIssueTypes)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPE_SCHEME_KEY, issueTypeScheme)
                .add(IssueTypesSummaryPanelContextProvider.MANAGE_URL, "")
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ERRORS_KEY, Collections.EMPTY_LIST)
                .toMap();

        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWithInvalidIssueTypeScheme() throws Exception
    {
        expect(issueTypeSchemeManager.getConfigScheme(eq(project))).andReturn(null);
        expect(issueTypeSchemeManager.getIssueTypesForProject(eq(project))).andReturn(issueTypes);
        expect(issueTypeSchemeManager.getDefaultValue(eq(projectGV))).andReturn(null);
        expect(i18nHelper.getText(IssueTypesSummaryPanelContextProvider.ISSUE_TYPE_SCHEME_ERROR_I18N_KEY))
                .andReturn("Unable to find an issue type scheme for your project.");
        expect(tabUrlFactory.forIssueTypes()).andReturn("");

        expect(issueType.getId()).andReturn("1818");
        expect(issueType.getNameTranslation()).andReturn("issueType");
        expect(issueType.getDescTranslation()).andReturn(null);
        expect(issueType.getIconUrl()).andReturn("url");
        expect(issueType.isSubTask()).andReturn(false);

        simpleIssueType =
                new SimpleIssueTypeImpl("1818", "issueType", "", "url", false, false);
        simpleIssueTypes = Lists
                .newArrayList(simpleIssueType);

        mockControl.replay();

        final IssueTypesSummaryPanelContextProvider issueTypesSummaryPanelContextProvider = new IssueTypesSummaryPanelContextProvider(issueTypeSchemeManager, tabUrlFactory, comparatorFactory);
        final Map<String, Object> contextMap = issueTypesSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedContextMap = MapBuilder.newBuilder(testContext)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPES_KEY, simpleIssueTypes)
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ISSUE_TYPE_SCHEME_KEY, null)
                .add(IssueTypesSummaryPanelContextProvider.MANAGE_URL, "")
                .add(IssueTypesSummaryPanelContextProvider.CONTEXT_ERRORS_KEY, Lists.<Object>newArrayList("Unable to find an issue type scheme for your project."))
                .toMap();

        assertEquals(contextMap, expectedContextMap);

        mockControl.verify();
    }
}
