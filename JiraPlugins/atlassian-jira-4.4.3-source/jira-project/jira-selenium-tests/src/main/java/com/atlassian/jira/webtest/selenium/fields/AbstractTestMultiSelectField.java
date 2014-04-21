package com.atlassian.jira.webtest.selenium.fields;

import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.webtest.ui.keys.SpecialKeys;

/**
 * Abstract test case for multi select frother controls.
 *
 * @since v4.0
 */
public abstract class AbstractTestMultiSelectField extends JiraSeleniumTest
{
    private static final String ISSUE_WORKFLOW_TRANSITION_SUBMIT = "issue-workflow-transition-submit";
    private static final String RESOLVE_ISSUE = "action_id_5";
    private static final String DOWN_KEY = "\\40";
    private static final String JQUERY_EDIT_ISSUE = "editIssue";
    private static final String ISSUE_EDIT_SUBMIT = "issue-edit-submit";
    private static final String CREATE_SUBTASK = "create-subtask";
    private static final String SUBTASK_CREATE_DETAILS_SUBMIT = "subtask-create-details-submit";
    private static final String MOVE_ISSUE = "move-issue";
    private static final String CREATE_LINK = "create_link";
    private static final String QUICK_CREATE_BUTTON = "quick-create-button";
    private static final String ISSUE_CREATE_SUBMIT = "issue-create-submit";
    private static final String BOGUS_ISSUE = "Bogus Issue";
    private static final String SUMMARY = "summary";
    private static final String NEXT_SUBMIT = "next_submit";
    private static final String MOVE_SUBMIT = "move_submit";
    private static final String MONKEY = "monkey";
    private static final String HSP_3 = "HSP-3";
    private static final String SUBTASK_TO_ISSUE = "subtask-to-issue";
    private static final String FINISH_SUBMIT = "finish_submit";
    private static final String MKY_2 = "MKY-2";
    private static final String QUICK_PID = "quick-pid";
    private static final String HOMOSAPIEN = "homosapien";

    abstract String getXMLFileName();

    public void onSetUp()
    {
        super.onSetUp();
        restoreData(getXMLFileName());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void doTestForm(final Form form)
    {
        form.gotoForm();
        form.assertControlApplied();
        form.selectSuggestion();
        form.submitForm();
        form.assertFieldSubmission();      
    }

    abstract class Form
    {
        abstract void gotoForm();

        abstract void submitForm();

        final String fieldSelector;
        final String fieldId;
        final String suggestionsSelector;
        final String optionLabelToSelect;
        final String submittedValueSelector;
        final String boogerSelector;

        public Form(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            this.fieldId = fieldId;
            this.fieldSelector = "jquery=#" + fieldId + "-textarea";
            this.suggestionsSelector = "jquery=#" + fieldId + "-suggestions";
            this.submittedValueSelector = submittedValueSelector;
            this.boogerSelector = "jquery=#" + fieldId + "-multi-select .value-text";
            this.optionLabelToSelect = optionLabelToSelect;
        }

        void removeAllSelected()
        {
            client.getEval("this.browserbot.getCurrentWindow().jQuery('#" + fieldId + "-multi-select .item-delete').click()");
        }

        void selectSuggestion()
        {
            removeAllSelected();
            client.focus(fieldSelector);
            context().ui().typeInLocator(fieldSelector, SpecialKeys.ARROW_DOWN);
            assertSuggestionsVisible();
            Mouse.mouseover(client, suggestionsSelector + " a:contains(" + optionLabelToSelect + ")");
            client.click(suggestionsSelector + " a:contains(" + optionLabelToSelect + ")");
            assertThat.elementContainsText(boogerSelector, optionLabelToSelect);
        }

        protected void assertFieldSubmission()
        {
            assertThat.elementContainsText(submittedValueSelector, optionLabelToSelect);
        }

        private void assertControlApplied()
        {
            assertThat.elementPresentByTimeout(fieldSelector, DROP_DOWN_WAIT);
        }

        private void assertSuggestionsVisible()
        {
            assertThat.elementPresentByTimeout(suggestionsSelector, DROP_DOWN_WAIT);
        }
    }

    final class EditIssueForm extends Form
    {
        EditIssueForm(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            getNavigator().gotoIssue(HSP_1);
            client.click(JQUERY_EDIT_ISSUE);
            client.waitForPageToLoad();
        }

        void submitForm()
        {
            client.click(ISSUE_EDIT_SUBMIT, true);
        }

    }

    final class MoveIssueForm extends Form
    {
        MoveIssueForm(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            getNavigator().gotoIssue(HSP_3);

            client.click(MOVE_ISSUE, true);
            client.selectOption("project", MONKEY);
            client.click(NEXT_SUBMIT, true);
        }

        void submitForm()
        {
            client.click(NEXT_SUBMIT, true);
            client.click(MOVE_SUBMIT, true);
        }

    }

    final class CreateSubTask extends Form
    {
        CreateSubTask(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }


        void gotoForm()
        {
            getNavigator().gotoIssue(HSP_1);
            client.click(CREATE_SUBTASK, true);
            client.type(SUMMARY, BOGUS_ISSUE);
        }

        void submitForm()
        {
            client.click(SUBTASK_CREATE_DETAILS_SUBMIT, true);
        }


    }

    final class CreateIssueForm extends Form
    {

        CreateIssueForm(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            client.click(CREATE_LINK);
            assertThat.visibleByTimeout("id=inline-dialog-create_issue_popup", 5000);
            assertThat.visibleByTimeout(QUICK_PID, 5000);
            client.selectOption(QUICK_PID, HOMOSAPIEN);
            client.click(QUICK_CREATE_BUTTON, true);
        }

        void submitForm()
        {
            client.type(SUMMARY, BOGUS_ISSUE);
            client.click(ISSUE_CREATE_SUBMIT, true);
        }

    }

    final class ConvertToIssue extends Form
    {
        ConvertToIssue(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            getNavigator().gotoIssue(HSP_1);
            client.click(CREATE_SUBTASK, true);
            client.type(SUMMARY, BOGUS_ISSUE);
            client.click(SUBTASK_CREATE_DETAILS_SUBMIT, true);
            client.click(SUBTASK_TO_ISSUE, true);
            client.click(NEXT_SUBMIT, true);
        }

        void submitForm()
        {
            client.click(NEXT_SUBMIT, true);
            client.click(FINISH_SUBMIT, true);
        }

    }

    final class BulkEditIssue extends Form
    {
        private static final String BULK_ALL = "bulkedit_all";
        private static final String NEXT = "Next";

        BulkEditIssue(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            getNavigator().findAllIssues();
            client.click(BULK_ALL, true);
            client.check("name=bulkedit_10061");
            client.click(NEXT, true);
            client.check("name=operation value=bulk.edit.operation.name");
            client.click(NEXT, true);
        }

        // todo assert that field value is updated. Wait till JRADEV-2408 is resolved to submit form without errors
        void submitForm() {}
        protected void assertFieldSubmission () {}
    }

    final class ConvertSubTaskToIssue extends Form
    {
        private static final String SUBTASK_TO_ISSUE = "id=subtask-to-issue";

        ConvertSubTaskToIssue(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            getNavigator().gotoIssue(MKY_2);
            assertThat.elementPresentByTimeout(SUBTASK_TO_ISSUE, 5000);
            client.click("id=opsbar-operations_more");
            assertThat.visibleByTimeout(SUBTASK_TO_ISSUE, 5000);
            client.click(SUBTASK_TO_ISSUE, true);
            client.click(NEXT_SUBMIT, true);
        }

        void submitForm()
        {
            client.click(NEXT_SUBMIT, true);
            client.click(FINISH_SUBMIT, true);
        }
    }

    final class ResolveIssue extends Form
    {
        ResolveIssue(final String fieldId, final String optionLabelToSelect, final String submittedValueSelector) {
            super(fieldId, optionLabelToSelect, submittedValueSelector);
        }

        void gotoForm()
        {
            getNavigator().gotoIssue(HSP_1);
            client.click(RESOLVE_ISSUE);
            assertThat.elementPresentByTimeout(fieldSelector, DROP_DOWN_WAIT);
        }

        void submitForm()
        {
            client.click(ISSUE_WORKFLOW_TRANSITION_SUBMIT, true);
        }
    }
}
