package com.atlassian.jira.webtest.selenium.auidialog.common;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.ContainsValueCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.HasClassCondition;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.components.CalendarPopup;
import com.atlassian.jira.webtest.selenium.framework.dialogs.IssueActionDialog;
import com.atlassian.jira.webtest.selenium.framework.dialogs.WorkflowTransitionDialog;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;
import com.atlassian.selenium.keyboard.SeleniumTypeWriter;
import com.atlassian.webtest.ui.keys.KeyEventType;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import com.atlassian.webtest.ui.keys.TypeMode;
import junit.framework.Test;
import org.apache.commons.lang.ArrayUtils;

import java.awt.event.KeyEvent;
import java.util.Arrays;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;


/**
 * Test correct behaviour of custom fields in the workflow dialogs.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestCustomFieldsInDialogs extends AbstractAuiDialogTest
{
    public static Test suite()
    {
        return suiteFor(TestCustomFieldsInDialogs.class);
    }

    // TODO use dialog page object, get rid of AbstractAuiDailogTest base class

    private static final String CASCADING_SELECT_CF_PARENT_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " #customfield_10020";
    private static final String CASCADING_SELECT_CF_CHILD_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " .cascadingselect-child";

    private static final String DATE_PICKER_CF_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " #customfield_10022";

    private static final String DATE_TIME_PICKER_CF_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " #customfield_10021";

    private static final String USER_PICKER_CONTAINER_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " #customfield_10010_container";
    private static final String USER_PICKER_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " #customfield_10010";

    private static final String USER_PICKER_DROPDOWN_LOCATOR = USER_PICKER_CONTAINER_LOCATOR + " div.suggestions";


    private static String customFieldCalendarTrigger(String calendarInput)
    {
        return calendarInput + "-trigger";
    }


    private IssueActionDialog[] workflowDialogs;


    @Override
    public void onSetUp()
    {
        super.onSetUp();
        initWorkflowDialogs();
        restoreData("TestCustomFieldsInDialogs.xml");
    }

    private void initWorkflowDialogs()
    {
        workflowDialogs = new IssueActionDialog[] {
            newWorkflowTransitionDialog(WorkflowTransition.RESOLVE),
            newWorkflowTransitionDialog(WorkflowTransition.CLOSE)
        };
    }

    private IssueActionDialog newWorkflowTransitionDialog(WorkflowTransition action)
    {
        return new WorkflowTransitionDialog(context(), action);
    }

    public void testFromViewIssue()
    {
        getNavigator().gotoIssue("HSP-1");
        for (IssueActionDialog dialog : workflowDialogs)
        {
            dialog.openFromViewIssue();
            _testFields();
            closeDialogByClickingCancel();
        }
    }

    private void _testFields()
    {
        _testCascadingSelect();
        _testDatePicker();
        _testDateTimePicker();
        _testUserPicker();
    }


    private void _testCascadingSelect()
    {
        assertThat.elementPresent(CASCADING_SELECT_CF_PARENT_LOCATOR);
        assertThat.elementPresent(CASCADING_SELECT_CF_CHILD_LOCATOR);
        assertParentAndChildValues("One", "One-one", "One-two", "One-three");
        assertParentAndChildValues("Two", "Two-one", "Two-two", "Two-three");
        assertParentAndChildValues("Three", "Three-one", "Three-two", "Three-three");
    }

    private void assertParentAndChildValues(String parentValue, String... childValues)
    {
        client.select(CASCADING_SELECT_CF_PARENT_LOCATOR, "label=" + parentValue);
        client.getEval("window.jQuery('#customfield_10020').change()"); // needed for IE to actually update second field
        String[] childOptions = client.getSelectOptions(CASCADING_SELECT_CF_CHILD_LOCATOR);
        for (String expectedOption : childValues)
        {
            assertTrue("option <" + expectedOption +"> not found in options " + Arrays.toString(childOptions),
                    ArrayUtils.contains(childOptions, expectedOption));   
        }
    }
    

    private void _testDatePicker()
    {
        // TODO use DateFieldWithCalendar
        CalendarPopup popup = createCalendarPopupFor(DATE_PICKER_CF_LOCATOR);
        popup.insertDate("8/Jun/10").open().assertReady(context().timeouts().components());
        popup.clickDay(1).assertClosed(context().timeouts().components());
        assertEquals("Calendar value should be changed by click", "1/Jun/10", popup.getDate());
    }

    private void _testDateTimePicker()
    {
        CalendarPopup popup = createCalendarPopupFor(DATE_TIME_PICKER_CF_LOCATOR);
        popup.insertDate("8/Jun/10 10:49 AM").open().assertReady(context().timeouts().components());
        popup.increaseHour(3).clickDay(1).assertClosed(context().timeouts().components());
        assertThat("Calendar value should be changed", datePickerValueCondition(popup, "1/Jun/10 01:49 AM"),
                isTrue().byDefaultTimeout());
    }

    private TimedCondition datePickerValueCondition(CalendarPopup popup, String expectedValue)
    {
        return ContainsValueCondition.forContext(context()).locator(popup.inputLocator()).expectedValue(expectedValue)
                .defaultTimeout(Timeouts.COMPONENT_LOAD).build();
    }

    private CalendarPopup createCalendarPopupFor(String calInputId)
    {
        return new CalendarPopup(calInputId, customFieldCalendarTrigger(calInputId), context());
    }
   
    private void _testUserPicker()
    {
        client.typeWithFullKeyEvents(USER_PICKER_LOCATOR, "adm");
        assertUserPickerDropDownOpen();
        closeUserPickerSuggestions();
    }

    private void closeUserPickerSuggestions()
    {
        client.focus(USER_PICKER_LOCATOR);
        new SeleniumTypeWriter(client, USER_PICKER_LOCATOR, TypeMode.TYPE).type(SpecialKeys.ESC.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYUP));
        assertUserPickerDropDownClosed();
    }

    private TimedCondition userPickerDropDownOpen()
    {
        return HasClassCondition.forContext(context()).locator(USER_PICKER_DROPDOWN_LOCATOR).cssClass("dropdown-ready")
                .defaultTimeout(Timeouts.AJAX_ACTION).build();
    }

    private TimedCondition userPickerDropDownClosed()
    {
        return not(userPickerDropDownOpen());
    }

    private void assertUserPickerDropDownOpen()
    {
        assertThat(userPickerDropDownOpen(), byDefaultTimeout());
    }

    private void assertUserPickerDropDownClosed()
    {
        assertThat(userPickerDropDownClosed(), byDefaultTimeout());
    }
}
