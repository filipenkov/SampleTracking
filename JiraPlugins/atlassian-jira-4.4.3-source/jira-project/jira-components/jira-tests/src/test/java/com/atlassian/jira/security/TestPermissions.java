/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

public class TestPermissions extends LegacyJiraMockTestCase
{
    public TestPermissions(String s)
    {
        super(s);
    }

    public void testGetType()
    {
        assertEquals(Permissions.SYSTEM_ADMIN, Permissions.getType("sysadmin"));
        assertEquals(Permissions.ADMINISTER, Permissions.getType("admin"));
        assertEquals(Permissions.USE, Permissions.getType("use"));
        assertEquals(Permissions.BROWSE, Permissions.getType("browse"));
        assertEquals(Permissions.CREATE_ISSUE, Permissions.getType("create"));
        assertEquals(Permissions.CREATE_ATTACHMENT, Permissions.getType("attach"));
        assertEquals(Permissions.EDIT_ISSUE, Permissions.getType("edit"));
        assertEquals(Permissions.EDIT_ISSUE, Permissions.getType("update"));
        assertEquals(Permissions.SCHEDULE_ISSUE, Permissions.getType("scheduleissue"));
        assertEquals(Permissions.ASSIGNABLE_USER, Permissions.getType("assignable"));
        assertEquals(Permissions.ASSIGN_ISSUE, Permissions.getType("assign"));
        assertEquals(Permissions.RESOLVE_ISSUE, Permissions.getType("resolve"));
        assertEquals(Permissions.COMMENT_ISSUE, Permissions.getType("comment"));
        assertEquals(Permissions.CLOSE_ISSUE, Permissions.getType("close"));
        assertEquals(Permissions.WORK_ISSUE, Permissions.getType("work"));
        assertEquals(Permissions.WORKLOG_DELETE_ALL, Permissions.getType("worklogdeleteall"));
        assertEquals(Permissions.WORKLOG_DELETE_OWN, Permissions.getType("worklogdeleteown"));
        assertEquals(Permissions.WORKLOG_EDIT_ALL, Permissions.getType("worklogeditall"));
        assertEquals(Permissions.WORKLOG_EDIT_OWN, Permissions.getType("worklogeditown"));
        assertEquals(Permissions.LINK_ISSUE, Permissions.getType("link"));
        assertEquals(Permissions.DELETE_ISSUE, Permissions.getType("delete"));
        assertEquals(Permissions.PROJECT_ADMIN, Permissions.getType("project"));
        assertEquals(Permissions.MOVE_ISSUE, Permissions.getType("move"));
        assertEquals(Permissions.COMMENT_EDIT_ALL, Permissions.getType("commenteditall"));
        assertEquals(Permissions.COMMENT_EDIT_OWN, Permissions.getType("commenteditown"));
        assertEquals(Permissions.COMMENT_DELETE_OWN, Permissions.getType("commentdeleteown"));
        assertEquals(Permissions.COMMENT_DELETE_ALL, Permissions.getType("commentdeleteall"));
        assertEquals(Permissions.ATTACHMENT_DELETE_ALL, Permissions.getType("attachdeleteall"));
        assertEquals(Permissions.ATTACHMENT_DELETE_OWN, Permissions.getType("attachdeleteown"));
        assertEquals(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, Permissions.getType("groupsubscriptions"));
        assertEquals(-1, Permissions.getType("foobar"));
    }

    public void testGetShortName()
    {
        assertEquals("commenteditall", Permissions.getShortName(Permissions.COMMENT_EDIT_ALL));
        assertEquals("commenteditown", Permissions.getShortName(Permissions.COMMENT_EDIT_OWN));
        assertEquals("worklogeditown", Permissions.getShortName(Permissions.WORKLOG_EDIT_OWN));
        assertEquals("worklogeditall", Permissions.getShortName(Permissions.WORKLOG_EDIT_ALL));
        assertEquals("worklogdeleteall", Permissions.getShortName(Permissions.WORKLOG_DELETE_ALL));
        assertEquals("worklogdeleteown", Permissions.getShortName(Permissions.WORKLOG_DELETE_OWN));
        assertEquals("sysadmin", Permissions.getShortName(Permissions.SYSTEM_ADMIN));
    }

    public void testGetDescription()
    {
        assertEquals("Ability to edit all comments made on issues.", Permissions.getDescription(Permissions.COMMENT_EDIT_ALL));
        assertEquals("Ability to edit own comments made on issues.", Permissions.getDescription(Permissions.COMMENT_EDIT_OWN));
        assertEquals("Users with this permission may delete own attachments.", Permissions.getDescription(Permissions.ATTACHMENT_DELETE_OWN));
        assertEquals("Users with this permission may delete all attachments.", Permissions.getDescription(Permissions.ATTACHMENT_DELETE_ALL));
        assertEquals("Ability to delete all comments made on issues.", Permissions.getDescription(Permissions.COMMENT_DELETE_ALL));
        assertEquals("Ability to delete own comments made on issues.", Permissions.getDescription(Permissions.COMMENT_DELETE_OWN));
        assertEquals("Ability to delete all worklogs made on issues.", Permissions.getDescription(Permissions.WORKLOG_DELETE_ALL));
        assertEquals("Ability to delete own worklogs made on issues.", Permissions.getDescription(Permissions.WORKLOG_DELETE_OWN));
        assertEquals("Ability to edit all worklogs made on issues.", Permissions.getDescription(Permissions.WORKLOG_EDIT_ALL));
        assertEquals("Ability to edit own worklogs made on issues.", Permissions.getDescription(Permissions.WORKLOG_EDIT_OWN));
        assertEquals("Ability to perform all administration functions. There must be at least one group with this permission.", Permissions.getDescription(Permissions.SYSTEM_ADMIN));
        assertEquals("Ability to perform most administration functions (excluding Import & Export, SMTP Configuration, etc.).", Permissions.getDescription(Permissions.ADMINISTER));
    }

    public void testIsGlobalPermission()
    {
        assertFalse(Permissions.isGlobalPermission(Permissions.COMMENT_EDIT_ALL));
        assertFalse(Permissions.isGlobalPermission(Permissions.COMMENT_EDIT_OWN));
        assertFalse(Permissions.isGlobalPermission(Permissions.COMMENT_DELETE_ALL));
        assertFalse(Permissions.isGlobalPermission(Permissions.COMMENT_DELETE_OWN));
        assertFalse(Permissions.isGlobalPermission(Permissions.ATTACHMENT_DELETE_ALL));
        assertFalse(Permissions.isGlobalPermission(Permissions.ATTACHMENT_DELETE_OWN));
        assertFalse(Permissions.isGlobalPermission(Permissions.WORKLOG_DELETE_ALL));
        assertFalse(Permissions.isGlobalPermission(Permissions.WORKLOG_DELETE_OWN));
        assertFalse(Permissions.isGlobalPermission(Permissions.WORKLOG_EDIT_ALL));
        assertFalse(Permissions.isGlobalPermission(Permissions.WORKLOG_EDIT_OWN));
        assertTrue(Permissions.isGlobalPermission(Permissions.SYSTEM_ADMIN));
    }

}
