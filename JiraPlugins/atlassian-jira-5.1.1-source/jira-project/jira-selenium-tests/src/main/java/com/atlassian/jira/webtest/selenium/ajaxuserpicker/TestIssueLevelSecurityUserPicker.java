package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestIssueLevelSecurityUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String USER_FIELD = "user";
    private static final String ADMIN_NOUSERPICKER = "admin_nouserpicker";

    public static Test suite()
    {
        return suiteFor(TestIssueLevelSecurityUserPicker.class);
    }

    public void testSingleUserField()
    {
        getNavigator().gotoAdmin();
        gotoISLSAdduser();
        testSimpleUserPicker(USER_FIELD);
        //submit the form so that we're not left with a half baked form!
        client.click("add_submit");
    }

    public void testNoPermissionSingleUserField()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_NOUSERPICKER, ADMIN_NOUSERPICKER);

        getNavigator().gotoAdmin();
        gotoISLSAdduser();
        testNotPermittedSimpleUserPicker(USER_FIELD);
        //submit the form so that we're not left with a half baked form!
        client.click("add_submit");
    }

    private void gotoISLSAdduser()
    {
        client.click("security_schemes", true);
		client.click("link=ISS1", true);
		client.click("add_Level1", true);
		client.click("user_id");
    }
}
