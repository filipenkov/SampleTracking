package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.workflow.condition.AllowOnlyReporter;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class TestAllowOnlyReporter extends AbstractUsersTestCase
{
    private AllowOnlyReporter condition;
    private GenericValue issue;
    private GenericValue project;
    private User bob;
    private User bill;
    private Mock wfc;
    private Mock wfe;
    private Map args;
    private Map transientVars;

    public TestAllowOnlyReporter(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        bob = UtilsForTests.getTestUser("bob");
        bill = UtilsForTests.getTestUser("bill");
        issue = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "project", new Long(2), "workflowId", new Long(100), "reporter", "bob"));
        project = EntityUtils.createValue("Project", EasyMap.build("id", new Long(2)));

        condition = new AllowOnlyReporter();
        wfc = new Mock(WorkflowContext.class);
        wfe = new Mock(WorkflowEntry.class);

        args = EasyMap.build("permission", "create issue");
        transientVars = EasyMap.build("context", wfc.proxy(), "entry", wfe.proxy());
    }

    public void testPermissionConditionOk1() throws WorkflowException
    {
        wfc.setupResult("getCaller", "bob");
        wfe.setupResult("getId", new Long(100));

        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    public void testPermissionConditionOk2()
    {
        wfc.setupResult("getCaller", "bill");
        wfe.setupResult("getId", new Long(100));

        assertFalse(condition.passesCondition(transientVars, args, null));
    }

    public void testPermissionConditionNoIssue()
    {
        wfc.setupResult("getCaller", "bob");
        wfe.setupResult("getId", new Long(101));

        try
        {
            assertFalse(condition.passesCondition(transientVars, args, null));
            fail("No issue; should have thrown a DataAccessException");
        } catch (DataAccessException dae) {}

    }
}
