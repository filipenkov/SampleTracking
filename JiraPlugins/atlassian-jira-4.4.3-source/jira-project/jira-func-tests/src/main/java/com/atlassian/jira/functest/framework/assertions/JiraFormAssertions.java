package com.atlassian.jira.functest.framework.assertions;

/**
 * An assertions helper for working with JIRA forms
 *
 * @since v3.13
 */
public interface JiraFormAssertions
{
    /**
     * Asserts that there is a "field" error message with the <tt>expectedText</tt>.
     *
     * @param expectedText the expected error message
     */
    void assertFieldErrMsg(String expectedText);

    /**
     * Asserts that there is a "field" error message with the <tt>expectedText</tt> in an AUI form.
     *
     * @param expectedText the expected error message
     */
    void assertAuiFieldErrMsg(String expectedText);

    /**
     * Asserts that there is a "form" error message with the <tt>expectedText</tt>.
     *
     * @param expectedText the expected error message
     */
    void assertFormErrMsg(String expectedText);

    /**
     * Asserts that there is <b>NO</b> "field" error message with the <tt>notExpectedText</tt>.
     *
     * @param notExpectedText the expected error message
     */
    void assertNoFieldErrMsg(String notExpectedText);

    /**
     * Asserts that there is <b>NO</b> "form" error message with the <tt>notExpectedText</tt>.
     *
     * @param notExpectedText the expected error message
     */
    void assertNoFormErrMsg(String notExpectedText);



    /**
     * Asserts that there is no errors present.
     */
    void assertNoErrorsPresent();

    /**
     * Asserts that there is a "form" notification message with the <tt>expectedText</tt>
     * @param expectedText the expected notification message
     */
    void assertFormNotificationMsg(String expectedText);

    /**
     * Asserts that there is a "form" success message with the <tt>expectedText</tt>
     * @param expectedText the expected notification message
     */
    void assertFormSuccessMsg(String expectedText);

    /**
     * Asserts that the specified Select form element has this option selected.
     *
     * @param selectElementName the name of the &lt;select&gt; element.
     * @param optionName the name of the option that should be selected (not the value of the option).
     */
    void assertSelectElementHasOptionSelected(final String selectElementName, final String optionName);
}
