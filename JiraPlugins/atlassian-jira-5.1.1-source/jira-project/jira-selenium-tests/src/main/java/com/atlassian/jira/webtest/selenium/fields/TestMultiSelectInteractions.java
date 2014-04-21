package com.atlassian.jira.webtest.selenium.fields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.components.GenericMultiSelect;
import com.atlassian.jira.webtest.selenium.framework.components.MultiSelectSuggestions;
import com.atlassian.jira.webtest.selenium.framework.components.Pickers;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.webtest.ui.keys.SpecialKeys;

/**
 * Test behaviour of the multi-select control. 
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestMultiSelectInteractions extends JiraSeleniumTest
{

    private static final String TEST_XML = "TestMultiSelectInteractions.xml";

    private static final int CUSTOM_VERSION_FIELD_ID = 10000;

    private static final String EDIT_FORM_LOCATOR = Locators.JQUERY.addPrefix("form#issue-edit");

    private static final String FIX_VERSIONS_SELECTOR = "css=#fixVersions-multi-select .drop-menu";
    private static final String FIX_VERSIONS_SUGGESTIONS_SELECTOR = "css=.ajs-layer.active #fixVersions-suggestions";
    private static final String CSS_LABELS_MULTI_SELECT_DROP_MENU = "css=#labels-multi-select .drop-menu";
    private static final String LABELS_SUGGESTIONS_ACTIVE = "css=.ajs-layer.active #labels-suggestions";
    private static final String BODY_LOCATOR = "css=body";
    private final String JQUERY_FIELD = "jquery=#issue-actions-dialog .text";
    private static final String CSS_AJS_LAYER_ACTIVE_ISSUEACTIONS_SUGGESTIONS = "css=.ajs-layer.active #issueactions-suggestions";
    private static final String CSS_ISSUEACTIONS_MULTI_SELECT_DROP_MENU = "css=#issueactions-queryable-container .drop-menu";
    private static final String CSS_FIX_VERSIONS_TEXTAREA = "css=#fixVersions-textarea";
    private static final String MY_OWN_CUSTOM_LABEL = "MyOwnCustomLabel";
    private static final String SECOND_TAB_TRIGGER = "jquery=.tabs-menu a:eq(1)";
    private static final String FIRST_TAB_TRIGGER = "jquery=.tabs-menu a:eq(0)";
    private static final String LABELS_TEXTAREA = "labels-textarea";
    private static final String VERSIONS_PICKER_ERROR = "css=.aui-field-versionspicker > .description + .error";
    private static final String FIX_VERSIONS_MULTI_SELECT_ID = "fixVersions-suggestions";

    private GenericMultiSelect fixVersionsPicker;
    private GenericMultiSelect componentPicker;
    private GenericMultiSelect customVersionPicker;

    public void onSetUp()
    {
        super.onSetUp();
        initPickers();
        restoreData(TEST_XML);
    }

    private void initPickers()
    {
        this.fixVersionsPicker = Pickers.newFixVersionPicker(EDIT_FORM_LOCATOR, context());
        this.componentPicker = Pickers.newComponentPicker(EDIT_FORM_LOCATOR, context());
        this.customVersionPicker = Pickers.newCustomFieldVersionPicker(EDIT_FORM_LOCATOR, CUSTOM_VERSION_FIELD_ID, context());
    }

    public void testInteractions()
    {
        _testSelectionWithSpaces();
        _testSuggestionsOnEditingQuery();
        _testExactMatch();
        _testDropDownIcon();
        _testBlurField();
        _testCorrectWidthWhenMovingTabs();
        _testDropdownPositionUpdatesOnLineWrap();
    }

    // JRA-22947
    private void _testSelectionWithSpaces()
    {
        getNavigator().editIssue("HSP-1");
        componentPicker.insertQueryFast("New comp");
        componentPicker.confirmInput();
        componentPicker.assertElementPicked("New Component 1");
    }

    private void _testCorrectWidthWhenMovingTabs()
    {
        getNavigator().editIssue("MKY-1");

        assertThat.elementPresentByTimeout(LABELS_TEXTAREA); // present but no visible
        assertThat.elementNotVisible(LABELS_TEXTAREA);

        client.click(SECOND_TAB_TRIGGER);
        assertThat.visibleByTimeout(LABELS_TEXTAREA);
        int visibleWidth = getLabelsTextAreaWidth();
        assertTrue("Expected width of field to be calculated when toggling tab as the field needs to be visible to "
                + "calculate correctly", visibleWidth > 10);

        client.click(FIRST_TAB_TRIGGER);
        assertThat.elementNotVisible(LABELS_TEXTAREA);

        client.click(SECOND_TAB_TRIGGER);
        assertThat.visibleByTimeout(LABELS_TEXTAREA);
        int toggledWidth = getLabelsTextAreaWidth();
        assertTrue("Expected field to remain same width when toggling back and forth between tabs", visibleWidth == toggledWidth);
    }


    private int getLabelsTextAreaWidth()
    {
        return Integer.parseInt(client.getEval("window.jQuery('#labels-textarea').width()"), 10);
    }

    private void _testExactMatch()
    {
        // Test that we can match by label (What appears in the dropdown)
        getNavigator().editIssue("HSP-1");
        assertThat.elementPresentByTimeout(CSS_FIX_VERSIONS_TEXTAREA, DROP_DOWN_WAIT);
        client.typeWithFullKeyEvents("css=#fixVersions-multi-select textarea", "2");
        assertThat.elementPresentByTimeout("css=li.aui-list-item-li-2-0-1.active");
        client.type("css=#fixVersions-multi-select textarea", "2."); // because typeWithFullEvents does not support "."
        client.typeWithFullKeyEvents("css=#fixVersions-multi-select textarea", "0", false);
        assertThat.elementPresentByTimeout("css=li.aui-list-item-li-2-0.active");

        // Test that we can match by value (What is submitted to server)
        getNavigator().gotoIssue("HSP-1");
        client.click("link-issue");
        assertThat.elementPresentByTimeout("jira-issue-keys-textarea", DROP_DOWN_WAIT);
        client.typeWithFullKeyEvents("jira-issue-keys-textarea", "HSP-2");
        assertThat.elementPresentByTimeout("css=li.aui-list-item-li-hsp-2---test.active");
    }

    private void _testDropDownIcon()
    {
        getNavigator().editIssue("HSP-1");
        assertThat.elementPresentByTimeout(CSS_FIX_VERSIONS_TEXTAREA, DROP_DOWN_WAIT);

        // test non ajax ones
        client.click(FIX_VERSIONS_SELECTOR);
        assertThat.elementPresentByTimeout(FIX_VERSIONS_SUGGESTIONS_SELECTOR);
        client.click(FIX_VERSIONS_SELECTOR);
        assertThat.elementNotPresentByTimeout(FIX_VERSIONS_SUGGESTIONS_SELECTOR);

        // ajax ones
        getNavigator().editIssue("HSP-2");
        client.click("jquery=.tabs-menu > .menu-item > a:contains('labels')");
        assertThat.elementPresentByTimeout("css=#labels-multi-select", DROP_DOWN_WAIT);
        client.click(CSS_LABELS_MULTI_SELECT_DROP_MENU);
        assertThat.elementPresentByTimeout(LABELS_SUGGESTIONS_ACTIVE, DROP_DOWN_WAIT);
        client.click(CSS_LABELS_MULTI_SELECT_DROP_MENU);
        assertThat.elementNotPresentByTimeout(LABELS_SUGGESTIONS_ACTIVE);

        getNavigator().gotoIssue("HSP-1");
        assertThat.elementPresentByTimeout(BODY_LOCATOR, DROP_DOWN_WAIT);
        client.focus(BODY_LOCATOR);
        context().ui().pressInBody(Shortcuts.DOT_DIALOG);
        assertThat.elementPresentByTimeout(JQUERY_FIELD, DROP_DOWN_WAIT);

        client.click(CSS_ISSUEACTIONS_MULTI_SELECT_DROP_MENU);
        assertThat.elementPresentByTimeout(CSS_AJS_LAYER_ACTIVE_ISSUEACTIONS_SUGGESTIONS, DROP_DOWN_WAIT);
        client.click(CSS_ISSUEACTIONS_MULTI_SELECT_DROP_MENU);
        assertThat.elementNotPresentByTimeout(CSS_AJS_LAYER_ACTIVE_ISSUEACTIONS_SUGGESTIONS, DROP_DOWN_WAIT);
    }

    private void _testBlurField()
    {
        getNavigator().editIssue("HSP-1");
        fixVersionsPicker.assertReady(500);
        fixVersionsPicker.insertQuery("New Version").awayFromInputArea();
        fixVersionsPicker.assertNoElementPicked();

        fixVersionsPicker.clearInputArea().insertQuery("new versioN 1").awayFromInputArea();
        fixVersionsPicker.assertElementPicked("New Version 1");

        getNavigator().editIssue("HSP-1"); //?
        customVersionPicker.assertReady(500);
        customVersionPicker.insertQuery(MY_OWN_CUSTOM_LABEL).awayFromInputArea();
        customVersionPicker.assertElementPicked(MY_OWN_CUSTOM_LABEL);

        customVersionPicker.clearInputArea().insertQuery("New Version 4").awayFromInputArea();
        customVersionPicker.suggestions().assertClosed();
    }

    private void _testDropdownPositionUpdatesOnLineWrap()
    {
        getNavigator().editIssue("HSP-1");
        assertThat.elementPresentByTimeout(CSS_FIX_VERSIONS_TEXTAREA, DROP_DOWN_WAIT);

        // test non ajax ones
        client.click(FIX_VERSIONS_SELECTOR);
        assertThat.elementPresentByTimeout(FIX_VERSIONS_SUGGESTIONS_SELECTOR);

        Number unwrappedOffset = client.getElementPositionTop("css=.ajs-layer.active");

        _selectItemWithClick(FIX_VERSIONS_MULTI_SELECT_ID, "2-0-1");
        _selectItemWithClick(FIX_VERSIONS_MULTI_SELECT_ID, "2-0");
        _selectItemWithClick(FIX_VERSIONS_MULTI_SELECT_ID, "new-version-1");
        _selectItemWithClick(FIX_VERSIONS_MULTI_SELECT_ID, "new-version-4");
        _selectItemWithClick(FIX_VERSIONS_MULTI_SELECT_ID, "new-version-5");
        _selectItemWithClick(FIX_VERSIONS_MULTI_SELECT_ID, "new-version-6");

        Number wrappedOffset = client.getElementPositionTop("css=.ajs-layer.active");

        if (unwrappedOffset.intValue() >= wrappedOffset.intValue())
        {
            throw new AssertionError("Expected suggestions top offset to be greater when text wraps");
        }

    }

    private void _backspace() {
        fixVersionsPicker.inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
    }

    private void _testSuggestionsOnEditingQuery()
    {
        fixVersionsPicker.assertReady(DROP_DOWN_WAIT);
        
        fixVersionsPicker.insertQuery("Version 1");
        MultiSelectSuggestions<GenericMultiSelect> suggestions = fixVersionsPicker.suggestions();
        suggestions.assertContains("New Version 1");
        suggestions.assertDoesNotContain("New Version 4", "New Version 5", "New Version 6");
        fixVersionsPicker.focusOnInputArea();
        _backspace();
        suggestions.assertContains("New Version 1", "New Version 4", "New Version 5", "New Version 6");

        fixVersionsPicker.clearInputArea().insertQuery("Version 1");
        suggestions = fixVersionsPicker.suggestions();
        suggestions.assertContains("New Version 1");
        suggestions.assertDoesNotContain("New Version 4", "New Version 5", "New Version 6");
        fixVersionsPicker.focusOnInputArea();
        _backspace();
        suggestions.assertContains("New Version 1", "New Version 4", "New Version 5", "New Version 6");

        fixVersionsPicker.clearInputArea().insertQuery("V");
        assertTrue(fixVersionsPicker.suggestions().isOpen());
        fixVersionsPicker.focusOnInputArea();
        _backspace();
        assertFalse(fixVersionsPicker.suggestions().isOpen());
    }

    private void _selectItemWithClick(String parentId, String suggestionsClass)
    {
        String itemSelector = "css=#" + parentId + " li.aui-list-item-li-" + suggestionsClass + " a";

        assertThat.elementPresentByTimeout(itemSelector, DROP_DOWN_WAIT);
        Mouse.mouseover(client, itemSelector);
        client.click(itemSelector);
        client.click(FIX_VERSIONS_SELECTOR);
        assertThat.elementPresentByTimeout(FIX_VERSIONS_SUGGESTIONS_SELECTOR);
    }
}
