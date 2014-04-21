package com.atlassian.jira.webtest.framework.page.dashboard;

import com.atlassian.jira.webtest.framework.page.ChildPage;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Represents the 'Create New Dashboard' page.
 *
 * @since v4.3
 */
public interface CreateNewDashboard extends ChildPage<Dashboard>
{

    /**
     * Type in dashboard name.
     *
     * @param name name of the Dashboard
     * @return this page instance
     */
    CreateNewDashboard name(KeySequence name);

    /**
     * Type in dashboard description.
     *
     * @param description description of the Dashboard
     * @return this page instance
     */
    CreateNewDashboard description(KeySequence description);

    // TODO other stuff

    /**
     * Click on the submit button to add new Dashboard.
     *
     * @return dashboard parent page
     */
    Dashboard submitAdd();
}
