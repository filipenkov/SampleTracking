package com.atlassian.jira.junit.rules;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Initializes Mocktio mocks before the tests.
 *
 * @since 5.1
 */
public class InitMockitoMocks extends TestWatcher
{
    private final Object test;

    public InitMockitoMocks(Object test)
    {
        this.test = test;
    }

    @Override
    protected void starting(Description description)
    {
        MockitoAnnotations.initMocks(test);
    }

    @Override
    protected void finished(Description description)
    {
        Mockito.validateMockitoUsage();
    }
}
