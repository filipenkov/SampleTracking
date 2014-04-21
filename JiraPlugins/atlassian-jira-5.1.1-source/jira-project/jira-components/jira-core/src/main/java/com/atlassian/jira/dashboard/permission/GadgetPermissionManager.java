package com.atlassian.jira.dashboard.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;

/**
 * Permission Manager to decide if a gadget should be shown to a user or not. Also responsible for filtering out gadgets
 * from a dashboardState that shouldn't be shown.
 *
 * @since v4.0
 */
public interface GadgetPermissionManager
{
    /**
     * Decides if a user has permission to view a particular gadget.
     *
     * @param pluginGadgetSpec The gadget spec to check permissions for
     * @param remoteUser The user viewing the dashboard.  May be null for anonymous users
     * @return Vote.DENY if the user doesn't have permission.  Vote.ALLOW otherwise
     */
    Vote voteOn(final PluginGadgetSpec pluginGadgetSpec, final User remoteUser);

    /**
     * Removes any gadgets a user doesn't have permission to see in the passed in dashboard state.  This will only be
     * done for dashboards where the user has read only permission.  If the user has write permission, the gadget will
     * be left in place, and the dashboard plugin will render a place holder with an appropriate error message.  The
     * user can the remove the gadget him/herself.
     *
     * @param dashboardState The dashboard state to filter
     * @param remoteUser The user viewing the dashboard.  May be null for anonymous users
     * @return A filtered dashboardstate with any offending gadgets removed.
     */
    DashboardState filterGadgets(final DashboardState dashboardState, final User remoteUser);

    /**
     * Decides if a user has permission to view a particular gadget.
     *
     * @param completeGadgetKey The plugin key to check permissions for
     * @param remoteUser The user viewing the dashboard.  May be null for anonymous users
     * @return Vote.DENY if the user doesn't have permission.  Vote.ALLOW otherwise
     */
    Vote voteOn(String completeGadgetKey, User remoteUser);

    /**
     * Used to extract/convert a url to a plugin key.
     *
     * @param gadgetUri The url to convert
     * @return The plugin key
     */
    String extractModuleKey(String gadgetUri);
}
