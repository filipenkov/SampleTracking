package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestNotificationsUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String USER_FIELD = "Single_User";
    private static final String ADMIN_NOUSERPICKER = "admin_nouserpicker";

    public static Test suite()
    {
        return suiteFor(TestNotificationsUserPicker.class);
    }

    public void testSingleUserField()
    {
        getNavigator().gotoAdmin();
        gotoNotificationsAdduser();
        testSimpleUserPicker(USER_FIELD);
        //submit the form so that we're not left with a half backed form!
        client.click("add_submit");
    }

    public void testNoPermissionSingleUserField()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_NOUSERPICKER, ADMIN_NOUSERPICKER);

        getNavigator().gotoAdmin();
        gotoNotificationsAdduser();
        testNotPermittedSimpleUserPicker(USER_FIELD);
        //submit the form so that we're not left with a half backed form!
        client.click("add_submit");
    }

    private void gotoNotificationsAdduser() {
        client.click("notification_schemes", true);
		client.click("10000_edit", true);
		client.click("add_1", true);
		client.click("label_Single_User", false);
    }

}
