package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestPermissionsUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String USER_FIELD = "user";
    private static final String ADMIN_NOUSERPICKER = "admin_nouserpicker";

    public static Test suite()
    {
        return suiteFor(TestPermissionsUserPicker.class);
    }
                                                                           
    public void testSingleUserField()
    {
        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);
		client.click("0_edit", true);
		client.click("//b", true);
		testSimpleUserPicker(USER_FIELD);
        client.click("id=add_submit", true);
    }

    public void testNoPermissionSingleUserField()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_NOUSERPICKER, ADMIN_NOUSERPICKER);

        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);
		client.click("0_edit", true);
		client.click("//b", true);
        testNotPermittedSimpleUserPicker(USER_FIELD);
        client.click("id=add_submit", true);
    }

}
