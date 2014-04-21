package com.atlassian.jira.webtest.selenium.framework;

/**
 * Subclasses may provide implementations of this interface to Selenium util methods.
 */
public interface SeleniumClosure
{
    /**
     * Body of the closure.
     *
     * @throws Exception on error
     */
    void execute() throws Exception;
}
