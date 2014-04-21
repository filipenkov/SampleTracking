package com.atlassian.jira.webtest.selenium.framework.fields;

/**
 * Represents a JIRA field in the selenium tests.
 *
 * @since v4.2
 */
public interface Field<T>
{
    /**
     * Return the value currently within the field.
     *
     * @return the value currently within the field.
     */
    T getValue();

    /**
     * Return the string value of the field.
     *
     * @return the string value of the field.
     */
    String getStringValue();

    /**
     * Make sure that the field is currently visible on the page.
     */
    void assertVisible();
}
