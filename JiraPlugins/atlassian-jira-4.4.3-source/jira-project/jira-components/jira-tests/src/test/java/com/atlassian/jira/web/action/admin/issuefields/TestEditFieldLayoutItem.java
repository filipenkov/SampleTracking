/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.issuefields.AbstractEditFieldLayoutItem;
import com.atlassian.jira.web.action.admin.issuefields.EditDefaultFieldLayoutItem;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestEditFieldLayoutItem extends AbstractTestEditFieldLayoutItem
{
    EditDefaultFieldLayoutItem efli;

    public TestEditFieldLayoutItem(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public AbstractEditFieldLayoutItem getEfli()
    {
        if (efli == null)
        {
            setNewEfli();
        }
        return efli;
    }

    public void setNewEfli()
    {
        efli = new EditDefaultFieldLayoutItem();
    }

    protected Long getFieldLayoutId()
    {
        return null;
    }

    public void testDoExecute() throws Exception
    {
        // Setup expected redirect on success
        MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ViewIssueFields.jspa");
        String testDescription = "test description";

        OrderableField orderableField = ManagerFactory.getFieldManager().getOrderableField(IssueFieldConstants.SUMMARY);
        FieldLayout fieldLayout = ManagerFactory.getFieldManager().getFieldLayoutManager().getFieldLayout();

        List items = new ArrayList(fieldLayout.getFieldLayoutItems());
        Collections.sort(items);
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(orderableField);
        int position = items.indexOf(fieldLayoutItem);

        getEfli().setPosition(new Integer(position));
        getEfli().setDescription(testDescription);

        String result = getEfli().execute();
        assertEquals(ActionSupport.NONE, result);

        fieldLayoutItem = (FieldLayoutItem) ManagerFactory.getFieldManager().getFieldLayoutManager().getFieldLayout().getFieldLayoutItem(orderableField);
        assertEquals(testDescription, fieldLayoutItem.getFieldDescription());

        // Ensure the redirect was received
        mockHttpServletResponse.verify();
    }

    protected void createFieldLayoutItem(String description)
    {
        GenericValue fieldLayout = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Test Layout", "type", FieldLayoutManager.TYPE_DEFAULT));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "description", description, "fieldidentifier", IssueFieldConstants.ISSUE_TYPE, "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.TRUE.toString()));
    }
}
