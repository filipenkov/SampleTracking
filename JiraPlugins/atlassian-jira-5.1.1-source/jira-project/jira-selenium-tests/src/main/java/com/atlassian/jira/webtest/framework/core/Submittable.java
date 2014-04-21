package com.atlassian.jira.webtest.framework.core;

/**
 * A page object capable of being submitted (e.g. forms, dialogs etc.)
 *
 * @param <T> type of the target page object of the submit operation (the one that the submit navigates to)
 * @since v4.2
 */
public interface Submittable<T extends PageObject> extends PageObject
{

    /**
     * <p>
     * Submit this page object.
     *
     * <p>
     * <b>NOTE:</b> as with most of the actions in the framework, this <tt>Submittable</tt> instance is only responsible
     * for validating any pre-conditions that may exist for the submit operation (usually there are none), but not
     * the results of it. It is conceivable that clients will attempt to submit page objects that contain invalid form
     * data and, as a result, the submit operation will not result in navigating to the target object. It is up to the
     * clients to validate if they assumptions as to the result of the operation were valid, which may be done by
     * querying appropriate objects participating in the interaction, e.g. calling
     * {@link com.atlassian.jira.webtest.framework.core.PageObject#isReady()}.
     *
     *
     * @return target page object of this submit operation.
     * @see 
     */
    T submit();
}
