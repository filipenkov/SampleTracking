package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.page.GlobalPage;

/**
 * Responsible for creating page objects being the entry point into the framework.
 *
 * @since v4.3
 */
public interface PageObjectFactory
{
    /**
     * Create a global page of given <tt>pageType</tt>.
     *
     * @param pageType class representing the <b>interface</b> of the desired global page.
     * @param <T> type of the global page to create
     * @return a global page instance that is a valid implementation of the {@link com.atlassian.jira.webtest.framework.page.GlobalPage}
     * interface in the test context represented by this factory
     * @throws IllegalArgumentException if <tt>pageType</tt> does not represent an abstract interface of the desired global page,
     * or this factory is not able to provide implementations of the interface represented by <tt>pageType</tt>. At the very least,
     * every factory should support default global pages specified in the {@link com.atlassian.jira.webtest.framework.page.GlobalPages}
     * enumeration
     *
     * @see com.atlassian.jira.webtest.framework.page.GlobalPages
     */
    <T extends GlobalPage> T createGlobalPage(Class<T> pageType);

    /**
     * Create locator given a locator <tt>type</tt> and <tt>value</tt>.
     *
     * @param type type of the desired locator
     * @param value value of the desired locator
     * @return new locator
     * @throws IllegalArgumentException if <tt>type</tt> is not recoginzed by this factory, or <tt>type</tt> and
     * <tt>value</tt> are deemed incompatible for the locator created by this factory
     *
     * @see com.atlassian.jira.webtest.framework.core.locator.Locator
     */
    Locator createLocator(LocatorType type, String value);

    /**
     * Create any page object component of the framework
     *
     * @param type component class
     * @param <P>  type of the component
     * @return component instance
     * @throws IllegalArgumentException if component type <tt>P</tt> is not supported by this factory
     */
    <P extends PageObject> P createPageObject(Class<P> type);
}
