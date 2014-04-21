package com.atlassian.jira.pageobjects.config;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;

/**
 * Provides composable web sudo control operations for
 * {@link com.atlassian.integrationtesting.runner.CompositeTestRunner}.
 *
 * @since v4.4
 */
public class WebSudoComposer
{
    /**
     * Global web-sudo disable before class and method so that other setup operations can run smoothly.
     *
     * @param product JIRA product instance
     * @return composer with global websudo disable operations added
     */
    public static CompositeTestRunner.Composer globalDisable(JiraTestedProduct product)
    {
        return CompositeTestRunner.compose()
                .beforeTestClass(triggerWebSudoBeforeClass(false, product))
                .beforeTestMethod(triggerWebSudoBeforeMethod(false, product));
    }

    /**
     * Global web-sudo disable before class and method so that other setup operations can run smoothly.
     *
     * @param product JIRA product instance
     * @return composer with global websudo disable operations added
     */
    public static CompositeTestRunner.Composer enableIfRequested(JiraTestedProduct product)
    {
        return CompositeTestRunner.compose().beforeTestMethod(triggerWebSudo(true, product, testMethodAnnotated()));
    }

    private static WebSudoControlFunction<CompositeTestRunner.BeforeTestClass> triggerWebSudoBeforeClass(boolean targetState, JiraTestedProduct product)
    {
        return new WebSudoControlFunction<CompositeTestRunner.BeforeTestClass>(targetState, product);
    }

    private static WebSudoControlFunction<CompositeTestRunner.BeforeTestMethod> triggerWebSudoBeforeMethod(boolean targetState, JiraTestedProduct product)
    {
        return new WebSudoControlFunction<CompositeTestRunner.BeforeTestMethod>(targetState, product);
    }

    private static <P> WebSudoControlFunction<P> triggerWebSudo(boolean targetState, JiraTestedProduct product,
            Predicate<P> predicate)
    {
        return new WebSudoControlFunction<P>(targetState, product, predicate);
    }

    private static Predicate<CompositeTestRunner.BeforeTestMethod> testMethodAnnotated()
    {
        return new Predicate<CompositeTestRunner.BeforeTestMethod>()
        {
            @Override
            public boolean apply(@Nullable CompositeTestRunner.BeforeTestMethod beforeTestMethod)
            {
                return beforeTestMethod.hasAnnotation(EnableWebSudo.class);
            }
        };
    }

    private static class WebSudoControlFunction<P> implements Function<P, Void>
    {
        private final boolean targetWebSudoState;
        private final JiraTestedProduct product;
        private final Predicate<P> predicate;

        public WebSudoControlFunction(boolean targetWebSudoState, JiraTestedProduct product,
                Predicate<P> predicate)
        {
            this.targetWebSudoState = targetWebSudoState;
            this.product = product;
            this.predicate = predicate;
        }

        public WebSudoControlFunction(boolean targetWebSudoState, JiraTestedProduct product)
        {
            this(targetWebSudoState, product, Predicates.<P>alwaysTrue());
        }

        @Override
        public Void apply(P input)
        {
            if (predicate.apply(input))
            {
                product.getPageBinder().bind(WebSudoControl.class).toogle(targetWebSudoState);
            }
            return null;
        }
    }

}
