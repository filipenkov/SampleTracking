package com.atlassian.jira.functest.framework.log;

/**
 * Implementors of this interface can log information to the
 * func test log.
 *
 * @since v3.13
 */
public interface FuncTestLogger
{
    /**
     * This will data via a String.valueOf(logData) in the specified object.
     *
     * @param logData the objct to log via String.valueOf().
     */
    public void log(Object logData);

    public void log(Throwable t);

}
