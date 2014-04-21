package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.core.Cancelable;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.Submittable;


/**
 * Dialog with the ability of being submitted and closed (cancelled). In practice most of the dialogs in JIRA is.
 *
 * @since v4.3
 */
public interface SubmittableDialog<T extends SubmittableDialog<T,P>,P extends PageObject> extends Dialog<T>, Submittable<P>, Cancelable<P>
{
}
