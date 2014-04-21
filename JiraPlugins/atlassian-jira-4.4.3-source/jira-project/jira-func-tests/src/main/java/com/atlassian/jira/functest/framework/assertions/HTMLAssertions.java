package com.atlassian.jira.functest.framework.assertions;

import net.sourceforge.jwebunit.WebTester;
import com.atlassian.jira.util.NotNull;

/**
 * Assertions for HTML content that may or may not be HTML encoded.
 *
 * @since v4.0.2
 */
public interface HTMLAssertions
{
    /**
     * The original should contain the expected text, regardless of whether it is encoded or not.
     *
     * @param original the response, may be HTML encoded.
     * @param expected the expected text, not encoded.
     */
    void assertContains(final @NotNull String original, final @NotNull String expected);

    /**
     * The original should contain the expected text, regardless of whether it is encoded or not.
     * Convenience method for getting the response text from the client.
     *
     * @param tester the client that contains the response, may be HTML encoded.
     * @param expected the expected text, not encoded.
     */
    void assertResponseContains(final @NotNull WebTester tester, final @NotNull String expected);
}
