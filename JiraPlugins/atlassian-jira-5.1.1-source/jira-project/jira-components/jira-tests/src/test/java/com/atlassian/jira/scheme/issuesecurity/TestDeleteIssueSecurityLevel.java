/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.web.action.admin.issuesecurity.DeleteIssueSecurityLevel;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Map;

public class TestDeleteIssueSecurityLevel extends AbstractWebworkTestCase
{
    DeleteIssueSecurityLevel del;
    GenericValue project;
    GenericValue securityLevelA;
    GenericValue securityLevelB;
    GenericValue securityLevelC;
    GenericValue securityLevelD;
    GenericValue issueA;
    GenericValue issueB;
    GenericValue issueScheme;
    SchemeManager issueSchemeManager;
    private FieldVisibilityBean origFieldVisibilityBean;

    public TestDeleteIssueSecurityLevel(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        del = new DeleteIssueSecurityLevel();
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        AssociationManager associationManager = CoreFactory.getAssociationManager();
        ManagerFactory.addService(IssueSecuritySchemeManager.class, new IssueSecuritySchemeManagerImpl(null, null, null, schemeFactory, null, associationManager, ofBizDelegator, null));
        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);
        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();

        issueScheme = issueSchemeManager.createScheme("IScheme", "Test Desc");
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "lead", "paul"));

        //create security levels
        securityLevelA = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(1), "scheme", issueScheme.getLong("id"), "name", "Test Level", "description", "Test Desc"));
        securityLevelB = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(2), "scheme", issueScheme.getLong("id"), "name", "Test Level2", "description", "Test Desc2"));
        securityLevelC = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(3), "scheme", issueScheme.getLong("id"), "name", "Test Level3", "description", "Test Desc3"));
        securityLevelD = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("id", new Long(4), "scheme", issueScheme.getLong("id"), "name", "Test Level4", "description", "Test Desc4"));

        //Set D as the default
        issueScheme.set("defaultlevel", securityLevelD.getLong("id"));
        issueScheme.store();

        //create issues with security level
        issueA = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1", "project", new Long(2), "reporter", "bob", "security", securityLevelA.getLong("id")));
        issueB = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-2", "project", new Long(2), "reporter", "bob", "security", securityLevelA.getLong("id")));

        // associate with scheme and set the current level
        del.setSchemeId(issueScheme.getLong("id"));
        del.setLevelId(securityLevelA.getLong("id"));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
    }

    public void testAffectedIssues() throws Exception
    {
        assertEquals(del.getAffectedIssues().size(), 2);
    }

    public void testOtherLevels() throws Exception
    {
        Map otherLevels = del.getOtherLevels();
        assertEquals(3, otherLevels.size());

        //The current level will not be returned but the other 3 will
        assertFalse(otherLevels.containsKey(securityLevelA.getLong("id")));
        assertTrue(otherLevels.containsKey(securityLevelB.getLong("id")));
        assertTrue(otherLevels.containsKey(securityLevelC.getLong("id")));
        assertTrue(otherLevels.containsKey(securityLevelD.getLong("id")));
    }

    public void testExecuteNewLevel() throws Exception
    {
        GenericValue newIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        GenericValue newIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(del.getRedirectURL());

        assertEquals(4, ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(issueScheme.getLong("id")).size());

        del.setSwapLevel(securityLevelB.getLong("id"));

        //check the security levels on the issues are set
        assertEquals(newIssueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(newIssueB.getLong("security"), securityLevelA.getLong("id"));

        //Check that the statistics are updated
        GenericValue expectedIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        GenericValue expectedIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));
        expectedIssueA.set("security", securityLevelB.getLong("id"));
        expectedIssueB.set("security", securityLevelB.getLong("id"));

        String result = del.doExecute();

        //there should be no errors
        assertEquals(0, del.getErrors().size());

        assertEquals(Action.NONE, result);

        //the scheme level should be gone and there should be 3 left (2, 3, 4)
        assertEquals(3, ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(issueScheme.getLong("id")).size());

        //The security levels on the issues should be changed
        newIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        newIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));

        assertEquals(newIssueA.getLong("security"), securityLevelB.getLong("id"));
        assertEquals(newIssueB.getLong("security"), securityLevelB.getLong("id"));
        response.verify();
    }

    public void testExecuteNoLevel() throws Exception
    {
        GenericValue newIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        GenericValue newIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(del.getRedirectURL());

        assertEquals(4, ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(issueScheme.getLong("id")).size());

        del.setSwapLevel(null);

        //check the security levels on the issues are set
        assertEquals(newIssueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(newIssueB.getLong("security"), securityLevelA.getLong("id"));

        String result = del.doExecute();

        //there should be no errors
        assertEquals(0, del.getErrors().size());

        assertEquals(Action.NONE, result);

        //the scheme level should be gone and there should be 3 left
        assertEquals(3, ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(issueScheme.getLong("id")).size());

        //The security levels on the issues should be changed
        newIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        newIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));

        assertNull(newIssueA.getLong("security"));
        assertNull(newIssueB.getLong("security"));
        response.verify();
    }

    public void testExecuteNoAffectedIssues() throws Exception
    {
        GenericValue newIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        GenericValue newIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));

        del.setLevelId(securityLevelC.getLong("id"));

        assertEquals(del.getAffectedIssues().size(), 0);

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(del.getRedirectURL());

        assertEquals(4, ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(issueScheme.getLong("id")).size());

        del.setSwapLevel(securityLevelB.getLong("id"));

        //check the security levels on the issues are set
        assertEquals(newIssueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(newIssueB.getLong("security"), securityLevelA.getLong("id"));

        String result = del.doExecute();

        //there should be no errors
        assertEquals(0, del.getErrors().size());

        assertEquals(Action.NONE, result);

        //the scheme level should be gone and there should be 3 left
        assertEquals(3, ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(issueScheme.getLong("id")).size());

        //The security levels on the issues should be the same as these werent in this level
        newIssueA = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueA.getLong("id")));
        newIssueB = CoreFactory.getGenericDelegator().findByPrimaryKey("Issue", EasyMap.build("id", issueB.getLong("id")));

        assertEquals(newIssueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(newIssueB.getLong("security"), securityLevelA.getLong("id"));
        response.verify();
    }

    public void testDeleteDefaultLevel() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(del.getRedirectURL());

        del.setLevelId(securityLevelD.getLong("id"));

        String result = del.execute();
        assertEquals(Action.NONE, result);

        response.verify();

        GenericValue scheme = issueSchemeManager.getScheme(issueScheme.getLong("id"));
        assertNull(scheme.getLong("defaultlevel"));
    }
}
