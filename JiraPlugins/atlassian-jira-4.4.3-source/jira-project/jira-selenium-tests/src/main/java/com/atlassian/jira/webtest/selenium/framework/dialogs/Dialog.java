package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.selenium.framework.core.LocalizablePageObject;
import com.atlassian.jira.webtest.selenium.framework.core.PageObject;

/**
 * Represen
 *
 * @since v4.2
 * @deprecated since v4.3. Use {@link com.atlassian.jira.webtest.framework.dialog.Dialog} instead.
 */
@Deprecated
public interface Dialog extends PageObject, LocalizablePageObject
{

    /**
     * <p>
     * Returns full in-dialog jQuery locator for the locator passed in argument. The returned locator
     * will only locate an element on an open dialog.
     *
     * <p>
     * <tt>jqueryLocator</tt> should not contain the 'jquery=' prefix.
     *
     * @param jqueryLocator locator of an element within dialog
     * @return full jQuery locator ready to use with selenium
     */
    String inDialog(String jqueryLocator);

    /**
     * Test if this dialog is open.
     *
     * @return <code>true</code>, if this dialog is open
     */
    boolean isOpen();


    /**
     * Test if the dialog can be opened at the current location.
     *
     * @return <code>true</code>, if opening of the dialog is currently possible
     */
    boolean isOpenable();

    /**
     * Test if this dialog is closed. That is, either {@link #isOpenable()},
     * or {@link #isOpen()} return <code>false<code>.
     *
     * @return <code>true</code>, if this dialog is NOT open (this includes situation where this dialog cannot be opened,
     * se {@link #isOpenable()}.
     */
    boolean isClosed();

    /**
     * Open this dialog if possible.
     *
     * @return this dialog instance
     * @throws IllegalStateException if current state does not allow for the dialog to be opened (e.g. the test is not
     * at correct page - may be queried by means of #isOpenable), or the dialog is already opened (which may be queried by #isOpen).
     */
    Dialog open();

}
