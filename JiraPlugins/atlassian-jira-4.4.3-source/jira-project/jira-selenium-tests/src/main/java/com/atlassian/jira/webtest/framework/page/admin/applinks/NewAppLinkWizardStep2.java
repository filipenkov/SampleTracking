package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.component.Checkbox;
import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.dialog.DialogContent;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Page object for step 2 of the "New Application Link" wizard.
 *
 * @since v4.3
 */
public interface NewAppLinkWizardStep2 extends DialogContent<NewAppLinkWizardStep2, NewAppLinkWizard>
{
    /**
     * Types a key sequence into the "Application Name" input box.
     *
     * @param applicationName a KeySequence to type
     * @return a NewAppLinkWizardStep2
     */
    NewAppLinkWizardStep2 insertApplicationName(KeySequence applicationName);

    /**
     * Selects an application type from the "Application Type" option list.
     *
     * @param applicationType an ApplicationType
     * @return a NewAppLinkWizardStep2
     */
    NewAppLinkWizardStep2 selectApplicationType(ApplicationType applicationType);

    /**
     * Returns the reciprocal RPC URL that will be used to create a back link.
     *
     * @return a String containing the reciprocal RPC URL that will be used to create a back link
     */
    Input getReciprocalURL();

    /**
     * Enters the username used to create the reciprocal link in the remote applicaiton.
     *
     * @param username the name of the user
     * @return NewAppLinkWizardStep2
     */
    NewAppLinkWizardStep2 enterUsername(String username);

    /**
     * The password of the user to use to create the reciprocal link.
     *
     * @param password the password of the user.
     * @return a NewAppLinkWizardStep2
     */
    NewAppLinkWizardStep2 enterPassword(String password);

    /**
     * The RPC Url used by the remote application to connect back to this server.
     * 
     * @param rpcUrl the rpc URL
     * @return a NewAppLinkWizardStep2
     */
    NewAppLinkWizardStep2 enterRpcUrl(String rpcUrl);

    /**
     * Returns the create reciprocal link checkbox.
     *
     * @return a Checkbox
     */
    Checkbox createReciprocalLink();

    /**
     * The supported application types.
     */
    public enum ApplicationType
    {
        JIRA,
    }
}
