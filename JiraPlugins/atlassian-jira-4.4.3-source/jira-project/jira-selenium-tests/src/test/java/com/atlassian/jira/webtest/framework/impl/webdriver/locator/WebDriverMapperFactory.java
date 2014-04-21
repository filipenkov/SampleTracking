package com.atlassian.jira.webtest.framework.impl.webdriver.locator;

import com.atlassian.jira.webtest.framework.core.locator.Locators;
import com.atlassian.jira.webtest.framework.core.locator.mapper.DefaultLocatorMapper;

/**
 * Creates instances of {@link com.atlassian.jira.webtest.framework.core.locator.mapper.DefaultLocatorMapper}s
 * suitable to use within WebDriver framework implementation that has (as of now) no jQuery locator support.
 *
 * @since v4.3
 */
public final class WebDriverMapperFactory
{
    private WebDriverMapperFactory()
    {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * Create new WebDriver mapper.
     *
     * @return new locator mapper
     */
    public static DefaultLocatorMapper newMapper()
    {
        return new DefaultLocatorMapper().removeAllMappingsOf(Locators.JQUERY);
    }
}
