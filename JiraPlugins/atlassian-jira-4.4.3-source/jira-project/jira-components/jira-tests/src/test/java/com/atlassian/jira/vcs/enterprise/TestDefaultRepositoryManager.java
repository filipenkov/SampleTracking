/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.vcs.enterprise;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.project.ProjectRelationConstants;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.vcs.DefaultRepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepository;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.Collection;
import java.util.List;

public class TestDefaultRepositoryManager extends LegacyJiraMockTestCase
{
    DefaultRepositoryManager erm;
    private GenericValue projectA;
    private GenericValue projectB;
    private PropertySet psa;
    private PropertySet psb;

    public TestDefaultRepositoryManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        projectA = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(101), "name", "ProjectOne"));
        GenericValue vcsConfigA = UtilsForTests.getTestEntity("VersionControl", EasyMap.build("id", new Long(101), "name", "JiraRepo", "description", "JIRA code repository", "type", "cvs"));
        psa = OFBizPropertyUtils.getPropertySet(vcsConfigA);
        psa.setLong("id", 101);
        psa.setString("name", "JiraRepo");
        psa.setString("description", "JIRA code repository");
        psa.setString(CvsRepository.KEY_PASSWORD, "secret");
        psa.setString(CvsRepository.KEY_LOG_FILE_PATH, "some log file path");
        psa.setString(CvsRepository.KEY_CVS_ROOT, "some cvs root");
        psa.setString(CvsRepository.KEY_MODULE_NAME, "some module name");
        psa.setString(CvsRepository.KEY_FETCH_LOG, "true");

        CoreFactory.getAssociationManager().createAssociation(projectA, vcsConfigA, ProjectRelationConstants.PROJECT_VERSIONCONTROL);

        projectB = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(102), "name", "ProjectTwo"));
        GenericValue vcsConfigB = UtilsForTests.getTestEntity("VersionControl", EasyMap.build("id", new Long(102), "name", "CoreRepo", "description", "core repo", "type", "cvs"));
        psb = OFBizPropertyUtils.getPropertySet(vcsConfigB);
        psb.setLong("id", 102);
        psb.setString("name", "CoreRepo");
        psb.setString("description", "core repo");
        psb.setString(CvsRepository.KEY_PASSWORD, "flibble");
        psb.setString(CvsRepository.KEY_LOG_FILE_PATH, "another log file path");
        psb.setString(CvsRepository.KEY_CVS_ROOT, "another cvs root");
        psb.setString(CvsRepository.KEY_MODULE_NAME, "another module name");
        psb.setString(CvsRepository.KEY_FETCH_LOG, "false");

        // Associate both repositories with project B
        CoreFactory.getAssociationManager().createAssociation(projectB, vcsConfigA, ProjectRelationConstants.PROJECT_VERSIONCONTROL);
        CoreFactory.getAssociationManager().createAssociation(projectB, vcsConfigB, ProjectRelationConstants.PROJECT_VERSIONCONTROL);

        erm = new DefaultRepositoryManager();
    }

    public void testGetRepositoriesForProjectNoProject() throws GenericEntityException
    {
        try
        {
            erm.getRepositoriesForProject(null);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Tried to get repository for null project", e.getMessage());
        }
    }

    public void testGetRepositoriesForProjectNotAProject() throws GenericEntityException
    {
        try
        {
            erm.getRepositoriesForProject(UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue")));
            fail("IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("getProviderForProject called with an entity of type 'Issue' - which is not a project", e.getMessage());
        }
    }

    public void testGetRepositoriesForProject() throws GenericEntityException
    {
        final Collection repositories = erm.getRepositoriesForProject(projectA);
        final CvsRepository expectedRepository = new CvsRepository(psa, null);
        expectedRepository.setId(new Long(psa.getLong("id")));
        checkSingleElementCollection(repositories, expectedRepository);
    }

    public void testGetRepositoriesForProjectMultipleRepositories() throws GenericEntityException
    {
        final CvsRepository expectedRepositoryA = new CvsRepository(psa, null);
        expectedRepositoryA.setId(new Long(psa.getLong("id")));

        final CvsRepository expectedRepositoryB = new CvsRepository(psb, null);
        expectedRepositoryB.setId(new Long(psb.getLong("id")));

        // Multiple repositories for a project ARE allowed in Enterprise version.
        final Collection repositories = erm.getRepositoriesForProject(projectB);
        final List expectedRepositories = EasyList.build(expectedRepositoryB, expectedRepositoryA);
        assertContainsOnly(expectedRepositories, repositories);

    }
}
