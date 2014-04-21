/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryManager;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.Action;

import java.util.Collection;

public class TestDeleteRepository extends LegacyJiraMockTestCase
{
    Long id = new Long(1);

    public TestDeleteRepository(String s)
    {
        super(s);
    }

    public void testIsConfirmed()
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        repositoryManager.setStrict(true);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        deleteRepository.setConfirmed(false);
        assertFalse(deleteRepository.isConfirmed());

        deleteRepository.setConfirmed(true);
        assertTrue(deleteRepository.isConfirmed());
        repositoryManager.verify();
    }

    public void testIsDeletableFalse()
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);
        Mock projects = new Mock(Collection.class);

        repositoryManager.setStrict(true);
        projects.setStrict(true);

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        projects.expectAndReturn("isEmpty", Boolean.FALSE);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        // Check Results
        assertFalse(deleteRepository.isDeletable());
        repositoryManager.verify();
        projects.verify();
    }

    public void testIsDeletableTrue()
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);
        Mock projects = new Mock(Collection.class);

        repositoryManager.setStrict(true);
        projects.setStrict(true);

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        projects.expectAndReturn("isEmpty", Boolean.TRUE);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        // Check Results
        assertTrue(deleteRepository.isDeletable());
        repositoryManager.verify();
        projects.verify();
    }

    public void testIsDeletableWithException()
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);

        repositoryManager.setStrict(true);
        repository.setStrict(true);

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndThrow("getProjectsForRepository", P.args(new IsEqual(repository)), new GenericEntityException());

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        // Check Results
        deleteRepository.isDeletable();
        checkSingleElementCollection(deleteRepository.getErrorMessages(), "Error occurred while retrieving the repository with id 'null'. Please refer to the log for more details.");
        repositoryManager.verify();
        repository.verify();
    }

    public void testValidationUnableToRemoveWithoutID() throws Exception
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);
        Mock projects = new Mock(Collection.class);

        repositoryManager.setStrict(true);
        projects.setStrict(true);

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        projects.expectAndReturn("isEmpty", Boolean.TRUE);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        // Check Results
        String result = deleteRepository.execute();
        assertEquals(result, Action.INPUT);
        checkSingleElementCollection(deleteRepository.getErrorMessages(), "Please confirm that you wish to delete this Repository.");
        repositoryManager.verify();
        projects.verify();
    }

    public void testValidationUnableToRemoveWithProjectAssociated() throws Exception
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);
        Mock projects = new Mock(Collection.class);

        repositoryManager.setStrict(true);
        projects.setStrict(true);

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        projects.expectAndReturn("isEmpty", Boolean.FALSE);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        deleteRepository.setId(id);
        deleteRepository.setConfirmed(true);

        String result = deleteRepository.execute();

        // Check Results
        assertEquals(result, Action.INPUT);
        checkSingleElementCollection(deleteRepository.getErrorMessages(), "Cannot delete the repository as there are projects associated with it.");
        repositoryManager.verify();
        projects.verify();
    }

    public void testExecuteWithException() throws Exception
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);
        Mock projects = new Mock(Collection.class);

        repositoryManager.setStrict(true);
        projects.setStrict(true);

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        repositoryManager.expectAndThrow("removeRepository", P.ANY_ARGS, new Exception());
        projects.expectAndReturn("isEmpty", Boolean.TRUE);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        deleteRepository.setId(id);
        deleteRepository.setConfirmed(true);

        // Check Results
        String result = deleteRepository.execute();
        assertEquals(result, Action.ERROR);
        checkSingleElementCollection(deleteRepository.getErrorMessages(), "Error occurred while removing the repository. Please refer to the log for more details.");
        repositoryManager.verify();
        projects.verify();
    }

    public void testExecuteSuccess() throws Exception
    {
        // Setup Mocks
        Mock repositoryManager = new Mock(RepositoryManager.class);
        Mock repository = new Mock(Repository.class);
        Mock projects = new Mock(Collection.class);

        repositoryManager.setStrict(true);
        projects.setStrict(true);

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewRepositories.jspa");

        // Expected Method Calls
        repositoryManager.expectAndReturn("getRepository", P.ANY_ARGS, repository.proxy());
        repositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        repositoryManager.expectVoid("removeRepository", P.ANY_ARGS);
        projects.expectAndReturn("isEmpty", Boolean.TRUE);

        DeleteRepository deleteRepository = new DeleteRepository((RepositoryManager) repositoryManager.proxy(), null);

        deleteRepository.setId(id);
        deleteRepository.setConfirmed(true);

        // Check Results
        String result = deleteRepository.execute();
        assertEquals(result, Action.NONE);
        assertTrue(deleteRepository.getErrorMessages().isEmpty());
        repositoryManager.verify();
        projects.verify();
        response.verify();
    }
}
