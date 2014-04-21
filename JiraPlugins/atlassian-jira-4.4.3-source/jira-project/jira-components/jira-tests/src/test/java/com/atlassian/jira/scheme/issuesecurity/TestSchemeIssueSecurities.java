/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueLevelSecurities;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.issue.security.IssueSecurityTypeManager;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import org.ofbiz.core.entity.GenericValue;

public class TestSchemeIssueSecurities extends AbstractWebworkTestCase
{
    private SchemeManager issueSchemeManager;
    private GenericValue issueScheme;
    private GenericValue project;
    private GenericValue project2;
    private GenericValue securityLevelA;
    private GenericValue securityLevelB;
    private GenericValue securityLevelC;
    IssueSecurityLevelManager sec;
    private User joe;
    private User bob;
    private Group group;
    private GenericValue securityA;
    private GenericValue securityB;

    public TestSchemeIssueSecurities(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        AssociationManager associationManager = CoreFactory.getAssociationManager();
        IssueSecuritySchemeManager issueSecuritySchemeManager = new IssueSecuritySchemeManagerImpl(null, null, null, schemeFactory, null, associationManager, ofBizDelegator, null);
        ManagerFactory.addService(IssueSecurityLevelManager.class, new IssueLevelSecurities(issueSecuritySchemeManager, new IssueSecurityTypeManager(), null, null, null));
        CrowdService crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);

        sec = ManagerFactory.getIssueSecurityLevelManager();
        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();
        issueScheme = issueSchemeManager.createScheme("IScheme", "Test Desc");
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "lead", "paul"));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "lead", "paul"));
        issueSchemeManager.addSchemeToProject(project, issueScheme);

        //create security levels
        securityLevelA = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(1), "scheme", issueScheme.getLong("id"), "name", "Test Level", "description", "Test Desc"));
        securityLevelB = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(2), "scheme", issueScheme.getLong("id"), "name", "Test Level2", "description", "Test Desc2"));
        securityLevelC = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(3), "scheme", issueScheme.getLong("id"), "name", "Test Level3", "description", "Test Desc3"));

        bob = new MockUser("bobTestGetUsersSecurityLevels");
        joe = new MockUser("joeTestGetUsersSecurityLevels");

        //create a group and add user 2 to this group
        crowdService.addUser(bob, "");
        crowdService.addUser(joe, "");

        group = new MockGroup("groupA");
        crowdService.addGroup(group);
        crowdService.addUserToGroup(bob, group);
        crowdService.addUserToGroup(joe, group);

        securityA = UtilsForTests.getTestEntity("SchemeIssueSecurities", EasyMap.build("id", new Long(1), "scheme", issueScheme.getLong("id"), "security", securityLevelA.getLong("id"), "parameter", "bobTestGetUsersSecurityLevels", "type", "user"));
        securityB = UtilsForTests.getTestEntity("SchemeIssueSecurities", EasyMap.build("id", new Long(2), "scheme", issueScheme.getLong("id"), "security", securityLevelB.getLong("id"), "parameter", "groupA", "type", "group"));
    }

    public void testGetUsersSecurityLevels() throws Exception
    {
        assertEquals(0, sec.getUsersSecurityLevels(project2, bob).size());
        assertEquals(2, sec.getUsersSecurityLevels(project, bob).size());
        assertEquals(1, sec.getUsersSecurityLevels(project, joe).size());

        assertTrue(sec.getUsersSecurityLevels(project, bob).contains(securityLevelA));
        assertTrue(sec.getUsersSecurityLevels(project, bob).contains(securityLevelB));
        assertEquals(securityLevelB, sec.getUsersSecurityLevels(project, joe).get(0));

        assertEquals(0, sec.getUsersSecurityLevels(project2, bob).size());
    }
}
