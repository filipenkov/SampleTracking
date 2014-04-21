package com.atlassian.jira.webtest.selenium.issue.dialogs;

import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;

/**
 * Base class for AUI dialog tests on the view issue page.
 *
 * @since v4.0
 */
public abstract class AbstractIssueDialogTest extends AbstractAuiDialogTest
{

    private static final String ISSUE_HEADER_SUMMARY = VISIBLE_DIALOG_CONTENT_SELECTOR + " #issue_header_summary";
    private static final String SECURITY_LEVEL_SUGGESTIONS = VISIBLE_DIALOG_CONTENT_SELECTOR + " .security-level .aui-list";
    private static final String ICON_SECURITY = VISIBLE_DIALOG_CONTENT_SELECTOR + " .icon-security";


    protected final void assertFormIsUndecorated () {
        assertThat.elementNotPresent(ISSUE_HEADER_SUMMARY);
    }

    protected final void setCommentLevel(String level) {
        client.click(ICON_SECURITY);
        assertThat.elementPresentByTimeout(SECURITY_LEVEL_SUGGESTIONS);
        client.click(SECURITY_LEVEL_SUGGESTIONS + " ." + level);
    }

}
