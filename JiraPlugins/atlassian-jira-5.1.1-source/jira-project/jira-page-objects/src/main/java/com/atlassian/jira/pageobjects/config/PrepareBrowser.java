package com.atlassian.jira.pageobjects.config;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Collection of functions to prepare browser for the test.
 *
 * @since v5.0
 */
public final class PrepareBrowser
{
    private PrepareBrowser()
    {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * Composer to clean up cookies.
     *
     * @param product JIRA product instance
     * @return composer to clean up test cookies
     */
    public static CompositeTestRunner.Composer cleanUpCookies(final JiraTestedProduct product)
    {
        return CompositeTestRunner.compose().beforeTestMethod(new Function<CompositeTestRunner.BeforeTestMethod, Void>()
        {
            @Override
            public Void apply(@Nullable CompositeTestRunner.BeforeTestMethod from)
            {
                product.getTester().getDriver().manage().deleteAllCookies();
                return null;
            }
        });
    }

    /**
     * Maximize browser window.
     *
     * @param product JIRA product instance
     * @return composer to maximize browser window before the test
     */
    public static CompositeTestRunner.Composer maximizeWindow(final JiraTestedProduct product)
    {
        return CompositeTestRunner.compose().beforeTestMethod(new Function<CompositeTestRunner.BeforeTestMethod, Void>()
        {
            @Override
            public Void apply(CompositeTestRunner.BeforeTestMethod from)
            {
                product.getTester().getDriver().manage().window().maximize();
                return null;
            }
        });
    }
}
