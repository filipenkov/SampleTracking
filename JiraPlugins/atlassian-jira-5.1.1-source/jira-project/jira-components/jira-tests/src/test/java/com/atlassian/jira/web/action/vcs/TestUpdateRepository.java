/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.vcs;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.web.action.admin.vcs.UpdateRepository;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.netbeans.lib.cvsclient.CVSRoot;
import webwork.action.Action;

import java.io.File;
import java.util.Map;

public class TestUpdateRepository extends LegacyJiraMockTestCase
{
    private UpdateRepository ur;

    private Mock mockRepositoryManager;
    private Mock mockCvsRepositoryUtil;
    private Long id = new Long(10000);

    public TestUpdateRepository(String s)
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

        ur = new UpdateRepository((RepositoryManager) mockRepositoryManager.proxy(), (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());
    }

    public void testDoValidationNoRepositoryId() throws Exception
    {
        final String result = ur.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(ur.getErrorMessages(), "Please specify a Repository to update");

        verifyMocks();
    }

    public void testDoValidationNoName() throws Exception
    {
        ur.setId(id);

        final String result = ur.execute();
        assertEquals(Action.INPUT, result);

        final Map errors = ur.getErrors();
        assertNotNull(errors);
        assertEquals("You must specify a name for the repository", errors.get("name"));

        verifyMocks();
    }

    public void testDoValidationNameExists() throws Exception
    {
        String name = "somename";
        Mock mockRepository = new Mock(Repository.class);
        mockRepository.setStrict(true);
        mockRepository.expectAndReturn("getId", new Long(id.longValue() + 1));
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(name)), mockRepository.proxy());
        ur.setId(id);
        ur.setName(name);

        final String result = ur.execute();
        assertEquals(Action.INPUT, result);

        final Map errors = ur.getErrors();
        assertNotNull(errors);
        assertEquals("Another repository with this name already exists", errors.get("name"));

        mockRepository.verify();
        verifyMocks();
    }

     public void testDoValidationSameName() throws Exception
    {
        String name = "somename";
        Mock mockRepository = new Mock(Repository.class);
        mockRepository.setStrict(true);
        mockRepository.expectAndReturn("getId", id);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(name)), mockRepository.proxy());
        ur.setId(id);
        ur.setName(name);

        ur.execute();
        final Map errors = ur.getErrors();
        assertNotNull(errors);
        assertFalse(errors.containsKey("name"));

        mockRepository.verify();
        verifyMocks();
    }

    private void verifyMocks()
    {
        mockCvsRepositoryUtil.verify();
        mockRepositoryManager.verify();
    }

    public void testDoExecute() throws Exception
    {
        String name = "name";
        String description = "description";
        String password = "some password";
        String cvsLogFilePath = System.getProperty("java.io.tempdir") + File.separator + "cvs.log";
        String moduleName = "testcvsmodule";
        String cvsRoot = ":pserver:test@testserver:/root/test";
        String fetchLog = "true";
        String timeout = "10";
        Long timeoutInt = new Long("10000");

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewRepositories.jspa");

        Mock mockRepository = new Mock(Repository.class);
        mockRepository.setStrict(true);
        mockRepository.expectAndReturn("getId", id);

        // Expected Method Calls
        mockCvsRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(Boolean.valueOf(fetchLog))));
        mockCvsRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeoutInt)});
        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, null);

        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsAnything()), mockRepository.proxy());
        mockRepositoryManager.expectVoid("updateRepository", P.ANY_ARGS);

        ur.setId(id);
        ur.setName(name);
        ur.setDescription(description);
        ur.setLogFilePath(cvsLogFilePath);
        ur.setCvsRoot(cvsRoot);
        ur.setModuleName(moduleName);
        ur.setPassword(password);
        ur.setFetchLog(Boolean.valueOf(fetchLog).booleanValue());
        ur.setTimeout(timeout);

        final String result = ur.execute();
        assertEquals(Action.NONE, result);

        response.verify();
        mockRepository.verify();
        verifyMocks();
    }
}
