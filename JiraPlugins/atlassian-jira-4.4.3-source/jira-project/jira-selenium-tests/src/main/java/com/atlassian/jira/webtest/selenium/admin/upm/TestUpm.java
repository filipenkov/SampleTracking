package com.atlassian.jira.webtest.selenium.admin.upm;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.driver.admin.plugins.PluginsManagement;
import com.atlassian.jira.webtest.framework.page.admin.CustomFields;
import com.atlassian.jira.webtest.framework.page.admin.plugins.AuditLogTab;
import com.atlassian.jira.webtest.framework.page.admin.plugins.InstallPluginDialog;
import com.atlassian.jira.webtest.framework.page.admin.plugins.InstallPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.JIRAUpgradeCheckPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginModuleComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.UpgradePlugins;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import org.junit.Ignore;

import java.io.File;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

@WebTest({Category.SELENIUM_TEST })
public class TestUpm extends JiraSeleniumTest
{
    private static final String CUSTOM_FIELDS_PLUGIN_KEY = "com.atlassian.jira.plugin.system.customfieldtypes";
    private static final String WIKI_RENDERER_PLUGIN_KEY = "com.atlassian.jira.plugin.system.renderers.wiki.macros";

    private PluginsManagement pluginsManagement;
    private Plugins plugins;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreBlankInstance();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        pluginsManagement = new PluginsManagement(globalPages());
        pluginsManagement.goToPlugins();
        plugins = pluginsManagement.plugins();
    }

    @Override
    protected void onTearDown() throws Exception
    {
        waitFor(1000);
        pluginsManagement.enableSystemPlugin(CUSTOM_FIELDS_PLUGIN_KEY);
        pluginsManagement.enableSystemPlugin(WIKI_RENDERER_PLUGIN_KEY);
        super.onTearDown();
    }

    public void testUpmTabsDisplay()
    {
        // We should always land on the manage existing tab
        assertThat(plugins.pluginTab(ManageExistingPlugins.class).isOpen(), byDefaultTimeout());
        plugins.openTab(InstallPlugins.class);
        assertThat(plugins.pluginTab(InstallPlugins.class).isOpen(), byDefaultTimeout());
        plugins.openTab(UpgradePlugins.class);
        assertThat(plugins.pluginTab(UpgradePlugins.class).isOpen(), byDefaultTimeout());
        plugins.openTab(JIRAUpgradeCheckPlugins.class);
        assertThat(plugins.pluginTab(JIRAUpgradeCheckPlugins.class).isOpen(), byDefaultTimeout());
        plugins.openTab(AuditLogTab.class);
        assertThat(plugins.pluginTab(AuditLogTab.class).isOpen(), byDefaultTimeout());
    }

    public void testDisableAnExistingPlugin()
    {
        pluginsManagement.disableSystemPlugin(CUSTOM_FIELDS_PLUGIN_KEY);
        CustomFields customFields = globalPages().goToAdministration().goToPage(CustomFields.class);
        if (customFields.canAddCustomFields().byDefaultTimeout())
        {
            assertThat(customFields.openAddCustomFields().cascadingSelectAvailable(), isFalse().byDefaultTimeout());
        }
    }


    public void testAuditLog()
    {
        pluginsManagement.disableSystemPlugin(WIKI_RENDERER_PLUGIN_KEY);
        pluginsManagement.goToTab(AuditLogTab.class);
        // TODO rewrite to page objects
        assertThat.textPresentByTimeout("Disabled plugin Wiki Renderer Macros Plugin (com.atlassian.jira.plugin.system.renderers.wiki.macros)", 1000);
        // TODO investigate if this message should show or not. I think not now.
        //assertThat.textPresentByTimeout("System successfully started", 1000);
    }

    public void testPluginsPresent()
    {
        // Bundled Plugins Check
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("jira.webfragments.admin"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.plugin.wiki.contentlinkresolvers"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.plugin.system.customfieldtypes"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.plugin.system.issueoperations"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.plugin.system.issuetabpanels"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("jira.issueviews"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("jira.footer"), byDefaultTimeout());


        // Bundled Plugins 2 Check
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.sal.jira"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.templaterenderer.api"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.templaterenderer.atlassian-template-renderer-velocity1.6-plugin"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.plugins.rest.atlassian-rest-module"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.gadgets.dashboard"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.gadgets.opensocial"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.gadgets.directory"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.gadgets.publisher"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.rest"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.oauth.consumer"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.oauth.consumer"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.oauth.serviceprovider"), byDefaultTimeout());
        // TODO this sucks because plugins generated by the plugins framework have version appended to the plugin key
        // we might want to change the way we test it (e.g. match regular expression), but it's non-trivial with
        // the current page objects design; so let's wait to see how often this will bite us!
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.oauth.atlassian-oauth-service-provider-spi-1.2.2"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.gadgets"), byDefaultTimeout());

    }

    @Ignore ("This test is not yet fully rewritten -- dkordonski & rsmart to tackle")
    public void testLoginGadgetCannotBeDisabled()
    {
        pluginsManagement.goToPlugins().goToTab(ManageExistingPlugins.class);
        ManageExistingPlugins managePlugins = plugins.pluginTab(ManageExistingPlugins.class);
        ManagePluginComponent gadgetsPlugin = managePlugins.showSystemPlugins().systemPlugins().findPluginComponent("com.atlassian.jira.gadgets").byDefaultTimeout();
        if (gadgetsPlugin.isCollapsed().byDefaultTimeout())
        {
            gadgetsPlugin.expand();
        }
        assertThat(gadgetsPlugin.isExpanded(), byDefaultTimeout());
        if (gadgetsPlugin.isModuleListCollapsed().byDefaultTimeout())
        {
            gadgetsPlugin.toggleExpandModulesList();
        }
        ManagePluginModuleComponent loginGadgetModule = gadgetsPlugin.moduleList().findPluginModuleComponent("login-gadget").byDefaultTimeout();
        assertThat(loginGadgetModule.cannotBeDisabled(), byDefaultTimeout());
    }



    // NOTE: if the plugin modules of the calendar plugin change then we will need to update the plugin used by this test
    @Ignore ("it's failing legitimately -- installing not working -- dkordonski & rsmart to tackle")
    public void testUploadPluginThatRequiresRestart()
    {
        // Navigate to the install tab
        InstallPlugins installPlugin = plugins.openTab(InstallPlugins.class);
        assertThat(installPlugin.isOpen(), byDefaultTimeout());
        InstallPluginDialog installDialog = installPlugin.openInstallPluginDialog();
        final File testPluginJarFile = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + File.separator + "jira-calendar-plugin-1.14.1_01.jar");
        installDialog.setFilePath(testPluginJarFile.getAbsolutePath()).submit();
        // client.type("upm-upload-file", testPluginJarFile.getAbsolutePath());
        // NOTE: If you click the button, it seems to be doing a double submit which is really bad.
        // client.keyPress("upm-upload-url", "\\13");
//        client.click("xpath=//button[text()='Upload']");
        assertThat(installDialog.isClosed(), byDefaultTimeout());
        assertThat.textPresentByTimeout("This plugin will be installed when the application is restarted.", 8000);

        // Now lets cancel the installation of the plugin
        assertThat.elementPresentByTimeout("jquery=#upm-requires-restart-show");
        client.click("upm-requires-restart-show");
        assertThat.textPresentByTimeout("Installation of \"JIRA Calendar Plugin\"");
        // Lets make sure there is only one thing we can cancel
        final Number count = client.getXpathCount("//ul[@id='upm-requires-restart-list']//li");
        assertEquals("The UPM has " + count + " plugin installs that can be canceled, should only be one.", 1, count);
        // Lets cancel the install
        client.click("xpath=//ul[@id='upm-requires-restart-list']//li[1]//a[@class='upm-requires-restart-cancel']");
        assertThat.textPresentByTimeout("The installation of plugin \"JIRA Calendar Plugin\" has been cancelled.");

        // Navigate away from the UPM and then back to see that the plugin is not listed
        //getNavigator().gotoPage("secure/admin/ViewPlugins!default.jspa", true);
        //getNavigator().clickAndWaitForPageLoad("upm-admin-link");
        pluginsManagement.goToTab(InstallPlugins.class);
        assertThat.textNotPresent("JIRA Calendar Plugin");
    }

}