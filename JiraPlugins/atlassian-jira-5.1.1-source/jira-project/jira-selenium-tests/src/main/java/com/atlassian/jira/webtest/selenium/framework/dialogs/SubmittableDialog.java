package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.selenium.framework.core.Cancelable;
import com.atlassian.jira.webtest.selenium.framework.core.Submittable;

/**
 * Dialog with the ability of being submitted and closed (cancelled). In practice most of the dialogs in JIRA is.
 *
 * @since v4.2
 */
public interface SubmittableDialog extends Dialog, Submittable<SubmittableDialog>, Cancelable<SubmittableDialog>
{

    /**
     * Cancel button locator within the dialog.
     *
     * @return cancel button locator
     */
    String cancelTriggerLocator();

    /**
     * Submit button locator within the dialog.
     *
     * @return submit button locator
     */
    String submitTriggerLocator();
}
