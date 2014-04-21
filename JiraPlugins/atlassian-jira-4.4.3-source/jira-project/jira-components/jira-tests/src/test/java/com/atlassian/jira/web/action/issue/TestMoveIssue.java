/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.MoveIssueBean;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;

public class TestMoveIssue extends AbstractUsersTestCase
{
    protected GenericValue project1;
    protected GenericValue project2;
    private User bob;
    private User bill;
    protected User fred;
    protected GenericValue issue;
    private Group group1;
    private Group group2;
    private Group group3;
    protected GenericValue scheme;

    private Mock constantsManger;
    private Mock projectManager;

    public TestMoveIssue(String s)
    {
        super(s);
        constantsManger = new Mock(ConstantsManager.class);
        projectManager = new Mock(ProjectManager.class);
    }

    protected MoveIssueBean setupMoveIssueBean()
    {
        MoveIssueBean moveIssueBean = new MoveIssueBean((ConstantsManager) constantsManger.proxy(), (ProjectManager) projectManager.proxy());
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, moveIssueBean);
        return moveIssueBean;
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(request);

        bob = UtilsForTests.getTestUser("bob");
        bill = UtilsForTests.getTestUser("bill");
        fred = UtilsForTests.getTestUser("fred");

        group1 = GroupUtils.getGroupSafely("group1");
        group2 = GroupUtils.getGroupSafely("group2");
        group3 = GroupUtils.getGroupSafely("group3");

        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "project1"));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "name", "project2"));

        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(10), "project", new Long(1), "assignee", "bob", "type", "1"));

        bob.addToGroup(group1);
        bill.addToGroup(group2);
        fred.addToGroup(group3);

        scheme = ManagerFactory.getPermissionSchemeManager().createDefaultScheme();
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project1, scheme);
        ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, scheme, null, GroupDropdown.DESC);
    }

    public void testDoDefault() throws Exception
    {
        MoveIssue mi = new MoveIssue(null, null, null, null, null, null, null, null, null);
        mi.setId(new Long(10));

        assertEquals("securitybreach", mi.doDefault());
    }
}
