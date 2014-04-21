package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.page.admin.AdminPage;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;


/**
 * Representation of the main administration page.
 *
 * @since v4.3
 */
public interface AdministrationPage extends GlobalPage<AdministrationPage>
{

    /**
     * Go to a given administration page 
     *
     * @param pageType identifies the target admin page
     * @param <T> identifies the target admin page
     * @return instance of <tt>T</tt> representing the target administration page
     */
    <T extends AdminPage> T goToPage(Class<T> pageType);

    /**
     * Leaves the administration area and goes back to the "normal" dashboard view with the issue navigator
     * and the projets view.
     *
     * @return instance of the Dashboard page object.
     */
    Dashboard backToJira();
}
