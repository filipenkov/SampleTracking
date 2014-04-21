package com.atlassian.jira.pageobjects.config;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.common.base.Function;
import org.junit.runners.model.TestClass;

import javax.annotation.Nullable;

import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Composes operations for JIRA setup.
 *
 * @since v4.4
 */
public final class JiraSetupComposer
{
    public static CompositeTestRunner.Composer compose(JiraTestedProduct product)
    {
        return CompositeTestRunner.compose()
                .beforeTestClass(new SetUpBeforeClassIfNecessary(product))
                .beforeTestMethod(new SetUpIfNecessary(product))
                .beforeTestMethod(new ResetSetUpIfNecessary(product));
    }

    private static abstract class AbstractSetupFunction
    {
        protected final JiraTestedProduct product;
        protected final JiraSetup setup;

        protected AbstractSetupFunction(JiraTestedProduct product)
        {
            this.product = product;
            this.setup = product.injector().getInstance(JiraSetup.class);
        }

        protected final boolean isSetUp()
        {
            return setup.isSetUp().byDefaultTimeout();
        }

        protected final boolean isNotSetUp()
        {
            return not(setup.isSetUp()).byDefaultTimeout();
        }

        protected final boolean shouldSetUp(CompositeTestRunner.BeforeTestMethod beforeTestMethod)
        {
            validate(beforeTestMethod.testClass);
            if (!beforeTestMethod.hasAnnotation(NoSetup.class))
            {
                return true;
            }
            else if (beforeTestMethod.hasAnnotation(Setup.class) && beforeTestMethod.hasAnnotation(NoSetup.class))
            {
                // if @Setup present on method, it takes precedence
                return beforeTestMethod.method.getMethod().isAnnotationPresent(Setup.class);
            }
            else if (beforeTestMethod.hasAnnotation(Setup.class) || !beforeTestMethod.hasAnnotation(NoSetup.class))
            {
                // should setup by default
                return true;
            }
            return false;
        }

        protected final boolean shouldSetUp(CompositeTestRunner.BeforeTestClass beforeTestClass)
        {
            validate(beforeTestClass.testClass);
            // easy - always setup unless NoSetup present
            return !beforeTestClass.hasAnnotation(NoSetup.class);
        }
    }

    private static class SetUpIfNecessary extends AbstractSetupFunction implements Function<CompositeTestRunner.BeforeTestMethod, Void>
    {
        SetUpIfNecessary(JiraTestedProduct product)
        {
            super(product);
        }

        @Override
        public Void apply(@Nullable CompositeTestRunner.BeforeTestMethod beforeTestMethod)
        {
            if (shouldSetUp(beforeTestMethod) && isNotSetUp())
            {
                setup.performSetUp();
            }
            return null;
        }
    }

    private static class SetUpBeforeClassIfNecessary extends AbstractSetupFunction implements Function<CompositeTestRunner.BeforeTestClass, Void>
    {
        SetUpBeforeClassIfNecessary(JiraTestedProduct product)
        {
            super(product);
        }

        @Override
        public Void apply(@Nullable CompositeTestRunner.BeforeTestClass beforeClass)
        {
            if (shouldSetUp(beforeClass) && isNotSetUp())
            {
                setup.performSetUp();
            }
            return null;
        }
    }


    // this will probably never work :(
    private static class ResetSetUpIfNecessary extends AbstractSetupFunction implements
            Function<CompositeTestRunner.BeforeTestMethod, Void>
    {
        ResetSetUpIfNecessary(JiraTestedProduct product)
        {
            super(product);
        }

        @Override
        public Void apply(@Nullable CompositeTestRunner.BeforeTestMethod beforeTestMethod)
        {
            if (!shouldSetUp(beforeTestMethod) && isSetUp())
            {
                setup.resetSetup();
            }
            return null;
        }
    }

    public static void validate(TestClass testClass)
    {
        checkArgument(!doubleSetupAnnotationOnClass(testClass), "Class " + testClass.getName()
                + " cannot annotated with both @Setup and @NoSetup");
    }

    private static boolean doubleSetupAnnotationOnClass(TestClass testClass)
    {
        return testClass.getJavaClass().isAnnotationPresent(Setup.class) && testClass.getJavaClass().isAnnotationPresent(NoSetup.class);
    }

}
