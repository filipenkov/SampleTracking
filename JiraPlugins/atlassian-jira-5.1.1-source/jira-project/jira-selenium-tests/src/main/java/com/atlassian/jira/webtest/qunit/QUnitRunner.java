package com.atlassian.jira.webtest.qunit;

import com.atlassian.aui.test.runner.QUnitSeleniumHelper;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.SeleniumClient;

import java.io.File;


/**
 * Runs and reports on the QUnit tests at /qunit
 */
@WebTest(Category.QUNIT)
public class QUnitRunner extends JiraSeleniumTest
{
    private File testOutputDir;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        testOutputDir = QUnitHelper.setUpOutputLocation();
        restoreBlankInstance();
    }

    public void testAll() throws Exception
    {
        final SeleniumClient client = getSeleniumClient();
        final QUnitSeleniumHelper seleniumHelper = new QUnitSeleniumHelper(client, assertThat);
        QUnitHelper.runAllTests(getNavigator(), seleniumHelper, testOutputDir);
    }
}
