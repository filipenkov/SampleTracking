package com.atlassian.jira.webtest.webdriver.tests.picker;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.userpicker.LegacyUserPicker;
import com.atlassian.jira.pageobjects.components.userpicker.UserPickerPopup;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.After;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <p/>
 * Test for legacy user and group picker.
 *
 * <p/>
 * NOTE: @RestoreOnce is used - this test is NOT supposed to manipulate any data in JIRA (e.g. by saving an issue).
 *
 * @since v5.0
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.USERS_AND_GROUPS, Category.CUSTOM_FIELDS })
@RestoreOnce ("xml/TestMultiUserPicker.xml")
public class TestUserAndGroupPickerPopup extends BaseJiraWebTest
{

    @After
    public void cancelCreateIssueDialog()
    {
        pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE).close();
    }

    /**
     * The 'select all' chceckbox used to result in 'Select' option being added to the picker target text field.
     *
     */
    @Test
    public void selectAllOnUserPickerShouldNotResultInAdditionalElementAddedToTarget()
    {
        final CreateIssueDialog createIssue = jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class).getHeader().createIssue();
        final LegacyUserPicker picker = createIssue.getCustomField("customfield_10010", LegacyUserPicker.class);
        final UserPickerPopup popup = picker.openPopup();
        assertTrue(popup.isMultiselect());
        popup.multiSelect().selectAll().submitSelect();
        waitUntilTrue(popup.isClosed());
        assertEquals("0, 1, 2, 3, 4, 5, 6, 7, 8, 9, admin, fred, user'with\"quotes", picker.getPickerValue());
    }

    // TODO moar tests!
    // 1) same as above, for group picker
    // 2) rewrite TestPopupUserPicker
}
