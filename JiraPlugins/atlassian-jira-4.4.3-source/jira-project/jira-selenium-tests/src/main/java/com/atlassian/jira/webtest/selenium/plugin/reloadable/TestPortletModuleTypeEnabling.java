package com.atlassian.jira.webtest.selenium.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.dialog.AddGadgetDialog;
import com.atlassian.jira.webtest.framework.gadget.ReferencePortlet;
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
 * Test that the 'portlet' plugin module type behaves correctly when going from 'never enabled'
 * to enabled state. Also referred to as 'ZERO to ON scenario'.
 *
 * <p>
 * This boils down to testing if the reference portlet is available as 'legacy' gadget after enabling the
 * reference plugin.
 *
 * @since v4.3
 */

@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestPortletModuleTypeEnabling extends AbstractReloadablePluginsSeleniumTest
{

    private Dashboard dashboard;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        dashboard = globalPages().dashboard();
    }

    public void testReferencePortletCannotBeAddedWhenReferencePluginDisabled()
    {
        copyDashboard();
        AddGadgetDialog addGadgetDialog = dashboard.openGadgetDialog();
        assertTrueByDefaultTimeout(addGadgetDialog.isOpen());
        assertFalseByDefaultTimeout(addGadgetDialog.canAddGadget(ReferencePortlet.class));
    }

    public void testReferencePortletWorksWhenReferencePluginEnabled()
    {
        enableReferencePlugin();
        copyDashboard();
        addReferencePortlet();
        assertTrueByDefaultTimeout(dashboard.defaultTab().hasGadget(ReferencePortlet.class));
        ReferencePortlet refPortlet = dashboard.defaultTab().gadget(ReferencePortlet.class);
        assertTrueByDefaultTimeout(refPortlet.isReady());
        assertThat(refPortlet.currentUserInfo(), isEqual("Current logged in user: Administrator").byDefaultTimeout());
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

    private void addReferencePortlet()
    {
        AddGadgetDialog addGadgetDialog = dashboard.openGadgetDialog();
        assertTrueByDefaultTimeout(addGadgetDialog.isOpen());
        assertTrueByDefaultTimeout(addGadgetDialog.canAddGadget(ReferencePortlet.class));
        assertTrueByDefaultTimeout(addGadgetDialog.addGadget(ReferencePortlet.class).canAddGadget(ReferencePortlet.class));
        addGadgetDialog.close().byClickInFinished();
    }

}
