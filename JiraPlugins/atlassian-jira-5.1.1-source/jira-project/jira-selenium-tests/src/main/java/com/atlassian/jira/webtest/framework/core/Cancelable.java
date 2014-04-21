package com.atlassian.jira.webtest.framework.core;

/**
 * <p>
 * A page object that is capable of being cancelled.
 *
 * <p>
 * <b>NOTE:</b> as with all 'flow-style' operations, {@link #cancel()} may return an object that is not actually
 * navigated to as a result of the cancel operation. E.g. cancel may work differently if a particular
 * piece of data is entered into its form etc. Clients are responsible for encapsulating this knowledge and
 * validating that the cancel operation result is valid in a particular test context.
 *
 * @param <T> target page object, i.e. the page object that is navigated to as a result of the cancel operation
 * @see com.atlassian.jira.webtest.framework.core.Submittable
 * @since v4.3
 */
public interface Cancelable<T extends PageObject> extends PageObject 
{
    /**
     * Cancel and get the target page object of the cancel operation.
     *
     * @return target page object of this cancel operation
     */
    T cancel();
}
