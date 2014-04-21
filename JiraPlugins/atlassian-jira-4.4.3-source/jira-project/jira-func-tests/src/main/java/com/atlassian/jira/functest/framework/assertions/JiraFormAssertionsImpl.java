package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

/**
 * And implementation of {@link JiraFormAssertions}.
 *
 * @since v3.13
 */
public class JiraFormAssertionsImpl extends AbstractFuncTestUtil implements JiraFormAssertions
{
    private final TextAssertions textAssertions;

    public JiraFormAssertionsImpl(TextAssertions textAssertions, WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
        this.textAssertions = textAssertions;
    }

    public void assertFieldErrMsg(final String expectedText)
    {
        Locator error = createFieldErrorMessageLocator();
        textAssertions.assertTextPresent(error, expectedText);
    }

    public void assertAuiFieldErrMsg(final String expectedText)
    {
        Locator error = createAuiFieldErrorMessageLocator();
        textAssertions.assertTextPresent(error, expectedText);
    }

    public void assertFormErrMsg(final String expectedText)
    {
        Locator error = createFormErrorMessageLocator();
        textAssertions.assertTextPresent(error, expectedText);
    }

    public void assertNoFieldErrMsg(final String notExpectedText)
    {
        textAssertions.assertTextNotPresent(createFieldErrorMessageLocator(), notExpectedText);
    }

    public void assertNoFormErrMsg(final String notExpectedText)
    {
        textAssertions.assertTextNotPresent(createFormErrorMessageLocator(), notExpectedText);
    }

    public void assertNoErrorsPresent()
    {
        Locator errorLocator = createFieldErrorMessageLocator();
        Assert.assertNull("Expected no errors on the page, but there was a field with an error.", errorLocator.getNode());

        errorLocator = createFormErrorMessageLocator();
        Assert.assertNull("Expected no errors on the page, but the page had a global error.", errorLocator.getNode());
    }

    public void assertSelectElementHasOptionSelected(final String selectElementName, final String optionName)
    {
        final String actual = tester.getDialog().getSelectedOption(selectElementName);
        Assert.assertEquals(
                "Expected option selected '" + optionName + "' was not selected in form element '" + selectElementName + "'. Actual selected option was '" + actual + "'.",
                optionName, actual);
    }

    public void assertFormNotificationMsg(final String expectedText)
    {
        Locator notification = createFormNotificationMessageLocator();
        textAssertions.assertTextPresent(notification, expectedText);
    }

    @Override
    public void assertFormSuccessMsg(String expectedText)
    {
        Locator notification = createFormSuccessMessageLocator();
        textAssertions.assertTextPresent(notification, expectedText);
    }

    private Locator createFieldErrorMessageLocator()
    {
        return new XPathLocator(tester, "//span[@class='errMsg']");
    }

    private Locator createAuiFieldErrorMessageLocator()
    {
        return new XPathLocator(tester, "//form[@class='aui']//div[@class='field-group']/div[@class='error']");
    }

    private Locator createFormErrorMessageLocator()
    {
        return new CssLocator(tester, ".aui-message.error");
    }

    private Locator createFormNotificationMessageLocator()
    {
        return new CssLocator(tester, ".aui-message.info");
    }

    private Locator createFormSuccessMessageLocator()
    {
        return new CssLocator(tester, ".aui-message.success");
    }
}