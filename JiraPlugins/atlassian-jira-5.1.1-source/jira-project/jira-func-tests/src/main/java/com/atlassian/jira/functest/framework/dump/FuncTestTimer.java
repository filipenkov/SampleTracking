package com.atlassian.jira.functest.framework.dump;

/**
 * A timer that can time certain FuncTest operations
 *
 * @since v4.1
 */
public interface FuncTestTimer
{
    /**
     * @return the name of the Timer
     */
    String getName();

    /**
     * Called to end the timer.
     * @return the number of milliseconds since the timer was created
     */
    long end();
}
