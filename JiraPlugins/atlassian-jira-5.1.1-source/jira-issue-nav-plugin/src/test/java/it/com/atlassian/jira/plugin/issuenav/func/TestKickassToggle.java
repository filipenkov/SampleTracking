package it.com.atlassian.jira.plugin.issuenav.func;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST })
public class TestKickassToggle extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testToggle()
    {
        final String oldNavigatorText = "The Issue Navigator allows you to browse all the issues in the system";

        navigation.issueNavigator().gotoNavigator();
        text.assertTextPresent(oldNavigatorText);
        
        navigation.gotoPage("/secure/TryKickAssAction!default.jspa?enable=true");

        String newNavUrl = "issues";
        assertTrue(tester.getDialog().getResponse().getURL().toString().contains(newNavUrl));
        
        tester.clickLink("find_link");
        text.assertTextNotPresent(oldNavigatorText);
        assertTrue(tester.getDialog().getResponse().getURL().toString().contains(newNavUrl));

        navigation.gotoPage("/secure/TryKickAssAction!default.jspa?enable=disable");

        String oldNavUrl = "secure/IssueNavigator";
        assertTrue(tester.getDialog().getResponse().getURL().toString().contains(oldNavUrl));
        text.assertTextPresent(oldNavigatorText);

        tester.clickLink("find_link");
        text.assertTextPresent(oldNavigatorText);
        assertTrue(tester.getDialog().getResponse().getURL().toString().contains(oldNavUrl));

    }
}
