package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.MockJiraHome;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.upgrade.UpgradeHistoryItem;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.log.JiraLogLocator;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.web.ServletContextProviderListener;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.velocity.VelocityManager;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import javax.activation.DataSource;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class TestJiraSupportRequest extends AbstractUsersTestCase
{
    private JiraSupportRequest jsr;
    private Mock vm;
    private static final String TEST = "Test_Value";
    private MockApplicationProperties mockApplicationProperties;
    private JiraLicenseService jiraLicenseService;
    private BuildUtilsInfo buildUtilsInfo;
    private UpgradeManager upgradeManager;
    private LocaleManager localeManager;
    private PluginInfoProvider pluginInfoProvider;

    public TestJiraSupportRequest(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        final Mock sc = new Mock(ServletContext.class);
        ActionContext.setServletContext((ServletContext) sc.proxy());
        sc.expectAndReturn("getServerInfo", "test");

        final ServletContextEvent scEven = new ServletContextEvent((ServletContext) sc.proxy());
        ServletContextProviderListener listener = new ServletContextProviderListener();
        listener.contextInitialized(scEven);

        mockApplicationProperties = new MockApplicationProperties();

        jiraLicenseService = createMock(JiraLicenseService.class);
        final LicenseDetails licenseDetails = createNiceMock(LicenseDetails.class);
        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(jiraLicenseService.getServerId()).andStubReturn("A server ID");
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andStubReturn(true);
        expect(licenseDetails.isEntitledToSupport()).andStubReturn(true);

        buildUtilsInfo = createMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getVersion()).andStubReturn("JIRA version");
        expect(buildUtilsInfo.getCurrentBuildNumber()).andStubReturn("111");
        expect(buildUtilsInfo.getBuildPartnerName()).andStubReturn("Partner");
        expect(buildUtilsInfo.getCurrentBuildDate()).andStubReturn(new Date());
        expect(buildUtilsInfo.getSvnRevision()).andStubReturn("123");

        upgradeManager = createMock(UpgradeManager.class);
        expect(upgradeManager.getUpgradeHistory()).andStubReturn(Collections.<UpgradeHistoryItem>emptyList());

        localeManager = createMock(LocaleManager.class);
        expect(localeManager.getLocale(EasyMock.<String>anyObject())).andStubReturn(Locale.getDefault());
        expect(localeManager.getInstalledLocales()).andStubReturn(Collections.<Locale>emptySet());

        pluginInfoProvider = createMock(PluginInfoProvider.class);
        expect(pluginInfoProvider.getUserPlugins()).andStubReturn(Collections.<PluginInfoProvider.Info>emptyList());
        expect(pluginInfoProvider.getSystemPlugins()).andStubReturn(Collections.<PluginInfoProvider.Info>emptyList());

        replay(jiraLicenseService, licenseDetails, buildUtilsInfo, upgradeManager, localeManager, pluginInfoProvider);

        final SystemInfoUtils systemInfoUtils = createNiceMock(SystemInfoUtils.class);
        final SystemInfoUtils.DatabaseMetaData databaseMetaData = createMock(SystemInfoUtils.DatabaseMetaData.class);
        expect(databaseMetaData.getDatabaseProductVersion()).andReturn("1.7.1");
        expect(databaseMetaData.getDriverName()).andReturn("hsql");
        expect(databaseMetaData.getDriverVersion()).andReturn("1.7.1");
        expect(systemInfoUtils.getDatabaseType()).andReturn("hsql");
        expect(systemInfoUtils.getDatabaseMetaData()).andReturn(databaseMetaData);
        expect(systemInfoUtils.getDbDescriptorValue()).andReturn("hsql://foo");
        replay(systemInfoUtils, databaseMetaData);

        final ExtendedSystemInfoUtils extendedSystemInfoUtils = new MyExtendedSystemInfoUtils(systemInfoUtils, mockApplicationProperties, new MockI18nBean());

        jsr = new MyJiraSupportRequest(extendedSystemInfoUtils, jiraLicenseService, localeManager, fileFactory, pluginInfoProvider);

        vm = new Mock(VelocityManager.class);
        ManagerFactory.addService(VelocityManager.class, (VelocityManager) vm.proxy());
        final MailServerManager mailServerManager = ComponentAccessor.getMailServerManager();
        final MailServer mailServer = new SMTPMailServerImpl(null, "test", "test", "andreask@atlassian.com", "[JIRA]", false, MailProtocol.SMTP, "localhost", "25", false, "user", "pass");
        mailServerManager.create(mailServer);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(VelocityManager.class, null);
    }

    public void testCopyProperties() throws Exception
    {
        Map<String, String> child = new HashMap<String, String>();
        child.put("AAA", "BBB");

        List<String> listChild = new ArrayList<String>();
        listChild.add("ListChild A");
        listChild.add("ListChild B");

        Map test = new HashMap();
        test.put("CCC", child);
        test.put("FFF", listChild);
        test.put("DDD", "EEE");

        DataSource persistedStr = jsr.persistProperties(test);
        Properties prop = new Properties();
        prop.load(persistedStr.getInputStream());

        assertEquals(4, prop.size());
        assertEquals("BBB", prop.getProperty("CCC_AAA"));
        assertEquals("EEE", prop.getProperty("DDD"));
        assertEquals("ListChild A", prop.getProperty("FFF_1"));
        assertEquals("ListChild B", prop.getProperty("FFF_2"));
    }

    public void testGettersSetters()
    {
        assertNull(jsr.getTo());
        jsr.setTo(TEST);
        assertEquals(TEST, jsr.getTo());

        assertNull(jsr.getCc());
        jsr.setCc(TEST);
        assertEquals(TEST, jsr.getCc());

        assertFalse(jsr.getAttachzipexport());
        jsr.setAttachzipexport(true);
        assertTrue(jsr.getAttachzipexport());

        assertNull(jsr.getDescription());
        jsr.setDescription(TEST);
        assertEquals(TEST, jsr.getDescription());

        assertNotNull(jsr.getName());
        jsr.setName(TEST);
        assertEquals(TEST, jsr.getName());

        assertNotNull(jsr.getEmail());
        jsr.setEmail(TEST);
        assertEquals(TEST, jsr.getEmail());

        assertNull(jsr.getPhone());
        jsr.setPhone(TEST);
        assertEquals(TEST, jsr.getPhone());
    }

    public void testValidationBadEmailAddress() throws Exception
    {
        jsr.setTo("dave");
        assertEquals(Action.INPUT, jsr.execute());
        assertEquals("You must specify a valid 'to' address(es).", jsr.getErrors().get("to"));
    }

    public void testValidation() throws Exception
    {
        jsr.setName("");
        jsr.setEmail("");

        final String result = jsr.execute();
        assertEquals(Action.INPUT, result);
        assertEquals("You must specify at least one 'to' address.", jsr.getErrors().get("to"));
        assertEquals("You must specify a description.", jsr.getErrors().get("description"));
        assertEquals("You must specify a name.", jsr.getErrors().get("name"));
        assertEquals("You must specify a contact email address.", jsr.getErrors().get("email"));
    }

    public void testMultipleToAddresses() throws Exception
    {
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("views/jirasupportrequest-success.jsp");
        response.setExpectedRedirect("views/jirasupportrequest-success.jsp");

        jsr.setTo("abc@def.com, xyz@def.com");
        jsr.setCc("abcsdf@def.com");
        jsr.setDescription("test");
        jsr.setSubject("subject");
        assertEquals("done", jsr.execute());
    }

    public void testExecute() throws Exception
    {
        mockApplicationProperties.setString(APKeys.JIRA_BASEURL, "base");
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_WEBWORK_ENCODING, "encoding");

        vm.expectAndReturn(
                "getEncodedBody",
                new Constraint[] { new IsEqual("templates/email/"), new IsEqual("text/jirasupportrequest.vm"), new IsEqual("base"), new IsEqual("UTF-8"), new IsAnything() },
                "test");

        // expect a call to ActionDispatcher to handle export if checkbox is ticked
        final MockActionDispatcher ad = setupActionDispatcher();

        jsr.setTo("dave@atlassian.com");
        jsr.setCc("");
        jsr.setAttachzipexport(true);
        jsr.setDescription("Test Description");
        jsr.setSubject("subject");


        final String result = jsr.execute();
        assertEquals("done", result);

        vm.verify();
        // commenting out because JIRA support requests configuration has been ripped out unceremoniously due to you know what
        //verifyActionDispatcher(ad);
    }

    private void verifyActionDispatcher(final MockActionDispatcher ad)
    {
        // Verify one call to the action was made
        assertEquals(1, ad.getCalls().size());

        // Get the call details
        final Object[] call = (Object[]) ad.getCalls().iterator().next();
        final String callName = (String) call[0];
        final Map calledWithParams = (Map) call[2];

        // Verify it was the execute method that was invoked
        assertEquals("execute", callName);

        // Verify the parameters the method was called with
        assertTrue(calledWithParams.containsKey("filename"));
        assertTrue(calledWithParams.containsKey("useZip"));
        assertEquals(Boolean.TRUE, calledWithParams.get("useZip"));
        assertTrue(calledWithParams.containsKey("anonymiseData"));
        assertEquals(Boolean.TRUE, calledWithParams.get("anonymiseData"));
    }

    private MockActionDispatcher setupActionDispatcher()
    {
        final MockActionDispatcher mockActionDispatcher = new MockActionDispatcher(false);
        mockActionDispatcher.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mockActionDispatcher);
        return mockActionDispatcher;
    }

    private class MyExtendedSystemInfoUtils extends ExtendedSystemInfoUtilsImpl
    {
        private static final String DEFAULT_LANGUAGE = "en_DefaultLanguage";

        public MyExtendedSystemInfoUtils(final SystemInfoUtils systemInfoUtils, final ApplicationProperties applicationProperties, final I18nBean i18nBean)
        {
            super(systemInfoUtils, null, null, applicationProperties, null, null, i18nBean, null, null, localeManager, jiraLicenseService, buildUtilsInfo, upgradeManager, null);
        }

        @Override
        public Collection<GenericValue> getListeners()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<Plugin> getPlugins()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<JiraServiceContainer> getServices()
        {
            return Collections.emptyList();
        }

        @Override
        public String getDefaultLanguage()
        {
            return DEFAULT_LANGUAGE;
        }
    }

    private static class MyJiraSupportRequest extends JiraSupportRequest
    {
        private User user;

        public MyJiraSupportRequest(final ExtendedSystemInfoUtils extendedSystemInfoUtils, final JiraLicenseService jiraLicenseService, final LocaleManager localeManager, FileFactory fileFactory, final PluginInfoProvider pluginInfoProvider)
        {
            super(extendedSystemInfoUtils, jiraLicenseService, new JiraLogLocator(new MockJiraHome()), localeManager, fileFactory, pluginInfoProvider);
        }

        protected String getUserName()
        {
            return "Owen Fellows";
        }

        @Override
        public User getLoggedInUser()
        {
            try
            {
                if (user == null)
                {
                    user = new MockUser("owen", "Owen Fellows", "owen@atlassian.com");
                }
                return user;
            }
            catch (final Exception e)
            {
                // nothing to see here, move on
            }
            return null;
        }

        @Override
        boolean sendEmail(final Email email)
        {
            return true;
        }
    }
}
