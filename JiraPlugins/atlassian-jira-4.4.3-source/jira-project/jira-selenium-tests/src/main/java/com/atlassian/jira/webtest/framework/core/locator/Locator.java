package com.atlassian.jira.webtest.framework.core.locator;

/**
 * <p>
 * Locator is used to locate page object in the current test context. Locator is uniquely identified by its type
 * (one of default types defined in {@link com.atlassian.jira.webtest.framework.core.locator.Locators}, or a custom one)
 * and value.
 *
 * <p>
 * Clients may examine and act upon page elements located by locators by means of the {@link #element()} method.
 *
 * <p>
 * Two distinct locator instances may, or may not be compatible with each other, which is not necessarily a matter of
 * their type. This may be examined by means of the
 * {@link #supports(com.atlassian.jira.webtest.framework.core.locator.Locator)} method. Compatibility is used to determined
 * if a, whether one locator may be nested within another, which is done by means of the
 * {@link #combine(com.atlassian.jira.webtest.framework.core.locator.Locator)} method.
 *
 * <p>
 * Note that so defined compatibility is not a symmetric relation, e.g. a jQuery locator may be able to nest an ID locator
 * within it, but a non-sophisticated ID locator might not accept a jQuery locator to nest within itself (even though it
 * is possible and trivial to implement). It follows that a.supports(b) == true does not imply b.support(a) == true.
 *
 * @since v4.2
 */
public interface Locator extends LocatorData
{
    /**
     * <p>
     * Retrieve element corresponding to this locator.
     *
     * <p>
     * In case the locator locates more then one element, implementations are allowed to:
     * <ul>
     * <li>return <b>only</b> the <b>first</b> of the located page elements
     * <li>throw an <b>IllegalStateException</b>
     * </ul>
     * In most cases locating more than one element is a either a mistake on the side of test developer, or a result
     * of an invalid page (e.g. two elements with the same ID). 
     *
     * @return element this locator on a page in the current test context
     * @throws IllegalStateException implementations are allowed to throw it if this locator locates more than one element
     */
    Element element();

    /**
     * <p>
     * Verify if the <tt>other</tt> locator is supported by this locator. This means that <tt>other</tt> may be
     * used in the {@link #combine(com.atlassian.jira.webtest.framework.core.locator.Locator)} method of this
     * locator.
     *
     * <p>
     * This usually involves type compatibility between the locators and optionally compatibility of the particular
     * locator instances in the current test context.
     *
     * <p>
     * Example: a jQuery locator would possibly return <code>true</code> for another jQuery locator, as well as an id
     * and a class locator, but might return false for an xpath locator, unless it is a very sophisticated implementation
     * capable of translating xpath into jQuery/css.
     *
     * @param other the other locator checked for compatibility
     * @return <code>true</code>, if this locator instance is compatible with the <tt>other</tt> in the current context 
     */
    boolean supports(Locator other);

    /**
     * <p>
     * Given <tt>toNest</tt>, return a new locator that represents an element located by <tt>toNest</tt> and nested within
     * the element represented by this locator.
     *
     * <p>
     * <tt>toNest</tt> must be compatible with this locator, which may be examined by means of the
     * {@link #supports(com.atlassian.jira.webtest.framework.core.locator.Locator)} method.
     *
     * @param toNest locator to nest within this locator
     * @return new locator representing the nested locator. Most likely (but not necessarily) it will have the same type
     * as this locator
     * @throws LocatorIncompatibleException if <tt>toNest</tt> is incompatible with this locator
     * (i.e. {@link #supports(com.atlassian.jira.webtest.framework.core.locator.Locator)} returns <code>false<code>
     * for <tt>toNest</tt>.
     */
    Locator combine(Locator toNest);
}
