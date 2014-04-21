package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.dialog.PageDialog;

/**
 * Delete application link confirmation dialog.
 *
 * @since v4.3
 */
public interface DeleteApplicationLink extends PageDialog<DeleteApplicationLink, AppLinksAdminPage>
{
    /**
     * Clicks the "Confirm" button.
     *
     * @return this
     */
    DeleteApplicationLink clickConfirm();

    /**
     * Clicks the "Cancel" link.
     *
     * @return this
     */
    DeleteApplicationLink clickCancel();
}
