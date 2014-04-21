package com.atlassian.jira.webtest.framework.core;

/**
 * <p>
 * Enumeration of default timeout types in the framework.
 *
 * <p>
 * <b>NOTE</b>: This enumeration should <b>NOT</b> be extended with further timeout types, unless it is a matter of
 * <i>common</i> (as in: involving more than one developer) consensus that a new timeout type is necessary.
 * Instead, use customized timeouts, if a particular test context requires timeout adjustments (to thet end, use
 * {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition#by(long)}).
 *
 * @since v4.3
 */
public enum Timeouts
{
    /**
     * Interval between consecutive attempts to evaluate state of a particular test component, used e.g. by timed
     * conditions.
     *
     * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
     * @see com.atlassian.jira.webtest.framework.core.PollingQuery
     */
    EVALUATION_INTERVAL,

    /**
     * Use for any actions executed by the test within the page UI, e.g. typing, clicking, moving mouse etc.
     *
     */
    UI_ACTION,

    /**
    * Page load of an average JIRA page.
    *
    */
    PAGE_LOAD,

    /**
    * Page load of a slow JIRA page, e.g. Dashboards, Restore Data, Reindex etc.
    *
    */
    SLOW_PAGE_LOAD,

    /**
     * Dialog load time.
     *
     */
    DIALOG_LOAD,

    /**
     * Load time of an UI-heavy component (e.g. Frother Control)
     *
     */
    COMPONENT_LOAD,

    /**
     * An AJAX-like action on the tested page, e.g. an asynchronous request, a dialog submit (without redirection) etc.  
     *
     */
    AJAX_ACTION
}
