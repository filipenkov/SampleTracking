package com.atlassian.jira.webtest.selenium.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.dialog.AddGadgetDialog;
import com.atlassian.jira.webtest.framework.gadget.ReferenceGadget;
import com.atlassian.jira.webtest.framework.page.dashboard.CreateNewDashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardToolsMenu;
import com.atlassian.webtest.ui.keys.TypeMode;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertFalseByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.QueryAssertions.isEqual;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;

/**
 * <p>
 * Test that the 'gadget' plugin module type behaves correctly when going from 'never enabled'
 * to enabled state. Also referred to as 'ZERO to ON scenario'.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestGadgetModuleTypeEnabling extends AbstractReloadablePluginsSeleniumTest
{

    private Dashboard dashboard;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        dashboard = globalPages().dashboard();
    }

    public void testReferenceGadgetCannotBeAddedWhenDisabled()
    {
        copyDashboard();
        AddGadgetDialog addGadgetDialog = dashboard.openGadgetDialog();
        assertTrueByDefaultTimeout(addGadgetDialog.isOpen());
        assertFalseByDefaultTimeout(addGadgetDialog.canAddGadget(ReferenceGadget.class));
    }

    public void testReferenceGadgetWorksWhenReferencePluginEnabled()
    {
        enableReferencePlugin();
        copyDashboard();
        addReferenceGadget();
        assertTrueByDefaultTimeout(dashboard.defaultTab().hasGadget(ReferenceGadget.class));
        ReferenceGadget refGadget = dashboard.defaultTab().gadget(ReferenceGadget.class);
        assertTrueByDefaultTimeout(refGadget.isReady());
        assertThat(refGadget.referenceEndpointResponse(), isEqual("{\"undefined\":{\"endpoint\":false}}").byDefaultTimeout());
    }

    private void copyDashboard()
    {
        assertTrueByDefaultTimeout(dashboard.goTo().isAt());
        assertTrueByDefaultTimeout(dashboard.toolsMenu().open().isOpen());
        dashboard.toolsMenu().close().byClickIn(DashboardToolsMenu.ToolItems.COPY_DASHBOARD);
        context().getPageObject(CreateNewDashboard.class).name(charsBuilder("test").typeMode(TypeMode.INSERT).build()).submitAdd();
        assertTrueByDefaultTimeout(dashboard.isAt());
        assertTrueByDefaultTimeout(dashboard.hasDefaultTab());
        assertTrueByDefaultTimeout(dashboard.defaultTab().isOpen());
    }

    private void addReferenceGadget()
    {
        AddGadgetDialog addGadgetDialog = dashboard.openGadgetDialog();
        assertTrueByDefaultTimeout(addGadgetDialog.isOpen());
        assertTrueByDefaultTimeout(addGadgetDialog.canAddGadget(ReferenceGadget.class));
        assertTrueByDefaultTimeout(addGadgetDialog.addGadget(ReferenceGadget.class).canAddGadget(ReferenceGadget.class));
        addGadgetDialog.close().byClickInFinished();
    }

}
