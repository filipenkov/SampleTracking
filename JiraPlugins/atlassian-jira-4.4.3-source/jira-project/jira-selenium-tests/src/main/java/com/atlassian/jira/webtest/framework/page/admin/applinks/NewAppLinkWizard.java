package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.dialog.PageDialog;

/**
 * Represents the "New Application Link" dialog, which contains a wizard. This interface allows you to test which step
 * of the wizard is currently active, as well as allowing you to navigate the wizard and test for error and warning
 * messages.
 *
 * @since v4.3
 */
public interface NewAppLinkWizard extends PageDialog<NewAppLinkWizard, AppLinksAdminPage>
{
    /**
     * Returns a page object for step 1 of the wizard.
     *
     * @return a NewAppLinkWizardStep1
     */
    NewAppLinkWizardStep1 step1();

    /**
     * Returns a page object for step 2 of the wizard.
     *
     * @return a NewAppLinkWizardStep2
     */
    NewAppLinkWizardStep2 step2();

    /**
     * Returns a page object for step 3 of the wizard.
     *
     * @return a NewAppLinkWizardStep3
     */
    NewAppLinkWizardStep3 step3();

    /**
     * Clicks the "Cancel" link on the wizard.
     *
     * @return a NewAppLinkWizard
     */
    NewAppLinkWizard clickCancel();

    /**
     * Clicks the "Next" button on the wizard.
     *
     * @return a NewAppLinkWizard
     */
    NewAppLinkWizard clickNext();

    /**
     * Click the "Submit" button on the wizard.
     *
     * @return a NewAppLinkWizard
     */
    NewAppLinkWizard clickSubmit();

    /**
     * Returns the title that is in the wizard (i.e. step title).
     *
     * @return a String containing the title
     */
    String title();

    /**
     * Returns a TimedCondition that can be used to test if the wizard contains a given error message.
     *
     * @param errorMessage a String containing an error message
     * @return a TimedCondition
     */
    TimedCondition hasErrorMessage(String errorMessage);

    /**
     * Returns a TimedCondition that can be used to test if the wizard contains a given warning message.
     *
     * @param warnMessage a String containing a warning message
     * @return a TimedCondition
     */
    TimedCondition hasWarning(String warnMessage);
}
