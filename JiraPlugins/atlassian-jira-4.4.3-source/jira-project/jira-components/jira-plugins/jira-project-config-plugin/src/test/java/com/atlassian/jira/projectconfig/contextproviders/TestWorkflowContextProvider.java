package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.MockFieldScreen;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.beans.SimpleProject;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.projectconfig.order.NativeComparator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestWorkflowContextProvider
{
    private IMocksControl control;
    private WorkflowSchemeManager workflowSchemeManager;
    private ContextProviderUtils providerUtils;
    private WorkflowManager workflowManager;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private ProjectWorkflowSchemeHelper helper;
    private ComparatorFactory comparatorFactory;

    @Before
    public void setUp() throws Exception
    {
        control = EasyMock.createControl();
        workflowSchemeManager = control.createMock(WorkflowSchemeManager.class);
        providerUtils = control.createMock(ContextProviderUtils.class);
        workflowManager = control.createMock(WorkflowManager.class);
        issueTypeSchemeManager = control.createMock(IssueTypeSchemeManager.class);
        helper = control.createMock(ProjectWorkflowSchemeHelper.class);
        comparatorFactory = new MockComparatorFactory();
    }

    @Test
    public void testGetContextMap() throws Exception
    {
        Map<String, Object> argumentContext = MapBuilder.<String, Object>build("argument", true);
        Map<String, Object> defaultContext = MapBuilder.<String, Object>build("default", true);
        final MockProject expectedProject = new MockProject(588L, "Something");

        WorkflowContextProvider provider = new WorkflowContextProvider(workflowSchemeManager, providerUtils,
                workflowManager, helper, issueTypeSchemeManager, comparatorFactory)
        {
            @Override
            MapBuilder<String, Object> addSchemeData(MapBuilder<String, Object> builder, Project actualProject)
            {
                assertSame(expectedProject, actualProject);
                return builder.add("scheme", true);
            }

            @Override
            MapBuilder<String, Object> addWorkflowData(MapBuilder<String, Object> builder, Project actualProject)
            {
                assertSame(expectedProject, actualProject);
                return builder.add("workflow", true);
            }
        };

        EasyMock.expect(providerUtils.getProject()).andReturn(expectedProject);
        EasyMock.expect(providerUtils.getDefaultContext()).andReturn(defaultContext);

        control.replay();

        Map<String, Object> actualMap = provider.getContextMap(argumentContext);

        MapBuilder expectedMap = MapBuilder.newBuilder(argumentContext).addAll(defaultContext)
                .add("workflow", true).add("scheme", true);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testAddSchemeDataNoScheme() throws Exception
    {
        final MockProject expectedProject = new MockProject(588L, "Something");
        EasyMock.expect(workflowSchemeManager.getWorkflowScheme(expectedProject.getGenericValue())).andReturn(null);

        WorkflowContextProvider provider = createContextProvider();
        control.replay();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        provider.addSchemeData(builder, expectedProject);
        assertTrue(builder.toMap().isEmpty());

        control.verify();
    }

    @Test
    public void testAddSchemeData() throws Exception
    {
        long id = 178L;
        String description = "Some kind of description";
        String name = "Something";
        final MockProject expectedProject = new MockProject(588L, "TST");

        MockGenericValue scheme = new MockGenericValue("DontCare");
        scheme.set("id", id);
        scheme.set("name", name);
        scheme.set("description", description);

        EasyMock.expect(workflowSchemeManager.getWorkflowScheme(expectedProject.getGenericValue())).andReturn(scheme);

        WorkflowContextProvider provider = createContextProvider();
        control.replay();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        provider.addSchemeData(builder, expectedProject);

        Map<String, Object> expectedMap = MapBuilder.<String, Object>build("workflowScheme", new WorkflowContextProvider.SimpleWorkflowScheme(id, name, description));
        assertEquals(expectedMap, builder.toMap());

        control.verify();
    }

    @Test
    public void testAddWorkflowDataSimple() throws Exception
    {
        final MockIssueType type1 = new MockIssueType("bug", "BUG");
        final MockIssueType type2 = new MockIssueType("improv", "Improvement");

        final MockProject expectedProject = new MockProject(588L, "TST").setIssueTypes(type1, type2);
        final MockProject otherProject = new MockProject(589L, "BJB");

        Multimap<String, Project> projects = ArrayListMultimap.create();
        projects.put("jira", otherProject);

        final TestingJiraWorkflow workflow = new TestingJiraWorkflow().setName("jira").setDefault(true);
        registerWorkflows(workflow);

        EasyMock.expect(providerUtils.getStringComparator()).andReturn(NativeComparator.<String>getInstance());
        EasyMock.expect(workflowSchemeManager.getWorkflowMap(expectedProject)).andReturn(MapBuilder.<String, String>build(null, "jira"));
        EasyMock.expect(helper.getProjectsForWorkflow(Collections.singleton("jira"))).andReturn(projects);
        EasyMock.expect(issueTypeSchemeManager.getDefaultValue(expectedProject.getGenericValue())).andReturn(type1);
        WorkflowContextProvider provider = createAddWorkflowProvider();

        control.replay();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        provider.addWorkflowData(builder, expectedProject);

        List<SimpleIssueType> simpleTypes = getSimpleIssueTypes(type1, type1, type2);
        List<SimpleProject> simpleProjects = getSimpleProjects(otherProject);
        WorkflowContextProvider.SimpleWorkflow simpleWorkflow = getSimpleWorkflow(workflow, true);
        WorkflowContextProvider.SimpleWorkflowData data = new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow);

        assertEquals(MapBuilder.<String, Object>build("workflows", Collections.singletonList(data)), builder.toMap());

        control.verify();
    }

    @Test
    public void testAddWorkflowDataComplex() throws Exception
    {
        final MockIssueType type1 = new MockIssueType("type1", "type1");
        final MockIssueType type2 = new MockIssueType("type2", "type2");
        final MockIssueType type3 = new MockIssueType("type3", "type3");
        final MockIssueType type4 = new MockIssueType("type4", "type4");
        final MockIssueType type5 = new MockIssueType("type5", "type5");

        final MockProject expectedProject = new MockProject(588L, "TST").setIssueTypes(type1, type2, type3, type4, type5);
        final MockProject otherProject1 = new MockProject(589L, "BJB", "BJB");
        final MockProject otherProject2 = new MockProject(599L, "SCT", "SCT");

        final TestingJiraWorkflow workflow1 = new TestingJiraWorkflow().setName("jira").setDefault(true);
        final TestingJiraWorkflow workflow2 = new TestingJiraWorkflow().setName("wf1");
        final TestingJiraWorkflow workflow3 = new TestingJiraWorkflow().setName("wf2");
        registerWorkflows(workflow1, workflow2, workflow3);

        Multimap<String, Project> workflowProjectMap = ArrayListMultimap.create();
        workflowProjectMap.putAll(workflow2.getName(), Arrays.asList(otherProject1, otherProject2));
        workflowProjectMap.putAll(workflow3.getName(), Arrays.asList(otherProject2));

        //A workflow map that generates: {type1, type2, type5} -> {workflow2} and {type3, type4} -> {workflow3}
        MapBuilder<String, String> workflowMapBuilder = MapBuilder.newBuilder(null, workflow2.getName());
        workflowMapBuilder.add(type1.getId(), workflow2.getName());
        workflowMapBuilder.add(type2.getId(), workflow2.getName());
        workflowMapBuilder.add(type3.getId(), workflow3.getName());
        workflowMapBuilder.add(type4.getId(), workflow3.getName());

        EasyMock.expect(providerUtils.getStringComparator()).andReturn(NativeComparator.<String>getInstance());
        EasyMock.expect(workflowSchemeManager.getWorkflowMap(expectedProject)).andReturn(workflowMapBuilder.toLinkedHashMap());
        EasyMock.expect(helper.getProjectsForWorkflow(Sets.newHashSet(workflow2.getName(), workflow3.getName()))).andReturn(workflowProjectMap);
        EasyMock.expect(issueTypeSchemeManager.getDefaultValue(expectedProject.getGenericValue())).andReturn(type2);

        WorkflowContextProvider provider = createAddWorkflowProvider();

        control.replay();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        provider.addWorkflowData(builder, expectedProject);

        List<WorkflowContextProvider.SimpleWorkflowData> datas = new ArrayList<WorkflowContextProvider.SimpleWorkflowData>();

        List<SimpleIssueType> simpleTypes = getSimpleIssueTypes(type2, type2, type1, type5);
        List<SimpleProject> simpleProjects = getSimpleProjects(otherProject1, otherProject2);
        WorkflowContextProvider.SimpleWorkflow simpleWorkflow = getSimpleWorkflow(workflow2, true);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        simpleTypes = getSimpleIssueTypes(type2, type3, type4);
        simpleProjects = getSimpleProjects(otherProject2);
        simpleWorkflow = getSimpleWorkflow(workflow3, false);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        assertEquals(MapBuilder.<String, Object>build("workflows", datas), builder.toMap());

        control.verify();
    }

    @Test
    public void testAddWorkflowDataNoDefault() throws Exception
    {
        final MockIssueType type1 = new MockIssueType("type1", "type1");
        final MockIssueType type2 = new MockIssueType("type2", "type2");
        final MockIssueType type3 = new MockIssueType("type3", "type3");
        final MockIssueType type4 = new MockIssueType("type4", "type4");
        final MockIssueType type5 = new MockIssueType("type5", "type5");

        final MockProject expectedProject = new MockProject(588L, "TST").setIssueTypes(type1, type2, type3, type4, type5);
        final MockProject otherProject1 = new MockProject(589L, "BJB", "BJB");
        final MockProject otherProject2 = new MockProject(599L, "ABC", "ABC");

        final TestingJiraWorkflow workflow1 = new TestingJiraWorkflow().setName("jira").setDefault(true);
        final TestingJiraWorkflow workflow2 = new TestingJiraWorkflow().setName("wf1");
        final TestingJiraWorkflow workflow3 = new TestingJiraWorkflow().setName("wf2");
        registerWorkflows(workflow1, workflow2, workflow3);

        //A workflow map that generates: {type1, type2} -> {workflow2}, {type3, type4} -> {workflow3} and {type5} -> {workflow1}
        MapBuilder<String, String> workflowMapBuilder = MapBuilder.newBuilder();
        workflowMapBuilder.add(type1.getId(), workflow2.getName());
        workflowMapBuilder.add(type2.getId(), workflow2.getName());
        workflowMapBuilder.add(type3.getId(), workflow3.getName());
        workflowMapBuilder.add(type4.getId(), workflow3.getName());

        Multimap<String, Project> workflowProjectMap = ArrayListMultimap.create();
        workflowProjectMap.putAll(workflow2.getName(), Arrays.asList(otherProject1, otherProject2));
        workflowProjectMap.putAll(workflow3.getName(), Arrays.asList(otherProject2));

        EasyMock.expect(providerUtils.getStringComparator()).andReturn(NativeComparator.<String>getInstance());
        EasyMock.expect(workflowSchemeManager.getWorkflowMap(expectedProject)).andReturn(workflowMapBuilder.toLinkedHashMap());
        EasyMock.expect(helper.getProjectsForWorkflow(Sets.newHashSet(workflow1.getName(), workflow2.getName(), workflow3.getName()))).andReturn(workflowProjectMap);
        EasyMock.expect(issueTypeSchemeManager.getDefaultValue(expectedProject.getGenericValue())).andReturn(type3);

        WorkflowContextProvider provider = createAddWorkflowProvider();

        control.replay();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        provider.addWorkflowData(builder, expectedProject);

        List<WorkflowContextProvider.SimpleWorkflowData> datas = new ArrayList<WorkflowContextProvider.SimpleWorkflowData>();

        List<SimpleProject> simpleProjects = getSimpleProjects();
        List<SimpleIssueType> simpleTypes = getSimpleIssueTypes(type3, type5);
        WorkflowContextProvider.SimpleWorkflow simpleWorkflow = getSimpleWorkflow(workflow1, true);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        //Project list should be ordered.
        simpleProjects = getSimpleProjects(otherProject2, otherProject1);
        simpleTypes = getSimpleIssueTypes(type3, type1, type2);
        simpleWorkflow = getSimpleWorkflow(workflow2, false);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        simpleProjects = getSimpleProjects(otherProject2);
        simpleTypes = getSimpleIssueTypes(type3, type3, type4);
        simpleWorkflow = getSimpleWorkflow(workflow3, false);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        assertEquals(MapBuilder.<String, Object>build("workflows", datas), builder.toMap());

        control.verify();
    }

    @Test
    public void testWorkflowOrdering() throws Exception
    {
        final MockIssueType type1 = new MockIssueType("type1", "type1");
        final MockIssueType type2 = new MockIssueType("type2", "type2");
        final MockIssueType type3 = new MockIssueType("type3", "type3");
        final MockIssueType type4 = new MockIssueType("type4", "type4");
        final MockIssueType type5 = new MockIssueType("type5", "type5");

        final MockProject expectedProject = new MockProject(588L, "TST").setIssueTypes(type1, type2, type3, type4, type5);
        final MockProject otherProject1 = new MockProject(589L, "BJB", "BJB");
        final MockProject otherProject2 = new MockProject(599L, "ABC", "ABC");

        //Order of the workflow is "zzz (default)", "aaa", "ZZZ"
        final TestingJiraWorkflow workflow1 = new TestingJiraWorkflow().setName("zzz").setDefault(true);
        final TestingJiraWorkflow workflow2 = new TestingJiraWorkflow().setName("aaa");
        final TestingJiraWorkflow workflow3 = new TestingJiraWorkflow().setName("ZZZ");
        registerWorkflows(workflow1, workflow2, workflow3);

        //A workflow map that generates: {type2} -> {workflow2}, {type3, type4} -> {workflow3} and {type1, type5} -> {workflow1}
        MapBuilder<String, String> workflowMapBuilder = MapBuilder.newBuilder();
        workflowMapBuilder.add(null, workflow1.getName());
        workflowMapBuilder.add(type2.getId(), workflow2.getName());
        workflowMapBuilder.add(type3.getId(), workflow3.getName());
        workflowMapBuilder.add(type4.getId(), workflow3.getName());
        workflowMapBuilder.add(type5.getId(), workflow1.getName());

        Multimap<String, Project> workflowProjectMap = ArrayListMultimap.create();
        workflowProjectMap.putAll(workflow2.getName(), Arrays.asList(otherProject1, otherProject2));
        workflowProjectMap.putAll(workflow3.getName(), Arrays.asList(otherProject2));
        workflowProjectMap.putAll(workflow1.getName(), Arrays.asList(otherProject2));

        EasyMock.expect(providerUtils.getStringComparator()).andReturn(NativeComparator.<String>getInstance());
        EasyMock.expect(workflowSchemeManager.getWorkflowMap(expectedProject)).andReturn(workflowMapBuilder.toLinkedHashMap());
        EasyMock.expect(helper.getProjectsForWorkflow(Sets.newHashSet(workflow1.getName(), workflow2.getName(), workflow3.getName()))).andReturn(workflowProjectMap);
        EasyMock.expect(issueTypeSchemeManager.getDefaultValue(expectedProject.getGenericValue())).andReturn(type3);

        WorkflowContextProvider provider = createAddWorkflowProvider();

        control.replay();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        provider.addWorkflowData(builder, expectedProject);

        List<WorkflowContextProvider.SimpleWorkflowData> datas = new ArrayList<WorkflowContextProvider.SimpleWorkflowData>();

        //Project list should be ordered.
        List<SimpleProject> simpleProjects = getSimpleProjects(otherProject2);
        List<SimpleIssueType> simpleTypes = getSimpleIssueTypes(type3, type1, type5);
        WorkflowContextProvider.SimpleWorkflow simpleWorkflow = getSimpleWorkflow(workflow1, true);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        simpleProjects = getSimpleProjects(otherProject2);
        simpleTypes = getSimpleIssueTypes(type3, type3, type4);
        simpleWorkflow = getSimpleWorkflow(workflow3, false);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        simpleProjects = getSimpleProjects(otherProject2, otherProject1);
        simpleTypes = getSimpleIssueTypes(type3, type2);
        simpleWorkflow = getSimpleWorkflow(workflow2, false);
        datas.add(new WorkflowContextProvider.SimpleWorkflowData(simpleTypes, simpleProjects, simpleWorkflow));

        assertEquals(MapBuilder.<String, Object>build("workflows", datas), builder.toMap());

        control.verify();
    }

    @Test
    public void testCreateWorkflow() throws Exception
    {
        Document document = readDocument("TestWorkflow.xml");
        WorkflowDescriptor descriptor = createDescriptor(document);

        TestingJiraWorkflow workflow = new TestingJiraWorkflow().setName("name")
                .setDescription("description")
                .setDescriptor(descriptor);

        Status status1 = createStatusForId(String.valueOf(1));
        Status status6 = createStatusForId(String.valueOf(6));
        Status status3 = createStatusForId(String.valueOf(3));

        FieldScreen screen3 = createFieldScreenForView("3");
        FieldScreen screen1 = createFieldScreenForView("1");

        List<WorkflowContextProvider.SimpleWorkflowSource> sources = new ArrayList<WorkflowContextProvider.SimpleWorkflowSource>();

        //First Open Status.
        List<WorkflowContextProvider.SimpleWorkflowTarget> targets = new ArrayList<WorkflowContextProvider.SimpleWorkflowTarget>();
        targets.add(new WorkflowContextProvider.SimpleWorkflowTarget(status6, "Close", screen1));
        targets.add(new WorkflowContextProvider.SimpleWorkflowTarget(status1, "Loop", null));
        targets.add(new WorkflowContextProvider.SimpleWorkflowTarget(status3, "Start Progress", null));
        sources.add(new WorkflowContextProvider.SimpleWorkflowSource(status1, targets));

        //Close Status
        sources.add(new WorkflowContextProvider.SimpleWorkflowSource(status6, Collections.<WorkflowContextProvider.SimpleWorkflowTarget>emptyList()));

        //In Progress Status
        targets = new ArrayList<WorkflowContextProvider.SimpleWorkflowTarget>();
        targets.add(new WorkflowContextProvider.SimpleWorkflowTarget(status6, "Close", screen3));
        targets.add(new WorkflowContextProvider.SimpleWorkflowTarget(status3, "Log Work", null));
        sources.add(new WorkflowContextProvider.SimpleWorkflowSource(status3, targets));

        WorkflowContextProvider.SimpleWorkflow expectedWorkflow = new WorkflowContextProvider.SimpleWorkflow(descriptor.getEntityId(), workflow.getName(), workflow.getDescription(), sources, false, false);

        control.replay();

        assertEquals(expectedWorkflow, createCreateWorkflowProvider().createWorkflow(workflow, false));

        control.verify();
    }

    private WorkflowDescriptor createDescriptor(Document document) throws Exception
    {
        return DescriptorFactory.getFactory().createWorkflowDescriptor(document.getDocumentElement());
    }

    private Document readDocument(String name) throws Exception
    {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(name);
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(stream);
        }
        finally
        {
            stream.close();
        }
    }

    private WorkflowContextProvider createAddWorkflowProvider()
    {
        return new WorkflowContextProvider(workflowSchemeManager, providerUtils, workflowManager,
                helper, issueTypeSchemeManager, comparatorFactory)
        {
            @Override
            SimpleWorkflow createWorkflow(JiraWorkflow workflow, boolean defaultWf)
            {
                return getSimpleWorkflow(workflow, defaultWf);
            }
        };
    }

    private WorkflowContextProvider createCreateWorkflowProvider()
    {
        return new WorkflowContextProvider(workflowSchemeManager, providerUtils, workflowManager,
                helper, issueTypeSchemeManager, comparatorFactory)
        {
            @Override
            WorkflowActionsBean getActionsBean()
            {
                return new WorkflowActionsBean()
                {
                    @Override
                    public FieldScreen getFieldScreenForView(ActionDescriptor actionDescriptor)
                    {
                        String id = (String) actionDescriptor.getMetaAttributes().get("jira.fieldscreen.id");
                        return createFieldScreenForView(id);
                    }
                };
            }
        };
    }

    private static MockFieldScreen createFieldScreenForView(String view)
    {
        if (view == null)
        {
            return null;
        }
        else
        {
            MockFieldScreen screen = new MockFieldScreen();
            screen.setId(Long.parseLong(view));
            screen.setName(view);
            screen.setDescription(format("Screen Description: %s", view));
            return screen;
        }
    }

    private static Status createStatusForId(String statusId)
    {
        return new MockStatus(statusId, String.format("Status-%s", statusId));
    }

    private static WorkflowContextProvider.SimpleWorkflow getSimpleWorkflow(JiraWorkflow workflow, boolean defaultWf)
    {
        return new WorkflowContextProvider.SimpleWorkflow(0, workflow.getName(), workflow.getDescription(), Collections.<WorkflowContextProvider.SimpleWorkflowSource>emptyList(), defaultWf, false);
    }

    private List<SimpleIssueType> getSimpleIssueTypes(IssueType defaultIssueType, IssueType... types)
    {
        List<SimpleIssueType> simpleTypes = new ArrayList<SimpleIssueType>();
        for (IssueType type : types)
        {
            if (type.equals(defaultIssueType))
            {
                simpleTypes.add(new SimpleIssueTypeImpl(type, true));
            }
            else
            {
                simpleTypes.add(new SimpleIssueTypeImpl(type, false));
            }
        }
        return simpleTypes;
    }

    private List<SimpleProject> getSimpleProjects(Project... projects)
    {
        List<SimpleProject> simpleProjects = new ArrayList<SimpleProject>();
        for (Project project : projects)
        {
            simpleProjects.add(new SimpleProject(project));
        }
        return simpleProjects;
    }

    private WorkflowContextProvider createContextProvider()
    {
        return new WorkflowContextProvider(workflowSchemeManager, providerUtils, workflowManager,
                helper, issueTypeSchemeManager, comparatorFactory);
    }

    private void registerWorkflows(JiraWorkflow... workflows)
    {
        for (JiraWorkflow workflow : workflows)
        {
            EasyMock.expect(workflowManager.getWorkflow(workflow.getName())).andReturn(workflow).anyTimes();
        }
    }

    private static class TestingJiraWorkflow implements JiraWorkflow
    {
        private String name;
        private String description;
        private boolean defaultWorkflow;
        private WorkflowDescriptor descriptor;

        @Override
        public String getName()
        {
            return name;
        }

        public TestingJiraWorkflow setName(String name)
        {
            this.name = name;
            return this;
        }

        @Override
        public String getDescription()
        {
            return description;
        }

        public TestingJiraWorkflow setDescription(String description)
        {
            this.description = description;
            return this;
        }

        @Override
        public WorkflowDescriptor getDescriptor()
        {
            return descriptor;
        }

        public TestingJiraWorkflow setDescriptor(WorkflowDescriptor descriptor)
        {
            this.descriptor = descriptor;
            return this;
        }

        @Override
        public Collection<ActionDescriptor> getAllActions()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public Collection<ActionDescriptor> getActionsWithResult(StepDescriptor stepDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean removeStep(StepDescriptor stepDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public StepDescriptor getLinkedStep(GenericValue status)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public List<GenericValue> getLinkedStatuses()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public List<Status> getLinkedStatusObjects()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isActive() throws WorkflowException
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isSystemWorkflow() throws WorkflowException
        {
            return false;
        }

        @Override
        public boolean isEditable() throws WorkflowException
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isDefault()
        {
            return defaultWorkflow;
        }

        public TestingJiraWorkflow setDefault(boolean defaultWorkflow)
        {
            this.defaultWorkflow = defaultWorkflow;
            return this;
        }

        @Override
        public boolean isDraftWorkflow()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean hasDraftWorkflow()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public int getNextActionId()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public Collection<StepDescriptor> getStepsForTransition(ActionDescriptor action)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public Collection<FunctionDescriptor> getPostFunctionsForTransition(ActionDescriptor actionDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isInitialAction(ActionDescriptor actionDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isCommonAction(ActionDescriptor actionDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isGlobalAction(ActionDescriptor actionDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isOrdinaryAction(ActionDescriptor actionDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public GenericValue getLinkedStatus(StepDescriptor stepDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public Status getLinkedStatusObject(StepDescriptor stepDescriptor)
        {
            final String statusId = (String) stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY);
            if (statusId != null)
            {
                return createStatusForId(statusId);
            }
            else
            {
                return null;
            }
        }

        @Override
        public String getActionType(ActionDescriptor actionDescriptor)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public void reset()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public Collection<ActionDescriptor> getActionsForScreen(FieldScreen fieldScreen)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public String getUpdateAuthorName()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public Date getUpdatedDate()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public String getMode()
        {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public int compareTo(JiraWorkflow o)
        {
            throw new UnsupportedOperationException("Not Implemented");
        }
    }
}
