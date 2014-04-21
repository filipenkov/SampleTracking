package com.atlassian.jira.local;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.mock.ComponentManagerMocker;
import com.atlassian.jira.local.runner.ListeningPowerMockRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * <p>
 * A test case base class that can mock the ComponentManager statics within JIRA.
 *
 * <p>
 * Use it whenever you see code like<br>
 * <code>ComponentAccessor.getComponentInstanceOf(...)</code><br>
 * in the tested code and you are not keen on refactoring it.
 *
 * <p>
 * This uses PowerMock API hooked into JUnit4, so in order for your test case to work, you need to use JUnit4 API.
 * Just make your test class extend <tt>MockedComponentManagerTestCase</tt> and provide all usual set up/tear down/test declarations
 * with JUnit4 annotations. No calls to this super class are further required.
 *
 * @since v4.3
 */
@PrepareForTest ( {ComponentManager.class} )
@PowerMockIgnore ("org.apache.log4j.*")
@RunWith(ListeningPowerMockRunner.class)
public abstract class MockedComponentManagerTestCase
{

    private final ComponentManagerMocker mocker = new ComponentManagerMocker();

    @Before
    public final void mockComponentManager() throws Exception
    {
        mocker.doMock();
    }

    /**
     * Use this method in inheriting tests to add given mock to the component manager used in tests. 
     *
     * @param componentClass mocked component class
     * @param compInstance mock instance
     * @param <T> type parameter of the component                       
     */
    protected final <T> void addMock(Class<T> componentClass, T compInstance)
    {
        mocker.addComponent(componentClass, compInstance);
    }
}
