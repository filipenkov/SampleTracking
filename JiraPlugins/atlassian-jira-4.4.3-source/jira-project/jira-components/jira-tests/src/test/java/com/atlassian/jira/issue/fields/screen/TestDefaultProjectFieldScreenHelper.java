package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.jira.workflow.MockWorkflowManager;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestDefaultProjectFieldScreenHelper extends MockControllerTestCase
{

    @Mock private ProjectWorkflowSchemeHelper projectWorkflowSchemeHelper;
    @Mock private ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper;
    @Mock private FieldScreenSchemeManager fieldScreenSchemeManager;

    private MockWorkflowManager workflowManager;

    @Before
    public void setUp()
    {
        workflowManager = new MockWorkflowManager();
    }

    @After
    public void tearDown()
    {
        workflowManager = null;
    }

    @Test
    public void testGetProjects()
    {
        final MockProject project1 = new MockProject(9090L, "mtan");
        final MockProject project2 = new MockProject(8080L, "mtan2");

        // Set up workflows
        final ActionDescriptor actionDescriptor = mockController.createMock(ActionDescriptor.class);
        final FieldScreen fieldScreen = mockController.createMock(FieldScreen.class);

        final MockJiraWorkflow workflow1 = new MockJiraWorkflow().addAction(actionDescriptor);
        workflow1.setName("wf1");

        final MockJiraWorkflow workflow2 = new MockJiraWorkflow();
        workflow2.setName("wf2");

        workflowManager.addActiveWorkflows(workflow1)
            .addActiveWorkflows(workflow2);


        final Map<String, Collection<Project>> backingWfMap = Maps.newHashMap();
        final SetMultimap<String, Project> wfMultimap = Multimaps.newSetMultimap(backingWfMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        wfMultimap.put("wf1", project1);

        expect(projectWorkflowSchemeHelper.getProjectsForWorkflow(eq(Sets.<String>newHashSet("wf1"))))
                .andStubReturn(wfMultimap);

        // Set up field screen schemes
        final MockFieldScreenScheme mockFieldScreenScheme = mockController.createMock(MockFieldScreenScheme.class);
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        fssMultimap.putAll(mockFieldScreenScheme, Arrays.asList(project1, project2));

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        expect(fieldScreenSchemeManager.getFieldScreenSchemes(eq(fieldScreen))).andStubReturn(fieldScreenSchemes);
        expect(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(eq(fieldScreenSchemes)))
                .andStubReturn(fssMultimap);


        mockController.replay();

        final ProjectFieldScreenHelper helper = createHelper(
            MapBuilder.<ActionDescriptor, FieldScreen>newBuilder()
                .add(actionDescriptor, fieldScreen)
                .toMap()
        );

        final List<Project> projectsForFieldScreen = helper.getProjectsForFieldScreen(fieldScreen);

        assertEquals(Arrays.<Project>asList(project1, project2), projectsForFieldScreen);

        mockController.verify();
    }

    @Test
    public void testGetProjectsWithNonePresent()
    {
        final ActionDescriptor actionDescriptor = mockController.createMock(ActionDescriptor.class);
        final FieldScreen fieldScreen = mockController.createMock(FieldScreen.class);

        final Map<String, Collection<Project>> backingWfMap = Maps.newHashMap();
        final SetMultimap<String, Project> wfMultimap = Multimaps.newSetMultimap(backingWfMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        expect(projectWorkflowSchemeHelper.getProjectsForWorkflow(eq(Collections.<String>emptySet())))
                .andStubReturn(wfMultimap);

        final MockFieldScreenScheme mockFieldScreenScheme = mockController.createMock(MockFieldScreenScheme.class);
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        expect(fieldScreenSchemeManager.getFieldScreenSchemes(eq(fieldScreen))).andStubReturn(fieldScreenSchemes);
        expect(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(eq(fieldScreenSchemes)))
                .andStubReturn(fssMultimap);


        mockController.replay();

        final ProjectFieldScreenHelper helper = createHelper(
            MapBuilder.<ActionDescriptor, FieldScreen>newBuilder()
                .add(actionDescriptor, fieldScreen)
                .toMap()
        );

        final List<Project> projectsForFieldScreen = helper.getProjectsForFieldScreen(fieldScreen);

        assertEquals(Collections.<Project>emptyList(), projectsForFieldScreen);

        mockController.verify();
    }

    private ProjectFieldScreenHelper createHelper(final Map<ActionDescriptor, FieldScreen> workflowDescriptorScreens)
    {
        return new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper, workflowManager,
                fieldScreenSchemeManager)
        {
            @Override
            WorkflowActionsBean getActionsBean()
            {
                return new WorkflowActionsBean()
                {
                    @Override
                    public FieldScreen getFieldScreenForView(ActionDescriptor actionDescriptor)
                    {
                        return workflowDescriptorScreens.get(actionDescriptor);
                    }
                };
            }
        };
    }
}
