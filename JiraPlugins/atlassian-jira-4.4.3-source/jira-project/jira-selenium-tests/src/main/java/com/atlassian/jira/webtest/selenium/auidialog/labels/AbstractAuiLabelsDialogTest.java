package com.atlassian.jira.webtest.selenium.auidialog.labels;

import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.components.LabelsPicker;

/**
 * Base class for testing AUI labels dialogs.
 *
 * @since v4.2
 */
public abstract class AbstractAuiLabelsDialogTest extends AbstractAuiDialogTest
{

    private static final String DIALOG_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR;
    private static final String FORM_SELECTOR = DIALOG_SELECTOR + " form.edit-labels";


    private static final String LABELS_NOTIFICATION_CHECKBOX_SELECTOR = DIALOG_SELECTOR
            + " input[type='checkbox'][id='send-notifications']";


    private static final String ENTER_KEY = "\\13";
    private static final int BACKSPACE_KEY = 8;

    private LabelsPicker currentLabelsPicker;
    private static final String USER_INPUTTED_OPTION_GROUP = "User Inputted Option";
    private static final String SUGGESTIONS_GROUP = "Suggestions";

    /**
     * Factory method for the picker used in this test.
     *
     * @return new label picker instance
     */
    protected final void setCurrentLabelsPicker(LabelsPicker labelsPicker)
    {
        this.currentLabelsPicker = labelsPicker;
    }

    protected final LabelsPicker getCurrentPicker()
    {
        if (currentLabelsPicker == null)
        {
            throw new IllegalStateException("current labels picker must not be null");
        }
        return currentLabelsPicker;
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestEditLabels.xml");
    }

    protected final void assertLabelsDialogOpen()
    {
        assertDialogIsOpenAndReady();
        getCurrentPicker().assertReady(DEFAULT_TIMEOUT);
    }


    protected final void assertLabelSelectedInDialog(String label)
    {
        getCurrentPicker().assertElementPicked(label);
    }

    protected final void assertNoLabelsInDialog()
    {
        getCurrentPicker().assertNoElementPicked();
    }


    protected final void insertLabelText(String labelText)
    {
        getCurrentPicker().insertQuery(labelText);
    }

    protected final void hackyInsertLabelText(String labelText)
    {
        getCurrentPicker().focusOnInputArea();
        client.type(getCurrentPicker().inputAreaLocator(), removeLastChar(labelText));
        client.keyPress(getCurrentPicker().inputAreaLocator(), lastChar(labelText));
    }

    private String removeLastChar(final String labelText)
    {
        return labelText.substring(0, labelText.length()-1);
    }
    private String lastChar(final String labelText)
    {
        return String.valueOf(labelText.charAt(labelText.length()-1));
    }

    protected final void addLabel(String labelText)
    {
        getCurrentPicker().insertQuery(labelText).awayFromInputArea();
    }

    protected final void removeLabels(int number)
    {
        client.simulateKeyPressForSpecialKey(getCurrentPicker().inputAreaLocator(), BACKSPACE_KEY);
        assertTrue("Expected to focus on an label lozenge", getCurrentPicker().hasAnyFocusedPickedElement());
        client.simulateKeyPressForSpecialKey(getCurrentPicker().inputAreaLocator(), BACKSPACE_KEY);
        for (int i=0; i<number; i++)
        {
            client.simulateKeyPressForSpecialKey(getCurrentPicker().inputAreaLocator(), BACKSPACE_KEY);
            client.simulateKeyPressForSpecialKey(getCurrentPicker().inputAreaLocator(), BACKSPACE_KEY);
            waitFor(100);
        }
    }

    protected final void triggerSuggestionsDropdown()
    {
        getCurrentPicker().triggerSuggestionsByClick();
    }

    protected final void assertSuggestionsDropdownPresent()
    {
        assertTrue(getCurrentPicker().suggestions().isOpen());
    }

    protected final void assertSuggestionsPresent()
    {
        assertTrue(getCurrentPicker().suggestions().containsGroup(SUGGESTIONS_GROUP));
    }

    protected final void assertSuggestionsContain(String suggestedLabel)
    {
        getCurrentPicker().suggestions().assertContains(suggestedLabel);
    }

    protected final void assertNoSuggestions()
    {
        assertFalse(getCurrentPicker().suggestions().containsGroup(SUGGESTIONS_GROUP));
    }

    protected final void assertNewLabelPresent(String newLabelValue)
    {
        getCurrentPicker().suggestions().assertContainsGroup(USER_INPUTTED_OPTION_GROUP);
        getCurrentPicker().suggestions().assertContains(USER_INPUTTED_OPTION_GROUP, newLabelValue);
    }


    protected final void assertSendNotificationsCheckboxUnchecked()
    {
        visibleByTimeoutWithDelay(LABELS_NOTIFICATION_CHECKBOX_SELECTOR, 1000);
        assertThat.elementNotPresentByTimeout(checkedNotificationCheckboxSelector());
    }

    protected final void assertSendNotificationsCheckboxChecked()
    {
        visibleByTimeoutWithDelay(checkedNotificationCheckboxSelector(), 1000);
    }

    private String checkedNotificationCheckboxSelector()
    {
        return LABELS_NOTIFICATION_CHECKBOX_SELECTOR + ":checked";
    }

    protected final void checkSendNotifications()
    {
        client.check(LABELS_NOTIFICATION_CHECKBOX_SELECTOR);
    }

    protected final void uncheckSendNotifications()
    {
        client.uncheck(LABELS_NOTIFICATION_CHECKBOX_SELECTOR);
    }
}
