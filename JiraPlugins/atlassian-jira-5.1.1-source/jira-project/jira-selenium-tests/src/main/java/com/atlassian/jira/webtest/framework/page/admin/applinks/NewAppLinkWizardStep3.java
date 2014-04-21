package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.dialog.DialogContent;

/**
 * Page object for step 3 of the "New Application Link" wizard.
 *
 * @since v4.3
 */
public interface NewAppLinkWizardStep3 extends DialogContent<NewAppLinkWizardStep3, NewAppLinkWizard>
{
    /**
     * Returns a reference to the radio button, which configures if the two applications share the same userbase.
     * @return a reference.
     */
    Input haveTheSameUserbase();

    /**
     * Returns a reference to the radio button, which configures if the two applications trust each other.
     * @return a reference.
     */
    Input trustEachOther();
    

}
