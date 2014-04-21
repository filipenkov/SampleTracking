package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.SUB_TASKS })
public class TestCreateSubTasksContextPermission extends JIRAWebTest
{
    public TestCreateSubTasksContextPermission(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestCreateSubTaskContextPermission.xml");
    }

    public void testCreateSubTasks()
    {
        createSubTaskStep1("HSP-1", "Sub-task");
        setFormElement("summary", "Subby 99");
        submit();

        assertTextPresent("Subby 99");
        assertTextPresent("HSP-1");
    }
}
