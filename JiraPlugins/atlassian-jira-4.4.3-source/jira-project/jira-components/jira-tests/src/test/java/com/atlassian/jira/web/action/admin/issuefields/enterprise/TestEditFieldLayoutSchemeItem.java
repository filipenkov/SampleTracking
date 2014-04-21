/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.web.action.admin.issuefields.AbstractEditFieldLayoutItem;
import com.atlassian.jira.web.action.admin.issuefields.AbstractTestEditFieldLayoutItem;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestEditFieldLayoutSchemeItem extends AbstractTestEditFieldLayoutItem
{
    private GenericValue fieldLayout;
    private EditFieldLayoutItem editFieldLayoutSchemeItem;

    public TestEditFieldLayoutSchemeItem(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        fieldLayout = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Name"));
    }

    public AbstractEditFieldLayoutItem getEfli()
    {
        if (editFieldLayoutSchemeItem == null)
        {
            setNewEfli();
        }
        return editFieldLayoutSchemeItem;
    }

    public void setNewEfli()
    {
        editFieldLayoutSchemeItem = new EditFieldLayoutItem();
        editFieldLayoutSchemeItem.setId(fieldLayout.getLong("id"));
    }

    protected Long getFieldLayoutId()
    {
        return fieldLayout.getLong("id");
    }

    public void testDoExecute() throws Exception
    {
        // Setup expected redirect on success
        MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ConfigureFieldLayout.jspa?id=" + getFieldLayoutId());
        String testDescription = "test description";


        OrderableField orderableField = ManagerFactory.getFieldManager().getOrderableField(IssueFieldConstants.SUMMARY);
        FieldLayout fl = ManagerFactory.getFieldManager().getFieldLayoutManager().getFieldLayout(fieldLayout.getLong("id"));

        List items = new ArrayList(fl.getFieldLayoutItems());
        Collections.sort(items);
        FieldLayoutItem fieldLayoutItem = fl.getFieldLayoutItem(orderableField);
        int position = items.indexOf(fieldLayoutItem);

        getEfli().setPosition(new Integer(position));
        getEfli().setDescription(testDescription);

        String result = getEfli().execute();
        assertEquals(ActionSupport.NONE, result);

        fieldLayoutItem = (FieldLayoutItem) ManagerFactory.getFieldManager().getFieldLayoutManager().getFieldLayout(fieldLayout.getLong("id")).getFieldLayoutItem(orderableField);
        assertEquals(testDescription, fieldLayoutItem.getFieldDescription());

        // Ensure the redirect was received
        mockHttpServletResponse.verify();
    }

    protected void createFieldLayoutItem(String testDescription)
    {
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", getFieldLayoutId(), "description", testDescription, "fieldidentifier", IssueFieldConstants.ISSUE_TYPE, "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.TRUE.toString()));
    }
}
