/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.HideableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import webwork.action.Action;

public abstract class AbstractTestViewIssueFields extends LegacyJiraMockTestCase
{
    protected ReindexMessageManager reindexMessageManager;
    protected FieldLayoutSchemeHelper fieldLayoutSchemeHelper;

    public AbstractTestViewIssueFields(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        JiraTestUtil.setupExpectedRedirect("ViewIssueFields.jspa");

        fieldLayoutSchemeHelper = EasyMock.createMock(FieldLayoutSchemeHelper.class);
        reindexMessageManager = EasyMock.createMock(ReindexMessageManager.class);

        // note: this is really cheating, but the testing of actions is not my main concern, so we're fudging it.
        EasyMock.expect(fieldLayoutSchemeHelper.doesChangingFieldLayoutRequireMessage(EasyMock.<User>anyObject(), EasyMock.<EditableFieldLayout>anyObject()))
                .andStubReturn(true);

        reindexMessageManager.pushMessage(EasyMock.<User>anyObject(), EasyMock.eq("admin.notifications.task.field.configuration"));
        EasyMock.expectLastCall().asStub();

        EasyMock.replay(fieldLayoutSchemeHelper, reindexMessageManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        EasyMock.verify(fieldLayoutSchemeHelper, reindexMessageManager);
    }

    public abstract void setNewVif();

    public abstract AbstractConfigureFieldLayout getVif();

    public abstract void refreshVif();

    public void testIsHideable()
    {
        OrderableField hideableField = (OrderableField) ManagerFactory.getFieldManager().getField(IssueFieldConstants.ENVIRONMENT);
        assertTrue(hideableField instanceof HideableField);
        FieldLayoutItemImpl fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(hideableField)
                .setFieldDescription("the environment field")
                .setHidden(false)
                .setRequired(false)
                .build();
        assertTrue(getVif().isHideable(fieldLayoutItem));
        assertTrue(getVif().isRequirable(fieldLayoutItem));
        fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(hideableField)
                .setFieldDescription("the environment field")
                .setHidden(true)
                .setRequired(false)
                .build();
        assertTrue(getVif().isHideable(fieldLayoutItem));
        assertFalse(getVif().isRequirable(fieldLayoutItem));
        fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(hideableField)
                .setFieldDescription("the environment field")
                .setHidden(false)
                .setRequired(true)
                .build();
        assertTrue(getVif().isHideable(fieldLayoutItem));
        assertTrue(getVif().isRequirable(fieldLayoutItem));
        try
        {
            new FieldLayoutItemImpl.Builder()
                    .setOrderableField((OrderableField) ManagerFactory.getFieldManager().getField(IssueFieldConstants.SUMMARY))
                    .setFieldDescription("the summary field")
                    .setHidden(true)
                    .setRequired(false)
                    .build();
            fail("Exception should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void testHide() throws Exception
    {
        FieldLayoutItem fieldLayoutItemBefore = (FieldLayoutItem) getVif().getOrderedList().get(0);
        getVif().setHide(new Integer(0));

        getVif().doHide();
        refreshVif();
        FieldLayoutItem fieldLayoutItemAfter = (FieldLayoutItem) getVif().getOrderedList().get(0);
        assertEquals(!fieldLayoutItemBefore.isHidden(), fieldLayoutItemAfter.isHidden());
        assertEquals(fieldLayoutItemBefore.isRequired(), fieldLayoutItemAfter.isRequired());
    }

    public void testHideNonHideable() throws Exception
    {
        OrderableField orderableField = ManagerFactory.getFieldManager().getOrderableField(IssueFieldConstants.SUMMARY);
        FieldLayoutItem fieldLayoutItem = getVif().getFieldLayout().getFieldLayoutItem(orderableField);
        int position = getVif().getOrderedList().indexOf(fieldLayoutItem);

        getVif().setHide(new Integer(position));

        final String result = getVif().doHide();
        assertEquals(Action.ERROR, result);
        assertFalse(getVif().getErrorMessages().isEmpty());
        assertEquals(1, getVif().getErrorMessages().size());
        assertEquals("Tried to hide field 'summary' that cannot be hidden.", getVif().getErrorMessages().iterator().next());
    }

    public void testMakeRequired() throws Exception
    {
        FieldLayoutItem fieldLayoutItemBefore = (FieldLayoutItem) getVif().getOrderedList().get(4);
        getVif().setRequire(new Integer(4));

        getVif().doRequire();
        refreshVif();
        
        FieldLayoutItem fieldLayoutItemAfter = (FieldLayoutItem) getVif().getOrderedList().get(4);
        assertEquals(fieldLayoutItemBefore.isHidden(), fieldLayoutItemAfter.isHidden());
        assertEquals(!fieldLayoutItemBefore.isRequired(), fieldLayoutItemAfter.isRequired());
    }

    public void testHideNonMandatoriable() throws Exception
    {
        OrderableField orderableField = ManagerFactory.getFieldManager().getOrderableField(IssueFieldConstants.SUMMARY);
        FieldLayoutItem fieldLayoutItem = getVif().getFieldLayout().getFieldLayoutItem(orderableField);
        int position = getVif().getOrderedList().indexOf(fieldLayoutItem);

        getVif().setRequire(new Integer(position));
        final String aResult = getVif().doRequire();
        assertEquals(Action.ERROR, aResult);
        assertFalse(getVif().getErrorMessages().isEmpty());
        assertEquals(1, getVif().getErrorMessages().size());
        assertEquals("Tried to make field 'Summary' optional.", getVif().getErrorMessages().iterator().next());
    }
}
