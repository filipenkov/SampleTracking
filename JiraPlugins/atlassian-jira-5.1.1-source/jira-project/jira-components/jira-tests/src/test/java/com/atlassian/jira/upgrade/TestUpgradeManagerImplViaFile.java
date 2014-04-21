package com.atlassian.jira.upgrade;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.EasyMockMatcherUtils;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.easymock.MockType;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyMap;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;

public class TestUpgradeManagerImplViaFile extends LegacyJiraMockTestCase
{
    private static final String VERSION = "4.0";


    private MockController mockController;
    private JiraLicenseService jiraLicenseService;
    private BuildUtilsInfo buildUtilsInfo;
    private I18nHelper.BeanFactory i18HelperFactory;
    private ApplicationProperties applicationProperties;
    private UpgradeManager um;
    private BuildVersionRegistry buildVersionRegistry;

    @Mock(MockType.NICE)
    private OfBizDelegator mockDelegator;

    @Mock(MockType.NICE)
    private IndexLifecycleManager mockIndexManager;

    @Mock(MockType.NICE)
    private EventPublisher mockEventPublisher;

    @Mock
    private FeatureManager mockFeatureManager;

    public TestUpgradeManagerImplViaFile(String s)
    {
        super(s);
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        EasyMockAnnotations.initMocks(this);
        mockController = new MockController();
        jiraLicenseService = mockController.getMock(JiraLicenseService.class);
        final LicenseDetails licenseDetails = mockController.getMock(LicenseDetails.class);
        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andStubReturn(false); // we bypass license checks if it is not set.

        buildUtilsInfo = mockController.getMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getCurrentBuildNumber()).andStubReturn("400");

        i18HelperFactory = mockController.getMock(I18nHelper.BeanFactory.class);

        applicationProperties = mockController.getMock(ApplicationProperties.class);
        applicationProperties.setString(eq(APKeys.JIRA_PATCHED_VERSION), EasyMock.<String>anyObject());
        expectLastCall().asStub();

        buildVersionRegistry = mockController.getMock(BuildVersionRegistry.class);

        um = new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory, applicationProperties,
                buildVersionRegistry, mockEventPublisher, mockDelegator, mockIndexManager, null, "teststandardupgrades.xml",
                mockFeatureManager);
    }

    public void testDoSetupUpgradeEnterprise() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        expect(mockDelegator.createValue(eq("UpgradeHistory"), EasyMockMatcherUtils.<String, Object>mapContaining(
                "upgradeclass", "com.atlassian.jira.upgrade.tasks.UpgradeTask1_2"))).andReturn(null);
        expect(mockDelegator.createValue(eq("UpgradeHistory"), EasyMockMatcherUtils.<String, Object>mapContaining(
                "upgradeclass", "com.atlassian.jira.upgrade.tasks.UpgradeTask_Build572"))).andReturn(null);
        EasyMockAnnotations.replayMocks(this);

        um.doSetupUpgrade();

        verify(mockDelegator);
    }

    public void testDoUpgradeEnterprise() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        expect(mockDelegator.createValue(eq("UpgradeHistory"), anyMap(String.class, Object.class))).andReturn(null).times(3);
        EasyMockAnnotations.replayMocks(this);

        um.doUpgradeIfNeededAndAllowed(null);

        verify(mockDelegator);
    }

    public void testDoUpgradeEnterpriseWithBuildSet() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("10");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        expect(mockDelegator.createValue(eq("UpgradeHistory"), anyMap(String.class, Object.class))).andReturn(null).times(1);
        EasyMockAnnotations.replayMocks(this);

        um.doUpgradeIfNeededAndAllowed(null);

        verify(mockDelegator);
    }
}
