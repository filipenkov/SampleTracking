package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.page.admin.SeleniumGeneralConfiguration;
import com.atlassian.jira.webtest.framework.impl.selenium.page.admin.SeleniumViewScreens;
import com.atlassian.jira.webtest.framework.page.admin.ViewGeneralConfiguration;
import com.atlassian.jira.webtest.framework.page.admin.ViewScreens;
import com.atlassian.selenium.mock.MockSeleniumClient;
import com.atlassian.selenium.mock.MockSeleniumConfiguration;
import junit.framework.TestCase;

/**
 * test case for {@link com.atlassian.jira.webtest.framework.impl.selenium.page.SeleniumAdministrationPage}.
 *
 * @since v4.3
 */
public class TestSeleniumAdministrationPage extends TestCase
{
    private MockSeleniumClient mockClient = new MockSeleniumClient();
    private SeleniumContext context = new SeleniumContext(mockClient, new MockSeleniumConfiguration().conditionInterval(100));
    private SeleniumAdministrationPage tested = new SeleniumAdministrationPage(context);

    @Override
    protected void setUp() throws Exception
    {
    }

    public void testCreateAdminPage()
    {
        mockClient.addPresentElements("id=field_screens", "id=general_configuration");
        assertTrue(tested.goToPage(ViewScreens.class) instanceof SeleniumViewScreens);
        assertTrue(tested.goToPage(ViewGeneralConfiguration.class) instanceof SeleniumGeneralConfiguration);
    }

    public void testGlobalLinkLocator()
    {
        assertEquals("id=admin_link", tested.linkLocator().fullLocator());
    }

    public void testPageDetector()
    {
        assertEquals("css=a#leave_admin", tested.detector().fullLocator());
    }
}
