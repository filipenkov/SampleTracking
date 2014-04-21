package com.atlassian.jira.webtest.framework.core.locator;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * <p>
 * Represents an element (existing or not-existing) on a page in given test context. May be used to query
 * existence and some properties of an arbitrary element on a tested page.
 *
 * <p>
 * Elements are bound to and may be retrieved via {@link Locator}s.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.locator.Locator
 */
public interface Element
{

    /* ---------------------------------------------- CONDITIONS ---------------------------------------------------- */

    /**
     * A condition representing existence of this locator on a page.
     *
     * @return timed condition representing existence of this element
     */
    TimedCondition isPresent();

    /**
     * A condition representing non-existence of this locator on a page.
     *
     * @return timed condition representing non-existence of this element
     *
     */
    TimedCondition isNotPresent();

    /**
     * A condition representing visibility of this locator on a page.
     *
     * @return timed condition representing visibility of this element
     *
     */
    TimedCondition isVisible();

    /**
     * A condition representing non-visibility of this locator on a page.
     *
     * @return timed condition representing non-visibility of this element
     *
     */
    TimedCondition isNotVisible();

    /**
     * A condition representing a query of this element in terms of it containing a particular <tt>text</tt>.
     *
     * @param text text to check against
     * @return timed condition representing a query of text contents of this element
     */
    TimedCondition containsText(String text);

    /**
     * A condition representing a query of this element in terms of it <b>NOT</b> containing a particular <tt>text</tt>.
     *
     * @param text text to check against
     * @return timed condition representing a query of text contents of this element
     */
    TimedCondition doesNotContainText(String text);


    /* ------------------------------------------------- QUERIES ---------------------------------------------------- */

    /**
     * TimedQuery for the element's value. This element must be present before this query returns (and within
     * the query's timeout).
     *
     * @return timed query for this element's value, <code>null</code> if this element is not present or has no value
     * and the timeout expires.
     */
    TimedQuery<String> value();


    /**
     * TimedQuery for the element's text (if any). This element must be present before this query returns (and within
     * the query's timeout).
     *
     * @return timed query for this element's text. <code>null</code> if this element is not present or has no text
     * and the timeout expires.
     */
    TimedQuery<String> text();

    /**
     * TimedQuery for the element's given attribute (if any). This element must be present before this query returns
     * (within the query's timeout).
     *
     * @param attrName name of the attribute
     * @return timed query for this element's attribute. <code>null</code> if this element is not present or has no
     * such attribute and the timeout expires.
     */
    TimedQuery<String> attribute(String attrName);

    /* ------------------------------------------------- ACTIONS ---------------------------------------------------- */
    
    /**
     * Type given key sequence into the element
     *
     * @param keys key sequence to type in
     * @return this element instance
     * @throws IllegalStateException if the element does not allow, or support typing (e.g. is not present on the page).
     */
    Element type(KeySequence keys);

    /**
     * If this element is an input, this will clear out it's value. Otherwise it will not have any effect.
     *
     * @return this element
     */
    Element clear();

    /**
     * Click this element
     *
     * @return this element instance
     * @throws IllegalStateException if the element does not allow, or support clicking (e.g. is not present on the page).
     */
    Element click();

}
