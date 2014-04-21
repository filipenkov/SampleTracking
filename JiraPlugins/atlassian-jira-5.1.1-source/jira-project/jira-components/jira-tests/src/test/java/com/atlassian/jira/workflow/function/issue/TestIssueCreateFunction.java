/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.workflow.MockWorkflowEntry;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TestIssueCreateFunction extends AbstractUsersTestCase
{
    private GenericValue project;
    private IssueCreateFunction icf;
    private GenericValue version;
    private GenericValue fixVersion;
    private GenericValue component;
    private Map transientVars;
    private GenericValue retrievedIssue;
    private GenericValue issueType;
    private GenericValue priority;
    private User assignee;
    private User reporter;
    private MutableIssue issueObject;

    public TestIssueCreateFunction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
        icf = new IssueCreateFunction();
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "test", "counter", new Long(0)));
        version = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(2)));
        fixVersion = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(3)));
        component = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(4),"project","1"));
        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1"));
        priority = UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "1"));

        assignee = new MockUser("Assignee User");
        reporter = new MockUser("Reporter User");

        issueObject = IssueImpl.getIssueObject(null);
        issueObject.setProject(project);
        issueObject.setIssueTypeId("1");
        issueObject.setSummary("issue summary");
        issueObject.setDescription("issue description");
        issueObject.setEnvironment("operating system");
        issueObject.setPriorityId("1");
        issueObject.setAssignee(assignee);
        issueObject.setReporter(reporter);
        issueObject.setOriginalEstimate(new Long(1000));
        issueObject.setEstimate(new Long(1000));

        transientVars = new HashMap();
        transientVars.put("issue", issueObject);
        GenericValue origianlIssueGV = ComponentAccessor.getIssueManager().getIssue(issueObject.getId());
        transientVars.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, IssueImpl.getIssueObject(origianlIssueGV));

        WorkflowEntry wfe = new MockWorkflowEntry(1, "test workflowName", true);
        transientVars.put("entry", wfe);

        Mock mockStep = new Mock(Step.class);
        mockStep.setStrict(true);
        mockStep.expectAndReturn("getStepId", new Integer(1));
        Mock mockStore = new Mock(WorkflowStore.class);
        mockStore.setStrict(true);
        mockStore.expectAndReturn("findCurrentSteps", P.args(new IsAnything()), EasyList.build(mockStep.proxy()));

        transientVars.put("store", mockStore.proxy());
        transientVars.put("descriptor", new WorkflowDescriptor());

    }

    private void doTestIssueCreated() throws GenericEntityException, WorkflowException
    {
        icf.execute(transientVars, null, null);

        retrievedIssue = ComponentAccessor.getIssueManager().getIssue("test-1");
        assertNotNull(retrievedIssue);
        assertEquals(assignee.getName(), retrievedIssue.get("assignee"));
        assertEquals(project.getLong("id"), retrievedIssue.get("project"));
        assertEquals(issueType.getString("id"), retrievedIssue.get("type"));
        assertEquals("issue summary", retrievedIssue.get("summary"));
        assertEquals("issue description", retrievedIssue.get("description"));
        assertEquals("operating system", retrievedIssue.get("environment"));
        assertEquals(priority.getString("id"), retrievedIssue.get("priority"));
        assertEquals(reporter.getName(), retrievedIssue.get("reporter"));
        assertEquals(new Long(0), retrievedIssue.get("votes"));
        assertEquals(new Long(1), retrievedIssue.get("workflowId"));
        assertEquals(new Long(1000), retrievedIssue.get("timeestimate"));

        Issue issue = (Issue) transientVars.get("issue");
        assertEquals("issue summary", issue.getSummary());
    }

    public void testIssueCreateNoAssociations() throws GenericEntityException, WorkflowException
    {
        doTestIssueCreated();
    }

    public void testIssueCreateWithAssociations() throws GenericEntityException, WorkflowException
    {
        VersionManager versionManager = ComponentAccessor.getVersionManager();
        issueObject.setAffectedVersions(EasyList.build(versionManager.getVersion(new Long(2))));

        issueObject.setFixVersions(EasyList.build(versionManager.getVersion(new Long(3))));

        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        issueObject.setComponents(EasyList.build(projectManager.getComponent(new Long(4))));

        doTestIssueCreated();

        assertNotNull(CoreFactory.getAssociationManager().getAssociation(retrievedIssue, version, IssueRelationConstants.VERSION));
        assertNotNull(CoreFactory.getAssociationManager().getAssociation(retrievedIssue, fixVersion, IssueRelationConstants.FIX_VERSION));
        assertNotNull(CoreFactory.getAssociationManager().getAssociation(retrievedIssue, component, IssueRelationConstants.COMPONENT));
    }

    public void testIssueCreationWithCreateDate() throws GenericEntityException, WorkflowException
    {
        Timestamp created = new Timestamp(1000L);
        issueObject.setCreated(created);

        doTestIssueCreated();

        assertEquals(created, retrievedIssue.get("created"));
    }

    public void testIssueCreationWithUpdateDate() throws GenericEntityException, WorkflowException
    {
        Timestamp updated = new Timestamp(1000L);
        issueObject.setUpdated(updated);

        doTestIssueCreated();

        assertEquals(updated, retrievedIssue.get("updated"));
    }
}

class WorkflowDescriptor extends com.opensymphony.workflow.loader.WorkflowDescriptor
{
    public StepDescriptor getStep(int i)
    {
        return DescriptorFactory.getFactory().createStepDescriptor();
    }
}
