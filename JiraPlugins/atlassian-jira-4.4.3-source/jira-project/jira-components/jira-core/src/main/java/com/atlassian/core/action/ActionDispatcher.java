package com.atlassian.core.action;

import webwork.dispatcher.ActionResult;

import java.util.Map;

/**
 * This was taken from atlassian-webwork1 and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public interface ActionDispatcher
{
    public ActionResult execute(String actionName) throws Exception;

    public ActionResult execute(String actionName, Map parameters) throws Exception;
}
