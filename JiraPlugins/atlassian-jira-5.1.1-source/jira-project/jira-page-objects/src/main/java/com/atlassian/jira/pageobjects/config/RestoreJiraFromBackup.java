package com.atlassian.jira.pageobjects.config;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.AfterTestMethod;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.BeforeTestClass;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.BeforeTestMethod;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.Composer;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * TODO this is almost 1 to 1 copy from AIT we need to find way to reuse it!
 *
 */
public final class RestoreJiraFromBackup
{

    public static Composer compose(JiraTestedProduct product)
    {
        return CompositeTestRunner.compose().
            beforeTestClass(new BeforeClass(product)).
            beforeTestMethod(new BeforeMethod(product)).
            afterTestMethod(new AfterMethod(product));
    }
    
    private static final class BeforeClass implements Function<BeforeTestClass, Void>
    {
        private final JiraTestedProduct product;
        private static final Set<Class<?>> alreadyRunClasses = Sets.newHashSet();

        public BeforeClass(JiraTestedProduct product)
        {
            this.product = product;
        }

        public Void apply(BeforeTestClass test)
        {
            if (test.hasAnnotation(RestoreOnce.class))
            {
                if (test.hasAnnotation(Restore.class))
                {
                    throw new RuntimeException("Both @Restore and @RestoreOnce found on class. Only one should be present.");
                }
                if (!alreadyRunClasses.contains(test.testClass.getJavaClass()))
                {
                    RestoreOnce restoreOnce = test.getAnnotation(RestoreOnce.class);
                    product.injector().getInstance(RestoreJiraData.class).execute(restoreOnce.value());
                    alreadyRunClasses.add(test.testClass.getJavaClass());
                }
            }
            return null;
        }
    }

    private static final class BeforeMethod implements Function<BeforeTestMethod, Void>
    {
        private final JiraTestedProduct product;

        public BeforeMethod(JiraTestedProduct product)
        {
            this.product = product;
        }

        public Void apply(BeforeTestMethod test)
        {
            if (test.hasAnnotation(Restore.class))
            {
                Restore restore = test.getAnnotation(Restore.class);
                product.injector().getInstance(RestoreJiraData.class).execute(restore.value());
            }
            return null;
        }
    }
    
    private static final class AfterMethod implements Function<AfterTestMethod, Void>
    {
        private final JiraTestedProduct product;

        public AfterMethod(JiraTestedProduct product)
        {
            this.product = product;
        }

        public Void apply(AfterTestMethod test)
        {
            if (test.hasAnnotation(Restore.class) && test.hasAnnotation(RestoreOnce.class))
            {
                product.injector().getInstance(RestoreJiraData.class).execute(test.getAnnotation(RestoreOnce.class).value());
            }
            return null;
        }
    }
}
