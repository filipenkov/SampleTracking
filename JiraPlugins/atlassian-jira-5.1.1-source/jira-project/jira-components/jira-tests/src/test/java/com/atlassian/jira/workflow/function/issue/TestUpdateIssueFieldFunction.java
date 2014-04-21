/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.WorkflowException;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.easymock.EasyMockAnnotations.initMocks;
import static com.atlassian.jira.easymock.EasyMockAnnotations.replayMocks;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.verify;

public class TestUpdateIssueFieldFunction extends AbstractUsersTestCase
{
    private UpdateIssueFieldFunction uif;
    private Map input;
    private User user;
    @com.atlassian.jira.easymock.Mock
    private MutableIssue mockIssue;
    private Mock mockFieldManager;

    public TestUpdateIssueFieldFunction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        initMocks(this);

        user = new MockUser("testuser");
        mockFieldManager = new Mock(FieldManager.class);

        uif = new MockUpdateIssueFieldFunction((FieldManager) mockFieldManager.proxy());
        mockIssue = createMock(MutableIssue.class);
        input = EasyMap.build("issue", mockIssue);
    }

    public void testUpdateIssueFieldFunctionUpdateStatus() throws GenericEntityException, WorkflowException
    {
        Map args = EasyMap.build("field.name", IssueFieldConstants.STATUS, "field.value", "2", "username", user.getName());
        mockIssue.setStatusId("2");
        expect(mockIssue.getStatusObject()).andStubReturn(new MockStatus("2", "test status"));
        replayMocks(this);
        uif.execute(input, args, null);
        verify(mockIssue);
        mockFieldManager.verify();
    }
    public void testUpdateIssueFieldFunctionUpdateTimeSpent() throws GenericEntityException, WorkflowException
    {
        Long value = new Long(20);
        Map args = EasyMap.build("field.name", IssueFieldConstants.TIME_SPENT, "field.value", value.toString(), "username", user.getName());
        Long oldValue = new Long(10);
        expect(mockIssue.getTimeSpent()).andStubReturn(oldValue);
        mockIssue.setTimeSpent(value);
        replayMocks(this);
        uif.execute(input, args, null);

        List changeItems = (List) input.get("changeItems");
        assertEquals(1, changeItems.size());

        ChangeItemBean cib = (ChangeItemBean) changeItems.iterator().next();
        assertEquals(ChangeItemBean.STATIC_FIELD, cib.getFieldType());
        assertEquals(IssueFieldConstants.TIME_SPENT, cib.getField());
        assertEquals(oldValue.toString(), cib.getFrom());
        assertEquals(oldValue.toString(), cib.getFromString());
        assertEquals(value.toString(), cib.getTo());
        assertEquals(value.toString(), cib.getToString());

        verify(mockIssue);
        mockFieldManager.verify();
    }

    public void testUpdateIssueFieldFunctionUpdateTimetracking() throws GenericEntityException, WorkflowException
    {
        _testUpdateField(IssueFieldConstants.TIMETRACKING, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, "1440", "86400");
    }

    public void testUpdateIssueFieldFunctionUpdateField() throws GenericEntityException, WorkflowException
    {
        String fieldValue = "test summary";
        String fieldId = IssueFieldConstants.SUMMARY;
        _testUpdateField(fieldId, fieldId, fieldValue, fieldValue);
    }

    private void _testUpdateField(String fieldId, String fieldIdArg, String fieldValue, String fieldValueArg) throws WorkflowException
    {
        Project project = new MockProject(1);
        IssueType issueType = new MockIssueType("bug", "Bug");

        Map args = EasyMap.build("field.name", fieldIdArg, "field.value", fieldValueArg, "username", user.getName());

        Mock mockOrderableField = new Mock(OrderableField.class);
        mockOrderableField.setStrict(true);
        mockOrderableField.expectVoid("populateFromParams", P.args(new IsEqual(new HashMap()), new IsAnything()));
// Temporary solution for JRA-7859 should find perm fix
//      mockOrderableField.expectVoid("validateParams", new Constraint[]{new IsEqual(new HashMap()), new IsAnything(), new IsAnything(), new IsEqual(mockIssue), new IsAnything()});
        mockOrderableField.expectVoid("updateIssue", P.args(new IsAnything(), new IsEqual(mockIssue), new IsAnything()));
        mockOrderableField.expectAndReturn("getId", fieldId);


        Mock mockFieldLayoutItem = new Mock(FieldLayoutItem.class);
        mockFieldLayoutItem.setStrict(true);

        Mock mockFieldLayout = new Mock(FieldLayout.class);
        mockFieldLayout.setStrict(true);
// Temporary solution for JRA-7859 should find perm fix
//      mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.args(new IsEqual(mockOrderableField)), mockFieldLayoutItem.proxy());

        ModifiedValue modifiedValue = new ModifiedValue(null, fieldValue);
        Map modifiedFields = EasyMap.build(fieldId, modifiedValue);
        ChangeItemBean cib = new ChangeItemBean();
        mockOrderableField.expectVoid("updateValue", new Constraint[]{new IsAnything(), new IsEqual(mockIssue), new IsEqual(modifiedValue), new IsAnything()});

        expect(mockIssue.getModifiedFields()).andStubReturn(modifiedFields);
// Temporary solution for JRA-7859 should find perm fix
        expect(mockIssue.getProjectObject()).andStubReturn(project);
        expect(mockIssue.getIssueTypeObject()).andStubReturn(issueType);
        expect(mockIssue.getGenericValue()).andStubReturn(null);

        Mock mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.setStrict(true);
        mockFieldManager.expectAndReturn("getOrderableField", P.args(new IsEqual(fieldId)), mockOrderableField.proxy());

// Temporary solution for JRA-7859 should find perm fix
//      mockFieldLayoutManager.expectAndReturn("getFieldLayout", P.args(new IsEqual(projectGV), new IsEqual("1")), mockFieldLayout.proxy());

        replayMocks(this);
        uif.execute(input, args, null);

        verify(mockIssue);
        mockFieldLayoutManager.verify();
        mockFieldLayoutItem.verify();
        mockFieldLayout.verify();
        mockFieldManager.verify();
        mockOrderableField.verify();
    }


}

class MockUpdateIssueFieldFunction extends UpdateIssueFieldFunction
{
    private FieldManager fieldManager;

    public MockUpdateIssueFieldFunction(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }

    protected FieldManager getFieldManager()
    {
        return fieldManager;
    }
}