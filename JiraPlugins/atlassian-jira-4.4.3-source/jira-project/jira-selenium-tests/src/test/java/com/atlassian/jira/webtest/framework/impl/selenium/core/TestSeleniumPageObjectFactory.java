package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.page.AdministrationPage;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;
import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.SeleniumConfiguration;
import com.atlassian.selenium.mock.MockSeleniumClient;
import com.atlassian.selenium.mock.MockSeleniumConfiguration;
import junit.framework.TestCase;

/**
 * Test case for {@link com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumPageObjectFactory}.
 *
 * @since v4.3
 */
public class TestSeleniumPageObjectFactory extends TestCase
{

    private SeleniumPageObjectFactory tested;
    private SeleniumClient mockClient = new MockSeleniumClient();
    private SeleniumConfiguration mockConfig = new MockSeleniumConfiguration();
    private SeleniumContext context = new SeleniumContext(mockClient, mockConfig);

    @Override
    protected void setUp() throws Exception
    {
        tested = new SeleniumPageObjectFactory(context);
    }

    public void testCreateAdminPage()
    {
        assertNotNull(tested.createGlobalPage(AdministrationPage.class));
    }

    public void testCreateIssueNavigator()
    {
        assertNotNull(tested.createGlobalPage(IssueNavigator.class));
    }
}
