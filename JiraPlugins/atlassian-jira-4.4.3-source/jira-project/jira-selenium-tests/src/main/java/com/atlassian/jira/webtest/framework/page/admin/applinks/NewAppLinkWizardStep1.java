package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.dialog.DialogContent;

/**
 * Page object for step 1 of the "New Application Link" wizard.
 *
 * @since v4.3
 */
public interface NewAppLinkWizardStep1 extends DialogContent<NewAppLinkWizardStep1, NewAppLinkWizard>
{
    /**
     * Returns the "Server URL" input box.
     *
     * @return a Input for the "Server URL"
     */
    Element serverURL();
}
