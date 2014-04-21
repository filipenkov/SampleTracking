package com.atlassian.jira.pageobjects.config.junit4;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.functest.framework.suite.JUnit4WebTestListener;
import com.atlassian.jira.functest.framework.suite.RunnerChildList;
import com.atlassian.jira.functest.framework.suite.SuiteTransform;
import com.atlassian.jira.functest.framework.suite.TransformableRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.JiraSetupComposer;
import com.atlassian.jira.pageobjects.config.PageObjectsInjector;
import com.atlassian.jira.pageobjects.config.PrepareBrowser;
import com.atlassian.jira.pageobjects.config.RestoreJiraFromBackup;
import com.atlassian.jira.pageobjects.config.WebSudoComposer;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Extension of {@link com.atlassian.integrationtesting.runner.CompositeTestRunner} for JIRA.
 *
 * <p>
 * This runner applies the following operations on the tested class instance:
 * <ul>
 *     <li>inject static and instance components of the page objects library
 *     <li>set up JIRA if necessary, as per {@link com.atlassian.jira.pageobjects.config.Setup} and
 *     {@link com.atlassian.jira.pageobjects.config.NoSetup} annotations
 *     <li>disable web sudo
 *     <li>restore data as per {@link com.atlassian.integrationtesting.runner.restore.RestoreOnce} and
 *     {@link com.atlassian.integrationtesting.runner.restore.Restore} annotations
 *     <li>enable web sudo as per {@link com.atlassian.jira.pageobjects.config.EnableWebSudo} annotation
 * </ul>
 *
 * <p>
 * Optionally a list of run listeners may provided that will be called for each test run by this runner.
 *
 * <p>
 * NOTE: This runner is not suitable to use with the {@link org.junit.runner.RunWith} annotation. It is designed to
 * be used by higher-level parent runner (e.g. in {@link org.junit.runners.Suite}, by means of
 * {@link org.junit.runners.model.RunnerBuilder}), or extended by runners that provide
 * {@link com.atlassian.jira.pageobjects.JiraTestedProduct} instance and implement the
 * {@link org.junit.runner.RunWith}-compatible constructor.
 *
 * @since 4.4
 */
public class JiraWebTestRunner extends CompositeTestRunner implements TransformableRunner<JiraWebTestRunner>
{
    public static Composer jiraComposer(JiraTestedProduct product, Iterable<RunListener> listeners)
    {
        return compose()
                .beforeTestClass(new AddListeners(listeners))
                .afterTestClass(new RemoveListeners(listeners))
                .from(PageObjectsInjector.compose(product))
                .from(PrepareBrowser.cleanUpCookies(product))
                .from(PrepareBrowser.maximizeWindow(product))
                .from(JiraSetupComposer.compose(product))
                .from(WebSudoComposer.globalDisable(product))
                .from(RestoreJiraFromBackup.compose(product))
                .from(WebSudoComposer.enableIfRequested(product));
    }

    private final List<RunListener> listeners = Lists.newArrayList();
    private final List<SuiteTransform> transforms = Lists.newArrayList();
    private final JiraTestedProduct product;

    public JiraWebTestRunner(Class<?> klass, JiraTestedProduct product, Iterable<RunListener> listeners) throws InitializationError
    {
        this(klass, product, listeners, Collections.<SuiteTransform>emptyList());
    }

    private JiraWebTestRunner(Class<?> klass, JiraTestedProduct product, Iterable<RunListener> listeners,
            Iterable<SuiteTransform> transforms) throws InitializationError
    {
        super(klass, jiraComposer(product, listeners));
        this.product = product;
        Iterables.addAll(this.listeners, listeners);
        injectStuffToListeners(product);
        Iterables.addAll(this.transforms, transforms);
    }

    private void injectStuffToListeners(JiraTestedProduct product)
    {
        for (RunListener runListener : listeners)
        {
            product.injector().injectMembers(runListener);
            if (runListener instanceof JUnit4WebTestListener)
            {
                 product.injector().injectMembers(((JUnit4WebTestListener)runListener).webTestListener());
            }
        }
    }

    public JiraWebTestRunner(Class<?> klass, JiraTestedProduct product) throws InitializationError
    {
        this(klass, product, Collections.<RunListener>emptyList());
    }

    public JiraWebTestRunner withTransforms(List<SuiteTransform> transforms) throws InitializationError
    {
        return new JiraWebTestRunner(getTestClass().getJavaClass(), product, listeners, transforms);
    }

    @Override
    protected List<FrameworkMethod> getChildren()
    {
        final List<FrameworkMethod> children = super.getChildren();
        final List<Description> descriptions = Lists.transform(children, new Function<FrameworkMethod, Description>()
        {
            @Override
            public Description apply(@Nullable FrameworkMethod from)
            {
                return describeChild(from);
            }
        });
        return RunnerChildList.matchingChildren(children, descriptions, transforms);
    }

    private static final class AddListeners implements Function<BeforeTestClass, Void>
    {
        private final Iterable<RunListener> listeners;

        public AddListeners(Iterable<RunListener> listeners)
        {
            this.listeners = listeners;
        }

        @Override
        public Void apply(@Nullable BeforeTestClass from)
        {
            for (RunListener listener : listeners)
            {
                from.notifier.addListener(listener);
            }
            return null;
        }
    }

    private static final class RemoveListeners implements Function<AfterTestClass, Void>
    {
        private final Iterable<RunListener> listeners;

        public RemoveListeners(Iterable<RunListener> listeners)
        {
            this.listeners = listeners;
        }

        @Override
        public Void apply(@Nullable AfterTestClass from)
        {
            for (RunListener listener : listeners)
            {
                from.notifier.removeListener(listener);
            }
            return null;
        }
    }
}
