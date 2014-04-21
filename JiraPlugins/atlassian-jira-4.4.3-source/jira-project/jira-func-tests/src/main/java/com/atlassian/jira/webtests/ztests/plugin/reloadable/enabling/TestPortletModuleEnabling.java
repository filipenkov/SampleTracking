package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_PORTLET_KEY;

/**
 * <p>
 * Test that the portlet (legacy gadget) module type behaves correctly when going from 'never enabled'
 * to 'enabled' state. Also referred to as 'ZERO to ON scenario'.
 *
 * <p>
 * This is tested by executing a 'RunPortlet' action with reference portlet key.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN})
public class TestPortletModuleEnabling extends AbstractReloadablePluginsTest
{
    public void testPortletShouldNotBeAvailableGivenReferencePluginDisabled() throws Exception
    {
        runPortlet(REFERENCE_PORTLET_KEY);
        text.assertTextPresent("A gadget with the key " + REFERENCE_PORTLET_KEY + " does not exist.");
    }

    public void testPortletShouldBeAvailableGivenReferencePluginEnabled() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        runPortlet(REFERENCE_PORTLET_KEY);
        text.assertTextPresent(locator.id("userinfo"), "Current logged in user: Administrator");
    }

    private void runPortlet(String portletKey)
    {
        tester.gotoPage("/RunPortlet.jspa?portletKey=" + portletKey);
    }
}
