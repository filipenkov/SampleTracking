/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.history;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockConstantsManager;

import java.util.List;

public class TestChangeLogUtils extends AbstractUsersTestCase
{
    public TestChangeLogUtils(String s)
    {
        super(s);
    }

    public void testGenerateChangeItemSame() throws GenericEntityException
    {
        GenericValue before = new MockGenericValue("Issue", EasyMap.build("id", "5"));
        GenericValue after = new MockGenericValue("Issue", EasyMap.build("id", "5"));

        assertNull(ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    public void testGenerateChangeItemNulls() throws GenericEntityException
    {
        GenericValue before = new MockGenericValue("Issue", EasyMap.build("id", null));
        GenericValue after = new MockGenericValue("Issue", EasyMap.build("id", null));

        assertNull(ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    public void testGenerateChangeItemBeforeNull() throws GenericEntityException
    {
        GenericValue before = new MockGenericValue("Issue", EasyMap.build("id", null));
        GenericValue after = new MockGenericValue("Issue", EasyMap.build("id", "5"));

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "id", null, null, null, "5");

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    public void testGenerateChangeItemAfterNull() throws GenericEntityException
    {
        GenericValue before = new MockGenericValue("Issue", EasyMap.build("id", "5"));
        GenericValue after = new MockGenericValue("Issue", EasyMap.build("id", null));

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "id", null, "5", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    public void testGenerateChangeItemDifferent() throws GenericEntityException
    {
        GenericValue before = new MockGenericValue("Issue", EasyMap.build("id", "5"));
        GenericValue after = new MockGenericValue("Issue", EasyMap.build("id", "7"));

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "id", null, "5", null, "7");

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    public void testGenerateChangeItemType() throws GenericEntityException
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("type", "10"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "10", "name", "Foo"));
        ManagerFactory.getConstantsManager().refreshIssueTypes();

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "type", "10", "Foo", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "type"));
    }

    public void testGenerateChangeItemResolution() throws GenericEntityException
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("resolution", "5"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "5", "name", "Solved"));
        ManagerFactory.getConstantsManager().refreshResolutions();

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "resolution", "5", "Solved", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "resolution"));
    }

    public void testGenerateChangeItemPriority() throws GenericEntityException
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("priority", "5"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "5", "name", "Top Shelf"));
        ManagerFactory.getConstantsManager().refreshPriorities();

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "priority", "5", "Top Shelf", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "priority"));
    }

    public void testGenerateChangeItemAssignee() throws Exception
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("assignee", "bob"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        User user = UserManager.getInstance().createUser("bob");
        user.setFullName("Bob the Builder");

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "assignee", "bob", "Bob the Builder", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "assignee"));
    }

    public void testGenerateChangeItemReporter() throws Exception
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", "bob"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        User user = UserManager.getInstance().createUser("bob");
        user.setFullName("Bob the Builder");

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "reporter", "bob", "Bob the Builder", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "reporter"));
    }

    public void testGenerateChangeItemEstimate() throws Exception
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("timeestimate", new Long(7200)));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timeestimate", "7200", "7200", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "timeestimate"));
    }

    public void testGenerateChangeItemSpent() throws Exception
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", null);
        GenericValue after = UtilsForTests.getTestEntity("Issue", EasyMap.build("timespent", new Long(3600)));

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timespent", null, null, "3600", "3600");

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "timespent"));
    }

    public void testGenerateChangeItemStatus() throws Exception
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("status", "1"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", null);

        MockConstantsManager mcm = new MockConstantsManager();
        ManagerFactory.addService(ConstantsManager.class, mcm);

        GenericValue status = UtilsForTests.getTestEntity("Status", EasyMap.build("id", "1", "name", "high"));
        mcm.addStatus(status);

        ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "status", "1", "high", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "status"));
    }

    public void testCreateChangeGroupIdentical() throws Exception
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("status", "1", "summary", "2"));
        assertNull(ChangeLogUtils.createChangeGroup(null, before, (GenericValue) before.clone(), null, true));
    }

    public void testCreateChangeGroupNoValues() throws Exception, DuplicateEntityException
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "status", null));
        GenericValue after = before;
        assertNull(ChangeLogUtils.createChangeGroup(null, before, after, null, true));
    }

    public void testCreateChangeGroup() throws Exception, DuplicateEntityException
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("status", "1", "summary", "2"));
        GenericValue after = UtilsForTests.getTestEntity("Issue", EasyMap.build("status", "2", "summary", "4"));

        User user = UserManager.getInstance().createUser("bob");

        GenericValue changeGroup = ChangeLogUtils.createChangeGroup(user, before, after, null, true);
        assertNotNull(changeGroup.getLong("id"));
        assertNotNull(changeGroup.getTimestamp("created"));
        assertEquals("bob", changeGroup.getString("author"));

        List changeItems = CoreFactory.getGenericDelegator().findByAnd("ChangeItem", EasyMap.build("group", changeGroup.getLong("id")));
        assertEquals(2, changeItems.size());
    }

    public void testCreateChangeGroupNoChangeJustList() throws Exception, DuplicateEntityException
    {
        GenericValue before = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "status", null));

        ChangeItemBean cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "foo", "bar", "baz");
        GenericValue changeGroup = ChangeLogUtils.createChangeGroup(null, before, (GenericValue) before.clone(), EasyList.build(cib), true);
        List changeItems = CoreFactory.getGenericDelegator().findByAnd("ChangeItem", EasyMap.build("group", changeGroup.getLong("id")));
        assertEquals(1, changeItems.size());
    }
}
