/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import webwork.action.ActionSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTestEditFieldLayoutItem extends LegacyJiraMockTestCase
{
    public AbstractTestEditFieldLayoutItem(String s)
    {
        super(s);
    }

    public abstract AbstractEditFieldLayoutItem getEfli();

    public abstract void setNewEfli();

    public void testGetSetDescription()
    {
        getEfli().setDescription("test descirption");
        assertEquals("test descirption", getEfli().getDescription());
    }

    public void testDoDefaultBadPosition() throws Exception
    {
        int position = 100;
        getEfli().setPosition(new Integer(position));

        String result = getEfli().doDefault();
        assertEquals(ActionSupport.INPUT, result);
        checkSingleElementCollection(getEfli().getErrorMessages(), "The field layout item at position '" + position + "' does not exist.");
    }

    public void testDoExecuteBadPosition() throws Exception
    {
        int position = 100;
        getEfli().setPosition(new Integer(position));

        String result = getEfli().execute();
        assertEquals(ActionSupport.ERROR, result);
        checkSingleElementCollection(getEfli().getErrorMessages(), "The field layout item at position '" + position + "' does not exist.");
    }

    protected abstract Long getFieldLayoutId();

    public void testDoDefault() throws Exception
    {
        // Set Up a field with a description

        String testDescription = "test description";

        createFieldLayoutItem(testDescription);
        OrderableField orderableField = ComponentAccessor.getFieldManager().getOrderableField(IssueFieldConstants.ISSUE_TYPE);
        FieldLayout fieldLayout =null;
        if (getFieldLayoutId() != null)
            fieldLayout = ComponentAccessor.getFieldManager().getFieldLayoutManager().getFieldLayout(getFieldLayoutId());
        else
            fieldLayout = ComponentAccessor.getFieldManager().getFieldLayoutManager().getFieldLayout();

        List items = new ArrayList(fieldLayout.getFieldLayoutItems());
        Collections.sort(items);
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(orderableField);
        int position = items.indexOf(fieldLayoutItem);


        getEfli().setPosition(new Integer(position));

        String result = getEfli().doDefault();
        assertEquals(ActionSupport.INPUT, result);
        assertEquals(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.ISSUE_TYPE).getName(), getEfli().getFieldName());
        assertEquals(testDescription, getEfli().getDescription());
    }

    public abstract void testDoExecute() throws Exception;

    protected abstract void createFieldLayoutItem(String description);
}
