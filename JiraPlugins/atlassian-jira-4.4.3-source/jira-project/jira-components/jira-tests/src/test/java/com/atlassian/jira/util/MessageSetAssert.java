package com.atlassian.jira.util;

import junit.framework.Assert;

/**
 * Defines assertions on the MessageSet type that can be used to make Unit tests more concise and readable.
 *
 *
 * @since v3.13
 */
public class MessageSetAssert
{

    /**
     * Asserts that there is exactly one Error message in the given MessageSet, and that it is as expected.
     * This assertion will fail if there are any warnings present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assert1ErrorNoWarnings(final MessageSet messageSet, final String expectedErrorMessage)
    {
        Assert.assertEquals("Expected exactly one message in the given MessageSet, but found " + messageSet.getErrorMessages().size(),
                1, messageSet.getErrorMessages().size());
        Assert.assertEquals(expectedErrorMessage, messageSet.getErrorMessages().iterator().next());
        Assert.assertTrue("Expected one error, but also found a warning.", messageSet.getWarningMessages().isEmpty());
    }

    /**
     * Asserts that there is exactly one Error message in the given MessageSet, and that it is as expected.
     * This assertion does not care if there are any warnings present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assert1Error(final MessageSet messageSet, final String expectedErrorMessage)
    {
        Assert.assertEquals("Expected exactly one message in the given MessageSet, but found " + messageSet.getErrorMessages().size(),
                1, messageSet.getErrorMessages().size());
        Assert.assertEquals(expectedErrorMessage, messageSet.getErrorMessages().iterator().next());
    }

    /**
     * Asserts that there is exactly one Warning message in the given MessageSet, and that it is as expected.
     * This assertion will fail if there are any errors present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedWarningMessage The expected warning message.
     */
    public static void assert1WarningNoErrors(final MessageSet messageSet, final String expectedWarningMessage)
    {
        Assert.assertEquals("Expected exactly one message in the given MessageSet, but found " + messageSet.getWarningMessages().size(),
                1, messageSet.getWarningMessages().size());
        Assert.assertEquals(expectedWarningMessage, messageSet.getWarningMessages().iterator().next());
        Assert.assertTrue("Expected one warning, but also found an error.", messageSet.getErrorMessages().isEmpty());
    }

    /**
     * Asserts that there is exactly one Warning message in the given MessageSet, and that it is as expected.
     * This assertion will fail if there are any errors present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedWarningMessage The expected warning message.
     */
    public static void assert1Warning(final MessageSet messageSet, final String expectedWarningMessage)
    {
        Assert.assertEquals("Expected exactly one message in the given MessageSet, but found " + messageSet.getWarningMessages().size(),
                1, messageSet.getWarningMessages().size());
        Assert.assertEquals(expectedWarningMessage, messageSet.getWarningMessages().iterator().next());
    }

    /**
     * Asserts that there are no messages at all in the given MessageSet.
     * This assertion will fail if there are either errors or warnings present.
     * The failure message will include the first message found whcih may help the developer debug the reason for the failure.
     *
     * @param messageSet MessageSet to test.
     */
    public static void assertNoMessages(final MessageSet messageSet)
    {
        if (messageSet.getErrorMessages().size() > 0)
        {
            // We are using colon and angle brackets in the failure message to be consistent with JUnit's assertEquals() message.
            Assert.fail("Expected no messages but found Error Message:<" + messageSet.getErrorMessages().iterator().next() + ">");
        }
        if (messageSet.getWarningMessages().size() > 0)
        {
            Assert.fail("Expected no messages but found Warning Message:<" + messageSet.getWarningMessages().iterator().next() + ">");
        }
    }

    public static void assertNoWarnings(final MessageSet messageSet)
    {
        if (messageSet.getWarningMessages().size() > 0)
        {
            // We are using colon and angle brackets in the failure message to be consistent with JUnit's assertEquals() message.
            Assert.fail("Expected no warnings but found Warning Message:<" + messageSet.getWarningMessages().iterator().next() + ">");
        }
    }

    public static void assertNoErrors(final MessageSet messageSet)
    {
        if (messageSet.getErrorMessages().size() > 0)
        {
            // We are using colon and angle brackets in the failure message to be consistent with JUnit's assertEquals() message.
            Assert.fail("Expected no errors but found Error Message:<" + messageSet.getErrorMessages().iterator().next() + ">");
        }
    }
}
