/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.mock.workflow.MockWorkflowContext;
import com.atlassian.jira.mock.workflow.MockWorkflowEntry;

import java.util.*;

public class TestGenerateChangeHistoryFunction extends AbstractUsersTestCase
{
    private Map transientVars;

    public TestGenerateChangeHistoryFunction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        createMockUser("Test User");

        WorkflowContext wfc = new MockWorkflowContext("Test User");
        WorkflowEntry wfe = new MockWorkflowEntry(100, "test workflowName", true);
        transientVars = EasyMap.build("entry", wfe, "context", wfc);
    }

    public void testGenerateChangeHistoryNoChange() throws GenericEntityException
    {
        MockIssue issue = new MockIssue();
        issue.setModifiedFields(Collections.EMPTY_MAP);
        transientVars.put("issue", issue);
        GenericValue origianlIssueGV = ComponentAccessor.getIssueManager().getIssue(issue.getId());
        transientVars.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, IssueImpl.getIssueObject(origianlIssueGV));

        GenerateChangeHistoryFunction gchf = new GenerateChangeHistoryFunction();
        gchf.execute(transientVars, null, null);

        assertTrue(CoreFactory.getGenericDelegator().findAll("ChangeGroup").isEmpty());
        assertTrue(CoreFactory.getGenericDelegator().findAll("ChangeItem").isEmpty());
    }

    public void testGenerateChangeHistory() throws GenericEntityException
    {
        Project project = new MockProject(EntityUtils.createValue("Project", EasyMap.build("id", new Long(1), "name", "Test Project")));
        GenericValue versionGV2 = EntityUtils.createValue("Version", EasyMap.build("id", new Long(2), "project", "1", "name", "new fix version"));

        ChangeItemBean cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Status", new Long(1).toString(), "old status", new Long(2).toString(), "new status");
        List changeitembeans = new ArrayList();
        changeitembeans.add(cib);

        GenericValue issueGV = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "status", "1", "project", project.getId(), "workflowId", new Long(100)));


        Mock issueMock = new Mock(MutableIssue.class);
        issueMock.setStrict(true);
        issueMock.expectAndReturn("getGenericValue", issueGV);
        issueMock.expectVoid("setUpdated", P.args(new IsAnything()));
        issueMock.expectVoid("store");
        issueMock.expectVoid("resetModifiedFields");
        issueMock.expectAndReturn("getProjectObject", project);
        issueMock.expectAndReturn("getIssueTypeObject", new MockIssueType("bug", "Bug"));

        Map modifiedFields = new HashMap();

        MockVersion mockVersion2 = new MockVersion(versionGV2);
        ModifiedValue modifiedFixVersionsValue = new ModifiedValue(Collections.EMPTY_LIST, EasyList.build(mockVersion2));
        modifiedFields.put(IssueFieldConstants.FIX_FOR_VERSIONS, modifiedFixVersionsValue);

        issueMock.expectAndReturn("getModifiedFields", modifiedFields);

        transientVars.put("issue", issueMock.proxy());
        transientVars.put("changeItems", changeitembeans);

        GenerateChangeHistoryFunction gchf = new GenerateChangeHistoryFunction();
        gchf.execute(transientVars, null, null);

        // check a new change group is made
        GenericValue retrievedchangegroup = (GenericValue) CoreFactory.getGenericDelegator().findAll("ChangeGroup").get(0);
        assertNotNull(retrievedchangegroup);
        assertEquals(new Long(1), retrievedchangegroup.get("issue"));
        assertEquals("Test User", retrievedchangegroup.get("author"));
        assertNotNull(retrievedchangegroup.get("created"));

        assertNotNull(transientVars.get("changeGroup"));
        assertEquals(retrievedchangegroup, transientVars.get("changeGroup"));

        // and the change items are made
        List retrievedchangeitems = CoreFactory.getGenericDelegator().findAll("ChangeItem");
        assertEquals(2, retrievedchangeitems.size());

        GenericValue retrievedchangeitem1 = (GenericValue) CoreFactory.getGenericDelegator().findByAnd("ChangeItem", EasyMap.build("field", "Status")).get(0);
        GenericValue retrievedchangeitem3 = (GenericValue) CoreFactory.getGenericDelegator().findByAnd("ChangeItem", EasyMap.build("field", "Fix Version")).get(0);

        assertEquals(retrievedchangegroup.getLong("id"), retrievedchangeitem1.get("group"));
        assertEquals("1", retrievedchangeitem1.get("oldvalue"));
        assertEquals("old status", retrievedchangeitem1.get("oldstring"));
        assertEquals("2", retrievedchangeitem1.get("newvalue"));
        assertEquals("new status", retrievedchangeitem1.get("newstring"));

        assertEquals(retrievedchangegroup.getLong("id"), retrievedchangeitem3.get("group"));
        assertNull(retrievedchangeitem3.get("oldvalue"));
        assertNull(retrievedchangeitem3.get("oldstring"));
        assertEquals("2", retrievedchangeitem3.get("newvalue"));
        assertEquals("new fix version", retrievedchangeitem3.get("newstring"));
    }
}
