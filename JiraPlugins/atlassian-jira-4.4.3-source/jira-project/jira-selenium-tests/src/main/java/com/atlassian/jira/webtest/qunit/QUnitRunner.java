package com.atlassian.jira.webtest.qunit;

import com.atlassian.aui.test.runner.QUnitSeleniumHelper;
import com.atlassian.aui.test.runner.QUnitTestResult;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.SeleniumClient;
import org.apache.commons.lang.StringUtils;

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
        String location = System.getProperty("jira.qunit.testoutput.location");
        if (StringUtils.isEmpty(location))
        {
            System.err.println("Writing result XML to tmp, jira.qunit.testoutput.location not defined");
            location = System.getProperty("java.io.tmpdir");
        }
        testOutputDir = new File(location);

        restoreBlankInstance();
    }

    public void testAll() throws Exception
    {
        getNavigator().gotoPage("qunit/", true);
        final SeleniumClient client = getSeleniumClient();
        QUnitSeleniumHelper helper = new QUnitSeleniumHelper(client, assertThat);

        for (String href : helper.findAllTestLinksOnFrontPage())
        {
            final QUnitTestResult results = helper.runTestsAtUrl(href);
            results.write(testOutputDir);
        }
    }

}
