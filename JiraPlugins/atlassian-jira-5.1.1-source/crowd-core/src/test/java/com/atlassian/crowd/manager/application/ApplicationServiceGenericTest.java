package com.atlassian.crowd.manager.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.event.EventStore;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.event.api.EventPublisher;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceGenericTest
{
    @Mock
    DirectoryManager directoryManager;

    @Mock
    PermissionManager permissionManager;

    @Mock
    DirectoryInstanceLoader directoryInstanceLoader;

    @Mock
    EventPublisher eventPublisher;

    @Mock
    EventStore eventStore;

    @Mock
    Application application;

    /**
     * These two strings are the same when lowercased in EN but different when lowercased in TR.
     */
    private static final Collection<String> namesWithTurkishCharacters = ImmutableList.of("i", "I"); // "ı", "İ"

    private ApplicationService serviceWithUsers(List<User> users) throws DirectoryNotFoundException, OperationFailedException
    {
        when(directoryManager.searchUsers(Mockito.anyLong(), Mockito.<EntityQuery<User>>anyObject())).thenReturn(users);

        ApplicationServiceGeneric appSvc = new ApplicationServiceGeneric(
                directoryManager, permissionManager, directoryInstanceLoader, eventPublisher, eventStore);

        Directory directory = mock(Directory.class);
        when(directory.isActive()).thenReturn(true);

        DirectoryMapping mapping = new DirectoryMapping(application, directory, true);
        List<DirectoryMapping> dirMappings = Collections.singletonList(mapping);
        when(application.getDirectoryMappings()).thenReturn(dirMappings);

        return appSvc;
    }

    private UserQuery<User> allUsersQuery()
    {
        return new UserQuery<User>(User.class, NullRestrictionImpl.INSTANCE, 0, -1);
    }

    private static void withCrowdIdentifierLanguage(String lang, Callable<Void> r) throws Exception
    {
        String before = System.getProperty("crowd.identifier.language");
        try
        {
            System.setProperty("crowd.identifier.language", lang);
            IdentifierUtils.prepareIdentifierCompareLocale();
            r.call();
        }
        finally
        {
            if (before != null)
            {
                System.setProperty("crowd.identifier.language", before);
            }
            else
            {
                System.clearProperty("crowd.identifier.language");
            }
            IdentifierUtils.prepareIdentifierCompareLocale();
        }
    }

    private static List<User> withNames(Iterable<String> names)
    {
        final List<User> value = new ArrayList<User>();

        for (String s : names)
        {
            value.add(new UserTemplate(s));
        }

        return value;
    }

    @Test
    public void turkishLettersIdenticalInEnglishLocale() throws Exception
    {
        final List<User> value = withNames(namesWithTurkishCharacters);

        final List<User> users = new ArrayList<User>();

        withCrowdIdentifierLanguage("en", new Callable<Void>() {
            @Override
            public Void call() throws Exception
            {
                users.addAll(serviceWithUsers(value).searchUsers(application, allUsersQuery()));
                return null;
            }
        });

        assertEquals(1, users.size());
    }

    @Test
    public void turkishLettersDistinctInTurkishLocale() throws Exception
    {
        final List<User> value = withNames(namesWithTurkishCharacters);

        final List<User> users = new ArrayList<User>();

        withCrowdIdentifierLanguage("tr", new Callable<Void>() {
            @Override
            public Void call() throws Exception
            {
                users.addAll(serviceWithUsers(value).searchUsers(application, allUsersQuery()));
                return null;
            }
        });

        assertEquals(2, users.size());
    }

    @Test
    public void usersComeBackInLexicalOrder() throws Exception
    {
        List<String> names = new ArrayList<String>();

        for (int i = 0; i < 100; i++)
        {
            names.add(String.format("user%03d", i));
        }

        List<String> namesInRandomOrder = new ArrayList<String>(names);
        Collections.shuffle(namesInRandomOrder);

        assertFalse(names.equals(namesInRandomOrder));

        List<String> namesAsReturned = new ArrayList<String>();

        for (User u : serviceWithUsers(withNames(namesInRandomOrder)).searchUsers(application, allUsersQuery()))
        {
            namesAsReturned.add(u.getName());
        }

        assertEquals(names, namesAsReturned);
    }

    @Test
    public void searchResultsIncludeDuplicateUsersFromDifferentDirectoriesWhenRequested() throws Exception
    {
        List<User> users = new ArrayList<User>();

        User u1 = mock(User.class);
        when(u1.getName()).thenReturn("user");
        when(u1.getDirectoryId()).thenReturn(1L);
        users.add(u1);

        User u2 = mock(User.class);
        when(u2.getName()).thenReturn("user");
        when(u2.getDirectoryId()).thenReturn(2L);
        users.add(u2);

        List<User> results = serviceWithUsers(users).searchUsersAllowingDuplicateNames(application, allUsersQuery());

        assertEquals(2, results.size());
    }

    /**
     * This is a non-functional test. It should pass, but its purpose is for benchmarking as the number
     * of users increases.
     */
    @Test
    public void queryForLargeNumberOfUsers() throws DirectoryNotFoundException, OperationFailedException
    {
        List<String> names = new ArrayList<String>();

        for (int i = 0; i < 10000; i++)
        {
            names.add("user" + i);
        }

        List<User> users = serviceWithUsers(withNames(names)).searchUsers(application, allUsersQuery());

        assertEquals(names.size(), users.size());
    }
}
