package com.atlassian.jira.vcs;

import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectRelationConstants;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepository;
import com.atlassian.jira.vcs.viewcvs.ViewCvsBrowser;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class TestDefaultRepositoryManager extends AbstractUsersIndexingTestCase
{
    private GenericValue projectA;
    private GenericValue projectB;
    private GenericValue vcsConfigA;
    private DefaultRepositoryManager repositoryManager;
    private Mock mockPermissionManager;
    private PropertySet psa;
    private PropertySet psb;
    private User user;
    private Issue issueObject;
    private static final String ISSUE_KEY = "PRA-1";

    public TestDefaultRepositoryManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        projectA = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(101), "name", "ProjectOne"));
        vcsConfigA = UtilsForTests.getTestEntity("VersionControl", EasyMap.build("id", new Long(101), "name", "JiraRepo", "description",
            "JIRA code repository", "type", "cvs"));
        psa = OFBizPropertyUtils.getPropertySet(vcsConfigA);
        psa.setLong("id", 101);
        psa.setString("name", "JiraRepo");
        psa.setString("description", "JIRA code repository");
        psa.setString(CvsRepository.KEY_PASSWORD, "secret");

        CoreFactory.getAssociationManager().createAssociation(projectA, vcsConfigA, ProjectRelationConstants.PROJECT_VERSIONCONTROL);

        projectB = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(102), "name", "ProjectTwo"));
        final GenericValue vcsConfigB = UtilsForTests.getTestEntity("VersionControl", EasyMap.build("id", new Long(102), "name", "CoreRepo",
            "description", "core repo", "type", "cvs"));
        psb = OFBizPropertyUtils.getPropertySet(vcsConfigB);
        psb.setLong("id", 102);
        psb.setString("name", "CoreRepo");
        psb.setString("description", "core repo");
        psb.setString(CvsRepository.KEY_PASSWORD, "flibble");

        // Associate both repositories with project B
        CoreFactory.getAssociationManager().createAssociation(projectB, vcsConfigA, ProjectRelationConstants.PROJECT_VERSIONCONTROL);
        CoreFactory.getAssociationManager().createAssociation(projectB, vcsConfigB, ProjectRelationConstants.PROJECT_VERSIONCONTROL);

        mockPermissionManager = new Mock(PermissionManager.class);

        final Mock mockChangeHistoryManager = new Mock(ChangeHistoryManager.class);
        mockChangeHistoryManager.expectAndReturn("getPreviousIssueKeys", P.ANY_ARGS, Collections.EMPTY_LIST);

        repositoryManager = new DefaultRepositoryManager(CoreFactory.getAssociationManager(), ComponentAccessor.getOfBizDelegator(),
            ManagerFactory.getServiceManager(), (PermissionManager) mockPermissionManager.proxy(),
            (ChangeHistoryManager) mockChangeHistoryManager.proxy(), null);

        user = createMockUser("Sam");

        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", projectA.getLong("id"), "key",
            ISSUE_KEY));
        issueObject = IssueImpl.getIssueObject(issue);
    }

    public void testEqualsUtilityMethod()
    {
        final Properties properties = new Properties();
        final MapPropertySet propertySet = new MapPropertySet();
        propertySet.setMap(new HashMap());

        // test on empty props
        assertTrue(DefaultRepositoryManager.equals("some property", propertySet, properties));

        // test on props with some values
        propertySet.setString("name", "Dushan");
        properties.setProperty("name", "Dushan");
        propertySet.setString("language", "Java");
        properties.setProperty("language", "Java");

        assertTrue(DefaultRepositoryManager.equals("name", propertySet, properties));
        assertTrue(DefaultRepositoryManager.equals("language", propertySet, properties));
        assertTrue(DefaultRepositoryManager.equals("some property", propertySet, properties));

        // add to property set only first
        propertySet.setString("product", "JIRA");
        assertFalse(DefaultRepositoryManager.equals("product", propertySet, properties));
        properties.setProperty("product", "JIRA");
        assertTrue(DefaultRepositoryManager.equals("product", propertySet, properties));

        // add to properties only first
        properties.setProperty("company", "Atlassian");
        assertFalse(DefaultRepositoryManager.equals("company", propertySet, properties));
        propertySet.setString("company", "Atlassian");
        assertTrue(DefaultRepositoryManager.equals("company", propertySet, properties));
    }

    public void testIsDifferentRepositoryNoProps()
    {
        final Properties properties = new Properties();
        final MapPropertySet propertySet = new MapPropertySet();
        assertFalse(repositoryManager.isDifferentRepository(propertySet, properties));
    }

    public void testIsDifferentRepositoryDefaultVital()
    {
        final PropertySet cvsPropertySet = createDefaultCvsPropertySet();
        final Properties cvsProperties = createDefaultCvsProperties();
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));

        // modify properties
        Properties modCvsProperties = createDefaultCvsProperties();
        modCvsProperties.setProperty(CvsRepository.KEY_MODULE_NAME, "other module");
        assertTrue(repositoryManager.isDifferentRepository(cvsPropertySet, modCvsProperties));

        modCvsProperties = createDefaultCvsProperties();
        modCvsProperties.setProperty(CvsRepository.KEY_PASSWORD, "other password");
        assertTrue(repositoryManager.isDifferentRepository(cvsPropertySet, modCvsProperties));

        modCvsProperties = createDefaultCvsProperties();
        modCvsProperties.setProperty(CvsRepository.KEY_CVS_ROOT, "other root");
        assertTrue(repositoryManager.isDifferentRepository(cvsPropertySet, modCvsProperties));

        modCvsProperties = createDefaultCvsProperties();
        modCvsProperties.setProperty(CvsRepository.KEY_FETCH_LOG, "false");
        assertTrue(repositoryManager.isDifferentRepository(cvsPropertySet, modCvsProperties));

        // modify property set
        final PropertySet modCvsPropertySet = createDefaultCvsPropertySet();
        modCvsPropertySet.setString(CvsRepository.KEY_MODULE_NAME, "other module");
        assertTrue(repositoryManager.isDifferentRepository(modCvsPropertySet, cvsProperties));

        modCvsPropertySet.setString(CvsRepository.KEY_PASSWORD, "other password");
        assertTrue(repositoryManager.isDifferentRepository(modCvsPropertySet, cvsProperties));

        modCvsPropertySet.setString(CvsRepository.KEY_CVS_ROOT, "other root");
        assertTrue(repositoryManager.isDifferentRepository(modCvsPropertySet, cvsProperties));

        modCvsPropertySet.setString(CvsRepository.KEY_FETCH_LOG, "false");
        assertTrue(repositoryManager.isDifferentRepository(modCvsPropertySet, cvsProperties));
    }

    public void testIsDifferentRepositoryDefaultNonVital()
    {
        final PropertySet cvsPropertySet = createDefaultCvsPropertySet();
        final Properties cvsProperties = createDefaultCvsProperties();
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));

        cvsProperties.setProperty(CvsRepository.KEY_LOG_FILE_PATH, "my file");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
        cvsProperties.setProperty(CvsRepository.KEY_CVS_TIMEOUT, "123");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
        cvsProperties.setProperty(CvsRepository.KEY_BASEDIR, "/");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
        cvsProperties.setProperty(AbstractRepository.KEY_DESCRIPTION, "some description");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));

        cvsPropertySet.setString(CvsRepository.KEY_LOG_FILE_PATH, "your file");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
        cvsPropertySet.setString(CvsRepository.KEY_CVS_TIMEOUT, "456");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
        cvsPropertySet.setString(CvsRepository.KEY_BASEDIR, "C:\\");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
        cvsPropertySet.setString(AbstractRepository.KEY_DESCRIPTION, "no description");
        assertFalse(repositoryManager.isDifferentRepository(cvsPropertySet, cvsProperties));
    }

    public void testMarkVcsServiceToRun() throws GenericEntityException
    {
        final Long SERVICE_ID = (long) 123;

        final Mock mockService = new Mock(JiraServiceContainer.class);
        mockService.setStrict(true);
        mockService.expectAndReturn("isUsable", Boolean.TRUE);
        mockService.expectAndReturn("getId", SERVICE_ID);

        final AtomicInteger count = new AtomicInteger(0);
        final ServiceManager.ServiceScheduleSkipper skipper = new ServiceManager.ServiceScheduleSkipper()
        {
            public boolean addService(final Long serviceId)
            {
                assertEquals(SERVICE_ID, serviceId);
                count.incrementAndGet();
                return true;
            }

            public void awaitServiceRun(final Long serviceId) throws InterruptedException
            {}
        };

        final Mock mockServiceManager = new Mock(ServiceManager.class);
        mockServiceManager.setStrict(true);
        mockServiceManager.expectAndReturn("getServiceWithName", new Constraint[] { P.eq(RepositoryManager.VCS_SERVICE_NAME) }, mockService.proxy());
        mockServiceManager.expectAndReturn("getScheduleSkipper", skipper);

        final ServiceManager serviceManager = (ServiceManager) mockServiceManager.proxy();
        final DefaultRepositoryManager repositoryManager = new DefaultRepositoryManager(null, null, serviceManager, null, null, null)
        {
            @Override
            public void refresh() throws GenericEntityException
            {
            // do nothing - not interested
            }
        };

        repositoryManager.markVcsServiceToRun();
        assertEquals(1, count.get());
    }

    public void testGetRepositoriesForProject() throws GenericEntityException
    {
        final Collection repositories = repositoryManager.getRepositoriesForProject(projectA);
        final CvsRepository expectedRepository = new CvsRepository(psa, null);
        expectedRepository.setId(psa.getLong("id"));
        checkSingleElementCollection(repositories, expectedRepository);
    }

    public void testGetRepositoriesForProjectNoProject() throws GenericEntityException
    {
        try
        {
            repositoryManager.getRepositoriesForProject(null);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Tried to get repository for null project", e.getMessage());
        }
    }

    public void testGetRepositoriesForProjectNotAProject() throws GenericEntityException
    {
        try
        {
            repositoryManager.getRepositoriesForProject(UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(10), "summary",
                "test issue")));
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("getProviderForProject called with an entity of type 'Issue' - which is not a project", e.getMessage());
        }
    }

    public void testGetProjectsForRepositoryNullRepository() throws GenericEntityException
    {
        try
        {
            repositoryManager.getProjectsForRepository(null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Tried to get projects for null repository", e.getMessage());
        }
    }

    public void testGetProjectsForRepository() throws GenericEntityException
    {
        final CvsRepository repository = new CvsRepository(psa, null);
        repository.setId(psa.getLong("id"));

        final Collection projects = repositoryManager.getProjectsForRepository(repository);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(projectA));
        assertTrue(projects.contains(projectB));
    }

    public void testGetRepository() throws GenericEntityException
    {
        final CvsRepository expectedRepository = new CvsRepository(psa, null);
        expectedRepository.setId(psa.getLong("id"));

        final Repository repository = repositoryManager.getRepository(new Long(psa.getLong("id")));
        assertEquals(expectedRepository, repository);
    }

    public void testIsValidType()
    {
        for (final String vcsType : RepositoryManager.VCS_TYPES)
        {
            assertTrue(repositoryManager.isValidType(vcsType));
        }

        assertFalse(repositoryManager.isValidType("some-other-type"));
    }

    public void testCreateRepositoryWrongType() throws Exception
    {
        final String type = "some-type";
        try
        {
            repositoryManager.createRepository(type, "name", "description", new Properties());
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Unhandled VCS provider type " + type, e.getMessage());
        }
    }

    public void testCreateRepository() throws Exception
    {
        final String name = "cvs test repo";
        final String description = "cvs test repo description";
        final String logFilePath = "some log file path";
        final String cvsRoot = "some cvs root";
        final String moduleName = "some module name";
        final String fetchLog = "true";
        final String password = "somepassword";
        final String baseUrl = "http://www.somewhere.com";

        final Mock mockActionDispatcher = new Mock(ActionDispatcher.class);
        mockActionDispatcher.setStrict(true);
        mockActionDispatcher.expectNotCalled("execute");
        CoreFactory.setActionDispatcher((ActionDispatcher) mockActionDispatcher.proxy());

        final Properties properties = new Properties();

        properties.setProperty(CvsRepository.KEY_LOG_FILE_PATH, logFilePath);
        properties.setProperty(CvsRepository.KEY_CVS_ROOT, cvsRoot);
        properties.setProperty(CvsRepository.KEY_MODULE_NAME, moduleName);
        properties.setProperty(CvsRepository.KEY_FETCH_LOG, fetchLog);
        properties.setProperty(CvsRepository.KEY_PASSWORD, password);
        properties.setProperty(Repository.KEY_REPOSITTORY_BROWSER_TYPE, RepositoryBrowser.VIEW_CVS_TYPE);
        properties.setProperty(ViewCvsBrowser.KEY_BASE_URL, baseUrl);

        final Repository repository = repositoryManager.createRepository(RepositoryManager.CVS_TYPE, name, description, properties);

        // Check the db that all the required records are created
        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final List versionControlGVs = genericDelegator.findByAnd("VersionControl", EasyMap.build("id", repository.getId()));
        assertEquals(1, versionControlGVs.size());

        final GenericValue versionControlGV = (GenericValue) versionControlGVs.get(0);
        assertEquals(repository.getId(), versionControlGV.getLong("id"));
        assertEquals(name, versionControlGV.getString("name"));
        assertEquals(description, versionControlGV.getString("description"));

        final PropertySet propertySet = OFBizPropertyUtils.getPropertySet(versionControlGV);
        assertEquals(logFilePath, propertySet.getString(CvsRepository.KEY_LOG_FILE_PATH));
        assertEquals(cvsRoot, propertySet.getString(CvsRepository.KEY_CVS_ROOT));
        assertEquals(moduleName, propertySet.getString(CvsRepository.KEY_MODULE_NAME));
        assertEquals(fetchLog, propertySet.getString(CvsRepository.KEY_FETCH_LOG));
        assertEquals(password, propertySet.getString(CvsRepository.KEY_PASSWORD));
        assertEquals(baseUrl, propertySet.getString(ViewCvsBrowser.KEY_BASE_URL));
        mockActionDispatcher.verify();
    }

    public void testCreateRepositoryFirstRepository() throws Exception
    {
        // Test with no cvs root parameter
        _testCreateRepository(null);
    }

    public void testCreateRepositoryFirstRepositoryWithCVsRootParameter() throws Exception
    {
        // Test with no cvs root parameter
        _testCreateRepository("mycvsroot");
    }

    private void _testCreateRepository(final String viewCVSRootParameter) throws Exception
    {
        final String name = "cvs test repo";
        final String description = "cvs test repo description";
        final String logFilePath = "some log file path";
        final String cvsRoot = "some cvs root";
        final String moduleName = "some module name";
        final String fetchLog = "true";
        final String password = "somepassword";
        final String baseUrl = "http://www.somewhere.com";

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final List all = genericDelegator.findAll("VersionControl");
        genericDelegator.removeAll(all);

        final Mock mockServiceManager = new Mock(ServiceManager.class);
        mockServiceManager.setStrict(true);
        mockServiceManager.expectAndReturn("addService", P.args(new IsEqual(RepositoryManager.VCS_SERVICE_NAME), new IsEqual(
            "com.atlassian.jira.service.services.vcs.VcsService"), new IsEqual(RepositoryManager.VCS_SERVICE_DELAY)), null);

        repositoryManager = new DefaultRepositoryManager(CoreFactory.getAssociationManager(), ComponentAccessor.getOfBizDelegator(),
            (ServiceManager) mockServiceManager.proxy(), null, null, null);

        final Properties properties = new Properties();
        properties.setProperty(CvsRepository.KEY_LOG_FILE_PATH, logFilePath);
        properties.setProperty(CvsRepository.KEY_CVS_ROOT, cvsRoot);
        properties.setProperty(CvsRepository.KEY_MODULE_NAME, moduleName);
        properties.setProperty(CvsRepository.KEY_FETCH_LOG, fetchLog);
        properties.setProperty(CvsRepository.KEY_PASSWORD, password);
        properties.setProperty(Repository.KEY_REPOSITTORY_BROWSER_TYPE, RepositoryBrowser.VIEW_CVS_TYPE);
        properties.setProperty(ViewCvsBrowser.KEY_BASE_URL, baseUrl);
        if (viewCVSRootParameter != null)
        {
            properties.setProperty(ViewCvsBrowser.ROOT_PARAMETER, viewCVSRootParameter);
        }

        final Repository repository = repositoryManager.createRepository(RepositoryManager.CVS_TYPE, name, description, properties);

        // Check the db that all the required records are created
        final List versionControlGVs = genericDelegator.findByAnd("VersionControl", EasyMap.build("id", repository.getId()));
        assertEquals(1, versionControlGVs.size());

        final GenericValue versionControlGV = (GenericValue) versionControlGVs.get(0);
        assertEquals(repository.getId(), versionControlGV.getLong("id"));
        assertEquals("cvs test repo", versionControlGV.getString("name"));
        assertEquals("cvs test repo description", versionControlGV.getString("description"));

        final PropertySet propertySet = OFBizPropertyUtils.getPropertySet(versionControlGV);
        assertEquals(logFilePath, propertySet.getString(CvsRepository.KEY_LOG_FILE_PATH));
        assertEquals(cvsRoot, propertySet.getString(CvsRepository.KEY_CVS_ROOT));
        assertEquals(moduleName, propertySet.getString(CvsRepository.KEY_MODULE_NAME));
        assertEquals(fetchLog, propertySet.getString(CvsRepository.KEY_FETCH_LOG));
        assertEquals(password, propertySet.getString(CvsRepository.KEY_PASSWORD));
        assertEquals(baseUrl, propertySet.getString(ViewCvsBrowser.KEY_BASE_URL));
        assertEquals(viewCVSRootParameter, propertySet.getString(ViewCvsBrowser.ROOT_PARAMETER));
        mockServiceManager.verify();
    }

    public void testUpdateRepository() throws Exception
    {
        _testUpdateRepository(null);
    }

    public void testUpdateRepositoryWithViewCVSRootParam() throws Exception
    {
        _testUpdateRepository("mycvsroot");
    }

    private void _testUpdateRepository(final String viewCVSRootParameter) throws Exception
    {
        final String name = "cvs test repo";
        final String description = "cvs test repo description";
        final String logFilePath = "some log file path";
        final String cvsRoot = "some cvs root";
        final String moduleName = "some module name";
        final String fetchLog = "true";
        final String password = "somepassword";
        final String baseUrl = "http://www.somewhere.com";

        final Properties properties = new Properties();
        properties.setProperty(CvsRepository.KEY_LOG_FILE_PATH, logFilePath);
        properties.setProperty(CvsRepository.KEY_CVS_ROOT, cvsRoot);
        properties.setProperty(CvsRepository.KEY_MODULE_NAME, moduleName);
        properties.setProperty(CvsRepository.KEY_FETCH_LOG, fetchLog);
        properties.setProperty(CvsRepository.KEY_PASSWORD, password);
        properties.setProperty(ViewCvsBrowser.KEY_BASE_URL, baseUrl);
        properties.setProperty(Repository.KEY_REPOSITTORY_BROWSER_TYPE, RepositoryBrowser.VIEW_CVS_TYPE);
        if (viewCVSRootParameter != null)
        {
            properties.setProperty(ViewCvsBrowser.ROOT_PARAMETER, viewCVSRootParameter);
        }

        final Long id = psa.getLong("id");
        repositoryManager.updateRepository(id, RepositoryManager.CVS_TYPE, name, description, properties);

        // Check the db that all the required records are created
        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final List versionControlGVs = genericDelegator.findByAnd("VersionControl", EasyMap.build("id", id));
        assertEquals(1, versionControlGVs.size());

        final GenericValue versionControlGV = (GenericValue) versionControlGVs.get(0);
        assertEquals(id, versionControlGV.getLong("id"));
        assertEquals(name, versionControlGV.getString("name"));
        assertEquals(description, versionControlGV.getString("description"));

        final PropertySet propertySet = OFBizPropertyUtils.getPropertySet(versionControlGV);
        assertEquals(logFilePath, propertySet.getString(CvsRepository.KEY_LOG_FILE_PATH));
        assertEquals(cvsRoot, propertySet.getString(CvsRepository.KEY_CVS_ROOT));
        assertEquals(moduleName, propertySet.getString(CvsRepository.KEY_MODULE_NAME));
        assertEquals(fetchLog, propertySet.getString(CvsRepository.KEY_FETCH_LOG));
        assertEquals(password, propertySet.getString(CvsRepository.KEY_PASSWORD));
        assertEquals(baseUrl, propertySet.getString(ViewCvsBrowser.KEY_BASE_URL));
        assertEquals(viewCVSRootParameter, propertySet.getString(ViewCvsBrowser.ROOT_PARAMETER));
    }

    public void testUpdateRepositoryNullRepository() throws GenericEntityException
    {
        final String type = "some-type";
        try
        {
            repositoryManager.updateRepository(new Long(psa.getLong("id")), type, "cvs test repo", "cvs test repo description", new Properties());
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Unhandled VCS provider type " + type, e.getMessage());
        }
    }

    public void testRemoveRepository() throws Exception
    {
        final Mock mockServiceManager = new Mock(ServiceManager.class);
        mockServiceManager.setStrict(true);
        mockServiceManager.expectNotCalled("removeServiceByName");

        final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        repositoryManager = new DefaultRepositoryManager(CoreFactory.getAssociationManager(), ofBizDelegator,
            (ServiceManager) mockServiceManager.proxy(), null, null, null);

        final Long id = psa.getLong("id");
        List versionControlGVs = ofBizDelegator.findByAnd("VersionControl", EasyMap.build("id", id));
        assertEquals(1, versionControlGVs.size());

        final GenericValue versionControlGV = (GenericValue) versionControlGVs.get(0);

        repositoryManager.removeRepository(id);

        // Ensure that all the records are gone
        versionControlGVs = ofBizDelegator.findByAnd("VersionControl", EasyMap.build("id", id));
        assertTrue(versionControlGVs.isEmpty());

        final PropertySet propertySet = OFBizPropertyUtils.getPropertySet(versionControlGV);
        assertTrue(propertySet.getKeys().isEmpty());

        mockServiceManager.verify();
    }

    public void testRemoveRepositoryLastRepository() throws Exception
    {
        final Mock mockServiceManager = new Mock(ServiceManager.class);
        mockServiceManager.setStrict(true);
        mockServiceManager.expectVoid("removeServiceByName", P.args(new IsEqual(RepositoryManager.VCS_SERVICE_NAME)));

        final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        repositoryManager = new DefaultRepositoryManager(CoreFactory.getAssociationManager(), ofBizDelegator,
            (ServiceManager) mockServiceManager.proxy(), null, null, null);

        repositoryManager.removeRepository(new Long(psa.getLong("id")));
        repositoryManager.removeRepository(new Long(psb.getLong("id")));

        // Ensure that all the records are gone
        mockServiceManager.verify();
    }

    public void testSetProjectRepositories() throws GenericEntityException
    {
        repositoryManager.setProjectRepositories(projectB, EasyList.build(psa.getLong("id"), psb.getLong("id")));

        final List versionControlGVs = CoreFactory.getAssociationManager().getSinkFromSource(projectB, "VersionControl",
            ProjectRelationConstants.PROJECT_VERSIONCONTROL, false);
        assertEquals(2, versionControlGVs.size());

        GenericValue versionControlGV = (GenericValue) versionControlGVs.get(1);
        assertEquals(new Long(psb.getLong("id")), versionControlGV.getLong("id"));
        versionControlGV = (GenericValue) versionControlGVs.get(0);
        assertEquals(new Long(psa.getLong("id")), versionControlGV.getLong("id"));
    }

    public void testGetRepositories() throws GenericEntityException
    {
        final Collection repositories = repositoryManager.getRepositories();
        assertEquals(2, repositories.size());

        CvsRepository expectedRepository = new CvsRepository(psa, null);
        expectedRepository.setId(psa.getLong("id"));
        assertTrue(repositories.contains(expectedRepository));
        expectedRepository = new CvsRepository(psb, null);
        expectedRepository.setId(psb.getLong("id"));
        assertTrue(repositories.contains(expectedRepository));
    }

    /**
     * Test IllegalArgumentException is thrown when a null Issue is passed in
     */
    public void testGetCommitsNullIssue()
    {
        try
        {
            repositoryManager.getCommits(null, user);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Issue cannot be null.", e.getMessage());
        }
    }

    /**
     * Test that an empty map is returned for getCommits() if the user does not have the correct permission to view the
     * commit information for the issue
     */
    public void testGetCommitsNoPermission() throws GenericEntityException, RepositoryException
    {
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.VIEW_VERSION_CONTROL), new IsEqual(
            issueObject), new IsEqual(user)), Boolean.FALSE);

        final Map commits = repositoryManager.getCommits(issueObject, user);

        assertNotNull(commits);
        assertTrue(commits.isEmpty());
        mockPermissionManager.verify();
    }

    /**
     * Test that if a repository has not parsed the index, that the repository id is mapped to null
     */
    public void testGetCommitsLogsNotParsed()
    {
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.VIEW_VERSION_CONTROL), new IsEqual(
            issueObject), new IsEqual(user)), Boolean.TRUE);

        final Map commits = repositoryManager.getCommits(issueObject, user);

        assertEquals(1, commits.size());
        assertNull(commits.get(vcsConfigA.getLong("id")));
        mockPermissionManager.verify();
    }

    /**
     * Test that if there is no repository, then an empty map is returned
     */
    public void testProjectWithNoRepository() throws GenericEntityException
    {
        final GenericValue projectWithNoRepo = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(103), "name",
            "Project with no repo"));

        //check that the getRepositoriesForProject() returns an empty collection
        final Collection repositoriesForProject = repositoryManager.getRepositoriesForProject(projectWithNoRepo);
        assertTrue(repositoriesForProject.isEmpty());

        //check that the getcommits will return an empty map as there are no repositories
        final Map commits = repositoryManager.getCommits(issueObject, user);
        assertTrue(commits.isEmpty());
    }

    private PropertySet createDefaultCvsPropertySet()
    {
        final MapPropertySet propertySet = new MapPropertySet();
        propertySet.setMap(new HashMap());
        propertySet.setString(CvsRepository.KEY_MODULE_NAME, "default module");
        propertySet.setString(CvsRepository.KEY_PASSWORD, "default password");
        propertySet.setString(CvsRepository.KEY_CVS_ROOT, "default root");
        propertySet.setString(CvsRepository.KEY_FETCH_LOG, "true");
        return propertySet;
    }

    private Properties createDefaultCvsProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(CvsRepository.KEY_MODULE_NAME, "default module");
        properties.setProperty(CvsRepository.KEY_PASSWORD, "default password");
        properties.setProperty(CvsRepository.KEY_CVS_ROOT, "default root");
        properties.setProperty(CvsRepository.KEY_FETCH_LOG, "true");
        return properties;
    }

}
