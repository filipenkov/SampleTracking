package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTestSuite;
import com.atlassian.jira.webtest.webdriver.tests.projectconfig.TestSummaryScreensPanel;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Run all the tests for a package
 *
 * @since v4.4
 */
@RunWith (LocalSuiteRunner.class)
public class PackageRunner implements WebTestSuite
{
    @Override
    public String webTestPackage()
    {
        return TestSummaryScreensPanel.class.getPackage().getName();
    }

    @Override
    public Set<Category> includes()
    {
        return EnumSet.of(Category.WEBDRIVER_TEST);
    }

    @Override
    public Set<Category> excludes()
    {
        return Collections.emptySet();
    }
}
