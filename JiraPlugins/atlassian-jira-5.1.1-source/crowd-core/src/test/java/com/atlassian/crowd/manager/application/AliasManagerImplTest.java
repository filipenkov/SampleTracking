package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.dao.alias.AliasDAO;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.user.UserTemplate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AliasManagerImplTest
{
    private AliasManager aliasManager;
    private ApplicationImpl application;
    private AliasDAO aliasDAO;
    private ApplicationService applicationService;

    @Before
    public void setUp()
    {
        aliasDAO = mock(AliasDAO.class);
        applicationService = mock(ApplicationService.class);

        application = ApplicationImpl.newInstanceWithPassword("test application", ApplicationType.GENERIC_APPLICATION, "secret");

        aliasManager = new AliasManagerImpl(aliasDAO, applicationService);
    }

    @Test
    public void testFindUsernameByAliasWithNullArguments()
    {
        try
        {
            aliasManager.findUsernameByAlias(null, "");
            fail("Should have thrown an IllegalArgumentException due to null application");
        }
        catch (IllegalArgumentException e)
        {
            // expected behaviour
        }

        try
        {

            aliasManager.findUsernameByAlias(application, null);
            fail("Should have thrown an IllegalArgumentException due to null username");
        }
        catch (IllegalArgumentException e)
        {
            // expected behaviour
        }
    }

    @Test
    public void testFindUsernameByAliasWhereApplicationIsNotAliasingEnabled()
    {
        application.setAliasingEnabled(false);

        final String alias = aliasManager.findUsernameByAlias(application, "authenticating-user");

        assertEquals("authenticating-user", alias);

        verify(aliasDAO, never()).findUsernameByAlias(application, "authenticating-user");
    }

    @Test
    public void testFindUsernameByAliasWhereAliasDoesNotExist()
    {
        application.setAliasingEnabled(true);
        when(aliasDAO.findUsernameByAlias(application, "authenticating-user")).thenReturn(null); // no alias exists

        final String alias = aliasManager.findUsernameByAlias(application, "authenticating-user");

        assertEquals("authenticating-user", alias);

        verify(aliasDAO, times(1)).findUsernameByAlias(application, "authenticating-user");
    }

    @Test
    public void testFindUsernameByAliasWhereAliasDoesExist()
    {
        application.setAliasingEnabled(true);
        when(aliasDAO.findUsernameByAlias(application, "authenticating-user")).thenReturn("real-username"); // alias exists

        final String alias = aliasManager.findUsernameByAlias(application, "authenticating-user");

        assertEquals("real-username", alias);

        verify(aliasDAO, times(1)).findUsernameByAlias(application, "authenticating-user");
    }

    @Test
    public void testFindAliasByUsernameWithNullArguments()
    {
        try
        {
            aliasManager.findAliasByUsername(null, "");
            fail("Should have thrown an IllegalArgumentException due to null application");
        }
        catch (IllegalArgumentException e)
        {
            // expected behaviour
        }

        try
        {

            aliasManager.findAliasByUsername(application, null);
            fail("Should have thrown an IllegalArgumentException due to null username");
        }
        catch (IllegalArgumentException e)
        {
            // expected behaviour
        }
    }

    @Test
    public void testFindAliasByUsernameWhereApplicationIsNotAliasingEnabled()
    {
        application.setAliasingEnabled(false);

        final String alias = aliasManager.findAliasByUsername(application, "real-username");

        assertEquals("real-username", alias);

        verify(aliasDAO, never()).findAliasByUsername(application, "real-username");
    }

    @Test
    public void testFindAliasByUsernameWhereAliasDoesNotExist()
    {
        application.setAliasingEnabled(true);
        when(aliasDAO.findAliasByUsername(application, "real-username")).thenReturn(null); // no alias exists

        final String alias = aliasManager.findAliasByUsername(application, "real-username");

        assertEquals("real-username", alias);

        verify(aliasDAO, times(1)).findAliasByUsername(application, "real-username");
    }

    @Test
    public void testFindAliasByUsernameWhereAliasDoesExist()
    {
        application.setAliasingEnabled(true);

        when(aliasDAO.findAliasByUsername(application, "real-username")).thenReturn("aliased-username"); // alias exists

        final String alias = aliasManager.findAliasByUsername(application, "real-username");

        assertEquals("aliased-username", alias);

        verify(aliasDAO, times(1)).findAliasByUsername(application, "real-username");
    }

    @Test
    public void testStoreAliasSuccess() throws Exception
    {
        String username = "user";
        String alias = "alias";

        when(aliasDAO.findUsernameByAlias(application, alias)).thenReturn(null);
        when(applicationService.findUserByName(application, alias)).thenThrow(new UserNotFoundException(username));

        aliasManager.storeAlias(application, username, alias);

        verify(aliasDAO).storeAlias(application, username, alias);
    }

    @Test
    public void testStoreAliasAlreadyInUseAsAlias() throws AliasAlreadyInUseException
    {
        String username = "user";
        String alias = "alias";

        when(aliasDAO.findUsernameByAlias(application, alias)).thenReturn("someone");

        try
        {
            aliasManager.storeAlias(application, username, alias);
            fail("AliasAlreadyInUseException expected");
        }
        catch (AliasAlreadyInUseException e)
        {
            // expected
        }
    }

    @Test
    public void testStoreAliasAlreadyInUseAsUnaliasedUser() throws Exception
    {
        String username = "user";
        String alias = "alias";

        UserTemplate user = new UserTemplate(alias, 1L);

        when(aliasDAO.findUsernameByAlias(application, alias)).thenReturn(null);
        when(applicationService.findUserByName(application, alias)).thenReturn(user);
        when(aliasDAO.findAliasByUsername(application, alias)).thenReturn(null);

        try
        {
            aliasManager.storeAlias(application, username, alias);
            fail("AliasAlreadyInUseException expected");
        }
        catch (AliasAlreadyInUseException e)
        {
            // expected
        }
    }

    @Test
    public void testStoreAliasSuccessBecauseUserWithAliasAsUsernameHasAnAliasToo() throws Exception
    {
        String username = "user";
        String alias = "alias";

        UserTemplate user = new UserTemplate(alias, 1L);

        when(aliasDAO.findUsernameByAlias(application, alias)).thenReturn(null);
        when(applicationService.findUserByName(application, alias)).thenReturn(user);
        when(aliasDAO.findAliasByUsername(application, alias)).thenReturn("i have alias too");

        aliasManager.storeAlias(application, username, alias);

        verify(aliasDAO).storeAlias(application, username, alias);
    }

    @Test
    public void testStoreAliasSuccessAllowAliasingToTheSameAlias() throws AliasAlreadyInUseException
    {
        String username = "user";
        String alias = "alias";

        when(aliasDAO.findUsernameByAlias(application, alias)).thenReturn(username);

        aliasManager.storeAlias(application, username, alias);
    }
}
