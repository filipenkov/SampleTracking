package com.atlassian.gadgets.dashboard.view;

import javax.annotation.Nullable;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardTab;
import com.atlassian.gadgets.view.ViewComponent;

/**
 * A factory which provides a way to create {@link ViewComponent}s for dashboards displaying other dashboards available
 * as tabs. A host application should use this for embedding dashboards in the desired locations by creating a view and
 * then calling the {@link ViewComponent#writeTo} method.
 */
public interface DashboardTabViewFactory
{

    /**
     * <p>Returns a {@code ViewComponent} that will render the {@code DashboardState} and {@code DashboardTab}s
     * customizing the view based on the permissions the user has to view/edit the dashboard and the
     * specified {@code Locale}.</p>
     * <p/>
     * <p>This can be used by host applications to place dashboards in a place of their choosing, including tabs
     * to navigate to other dashboards.</p>
     *
     * @param tabs a list of dashboards a particular user can see. This can be empty if no tabs are desired.
     * @param selectedTabDashboardState state of the dashboard to be rendered by the {@code ViewComponent}
     * @param username user that is viewing the dashboard and whose permissions will be checked
     * @param maxGadgets the (application-wide) maximum number of supported gadgets
     * @param gadgetRequestContext the context of this request
     * @return a {@code ViewComponent} that will render the {@code DashboardState}
     */
    ViewComponent createDashboardView(Iterable<DashboardTab> tabs, DashboardState selectedTabDashboardState, @Nullable String username,
            int maxGadgets, GadgetRequestContext gadgetRequestContext);
}
