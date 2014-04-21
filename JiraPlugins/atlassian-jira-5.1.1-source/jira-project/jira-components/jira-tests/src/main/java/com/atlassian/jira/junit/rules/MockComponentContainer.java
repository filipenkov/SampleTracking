package com.atlassian.jira.junit.rules;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit @Rule that allows for providing mcok JIRA components accessed in production code via
 * {@link com.atlassian.jira.component.ComponentAccessor} static methods.
 *
 * <p/>
 * Example usage (in your test classes):
 *
 * <code>
 *     &#64;Rule public MockComponentContainer container = new MockComponentContainer();
 *
 *     &#64;Before
 *     public void addMocks()
 *     {
 *         container.addMock(ServiceOne.class, mockServiceOne).addMock(ServiceTwo.class, mockServiceTwo);
 *     }
 *
 *     &#64;Test
 *     public void testCodeThatUsesComponentAccessor()
 *     {
 *         assertSame(mockServiceOne, ComponentAccessor.getComponent(ServiceOne.class));
 *         // etc.
 *     }
 * </code>
 *
 * @since 5.1
 */
public class MockComponentContainer extends TestWatcher
{
    private final MockComponentWorker mockWorker = new MockComponentWorker();

    public <I, C extends I> MockComponentContainer addMockComponent(Class<I> componentInterface, C mockComponentImplementation)
    {
        mockWorker.addMock(componentInterface, mockComponentImplementation);
        return this;
    }

    public <I, C extends I> MockComponentContainer addMock(Class<I> componentInterface, C mockComponentImplementation)
        {
            return addMockComponent(componentInterface, mockComponentImplementation);
        }

    @Override
    protected void starting(Description description)
    {
        ComponentAccessor.initialiseWorker(mockWorker);
    }

    @Override
    protected void finished(Description description)
    {
        ComponentAccessor.initialiseWorker(null); // reset
    }



}
