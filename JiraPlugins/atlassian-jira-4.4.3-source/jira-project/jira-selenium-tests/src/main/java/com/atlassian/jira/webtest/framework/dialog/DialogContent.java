package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.PageObject;

/**
 * A section of the page that is bound to a dialog.
 *
 * @since v4.3
 */
public interface DialogContent<C extends DialogContent<C, D>, D extends Dialog> extends PageObject, Localizable
{
    /**
     * Returns the dialog that contains this content.
     *
     * @return the dialog that contains this content.
     */
    D dialog();
}
