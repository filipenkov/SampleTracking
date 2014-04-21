package com.atlassian.jira.functest.framework.assertions;

/**
 * Assertions for common messages displayed
 *
 * @since v4.3
 */
public interface JiraMessageAssertions
{
    void assertHasTitle(String expectedTitle);

    void assertHasMessage(String expectedMsg);

}
