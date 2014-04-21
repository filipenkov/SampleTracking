package com.atlassian.jira.webtest.selenium.framework.core;

import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;

/**
 * A poge object capable of being submitted (e.g. forms, dialogs etc.)
 *
 * @param <T> type of this page object returned by the submit
 * @since v4.2
 */
@Deprecated
public interface Submittable<T extends PageObject> extends PageObject
{

    /**
     * Type of action happening after submitting this page object.
     *
     * @return action type
     * @see com.atlassian.jira.webtest.selenium.framework.model.ActionType
     */
    ActionType afterSubmit();

    /**
     * Submit this page object.
     *
     * @param submitType type of submit action to perform
     * @param waitForAction if <code>true</code>, the method will perform wait for this object's after submit action
     * to happen
     * @return this page object
     * @see #afterSubmit()
     */
    T submit(SubmitType submitType, boolean waitForAction);

    /**
     * Submit this page object and wait for the after submit action. Equivalent to invoking #submit(true).
     *
     * @param submitType type of submit action to perform 
     * @return this page object
     */
    T submit(SubmitType submitType);
}
