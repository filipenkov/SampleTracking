package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;


/**
 * <p>
 * Represents a page object that may be opened and closed.
 *
 *
 * @param <T> type of this openable component
 * @since v4.3
 */
public interface Openable<T extends PageObject> extends PageObject
{
    /**
     * Checks if this component is open.
     *
     * @return timed condition representing a query if this component is open
     * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
     */
    TimedCondition isOpen();

    /**
     * A timed condition that queries, whether this component is currently closed.
     *
     * @return timed condition: is this component closed?
     * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
     */
    TimedCondition isClosed();

    /**
     * <p>
     * Represents a question: is this component capable of being opened in the current test context?
     *
     * <p>
     * If the returned condition evaluates to <code>true</code>, this component may be safely opened by means of
     * {@link #open()}.
     *
     * <p>
     * NOTE: if {@link #isOpen()} evaluates to <code>true</code>, this condition will automatically evaluate to
     * <code>false<code>, as it is not possible to open a component that is already open.
     *
     * @return timed condition: is this component openable in the current context? 
     */
    TimedCondition isOpenable();


    /**
     * <p>
     * Open this component and return this instance.
     *
     * <p>
     * A pre-condition for this action is that the {@link #isClosed()} and {@link #isOpenable()} condition is met.
     *
     * <p>
     * <b>NOTE:</b> this method, like most action methods in the framework, does not verify its <i>results</i> (as opposed
     * to pre-conditions), as in some cases it might be legitimate to attempt to open a component given the test context
     * state deliberately set up to make this attempt fail and then verify that the failure did happen. Thus, invoking
     * clients are responsible for validation results of this action, which may be done by calling {@link #isOpen()}.
     *
     * @return timed query for this component's instance
     */
    T open();
}
