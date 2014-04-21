package com.atlassian.jira.projectconfig.contextproviders;


import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.projectconfig.contextproviders.WorkflowSummaryPanelContextProvider.SimpleWorkflow;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestWorkflowSummaryPanelContextProvider
{
    private IMocksControl control;
    private ContextProviderUtils utils;
    private WorkflowSchemeManager schemeManager;
    private JiraAuthenticationContext context;
    private TabUrlFactory urlFactory;
    private MockComparatorFactory comparatorFactory;
    private WorkflowManager workflowManager;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        utils = control.createMock(ContextProviderUtils.class);
        schemeManager = control.createMock(WorkflowSchemeManager.class);
        context = new MockSimpleAuthenticationContext(new MockUser("bbain"), Locale.ENGLISH,
                new NoopI18nHelper());
        urlFactory = control.createMock(TabUrlFactory.class);
        workflowManager = control.createMock(WorkflowManager.class);

        comparatorFactory = new MockComparatorFactory();
    }

    @Test
    public void testSchemeWithoutDefaultWorkflow() throws GenericEntityException
    {
        String changeLink = "test";
        MockProject project = new MockProject(678L, "ABC").setIssueTypes("one", "two", "three", "four");

        Map<String, String> workflowMap = MapBuilder.<String, String>newBuilder("one", "abc")
                .add("two", "zzzz").add("three", "ccc").add("four", null).toMap();

        expect(utils.getProject()).andReturn(project).anyTimes();
        expect(schemeManager.getWorkflowMap(project)).andReturn(workflowMap);
        JiraWorkflow workflow = getWorkflow("abc", "abc-description");
        expect(workflowManager.getWorkflow("abc")).andReturn(workflow);
        workflow = getWorkflow("zzzz", "zzzz-description");
        expect(workflowManager.getWorkflow("zzzz")).andReturn(workflow);
        workflow = getWorkflow("ccc", "ccc-description");
        expect(workflowManager.getWorkflow("ccc")).andReturn(workflow);
        workflow = getWorkflow("jira", null);
        expect(workflowManager.getWorkflow("jira")).andReturn(workflow);
        expect(schemeManager.getWorkflowScheme(project.getGenericValue())).andReturn(null);
        expect(urlFactory.forWorkflows()).andReturn(changeLink);

        control.replay();

        WorkflowSummaryPanelContextProvider provider = new WorkflowSummaryPanelContextProvider(utils, schemeManager, context, urlFactory, comparatorFactory, workflowManager)
        {
            @Override
            String getWorkflowUrl(String workflowName)
            {
                return getWorkflowLink(workflowName);
            }
        };

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String,Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("workflows", workflows("jira", "jira", "abc", "ccc", "zzzz"))
                .add("schemeLink", changeLink)
                .add("schemeName", NoopI18nHelper.makeTranslation("admin.schemes.workflows.default"));

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testSchemeWithDefaultWorkflow() throws GenericEntityException
    {
        String changeLink = "http://htfu.com";
        MockProject project = new MockProject(678L, "ABC").setIssueTypes("something");
        MockGenericValue mockGenericValue = new MockGenericValue("dontCare");
        long schemeId = 120L;
        String schemeName = "name";

        mockGenericValue.set("id", schemeId);
        mockGenericValue.set("name", schemeName);

        Map<String, String> workflowMap = MapBuilder.<String, String>newBuilder(null, "abc").toMap();

        expect(utils.getProject()).andReturn(project).anyTimes();
        expect(schemeManager.getWorkflowMap(project)).andReturn(workflowMap);
        JiraWorkflow workflow = getWorkflow("abc", "abc-description");
        expect(workflowManager.getWorkflow("abc")).andReturn(workflow);

        
        expect(schemeManager.getWorkflowScheme(project.getGenericValue())).andReturn(mockGenericValue);

        expect(urlFactory.forWorkflows()).andReturn(changeLink);

        control.replay();

        WorkflowSummaryPanelContextProvider provider = new WorkflowSummaryPanelContextProvider(utils, schemeManager, context, urlFactory, comparatorFactory, workflowManager)
        {
            @Override
            String getWorkflowUrl(String workflowName)
            {
                return getWorkflowLink(workflowName);
            }
        };

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String,Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("workflows", workflows("abc", "abc"))
                .add("schemeName", schemeName)
                .add("schemeDescription", null)
                .add("schemeLink", changeLink);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    /**
     * This test hits the case where every issue type is assigned to a workflow and the default workflow is not used.
     * In this case we should not see the default in the list of workflows.
     *
     * @throws org.ofbiz.core.entity.GenericEntityException its a test, who cares.
     */
    @Test
    public void testSchemeAllIssueTypesAssigned() throws GenericEntityException
    {
        MockProject project = new MockProject(678L, "ABC").setIssueTypes("one");
        MockGenericValue mockGenericValue = new MockGenericValue("dontCare");
        long schemeId = 120L;
        String schemeName = "name";
        String schemeDescription = "description";
        String changeLink = "http://htfu.com";

        mockGenericValue.set("id", schemeId);
        mockGenericValue.set("name", schemeName);
        mockGenericValue.set("description", schemeDescription);

        Map<String, String> workflowMap = MapBuilder.<String, String>newBuilder(null, "default", "one", "one").toMap();

        expect(utils.getProject()).andReturn(project).anyTimes();

        expect(schemeManager.getWorkflowMap(project)).andReturn(workflowMap);
        JiraWorkflow workflow = getWorkflow("one", "one-description");
        expect(workflowManager.getWorkflow("one")).andReturn(workflow);

        expect(schemeManager.getWorkflowScheme(project.getGenericValue())).andReturn(mockGenericValue);

        expect(urlFactory.forWorkflows()).andReturn(changeLink);

        control.replay();

        WorkflowSummaryPanelContextProvider provider = new WorkflowSummaryPanelContextProvider(utils, schemeManager, context, urlFactory, comparatorFactory, workflowManager)
        {
            @Override
            String getWorkflowUrl(String workflowName)
            {
                return getWorkflowLink(workflowName);
            }
        };

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String,Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("workflows", workflows("abc", "one"))
                .add("schemeName", schemeName)
                .add("schemeDescription", schemeDescription)
                .add("schemeLink", changeLink);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testSchemeReadError() throws GenericEntityException
    {
        MockProject project = new MockProject(678L, "ABC").setIssueTypes("something");

        Map<String, String> workflowMap = MapBuilder.<String, String>newBuilder(null, "abc").toMap();
        expect(utils.getProject()).andReturn(project).anyTimes();
        expect(schemeManager.getWorkflowMap(project)).andReturn(workflowMap);
        JiraWorkflow workflow = getWorkflow("abc", "abc-description");
        expect(workflowManager.getWorkflow("abc")).andReturn(workflow);

        expect(schemeManager.getWorkflowScheme(project.getGenericValue())).andThrow(new GenericEntityException());

        control.replay();

        WorkflowSummaryPanelContextProvider provider = new WorkflowSummaryPanelContextProvider(utils, schemeManager, context, urlFactory, comparatorFactory, workflowManager)
        {
            @Override
            String getWorkflowUrl(String workflowName)
            {
                return getWorkflowLink(workflowName);
            }
        };

        Map<String, Object> paramMap = MapBuilder.<String, Object>build("param", true);
        Map<String,Object> actualMap = provider.getContextMap(paramMap);

        MapBuilder<String, Object> expectedMap = MapBuilder.newBuilder(paramMap)
                .add("workflows", workflows("abc", "abc"))
                .add("error", true);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testWorkflowUrl()
    {
        String workflow = "name";
        expect(utils.createUrlBuilder("/secure/admin/workflows/WorkflowDesigner.jspa?workflowMode=live"))
                .andReturn(new UrlBuilder("workflow", "UTF-8", false));

        control.replay();

        WorkflowSummaryPanelContextProvider provider = new WorkflowSummaryPanelContextProvider(utils, schemeManager, context, urlFactory, comparatorFactory, workflowManager);
        assertEquals("workflow?wfName=" + workflow, provider.getWorkflowUrl(workflow));

        control.verify();
    }

    private JiraWorkflow getWorkflow(String name, String description)
    {
        final JiraWorkflow mockWorkflow = control.createMock(JiraWorkflow.class);

        expect(mockWorkflow.getName()).andReturn(name);
        expect(mockWorkflow.getDescription()).andReturn(description);

        return mockWorkflow;
    }

    private List<SimpleWorkflow> workflows(String defaultName, String...names)
    {
        List<SimpleWorkflow> flows = new ArrayList<SimpleWorkflow>();
        for (String name : names)
        {
            flows.add(new SimpleWorkflow(name, getWorkflowLink(name), name.equals("jira") ? null : name + "-description", defaultName.equals(name)));
        }
        return flows;
    }

    private static String getWorkflowLink(String workflowName)
    {
        return "workflow?" + workflowName;
    }

    private static String getChangeLink(Project project)
    {
        return "changescheme?" + project.getKey();
    }

    private static String getEditLink(Long id)
    {
        return "changeworkflow?" + id;
    }
}
