package com.atlassian.jira.local;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessorWorker;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.util.FileFactory;
import com.opensymphony.user.UserManager;

import java.util.Collection;

import static org.easymock.classextension.EasyMock.createNiceMock;

/**
 * <p/>
 * Anything would be better than this base class.  Its SLOW, SLOW, SLOW.
 *
 * <p/>
 * The only use for this test class is when you are testing say OfBiz Stores.
 *
 * @deprecated since v4.0. Use {@link MockControllerTestCase} instead or just plain {@link ListeningTestCase}
 */
public abstract class LegacyJiraMockTestCase extends Junit3ListeningTestCase
{
    static
    {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    /**
     * This will be set to true if one or more JiraMockTestCase have actually been run
     */
    static boolean jiraMockTestCaseHasBeenRun = false;
    protected FileFactory fileFactory;

    protected LegacyJiraMockTestCase()
    {
    }

    public LegacyJiraMockTestCase(String s)
    {
        super(s);
    }

    @Override
    public void runBare() throws Throwable
    {
        //
        // In fast mode we can do all tests in 43 seconds otherwise its 300 seconds
        //
        // LegacyJiraMockTestCase is the culprit, especially its setUp() method
        //
        if (!Boolean.getBoolean("unit.test.fastmode"))
        {
            super.runBare();
        }
        else
        {
            System.err.printf("*** Not running slow test case %s because unit.test.fastmode=true\n", getClass().getSimpleName() + "." + getName());

        }
    }

    protected void setUp() throws Exception
    {
        jiraMockTestCaseHasBeenRun = true;
        fileFactory = createNiceMock(FileFactory.class);

        // Ensure the UserManager and static crowd service factory are initialised with the Mock service
        new StaticCrowdServiceFactory(new MockCrowdService());
        // Ensure we throw away the old singleton and create a new one with the new MockCrowdService for each unit test.
        UserManager.reset();

        UtilsForTestSetup.mockTestCaseSetup(getServiceOverrider());
        UtilsForTestSetup.configureOsWorkflow();

        ComponentAccessor.initialiseWorker(new ComponentAccessorWorker());
    }

    private UtilsForTestSetup.ServiceOverrider getServiceOverrider()
    {
        return new UtilsForTestSetup.ServiceOverrider()
        {
            public void override()
            {
                overrideServices();
            }
        };
    }

    /**
     * Override services by setting {@link ManagerFactory#addService(Class, Object)} for any objects you want to
     * override
     */
    protected void overrideServices()
    {
        ManagerFactory.addService(SharedEntityIndexer.class, new MockSharedEntityIndexer());
        ManagerFactory.addService(FileFactory.class, fileFactory); // don't let tests get at the real file system
    }


    protected void tearDown() throws Exception
    {
        UtilsForTestSetup.mockTestCaseTearDown();

        // Clean up any users that might be hanging about in the MockCrowdService
        UtilsForTests.cleanUsers();

        super.tearDown();
    }

    /**
     * Check that a collection has only one element, and that is the object provided
     */
    protected void checkSingleElementCollection(Collection collection, Object expected)
    {
        assertEquals(1, collection.size());
        assertEquals(expected, collection.iterator().next());
    }

    /**
     * Checks that the given collection contains only the objects in the other collection.
     */
    public void assertContainsOnly(Collection collection, Collection other)
    {
        assertEquals(collection.size(), other.size());
        assertTrue(collection.containsAll(other));
    }

    /**
     * Asserts that the given object is the only thing in the given collection.
     */
    public void assertContainsOnly(Object expected, Collection other)
    {
        assertContainsOnly(EasyList.build(expected), other);
    }
}
