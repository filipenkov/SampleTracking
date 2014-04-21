package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueSecurityLevelResolver extends MockControllerTestCase
{
    private User theUser;
    private boolean overrideSecurity = false;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
    }

    @After
    public void tearDown() throws Exception
    {
        theUser = null;

    }

    @Test
    public void testGetAllSecurityLevels() throws Exception
    {
        List<GenericValue> expectedList = ImmutableList.<GenericValue>of(createMockSecurityLevel(4L, "4"), createMockSecurityLevel(5L, "5"));

        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        EasyMock.expect(manager.getAllUsersSecurityLevels(theUser)).andReturn(expectedList);

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        List<GenericValue> actualList = resolver.getAllSecurityLevels(theUser);
        assertEquals(expectedList, actualList);

        mockController.verify();
    }

    @Test
    public void testGetAllSecurityLevelsException() throws Exception
    {
        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        EasyMock.expect(manager.getAllUsersSecurityLevels(theUser)).andThrow(new GenericEntityException());

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        List<GenericValue> actualList = resolver.getAllSecurityLevels(theUser);
        assertTrue(actualList.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelByIdHappyPath() throws Exception
    {
        final MockGenericValue expectedLevel = createMockSecurityLevel(1L, "1");
        final Collection<GenericValue> levels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(3L, "3"),
                createMockSecurityLevel(2L, "2"),
                expectedLevel
        );

        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        manager.getAllUsersSecurityLevels(theUser);
        mockController.setReturnValue(levels);

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        final GenericValue level = resolver.getIssueSecurityLevelById(theUser, overrideSecurity, 1L);
        assertEquals(expectedLevel, level);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelByIdOverrideSecurity() throws Exception
    {
        overrideSecurity = true;
        final MockGenericValue expectedLevel = createMockSecurityLevel(1L, "1");
        final Collection<GenericValue> levels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(3L, "3"),
                createMockSecurityLevel(2L, "2"),
                expectedLevel
        );

        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        EasyMock.expect(manager.getAllSecurityLevels())
                .andReturn(levels);

        replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        final GenericValue level = resolver.getIssueSecurityLevelById(theUser, overrideSecurity, 1L);
        assertEquals(expectedLevel, level);
    }

    @Test
    public void testGetIssueSecurityLevelByIdNotFound() throws Exception
    {
        final Collection<GenericValue> levels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(3L, "3"),
                createMockSecurityLevel(2L, "2"),
                createMockSecurityLevel(1L, "1")
        );

        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        manager.getAllUsersSecurityLevels(theUser);
        mockController.setReturnValue(levels);

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        final GenericValue level = resolver.getIssueSecurityLevelById(theUser, overrideSecurity, 4L);
        assertNull(level);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelByIdThrowsException() throws Exception
    {
        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        manager.getAllUsersSecurityLevels(theUser);
        mockController.setThrowable(new GenericEntityException());

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        assertNull(resolver.getIssueSecurityLevelById(theUser, overrideSecurity, 1L));

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsByNameHappyPath() throws Exception
    {
        final Collection<GenericValue> expectedLevels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(3L, "3"),
                createMockSecurityLevel(2L, "2"),
                createMockSecurityLevel(1L, "TheName")
        );

        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        manager.getUsersSecurityLevelsByName(theUser, "TheName");
        mockController.setReturnValue(expectedLevels);

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        final Collection<GenericValue> levels = resolver.getIssueSecurityLevelsByName(theUser, overrideSecurity, "TheName");
        assertEquals(expectedLevels, levels);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsByNameOverrideSecurity() throws Exception
    {
        overrideSecurity = true;
        final Collection<GenericValue> expectedLevels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(3L, "3"),
                createMockSecurityLevel(2L, "2"),
                createMockSecurityLevel(1L, "TheName")
        );

        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        EasyMock.expect(manager.getSecurityLevelsByName("TheName"))
                .andReturn(expectedLevels);

        replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        final Collection<GenericValue> levels = resolver.getIssueSecurityLevelsByName(theUser, overrideSecurity, "TheName");
        assertEquals(expectedLevels, levels);
    }

    @Test
    public void testGetIssueSecurityLevelsByNameThrowsException() throws Exception
    {
        IssueSecurityLevelManager manager = mockController.getMock(IssueSecurityLevelManager.class);
        manager.getUsersSecurityLevelsByName(theUser, "TheName");
        mockController.setThrowable(new GenericEntityException());

        mockController.replay();

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(manager);
        assertNull(resolver.getIssueSecurityLevelsByName(theUser, overrideSecurity, "TheName"));

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingStringHappyPath() throws Exception
    {
        final Collection<GenericValue> expectedLevels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(2L, "TheName"),
                createMockSecurityLevel(1L, "TheName")
        );

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                return expectedLevels;
            }
        };
        mockController.replay();
        
        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral("TheName"));
        assertEquals(expectedLevels, levels);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingStringOverrideSecurity() throws Exception
    {
        final Collection<GenericValue> expectedLevels = ImmutableList.<GenericValue>of(
                createMockSecurityLevel(2L, "TheName"),
                createMockSecurityLevel(1L, "TheName")
        );

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                assertTrue(overrideSecurity);
                return expectedLevels;
            }
        };
        replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevelsOverrideSecurity(ImmutableList.of(createLiteral("TheName")));
        assertEquals(expectedLevels, levels);
    }

    @Test
    public void testGetIssueSecurityLevelsUsingStringNameDoesntExistIsntId() throws Exception
    {
        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                return null;
            }
        };
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral("TheName"));
        assertTrue(levels.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingStringNameDoesntExistIsIdAndExists() throws Exception
    {
        final MockGenericValue expectedLevel = createMockSecurityLevel(1L, "TheName");

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                return null;
            }

            @Override
            GenericValue getIssueSecurityLevelById(final User searcher, final boolean overrideSecurity, final Long valueAsLong)
            {
                return expectedLevel;
            }
        };
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral("1"));
        assertEquals(1, levels.size());
        assertEquals(expectedLevel, levels.iterator().next());

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingStringNameDoesntExistIsIdDoesntExist() throws Exception
    {
        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                return null;
            }

            @Override
            GenericValue getIssueSecurityLevelById(final User searcher, final boolean overrideSecurity, final Long valueAsLong)
            {
                return null;
            }
        };
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral("1"));
        assertTrue(levels.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingEmptyLiteral() throws Exception
    {
        final Collection<GenericValue> expectedLevels = Collections.singletonList(null);

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class));
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, new QueryLiteral());
        assertEquals(expectedLevels, levels);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingLongHappyPath() throws Exception
    {
        final MockGenericValue expectedLevel = createMockSecurityLevel(2L, "TheName");
        final Collection<GenericValue> expectedLevels = ImmutableList.<GenericValue>of(expectedLevel);

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            GenericValue getIssueSecurityLevelById(final User searcher, final boolean overrideSecurity, final Long valueAsLong)
            {
                return expectedLevel;
            }
        };
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral(2L));
        assertEquals(expectedLevels, levels);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingLongIdDoesntExistNameDoes() throws Exception
    {
        final MockGenericValue expectedLevel = createMockSecurityLevel(2L, "2");
        final Collection<GenericValue> expectedLevels = ImmutableList.<GenericValue>of(expectedLevel);

        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            GenericValue getIssueSecurityLevelById(final User searcher, final boolean overrideSecurity, final Long valueAsLong)
            {
                return null;
            }

            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                return expectedLevels;
            }
        };
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral(2L));
        assertEquals(expectedLevels, levels);

        mockController.verify();
    }

    @Test
    public void testGetIssueSecurityLevelsUsingLongIdDoesntExistNameDoesnt() throws Exception
    {
        IssueSecurityLevelResolver resolver = new IssueSecurityLevelResolver(mockController.getMock(IssueSecurityLevelManager.class))
        {
            @Override
            GenericValue getIssueSecurityLevelById(final User searcher, final boolean overrideSecurity, final Long valueAsLong)
            {
                return null;
            }

            @Override
            Collection<GenericValue> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
            {
                return null;
            }
        };
        mockController.replay();

        final Collection<GenericValue> levels = resolver.getIssueSecurityLevels(theUser, createLiteral(2L));
        assertTrue(levels.isEmpty());

        mockController.verify();
    }

    private MockGenericValue createMockSecurityLevel(final Long id, final String name)
    {
        return new MockGenericValue("IssueSecurityLevel", ImmutableMap.of("id", id, "name", name));
    }
}
