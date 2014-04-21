package com.atlassian.jira.web.action.admin.vcs;

import alt.java.io.FileImpl;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.LockException;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryBrowser;
import com.atlassian.jira.vcs.RepositoryException;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepository;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.vcs.cvsimpl.ValidationException;
import com.atlassian.jira.vcs.viewcvs.ViewCvsBrowser;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.model.Commit;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import webwork.action.Action;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
public class TestRepositoryActionSupport extends LegacyJiraMockTestCase
{
    RepositoryActionSupport repositoryActionSupport;
    private Mock mockRepositoryManager;
    private Mock mockRepositoryUtil;

    private Long id = 10000L;
    private String name = "Test CVS Repository";
    private String description = "Test-cvs-description for the repository";
    private String cvsLogFilePath = System.getProperty("java.io.tempdir") + File.separator + "cvs.log";
    private String moduleName = "testcvsmodule";
    private String cvsRoot = ":pserver:test@testserver:/root/test";
    private String password = "test-cvs-password";
    private boolean fetchLog = true;
    private String baseURL = "http://test-cvs-baseURL";
    private String badBaseUrl = "badBaseUrl";
    private String repositoryBroswerType = "VIEW_CVS";
    private String viewCvsRootParameter = "mymoduleroot";
    private String type = "cvs";
    private String timeout = "10";
    private long timeOutLong = 10000;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);

        mockRepositoryUtil = new Mock(CvsRepositoryUtil.class);
        mockRepositoryUtil.setStrict(true);

        repositoryActionSupport = new RepositoryActionSupport((RepositoryManager) mockRepositoryManager.proxy(), (CvsRepositoryUtil) mockRepositoryUtil.proxy());
    }

    public void testGettersSetters()
    {
        repositoryActionSupport.setId(id);
        assertEquals(id, repositoryActionSupport.getId());

        repositoryActionSupport.setName(name);
        assertEquals(name, repositoryActionSupport.getName());

        repositoryActionSupport.setDescription(description);
        assertEquals(description, repositoryActionSupport.getDescription());

        repositoryActionSupport.setLogFilePath(cvsLogFilePath);
        assertEquals(cvsLogFilePath, repositoryActionSupport.getLogFilePath());

        repositoryActionSupport.setCvsRoot(cvsRoot);
        assertEquals(cvsRoot, repositoryActionSupport.getCvsRoot());

        repositoryActionSupport.setModuleName(moduleName);
        assertEquals(moduleName, repositoryActionSupport.getModuleName());

        repositoryActionSupport.setFetchLog(fetchLog);
        assertEquals(fetchLog, repositoryActionSupport.isFetchLog());

        mockRepositoryManager.expectAndReturn("isValidType", P.args(new IsEqual(type)), Boolean.TRUE);

        repositoryActionSupport.setType(type);
        assertEquals(type, repositoryActionSupport.getType());

        repositoryActionSupport.setRepositoryBrowserRootParam(viewCvsRootParameter);
        assertEquals(viewCvsRootParameter, repositoryActionSupport.getRepositoryBrowserRootParam());

        repositoryActionSupport.setRepositoryBrowserURL(baseURL);
        assertEquals(baseURL, repositoryActionSupport.getRepositoryBrowserURL());
    }

    public void testBadBrowserType()
    {
        mockRepositoryManager.expectAndReturn("isValidType", P.args(new IsEqual(type)), Boolean.FALSE);

        repositoryActionSupport.setType(type);
        assertNull(repositoryActionSupport.getType());
    }

    public void testDoDefaultNoId() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ViewRepositories.jspa");

        final String result = repositoryActionSupport.doDefault();
        assertEquals(Action.NONE, result);
        mockHttpServletResponse.verify();
    }

    public void testDoDefaultNotCvsRepository() throws Exception
    {
        repositoryActionSupport.setId(id);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(id)), new Repository()
        {
            public List<Commit> getCommitsForIssue(String issueKey) throws RepositoryException
            {
                return null;
            }

            public Long getId()
            {
                return null;
            }

            public String getName()
            {
                return null;
            }

            public String getDescription()
            {
                return null;
            }

            public String getType()
            {
                return null;
            }

            public void setRepositoryBrowser(RepositoryBrowser repositoryBrowser)
            {
            }

            public RepositoryBrowser getRepositoryBrowser()
            {
                return null;
            }

            public void copyContent(Repository repository)
            {
            }

            public int compareTo(Repository o)
            {
                return 0;
            }
        });

        final String result = repositoryActionSupport.doDefault();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(repositoryActionSupport.getErrorMessages(), "The repository with id '" + id + "' is not a CVS repository.");
    }

    public void testDoDefault() throws Exception
    {
        repositoryActionSupport.setId(id);
        CvsRepository cvsRepository = new CvsRepository(setupPropertySet(), null);

        mockRepositoryManager.expectAndReturn("isValidType", P.args(new IsEqual(type)), Boolean.TRUE);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(id)), cvsRepository);

        final String result = repositoryActionSupport.doDefault();
        assertEquals(Action.INPUT, result);

        assertEquals(name, repositoryActionSupport.getName());
        assertEquals(description, repositoryActionSupport.getDescription());
        assertEquals(cvsLogFilePath, repositoryActionSupport.getLogFilePath());
        assertEquals(cvsRoot, repositoryActionSupport.getCvsRoot());
        assertEquals(moduleName, repositoryActionSupport.getModuleName());
        assertEquals(fetchLog, repositoryActionSupport.isFetchLog());
        assertEquals(viewCvsRootParameter, repositoryActionSupport.getRepositoryBrowserRootParam());
        assertEquals(baseURL + "/", repositoryActionSupport.getRepositoryBrowserURL());
    }

    private PropertySet setupPropertySet()
    {
        return new MapPropertySet()
        {
            public String getString(String key)
            {
                if (CvsRepository.KEY_PASSWORD.equals(key))
                {
                    return password;
                }
                else if (CvsRepository.KEY_LOG_FILE_PATH.equals(key))
                {
                    return cvsLogFilePath;
                }
                else if (CvsRepository.KEY_CVS_ROOT.equals(key))
                {
                    return cvsRoot;
                }
                else if (CvsRepository.KEY_DESCRIPTION.equals(key))
                {
                    return description;
                }
                else if (CvsRepository.KEY_NAME.equals(key))
                {
                    return name;
                }
                else if (CvsRepository.KEY_MODULE_NAME.equals(key))
                {
                    return moduleName;
                }
                else if (CvsRepository.KEY_FETCH_LOG.equals(key))
                {
                    return String.valueOf(fetchLog);
                }
                else if (CvsRepository.KEY_CVS_TIMEOUT.equals(key))
                {
                    return timeout;
                }
                else if (CvsRepository.KEY_REPOSITTORY_BROWSER_TYPE.equals(key))
                {
                    return repositoryBroswerType;
                }
                else if (ViewCvsBrowser.KEY_BASE_URL.equals(key))
                {
                    return baseURL;
                }
                else if (ViewCvsBrowser.ROOT_PARAMETER.equals(key))
                {
                    return viewCvsRootParameter;
                }

                throw new IllegalArgumentException("Invalid property " + key);
            }
        };
    }

    public void testDoView()
    {
        assertEquals(Action.SUCCESS, repositoryActionSupport.doView());
    }

    public void testValidateRepositoryParameters()
    {
        _testNoParameters(4);
    }


    public void testValidateRepositoryParametersWithBadUrl()
    {
        repositoryActionSupport.setRepositoryBrowserURL(badBaseUrl);
        Map errors = _testNoParameters(5);
        assertEquals("Invalid URL format", errors.get("repositoryBrowserURL"));
    }

    private Map _testNoParameters(int size)
    {
        repositoryActionSupport.validateRepositoryParameters();
        final Map errors = repositoryActionSupport.getErrors();
        assertNotNull(errors);
        assertEquals(size, errors.size());
        assertEquals("You must specify a name for the repository", errors.get("name"));
        assertEquals("You must specify the cvs root of the module", errors.get("cvsRoot"));
        assertEquals("You must specify the name of the cvs module", errors.get("moduleName"));
        assertEquals("You must specify the full path to the cvs log file", errors.get("logFilePath"));
        return errors;
    }

    public void testCheckLogFilePath()
    {
        final ValidationException validationException = new ValidationException("Some error message.");
        mockRepositoryUtil.expectAndThrow("checkLogFilePath", P.args(new IsAnything(), new IsEqual(fetchLog)), validationException);
        mockRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));

        repositoryActionSupport.setName(name);
        repositoryActionSupport.setCvsRoot(cvsRoot);
        repositoryActionSupport.setModuleName(moduleName);
        repositoryActionSupport.setLogFilePath(cvsLogFilePath);
        repositoryActionSupport.setFetchLog(fetchLog);

        repositoryActionSupport.validateRepositoryParameters();
        final Map errors = repositoryActionSupport.getErrors();
        assertNotNull(errors);
        assertEquals(validationException.getMessage(), errors.get("logFilePath"));
    }

    public void testCheckCvsRoot()
    {
        final ValidationException validationException = new ValidationException("Some error message.");
        mockRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(fetchLog)));
        mockRepositoryUtil.expectAndThrow("checkCvsRoot", P.args(new IsEqual(cvsRoot)), validationException);

        repositoryActionSupport.setName(name);
        repositoryActionSupport.setCvsRoot(cvsRoot);
        repositoryActionSupport.setModuleName(moduleName);
        repositoryActionSupport.setLogFilePath(cvsLogFilePath);
        repositoryActionSupport.setFetchLog(fetchLog);

        repositoryActionSupport.validateRepositoryParameters();
        final Map errors = repositoryActionSupport.getErrors();
        assertNotNull(errors);
        assertEquals(validationException.getMessage(), errors.get("cvsRoot"));
    }

    public void testCheckRepositoryAuthenticationException()
    {
        final AuthenticationException exception = new AuthenticationException("some message", "some localized message");
        _testCheckRepository(exception, "Error authenticating with the repository: " + exception.getLocalizedMessage());

    }

    public void testCheckRepositoryLockExcpetion()
    {
        final LockException exception = new LockException("some lock message");
        _testCheckRepository(exception, "Error obtaining lock: " + exception.getMessage());
    }

    public void testCheckRepositoryIOException()
    {
        final IOException exception = new IOException("some io mesage message");
        _testCheckRepository(exception, exception.getMessage());
    }

    public void testCheckRepositoryCommandException()
    {
        final CommandException exception = new CommandException("some message", "some command error message");
        _testCheckRepository(exception, "Error occurred while retrieving cvs log: " + exception.getMessage());
    }

    public void testCheckRepositoryThrowable()
    {
        final RuntimeException exception = new RuntimeException("some throwable error message");
        _testCheckRepository(exception, "Error occurred while obtaining cvs log or parsing the cvs log. Please consult the log file for more details.");
    }

    public void testCheckRepositoryNoFetchLogNoLogFile()
    {
        final boolean fetchLog = false;
        mockRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(fetchLog)));
        mockRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));

        alt.java.io.File file = new FileImpl("file-that-hopefully-will-never-ever-exist-if-it-does-the-test-is-useless");
        repositoryActionSupport.setName(name);
        repositoryActionSupport.setCvsRoot(cvsRoot);
        repositoryActionSupport.setModuleName(moduleName);
        repositoryActionSupport.setLogFilePath(file.getAbsolutePath());
        repositoryActionSupport.setPassword(password);
        repositoryActionSupport.setFetchLog(fetchLog);

        repositoryActionSupport.validateRepositoryParameters();
        checkSingleElementCollection(repositoryActionSupport.getErrorMessages(),
                                    "Log file cannot be found. Please retrieve the CVS log manually and place it on the specified path: " + file.getAbsolutePath());
    }

    public void testCheckRepositoryParseLogLockException()
    {
        final LockException exception = new LockException("Some lock message");
        _testCheckRepositoryParseLog(exception, "Error obtaining lock: " + exception.getMessage());
    }

    public void testCheckRepositoryParseLogIOException()
    {
        final IOException exception = new IOException("Some io exception message");
        _testCheckRepositoryParseLog(exception, exception.getMessage());
    }

    public void testCheckRepositoryParseLogLogSyntaxException()
    {
        final LogSyntaxException exception = new LogSyntaxException("Some log syntax error message");
        _testCheckRepositoryParseLog(exception, "Error parsing cvs log: " + exception.getMessage());
    }

    public void testCheckRepository()
    {
        mockRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(fetchLog)));
        mockRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeOutLong)});

        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, null);

        repositoryActionSupport.setName(name);
        repositoryActionSupport.setCvsRoot(cvsRoot);
        repositoryActionSupport.setModuleName(moduleName);
        repositoryActionSupport.setLogFilePath(cvsLogFilePath);
        repositoryActionSupport.setPassword(password);
        repositoryActionSupport.setFetchLog(fetchLog);
        repositoryActionSupport.setTimeout(timeout);

        repositoryActionSupport.validateRepositoryParameters();
        assertFalse(repositoryActionSupport.invalidInput());
    }

    private void _testCheckRepositoryParseLog(Exception exception, String expectedErrorMessage)
    {
        mockRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(fetchLog)));
        mockRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeOutLong)});

        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockRepositoryUtil.expectAndThrow("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, exception);

        repositoryActionSupport.setName(name);
        repositoryActionSupport.setCvsRoot(cvsRoot);
        repositoryActionSupport.setModuleName(moduleName);
        repositoryActionSupport.setLogFilePath(cvsLogFilePath);
        repositoryActionSupport.setPassword(password);
        repositoryActionSupport.setFetchLog(fetchLog);
        repositoryActionSupport.setTimeout(timeout);

        repositoryActionSupport.validateRepositoryParameters();
        checkSingleElementCollection(repositoryActionSupport.getErrorMessages(), expectedErrorMessage);
    }

    private void _testCheckRepository(Exception exception, String expectedErrorMessage)
    {
        mockRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(fetchLog)));
        mockRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockRepositoryUtil.expectAndThrow("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeOutLong)}, exception);

        repositoryActionSupport.setName(name);
        repositoryActionSupport.setCvsRoot(cvsRoot);
        repositoryActionSupport.setModuleName(moduleName);
        repositoryActionSupport.setLogFilePath(cvsLogFilePath);
        repositoryActionSupport.setPassword(password);
        repositoryActionSupport.setFetchLog(fetchLog);
        repositoryActionSupport.setTimeout(timeout);

        repositoryActionSupport.validateRepositoryParameters();
        checkSingleElementCollection(repositoryActionSupport.getErrorMessages(), expectedErrorMessage);
    }

    protected void tearDown() throws Exception
    {
        mockRepositoryManager.verify();
        mockRepositoryUtil.verify();

        super.tearDown();
    }
}
