package com.atlassian.jira.functest.framework.navigation;

/**
 * Abstract implementation of the {@link BulkChangeWizard}. Defines the state and operations of the wizard. Specific
 * details on how to drive the wizard are supplied by the implementations for func tests and selenium tests.
 *
 * @since v4.2
 */
public abstract class AbstractBulkChangeWizard implements BulkChangeWizard
{
    protected static final String SAME_FOR_ALL = "sameAsBulkEditBean";
    protected static final String BULK_EDIT_KEY = "10000_1_";
    protected static final String TARGET_PROJECT_ID = "10000_1_pid";

    private WizardState state = WizardState.SELECT_ISSUES;
    private BulkOperations operation = null;

    public BulkChangeWizard selectAllIssues()
    {
        validateState(WizardState.SELECT_ISSUES);

        selectAllIssueCheckboxes();
        clickOnNext();

        state = WizardState.CHOOSE_OPERATION;

        return this;
    }

    public BulkChangeWizard chooseOperation(final BulkOperations operation)
    {
        validateState(WizardState.CHOOSE_OPERATION);

        chooseOperationRadioButton(operation);
        clickOnNext();

        this.operation = operation;
        switch (operation)
        {
            case MOVE:
                state = WizardState.CHOOSE_TARGET_CONTEXTS;
                break;
            case EDIT:
                state = WizardState.SET_FIELDS;
                break;
        }

        return this;
    }

    public BulkChangeWizard chooseTargetContextForAll(final String projectName)
    {
        validateState(BulkOperations.MOVE, WizardState.CHOOSE_TARGET_CONTEXTS);

        // note that this only currently works when you are moving issues from Homosapien project, and when that is the
        // first project context offered on the page. Might need to fix it if the data is different!
        checkSameTargetForAllCheckbox();
        selectFirstTargetProject(projectName);
        clickOnNext();

        state = WizardState.SET_FIELDS;

        return this;
    }

    public BulkChangeWizard setFieldValue(final String fieldName, final String value)
    {
        return setFieldValue(InputTypes.TEXT, fieldName, value);
    }

    public BulkChangeWizard setFieldValue(final InputTypes inputType, final String fieldName, final String value)
    {
        validateState(WizardState.SET_FIELDS);

        switch (inputType)
        {
            case SELECT:
                setSelectElement(fieldName, value);
                break;
            default:
                setTextElement(fieldName, value);
        }

        return this;
    }

    public BulkChangeWizard checkRetainForField(final String fieldName)
    {
        validateState(BulkOperations.MOVE, WizardState.SET_FIELDS);

        checkCheckbox("retain_" + fieldName);

        return this;
    }

    public BulkChangeWizard checkActionForField(final String fieldName)
    {
        validateState(BulkOperations.EDIT, WizardState.SET_FIELDS);

        checkCheckbox("actions", fieldName);

        return this;
    }

    public BulkChangeWizard finaliseFields()
    {
        validateState(WizardState.SET_FIELDS);

        clickOnNext();

        // check to see if we have any more fields to set
        if (operation == BulkOperations.MOVE)
        {
            // this particular text appears at the top of the "Issue Fields" screen
            if (!pageContainsText("Update the fields for the new issues."))
            {
                this.state = WizardState.CONFIRMATION;
            }
        }
        else if (operation == BulkOperations.EDIT)
        {
            if (pageContainsText("Please confirm that the correct issues and changes have been entered"))
            {
                this.state = WizardState.CONFIRMATION;
            }
        }

        return this;
    }

    public BulkChangeWizard complete()
    {
        validateState(WizardState.CONFIRMATION);

        if (operation == BulkOperations.MOVE)
        {
            clickOnNext();
        }
        else if (operation == BulkOperations.EDIT)
        {
            clickOnConfirm();
        }

        this.state = WizardState.COMPLETE;

        return this;
    }

    protected abstract void clickOnNext();

    protected abstract void clickOnConfirm();

    protected abstract void selectAllIssueCheckboxes();

    protected abstract void chooseOperationRadioButton(BulkOperations operation);

    protected abstract void selectFirstTargetProject(String projectName);

    protected abstract void checkSameTargetForAllCheckbox();

    protected abstract void setTextElement(String fieldName, String value);

    protected abstract void setSelectElement(String fieldName, String value);

    protected abstract void checkCheckbox(String fieldName);

    protected abstract void checkCheckbox(final String checkboxName, final String value);

    protected abstract boolean pageContainsText(String text);

    private void validateState(final WizardState expectedState)
    {
        if (this.state != expectedState || this.state == WizardState.COMPLETE)
        {
            throw new IllegalStateException("Wizard is in invalid state. Expected state: " + expectedState + "; actual state: " + state.toString());
        }
    }

    private void validateState(final BulkOperations expectedOperation, final WizardState expectedState)
    {
        if (this.operation != expectedOperation)
        {
            throw new IllegalStateException("Wizard is in invalid state. Expected operation: " + expectedOperation + "; actual operation: " + expectedOperation.toString());
        }

        validateState(expectedState);
    }
}
