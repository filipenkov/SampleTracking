package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.functest.framework.navigation.AbstractBulkChangeWizard;
import com.atlassian.selenium.SeleniumClient;

/**
 * Implementation of the Bulk Change Wizard for Selenium Tests. Works with basic cases of Bulk Move and Bulk Edit, but
 * it needs improvement to work for other things!
 *
 * @since v4.2
 */
public class BulkChangeWizardImpl extends AbstractBulkChangeWizard
{
    private final SeleniumClient client;

    public BulkChangeWizardImpl(final SeleniumClient client)
    {
        this.client = client;
    }

    protected void clickOnNext()
    {
        client.click("id=Next", true);
    }

    protected void clickOnConfirm()
    {
        client.click("css=input[name=Confirm]", true);
    }

    protected void selectAllIssueCheckboxes()
    {
        client.click("css=input[name=all]");
    }

    protected void chooseOperationRadioButton(final BulkOperations operation)
    {
        client.check("operation", operation.getRadioValue());
    }

    protected void selectFirstTargetProject(final String projectName)
    {
        client.selectOption("css=select[class=select]:nth-child(1)", projectName);
    }

    protected void checkSameTargetForAllCheckbox()
    {
        client.click("id=" + SAME_FOR_ALL);
    }

    protected void setTextElement(final String fieldName, final String value)
    {
        client.type(fieldName, value);
    }

    protected void setSelectElement(final String fieldName, final String value)
    {
        client.selectOption(fieldName, value);
    }

    protected void checkCheckbox(final String fieldName)
    {
        client.check(fieldName);
    }

    protected void checkCheckbox(final String checkboxName, final String value)
    {
        client.check(checkboxName, value);
    }

    protected boolean pageContainsText(final String text)
    {
        return client.isTextPresent(text);
    }
}
