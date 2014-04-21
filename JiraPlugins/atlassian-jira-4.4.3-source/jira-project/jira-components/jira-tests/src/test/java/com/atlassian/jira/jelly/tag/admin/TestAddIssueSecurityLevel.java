package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.easymock.MockControl;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestAddIssueSecurityLevel extends AbstractJellyTestCase
{
    private User u;
    private Group g;

    private MockControl mockIssueSecuritySchemeManagerControl;
    private MockControl mockIssueSecurityLevelManagerControl;

    protected void setUp() throws Exception
    {
        super.setUp();
        //Create user and place in the action context
        u = UtilsForTests.getTestUser("AddIssueSecurityLevel-in-user");
        g = UtilsForTests.getTestGroup("admin-group");
        u.addToGroup(g);

        MockGenericValue mockScheme = new MockGenericValue("IssueSecurityScheme", EasyMap.build("id", new Long(12345)));
        JiraTestUtil.loginUser(u);
        mockIssueSecuritySchemeManagerControl = MockControl.createStrictControl(IssueSecuritySchemeManager.class);
        final IssueSecuritySchemeManager mockIssueSecuritySchemeManager = (IssueSecuritySchemeManager) mockIssueSecuritySchemeManagerControl.getMock();
        mockIssueSecuritySchemeManager.getSchemeObject("DUDE");
        mockIssueSecuritySchemeManagerControl.setReturnValue(null);
        mockIssueSecuritySchemeManager.getSchemeObjects();
        mockIssueSecuritySchemeManagerControl.setReturnValue(Collections.EMPTY_LIST);
        mockIssueSecuritySchemeManager.createScheme("DUDE", null);
        mockIssueSecuritySchemeManagerControl.setReturnValue(mockScheme);
        mockIssueSecuritySchemeManagerControl.replay();

        MockGenericValue mockIssueSecLevel = new MockGenericValue("IssueSecurityLevel", EasyMap.build("name", "seclevel", "id", new Long(54321)));
        mockIssueSecurityLevelManagerControl = MockControl.createStrictControl(IssueSecurityLevelManager.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = (IssueSecurityLevelManager) mockIssueSecurityLevelManagerControl.getMock();
        mockIssueSecurityLevelManager.getSchemeIssueSecurityLevels(12345L);
        mockIssueSecurityLevelManagerControl.setReturnValue(EasyList.build(mockIssueSecLevel));
        mockIssueSecurityLevelManagerControl.replay();

        ManagerFactory.addService(IssueSecuritySchemeManager.class, mockIssueSecuritySchemeManager);
        ManagerFactory.addService(IssueSecurityLevelManager.class, mockIssueSecurityLevelManager);
    }

    protected void tearDown() throws Exception
    {
        mockIssueSecuritySchemeManagerControl.verify();
        mockIssueSecurityLevelManagerControl.verify();

        ManagerFactory.addService(IssueSecuritySchemeManager.class, null);
        ManagerFactory.addService(IssueSecurityLevelManager.class, null);
        CoreFactory.getGenericDelegator().removeByAnd("SchemeIssueSecurityLevels", EasyMap.build());
        super.tearDown();
    }

    public TestAddIssueSecurityLevel(String s)
    {
        super(s);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }

    public void testSecurityLevelAddedWithCorrectDescription() throws Exception
    {
        runScriptAndAssertTextResultEquals(null, "add-issue-security-level.test.description.add.jelly");

        final List values = CoreFactory.getGenericDelegator().findByAnd("SchemeIssueSecurityLevels", EasyMap.build("name", "seclevel", "description", "Defines visibility"));
        assertEquals(1, values.size());
    }

}
