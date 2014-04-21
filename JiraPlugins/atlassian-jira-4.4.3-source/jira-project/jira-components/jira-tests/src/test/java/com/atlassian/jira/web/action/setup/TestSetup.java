package com.atlassian.jira.web.action.setup;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.core.ofbiz.test.mock.MockAtlassianServletRequest;
import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.MockAttachmentPathManager;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockJiraHome;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpSession;
import org.easymock.EasyMock;
import webwork.action.Action;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpSession;
import java.util.Map;

import static org.easymock.EasyMock.expect;

public class TestSetup extends AbstractUsersIndexingTestCase
{
    private Setup asa;
    private Mock mockIndexManager;
    private ExternalLinkUtil externalLinkUtil;
    private final MockAttachmentPathManager attachmentPathManager = new MockAttachmentPathManager();
    private final MockIndexPathManager indexPathManager = new MockIndexPathManager();
    private MockController mockController;
    private JiraLicenseUpdaterService jiraLicenseService;
    private LicenseDetails licenseDetails;
    private JiraLicenseService.ValidationResult validationResult;
    private BuildUtilsInfo buildUtilsInfo;
    private JiraSystemRestarter jiraSystemRestarter;
    private MockJiraHome jiraHome;

    public TestSetup(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mockController = new MockController();

        jiraLicenseService = mockController.getMock(JiraLicenseUpdaterService.class);
        licenseDetails = mockController.getMock(LicenseDetails.class);
        validationResult = mockController.getMock(JiraLicenseService.ValidationResult.class);
        buildUtilsInfo = mockController.getMock(BuildUtilsInfo.class);
        jiraSystemRestarter = mockController.getMock(JiraSystemRestarter.class);
        externalLinkUtil = mockController.getMock(ExternalLinkUtil.class);

        final MockAtlassianServletRequest request = new MockAtlassianServletRequest();
        request.setScheme("scheme");
        request.setServerName("server");
        request.setServerPort(1);
        request.setContextPath("contextpath");
        ServletActionContext.setRequest(request);

        final HttpSession session = new MockHttpSession();
        request.setSession(session);

        mockIndexManager = new Mock(IssueIndexManager.class);
        mockIndexManager.setStrict(true);
        mockIndexManager.expectAndReturn("isIndexingEnabled", true);

        final Mock mockServiceManager = new Mock(ServiceManager.class);
        mockServiceManager.setStrict(true);
        mockServiceManager.expectAndReturn("addService", new Constraint[] { new IsEqual("Backup Service"), new IsEqual(
                "com.atlassian.jira.service.services.export.ExportService"), new IsEqual(new Long(DateUtils.HOUR_MILLIS * 12)), new IsAnything() }, null);
        mockServiceManager.expectAndReturn("getServiceWithName", P.args(new IsEqual("Backup Service")), null);

        jiraHome = new MockJiraHome();
        
        asa = new Setup((IssueIndexManager) mockIndexManager.proxy(),
                (ServiceManager) mockServiceManager.proxy(), indexPathManager, attachmentPathManager, jiraHome, jiraLicenseService, buildUtilsInfo, jiraSystemRestarter, null, externalLinkUtil);
        asa.setNextStep("true");
    }

    @Override
    protected void tearDown() throws Exception
    {
        // Reset the request object in the thread local
        ServletActionContext.setRequest(null);
        super.tearDown();
    }

    public void testGetSets()
    {
        assertNull(asa.getAttachmentPath());
        assertNull(asa.getIndexPath());
        assertNull(asa.getLicense());
        assertNull(asa.getTitle());
        assertEquals("public", asa.getMode());

        // license
        asa.setLicense("testLicense");
        assertEquals("testLicense", asa.getLicense());

        // the mode
        asa.setMode("public");
        assertEquals("public", asa.getMode());

        // get allowed modes
        final Map<?, ?> modes = asa.getAllowedModes();
        assertEquals("Public", modes.get("public"));
        assertEquals("Private", modes.get("private"));
        assertEquals(2, modes.size());

        // base URL
        assertEquals("scheme://server:1contextpath", asa.getBaseURL());
        asa.setBaseURL("http://www.google.com");
        assertEquals("http://www.google.com", asa.getBaseURL());

        // titles
        asa.setTitle("Test");
        assertEquals("Test", asa.getTitle());
    }

    public void testDoDefault() throws Exception
    {
        // setup already
        asa.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", asa.doDefault());
        asa.getApplicationProperties().setString(APKeys.JIRA_SETUP, null);

        expect(jiraLicenseService.getLicense()).andReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andReturn(false);
        mockController.replay();

        // all nulls
        assertEquals(Action.NONE, asa.doDefault());
        assertEquals("Your Company JIRA", asa.getTitle());
        assertEquals("public", asa.getMode());

        mockController.reset();
        expect(jiraLicenseService.getLicense()).andReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andReturn(true);
        expect(licenseDetails.getLicenseString()).andReturn("A license String");
        mockController.replay();

        // Initialize the license manager with JIRA specific license details
        asa.getApplicationProperties().setString(APKeys.JIRA_TITLE, "Test Title");
        asa.getApplicationProperties().setString(APKeys.JIRA_BASEURL, "http://www.google.com");
        asa.getApplicationProperties().setString(APKeys.JIRA_MODE, "private");
        attachmentPathManager.setCustomAttachmentPath("c:/testAttachmentPath");
        indexPathManager.setIndexRootPath("c:/testPathIndex");
        asa.getApplicationProperties().setString("License Message", "testLicense");
        asa.getApplicationProperties().setString("License Hash 1", "testLicenseHash1");
        assertEquals(Action.NONE, asa.doDefault());
        assertEquals("Test Title", asa.getTitle());
        assertEquals("private", asa.getMode());
        assertEquals("c:/testAttachmentPath", asa.getAttachmentPath());
        assertEquals("c:/testPathIndex", asa.getIndexPath());
        assertNotNull(asa.getLicense());

        // check that the encoding is set to UTF-8 by default
        assertEquals("UTF-8", asa.getApplicationProperties().getString(APKeys.JIRA_WEBWORK_ENCODING));
    }

    public void testdoValidationAlreadySetup() throws Exception
    {
        asa.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", asa.execute());
    }

    public void testdoValidationTitle() throws Exception
    {
        setAllValidData();
        asa.setTitle(null);
        checkSingleError("title", "You must specify a title.");
    }

    public void testdoValidationURL() throws Exception
    {
        setAllValidData();
        asa.setBaseURL(null);
        checkSingleError("baseURL", "The URL specified is not valid.");
        asa.setBaseURL("abc");
        checkSingleError("baseURL", "The URL specified is not valid.");
    }

    public void testdoValidationMode() throws Exception
    {
        setAllValidData();
        asa.setMode(null);
        checkSingleError("mode", "Invalid mode specified.");
        asa.setMode("invalid mode");
        checkSingleError("mode", "Invalid mode specified.");
    }

    public void testdoValidationLicense() throws Exception
    {
        setAllValidData();

        mockController.reset();
        expect(jiraLicenseService.getLicense()).andReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andReturn(true);
        expect(licenseDetails.getLicenseString()).andReturn("A license String");
        expect(jiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(validationResult);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("license", "Invalid license key specified.");
        expect(validationResult.getErrorCollection()).andStubReturn(errorCollection);
        mockController.replay();

        asa.setLicense("abc");
        checkSingleError("license", "Invalid license key specified.");
        asa.setLicense("");
        checkSingleError("license", "Invalid license key specified.");
    }



    public void testExecuteFine() throws Exception
    {
        mockIndexManager.expectAndReturn("activate", P.args(P.isA(Context.class)), new Long(0));
        mockIndexManager.expectVoid("deactivate");
        mockIndexManager.expectAndReturn("size", P.args(), new Integer(42));

        setAllValidData();

        final String result = asa.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Test Title", asa.getApplicationProperties().getString(APKeys.JIRA_TITLE));
        assertEquals("http://www.atlassian.com", asa.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        assertEquals("public", asa.getApplicationProperties().getString(APKeys.JIRA_MODE));
        assertEquals(true, asa.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS));
        verifyMocks();
    }

    public void testExecuteFineJiraHomeAttachments() throws Exception
    {
        mockIndexManager.expectAndReturn("activate", P.args(P.isA(Context.class)), new Long(0));
        mockIndexManager.expectVoid("deactivate");
        mockIndexManager.expectAndReturn("size", P.args(), new Integer(42));

        setAllValidData(AttachmentPathManager.Mode.DEFAULT, IndexPathManager.Mode.DEFAULT);

        final String result = asa.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Test Title", asa.getApplicationProperties().getString(APKeys.JIRA_TITLE));
        assertEquals("http://www.atlassian.com", asa.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        assertEquals("public", asa.getApplicationProperties().getString(APKeys.JIRA_MODE));
        assertEquals(true, asa.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS));
        assertEquals(MockAttachmentPathManager.DEFAULT_PATH, attachmentPathManager.getAttachmentPath());
        assertTrue(attachmentPathManager.getUseDefaultDirectory());
        verifyMocks();
    }

    public void testExecuteFail() throws Exception
    {
        setAllValidData();
        final String message = "something went wrong";
        mockIndexManager.expectAndThrow("activate", P.args(P.isA(Context.class)), new RuntimeException(message));
        mockIndexManager.expectVoid("deactivate");
        mockIndexManager.expectAndReturn("size", P.args(), new Integer(42));

        assertEquals(Action.ERROR, asa.execute());
        assertEquals("Could not activate indexing: " + message, asa.getErrors().get("indexPath"));

        verifyMocks();
    }

    public void testLicenseCreated() throws Exception
    {
        mockIndexManager.expectAndReturn("activate", P.args(P.isA(Context.class)), new Long(0));
        mockIndexManager.expectVoid("deactivate");
        mockIndexManager.expectAndReturn("size", P.args(), new Integer(42));

        setAllValidData();

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        assertEquals(Action.SUCCESS, asa.execute());

        verifyMocks();
    }

    private void checkSingleError(final String element, final String error) throws Exception
    {
        assertEquals(Action.INPUT, asa.execute());
        assertEquals(error, asa.getErrors().get(element));
        assertEquals(1, asa.getErrors().size());
    }

    private void setAllValidData()
    {
        setAllValidData(AttachmentPathManager.Mode.CUSTOM, IndexPathManager.Mode.CUSTOM);
    }

    private void setAllValidData(final AttachmentPathManager.Mode attachments, final IndexPathManager.Mode indexes)
    {
        asa.setTitle("Test Title");
        asa.setBaseURL("http://www.atlassian.com");
        asa.setMode("public");

        asa.setAttachmentPathOption(attachments.toString());


        asa.setIndexPathOption(indexes.toString());

        mockController.reset();
        expect(jiraLicenseService.getLicense()).andReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andReturn(true);
        expect(licenseDetails.getLicenseString()).andReturn("A license String");
        expect(jiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(validationResult);
        expect(validationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());
        expect(jiraLicenseService.setLicense(validationResult)).andStubReturn(licenseDetails);
        jiraSystemRestarter.ariseSirJIRA(); EasyMock.expectLastCall();
        mockController.replay();
    }

    private void verifyMocks()
    {
        mockIndexManager.verify();
    }
}
