package com.atlassian.jira.webtest.framework.page.dashboard;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;

/**
 * Tools menu on the dashboard. This menu does not have any sections, all items are appended directly to the dropdown.
 * Thus section related methods will return empty collections and <code>null</code>s.
 *
 * @since v4.3
 */
public interface DashboardToolsMenu extends AjsDropdown<Dashboard>
{

    public static enum ToolItems
    {
        COPY_DASHBOARD("copy_dashboard"),
        EDIT_DASHBOARD("edit_dashboard"),
        SHARE_DASHBOARD("share_dashboard"),
        DELETE_DASHBOARD("delete_dashboard"),
        FIND_DASHBOARDS("find"),
        CREATE_DASHBOARD("create_dashboard");

        private final String id;

        ToolItems(String id)
        {
            this.id = id;
        }

        public String id()
        {
            return id;
        }
    }

    public interface CloseMode extends AjsDropdown.CloseMode<Dashboard>
    {
        /**
         * Close the drop-down by clicking in position corresponding to the <tt>item</tt> representation.
         *
         * @param item domain representation of the position in the list to click in.
         * @return dashboard parent page
         */
        Dashboard byClickIn(ToolItems item);
    }

    /**
     * Find item of the tools menu.
     *
     * @param item item representation
     * @return item instance
     */
    Item<Dashboard> toolItem(ToolItems item);


    /**
     * Extended close mechanism.
     *
     * @return close mode of this tools menu
     * @see com.atlassian.jira.webtest.framework.component.AjsDropdown#close()
     * @see AjsDropdown.CloseMode
     */
    CloseMode close();
}
