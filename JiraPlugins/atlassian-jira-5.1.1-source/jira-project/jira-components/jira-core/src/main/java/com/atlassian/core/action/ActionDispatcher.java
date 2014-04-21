package com.atlassian.core.action;

import webwork.dispatcher.ActionResult;

import java.util.Map;

/**
 * This was taken from atlassian-webwork1 and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 * @deprecated See individual BackEnd Action or constants in {@link com.atlassian.jira.action.ActionNames} for particular replacement. Normally a Service or Manager class in Pico Dependency Injection container. Since v5.0.
 */
public interface ActionDispatcher
{
    /**
     * @deprecated See individual BackEnd Action or constants in {@link com.atlassian.jira.action.ActionNames} for particular replacement. Normally a Service or Manager class in Pico Dependency Injection container. Since v5.0.
     */
    public ActionResult execute(String actionName) throws Exception;

    /**
     * @deprecated See individual BackEnd Action or constants in {@link com.atlassian.jira.action.ActionNames} for particular replacement. Normally a Service or Manager class in Pico Dependency Injection container. Since v5.0.
     */
    public ActionResult execute(String actionName, Map parameters) throws Exception;
}
