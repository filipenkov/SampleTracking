package com.atlassian.jira.pageobjects.config;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

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
                .beforeTestClass(new BeforeClassWebSudo(false, product));
    }

    /**
     * Global web-sudo disable before class and method so that other setup operations can run smoothly.
     *
     * @param product JIRA product instance
     * @return composer with global websudo disable operations added
     */
    public static CompositeTestRunner.Composer enableIfRequested(JiraTestedProduct product)
    {
        return CompositeTestRunner.compose()
                .beforeTestMethod(triggerWebSudo(true, product, testAnnotatedForBefore()))
                .afterTestMethod(triggerWebSudo(false, product, testAnnotatedForAfter()));
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

    private static Predicate<CompositeTestRunner.BeforeTestMethod> testAnnotatedForBefore()
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

    private static Predicate<CompositeTestRunner.AfterTestMethod> testAnnotatedForAfter()
    {
        return new Predicate<CompositeTestRunner.AfterTestMethod>()
        {
            @Override
            public boolean apply(@Nullable CompositeTestRunner.AfterTestMethod afterTestMethod)
            {
                return afterTestMethod.hasAnnotation(EnableWebSudo.class);
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

    private static class BeforeClassWebSudo implements Function<CompositeTestRunner.BeforeTestClass, Void>
    {
        private boolean targetState;
        private JiraTestedProduct product;
        private static final Set<Class<?>> alreadyRunClasses = Sets.newHashSet();

        public BeforeClassWebSudo(boolean targetState, JiraTestedProduct product)
        {
            this.targetState = targetState;
            this.product = product;
        }

        @Override
        public Void apply(@Nullable CompositeTestRunner.BeforeTestClass input)
        {
            input = checkNotNull(input);
            if (!alreadyRunClasses.contains(input.testClass.getJavaClass()))
            {
                product.getPageBinder().bind(WebSudoControl.class).toogle(targetState);
                alreadyRunClasses.add(input.testClass.getJavaClass());
            }
            return null;
        }
    }
}
