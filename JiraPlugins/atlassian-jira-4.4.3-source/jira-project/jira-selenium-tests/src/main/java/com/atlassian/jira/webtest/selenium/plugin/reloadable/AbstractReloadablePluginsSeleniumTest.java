package com.atlassian.jira.webtest.selenium.plugin.reloadable;

import com.atlassian.jira.webtest.framework.driver.admin.plugins.PluginsManagement;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;

/**
 * Abstract Selenium test case for re-loadable plugin modules in JIRA.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
public abstract class AbstractReloadablePluginsSeleniumTest extends JiraSeleniumTest
{

    private static final String TEST_XML = "ReloadablePluginModulesDisabled.xml";
    private static final String REFERENCE_PLUGIN_KEY = "com.atlassian.jira.dev.reference-plugin";

    protected PluginsManagement plugins;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreDataWithPluginsReload(TEST_XML);
        this.plugins = new PluginsManagement(globalPages());
        assertReferencePluginDisabled();
    }

    protected final void enableReferencePlugin()
    {
        plugins.enableSystemPlugin(REFERENCE_PLUGIN_KEY);
    }

    protected final void assertReferencePluginEnabled()
    {
        assertTrueByDefaultTimeout(plugins.isSystemPluginEnabled(REFERENCE_PLUGIN_KEY));
    }

    protected final void disableReferencePlugin()
    {
        plugins.disableSystemPlugin(REFERENCE_PLUGIN_KEY);
    }

    protected final void assertReferencePluginDisabled()
    {
        assertTrueByDefaultTimeout(plugins.isSystemPluginDisabled(REFERENCE_PLUGIN_KEY));
    }
}
