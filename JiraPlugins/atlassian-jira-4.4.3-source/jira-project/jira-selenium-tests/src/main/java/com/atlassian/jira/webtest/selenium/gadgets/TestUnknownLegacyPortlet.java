package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestUnknownLegacyPortlet extends GadgetTest
{
    @Override
    public void onSetUp()
    {
        internalSetup();
        restoreData("TestUnknownLegacyPortlet.xml");
    }

    public void testUnknownLegacyPortlet() throws Exception
    {
        getNavigator().logout(getXsrfToken()).gotoHome();
        
        selectGadget("Unknown Legacy Portlet");

        client.clickElementWithClass("renderer-error-toggle");
        assertThat.textPresentByTimeout("portletkey:bad", 4000);
    }
}
