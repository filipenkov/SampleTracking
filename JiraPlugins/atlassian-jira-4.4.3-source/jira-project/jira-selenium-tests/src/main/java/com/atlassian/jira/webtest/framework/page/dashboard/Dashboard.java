package com.atlassian.jira.webtest.framework.page.dashboard;

import com.atlassian.jira.webtest.framework.component.tab.NamedTabContainer;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.AddGadgetDialog;
import com.atlassian.jira.webtest.framework.page.GlobalPage;

/**
 * Represents JIRA Dashboard page. Dashboard is the default JIRA page that contains a number of gadgets.
 *
 * @since v4.3
 * @see com.atlassian.jira.webtest.framework.gadget.Gadget
 */
public interface Dashboard extends GlobalPage<Dashboard>, NamedTabContainer<DashboardTab>
{

    /**
     * Open and return gadget dialog.
     *
     * @return add gadget dialog instance
     */
    AddGadgetDialog openGadgetDialog();

    /**
     * Return instance of gadget dialog associated with this page. This does not perform any operations on the dialog.
     *
     * @return add gadget dialog instance
     */
    AddGadgetDialog gadgetDialog();

    /**
     * Return tools menu associated with this dashboard page.
     *
     * @return dashboard tools menu instance
     */
    DashboardToolsMenu toolsMenu();

    /**
     * <p>
     * If this dashboard has only one tab, the 'tabs' side menu is not accessible and the methods from
     * {@link com.atlassian.jira.webtest.framework.component.tab.NamedTabContainer} will throw
     * {@link IllegalStateException}.
     *
     * <p>
     * If this condition returns <code>true</code>, use {@link #defaultTab()} to access the default tab.
     *
     * @return timed condition checking if this dashboard has only one default tab.
     */
    TimedCondition hasDefaultTab();

    /**
     * Default dashboard tab. If {@link #hasDefaultTab()} will return <code>false<code>, this method may
     * throw {@link IllegalStateException}
     *
     * @return default tab of this dashboard
     * @throws IllegalStateException if this dashboard has more than one dashboard tabs (non-default mode)
     */
    DashboardTab defaultTab();

    // TODO there is also a case where there are no dashboards at all - should expose check for that case as well
}
