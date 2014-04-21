package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.NavigationImpl;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Implementation of the Bulk Change Wizard for Functional Tests. Works with basic cases of Bulk Move and Bulk Edit, but
 * it needs improvement to work for other things!
 *
 * @since v4.2
 */
public class BulkChangeWizardImpl extends AbstractBulkChangeWizard
{
    private final WebTester tester;
    private final Navigation navigation;

    public BulkChangeWizardImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        this.tester = tester;
        this.navigation = new NavigationImpl(tester, environmentData);
    }

    protected void clickOnNext()
    {
        navigation.clickOnNext();
    }

    protected void clickOnConfirm()
    {
        tester.submit("Confirm");
    }

    protected void selectAllIssueCheckboxes()
    {
        tester.setWorkingForm("bulkedit");
        final String[] paramNames = tester.getDialog().getForm().getParameterNames();
        for (String paramName : paramNames)
        {
            if (paramName.startsWith("bulkedit_"))
            {
                tester.checkCheckbox(paramName);
            }
        }
    }

    protected void chooseOperationRadioButton(final BulkOperations operation)
    {
        setTextElement(FunctTestConstants.FIELD_OPERATION, operation.getRadioValue());
    }

    protected void selectFirstTargetProject(final String projectName)
    {
        tester.selectOption(TARGET_PROJECT_ID, projectName);
    }

    protected void checkSameTargetForAllCheckbox()
    {
        tester.checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
    }

    protected void setTextElement(final String fieldName, final String value)
    {
        tester.setFormElement(fieldName, value);
    }

    protected void setSelectElement(final String fieldName, final String value)
    {
        tester.setFormElement(fieldName, value);
    }

    protected void checkCheckbox(final String fieldName)
    {
        tester.checkCheckbox(fieldName);
    }

    protected void checkCheckbox(final String checkboxName, final String value)
    {
        tester.checkCheckbox(checkboxName, value);
    }

    protected boolean pageContainsText(final String text)
    {
        return tester.getDialog().getResponseText().contains(text);
    }
}
