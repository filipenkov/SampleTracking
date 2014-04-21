/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryBrowser;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.Action;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TestAddRepository extends LegacyJiraMockTestCase
{
    AddRepository addRepository;
    private Mock mockRepositoryManager;
    private Mock mockCvsRepositoryUtil;

    private String baseURL = "http://test-cvs-baseURL";

    public TestAddRepository(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);
        mockCvsRepositoryUtil =  new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil.setStrict(true);
        addRepository = new AddRepository((RepositoryManager) mockRepositoryManager.proxy(), (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy())
        {
            public boolean isSystemAdministrator()
            {
                return true;
            }
        };
    }

    public void testDoDefault() throws Exception
    {
        final String result = addRepository.doDefault();
        assertEquals(Action.INPUT, result);
        assertTrue(addRepository.isFetchLog());
        assertNull(addRepository.getId());
        assertNull(addRepository.getName());
        assertNull(addRepository.getDescription());
        assertNull(addRepository.getLogFilePath());
        assertNull(addRepository.getCvsRoot());
        assertNull(addRepository.getModuleName());
        assertNull(addRepository.getRepositoryBrowserURL());
        assertNull(addRepository.getRepositoryBrowserRootParam());

        verifyMocks();
    }

    public void testDoValidationNoName() throws Exception
    {
        final String result = addRepository.execute();
        assertEquals(Action.INPUT, result);
        final Map errors = addRepository.getErrors();
        assertNotNull(errors);
        assertEquals("You must specify a name for the repository", errors.get("name"));

        verifyMocks();
    }

    public void testDoValidationExistantName() throws Exception
    {
        final String name = "somename";

        Mock mockRepository = new Mock(Repository.class);
        mockRepository.setStrict(true);

        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(name)), mockRepository.proxy());
        addRepository.setName(name);
        final String result = addRepository.execute();
        assertEquals(Action.INPUT, result);
        final Map errors = addRepository.getErrors();
        assertNotNull(errors);
        assertEquals("Another repository with this name already exists", errors.get("name"));

        verifyMocks();
    }

    public void testDoValidationNameOK() throws Exception
    {
        final String name = "somename";

        Mock mockRepository = new Mock(Repository.class);
        mockRepository.setStrict(true);

        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(name)), null);
        addRepository.setName(name);
        final String result = addRepository.execute();
        assertEquals(Action.INPUT, result);
        final Map errors = addRepository.getErrors();
        assertNotNull(errors);
        assertFalse(errors.containsKey("name"));

        verifyMocks();
    }



    private void verifyMocks()
    {
        mockRepositoryManager.verify();
        mockCvsRepositoryUtil.verify();
    }

    public void testIsDeletableWithException() throws GenericEntityException
    {
        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);

        // Expected Method Calls
        mockRepositoryManager.expectAndThrow("getProjectsForRepository", P.args(new IsEqual(repository)), new RuntimeException());

        // Check Results
        assertFalse(addRepository.isDeletable((Repository) repository.proxy()));
        checkSingleElementCollection(addRepository.getErrorMessages(), "Error occurred while retrieving projects for repository '" + repository + "'. Please refer to the log for more details.");

        repository.verify();
        verifyMocks();
    }

    public void testIsDeletableTrue() throws Exception
    {
        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);
        Mock projects = new Mock(Collection.class);
        projects.setStrict(true);

        // Expected Method Calls
        mockRepositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        projects.expectAndReturn("isEmpty", Boolean.TRUE);

        // Check Results
        assertTrue(addRepository.isDeletable((Repository) repository.proxy()));

        repository.verify();
        projects.verify();
        verifyMocks();
    }

    public void testIsDeletableFalse() throws Exception
    {
        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);
        Mock projects = new Mock(Collection.class);
        projects.setStrict(true);

        // Expected Method Calls
        mockRepositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        projects.expectAndReturn("isEmpty", Boolean.FALSE);

        //Check Results
        assertFalse(addRepository.isDeletable((Repository) repository.proxy()));

        repository.verify();
        projects.verify();
        verifyMocks();
    }


    public void testGetRepositories() throws Exception
    {
        Collection expectedRepositories = new ArrayList();
        mockRepositoryManager.expectAndReturn("getRepositories", expectedRepositories);

        // Check Results
        assertTrue(addRepository.getRepositories().equals(expectedRepositories));
    }


    public void testGetViewCVSBaseUrlWithNoBrowserType() throws Exception
    {
        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);
        Mock repositoryBrowser = new Mock(RepositoryBrowser.class);
        repositoryBrowser.setStrict(true);

        repositoryBrowser.expectAndReturn("getType", null);

        // Expected Method Calls
        final Repository repositoryProxy = (Repository) repository.proxy();
        repository.expectAndReturn("getRepositoryBrowser", repositoryBrowser.proxy());

        // Check Results
        String result = addRepository.getViewCVSBaseUrl(repositoryProxy);

        assertEquals("", result);
        repository.verify();
        repositoryBrowser.verify();

        verifyMocks();
    }


    public void testGetViewCVSBaseUrl() throws Exception
    {
        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);

        // Need to create new ViewCvsBrowser instance - as there is no interface available to mock.
        MockViewCvsBrowser mockViewCvsBrowser = new MockViewCvsBrowser(baseURL, Collections.EMPTY_MAP);

        // Expected Method Calls
        final Repository repositoryProxy = (Repository) repository.proxy();
        repository.expectAndReturn("getRepositoryBrowser", mockViewCvsBrowser);

        addRepository.setRepositoryBrowserURL(baseURL);

        // Check Results
        String result = addRepository.getViewCVSBaseUrl(repositoryProxy);
        assertEquals(baseURL + "/", result);

        repository.verify();
        verifyMocks();
    }


    public void testGetProjectsWithException() throws Exception
    {
        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);
        repository.expectAndReturn("getId", null);

        // Expected Method Calls
        mockRepositoryManager.expectAndThrow("getProjectsForRepository", P.args(new IsEqual(repository)), new GenericEntityException());


        addRepository.getProjects((Repository) repository.proxy());

        // Check Results
        checkSingleElementCollection(addRepository.getErrorMessages(), "Error occurred while retrieving projects for repository '" + repository + "'. Please refer to the log for more details.");

        repository.verify();
        verifyMocks();
    }

    public void testGetProjects() throws Exception
    {
        Long id = new Long(10000);

        // Setup Mocks
        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);
        Mock projects = new Mock(Collection.class);
        projects.setStrict(true);

        mockRepositoryManager.setStrict(true);

        // Expected Method Calls
        mockRepositoryManager.expectAndReturn("getProjectsForRepository", P.args(new IsEqual(repository)), projects.proxy());
        repository.expectAndReturn("getId", id);

        addRepository.getProjects((Repository) repository.proxy());

        // Check Results
        assertTrue(addRepository.getErrors().isEmpty());
        assertTrue(addRepository.getErrorMessages().isEmpty());

        repository.verify();
        projects.verify();
        verifyMocks();
    }

    public void testExecuteException() throws Exception
    {
        String name = "name";
        String description = "description";
        String password = "some password";
        String cvsLogFilePath = System.getProperty("java.io.tempdir") + File.separator + "cvs.log";
        String moduleName = "testcvsmodule";
        String cvsRoot = ":pserver:test@testserver:/root/test";
        String fetchLog = "true";
        String timeoutStrSecs = "10";
        long timeoutInt = 10000L;

        // Expected Method Calls
        mockCvsRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(Boolean.valueOf(fetchLog))));
        mockCvsRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeoutInt)});
        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, null);

        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(name)), null);
        mockRepositoryManager.expectAndThrow("createRepository", P.ANY_ARGS, new Exception());

        addRepository.setName(name);
        addRepository.setDescription(description);
        addRepository.setLogFilePath(cvsLogFilePath);
        addRepository.setCvsRoot(cvsRoot);
        addRepository.setModuleName(moduleName);
        addRepository.setPassword(password);
        addRepository.setFetchLog(Boolean.valueOf(fetchLog).booleanValue());
        addRepository.setTimeout(timeoutStrSecs);

        final String result = addRepository.execute();
        assertEquals(Action.ERROR, result);

        // Check Results
        checkSingleElementCollection(addRepository.getErrorMessages(), "Error occurred while creating the repository. Please refer to the log for more details.");

        verifyMocks();
    }

    public void testExecuteSuccess() throws Exception
    {
        String name = "name";
        String description = "description";
        String password = "some password";
        String cvsLogFilePath = System.getProperty("java.io.tempdir") + File.separator + "cvs.log";
        String moduleName = "testcvsmodule";
        String cvsRoot = ":pserver:test@testserver:/root/test";
        String fetchLog = "true";
        String timeoutStrSecs = "10";
        long timeoutInt = 10000;

        Mock repository = new Mock(Repository.class);
        repository.setStrict(true);
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewRepositories.jspa");

        // Expected Method Calls
        mockCvsRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(Boolean.valueOf(fetchLog))));
        mockCvsRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeoutInt)});
        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, null);

        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(name)), null);
        mockRepositoryManager.expectAndReturn("createRepository", P.ANY_ARGS, repository.proxy());

        addRepository.setName(name);
        addRepository.setDescription(description);
        addRepository.setLogFilePath(cvsLogFilePath);
        addRepository.setCvsRoot(cvsRoot);
        addRepository.setModuleName(moduleName);
        addRepository.setPassword(password);
        addRepository.setFetchLog(Boolean.valueOf(fetchLog).booleanValue());
        addRepository.setTimeout(timeoutStrSecs);

        String result = addRepository.execute();
        assertEquals(result, Action.NONE);
        assertTrue(addRepository.getErrors().isEmpty());
        assertTrue(addRepository.getErrorMessages().isEmpty());

        repository.verify();
        response.verify();
        verifyMocks();
    }

}


