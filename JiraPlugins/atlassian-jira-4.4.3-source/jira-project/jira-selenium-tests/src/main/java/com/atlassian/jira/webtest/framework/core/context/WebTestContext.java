package com.atlassian.jira.webtest.framework.core.context;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.PageObjectFactory;
import com.atlassian.jira.webtest.framework.core.ui.WebTestUi;
import com.atlassian.jira.webtest.framework.page.GlobalPages;

/**
 * Encapsulates all context information a web test needs to know. A single entry point into
 * the framework.
 *
 * @since v4.3
 */
public interface WebTestContext
{
    /**
     * A low-level interface for creating components of the framework. Should not be used excessively, i most cases
     * clients should use {@link #getPageObject(Class)} instead.
     *
     * @return page object factory bound to this context
     */
    PageObjectFactory pageObjectFactory();


    /**
     * Get framework component of given type. The component may be created or cached within this context instance.
     *
     * @param componentType class of the component to retrieve
     * @param <P> component type param
     * @return component instance
     * @throws IllegalArgumentException if this context does not support retrieval of components of
     * <tt>componentType</tt>
     */
    <P extends PageObject> P getPageObject(Class<P> componentType);

    /**
     * Collection of JIRA global pages that may be used to start navigating within the test.
     *
     * @see com.atlassian.jira.webtest.framework.page.GlobalPages
     * @see com.atlassian.jira.webtest.framework.page.GlobalPage
     * @see com.atlassian.jira.webtest.framework.page.GlobalPage#goTo() 
     * @return global pages collection instance
     */
    GlobalPages globalPages();

    /**
     * Retrieve information about the browser used by this test.
     *
     * @return browser information
     */
    Browser browser();

    /**
     * Expose APIs for common UI operations performed by web tests, e.g. pressing shortcuts, moving mouse etc.
     *
     * @return an object encapsulating web tests UI operations
     * @see com.atlassian.jira.webtest.framework.core.ui.WebTestUi 
     */
    WebTestUi ui();

}
