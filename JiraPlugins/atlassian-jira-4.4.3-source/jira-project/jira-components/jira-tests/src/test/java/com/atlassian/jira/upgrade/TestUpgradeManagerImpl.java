package com.atlassian.jira.upgrade;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bean.export.AutoExport;
import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.easymock.MockType;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.upgrade.MockUpgradeTask;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.util.OutlookDate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyCollection;
import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyList;
import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

public class TestUpgradeManagerImpl extends LegacyJiraMockTestCase
{
    private static final String VERSION = "4.0";

    private static final Collection<MockUpgradeTask> ALL_UPGRADES = ImmutableList.of(
            new MockUpgradeTask("1.0", "short desc"),
            new MockUpgradeTask("1.0.1", "short desc2"),
            new MockUpgradeTask("1.2", "short desc3"),
            new MockUpgradeTask("1.2.3", "short desc4"),
            new MockUpgradeTask("1.3", "short desc5"),
            new MockUpgradeTask("27", "short desc6"),
            new MockUpgradeTask("0.9", "short desc7"),
            new MockUpgradeTask("1.1", "short desc8")
    );

    private MockController mockController;
    private JiraLicenseService jiraLicenseService;
    private BuildUtilsInfo buildUtilsInfo;
    private I18nHelper.BeanFactory i18HelperFactory;
    private ApplicationProperties applicationProperties;
    private LicenseDetails licenseDetails;
    private BuildVersionRegistry buildVersionRegistry;

    @Mock(MockType.NICE)
    private OfBizDelegator mockDelegator;

    public TestUpgradeManagerImpl(final String s)
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
        licenseDetails = mockController.getMock(LicenseDetails.class);

        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andStubReturn(false); // we bypass license checks if it is not set.

        buildUtilsInfo = mockController.getMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getCurrentBuildNumber()).andStubReturn("99999");

        i18HelperFactory = mockController.getMock(I18nHelper.BeanFactory.class);

        applicationProperties = mockController.getMock(ApplicationProperties.class);
        applicationProperties.setString(EasyMock.eq(APKeys.JIRA_PATCHED_VERSION), EasyMock.<String>anyObject());
        expectLastCall().asStub();

        buildVersionRegistry = mockController.getMock(BuildVersionRegistry.class);
        EasyMockAnnotations.replayMocks(this);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    private UpgradeManagerImpl createForTest()
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, ALL_UPGRADES, ALL_UPGRADES, null);
    }

    private UpgradeManagerImpl createForTest(Iterable<UpgradeTask> upgradeTasks, Iterable<UpgradeTask> setupUpgrade)
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, upgradeTasks, setupUpgrade, null);
    }

    private UpgradeManagerImpl createForTest(final AutoExport mockAutoExport)
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, ALL_UPGRADES, ALL_UPGRADES, null)
        {
            @Override
            protected AutoExport getAutoExport(String defaultBackupPath)
            {
                return mockAutoExport;
            }
        };
    }

    private UpgradeManagerImpl createForTest(final UpgradeHistoryItem upgradeHistoryItem)
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, ALL_UPGRADES, ALL_UPGRADES, null)
        {
            @Override
            UpgradeHistoryItem getUpgradeHistoryItemFromTasks() throws GenericEntityException
            {
                return upgradeHistoryItem;
            }
        };
    }

    public void testDoUpdate() throws IllegalXMLCharactersException
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        replay(mockDelegator);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final UpgradeManager man = createForTest();
        final Collection<String> actualResult = man.doUpgradeIfNeededAndAllowed(null);

        // we should have no errors
        assertEquals(0, actualResult.size());
    }

    public void testDoUpgradeIfNeededAndAllowed_BadLicense_TooOldForBuild() throws IllegalXMLCharactersException
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");

        final Date theDate = new Date();


        expect(buildUtilsInfo.getCurrentBuildDate()).andStubReturn(theDate);
        expect(buildUtilsInfo.getVersion()).andStubReturn("v99");

        expect(licenseDetails.isLicenseSet()).andReturn(true);
        expect(licenseDetails.isMaintenanceValidForBuildDate(theDate)).andReturn(false);
        expect(licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone()).andReturn(false);
        expect(licenseDetails.getMaintenanceEndString(EasyMock.<OutlookDate>anyObject())).andReturn("today");
        mockController.replay();

        final UpgradeManager man = createForTest();
        final Collection<String> actualResult = man.doUpgradeIfNeededAndAllowed(null);

        // we should have one error
        assertEquals(1, actualResult.size());
    }

    public void testDoUpgradeIfNeededAndAllowed_BadLicense_ValidationFailed() throws IllegalXMLCharactersException
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");

        final Date theDate = new Date();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("shite happens");
        JiraLicenseService.ValidationResult validationResult = mockController.getMock(JiraLicenseService.ValidationResult.class);

        expect(i18HelperFactory.getInstance(EasyMock.<Locale>anyObject())).andReturn(null);

        expect(buildUtilsInfo.getCurrentBuildDate()).andStubReturn(theDate);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);

        expect(licenseDetails.isLicenseSet()).andReturn(true);
        expect(licenseDetails.isMaintenanceValidForBuildDate(theDate)).andReturn(true);

        expect(licenseDetails.getLicenseString()).andReturn("licString");
        expect(jiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.eq("licString"))).andReturn(validationResult);

        expect(validationResult.getErrorCollection()).andStubReturn(errorCollection);

        mockController.replay();

        final UpgradeManager man = createForTest();
        final Collection<String> actualResult = man.doUpgradeIfNeededAndAllowed(null);
        assertEquals(1, actualResult.size());
    }


    public void testUpgradesInOrder()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0.1");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc7");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc2");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc8");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    public void testUpgradesSubset0_9()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0.9");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc2");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc8");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    public void testUpgradesSubset1_1()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("1.1");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();

        final SortedMap<String, UpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    public void testUpgradesSubset1_3()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("1.3");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        final UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    public void testUpgradesSubset27()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("27");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = man.getRelevantUpgradesFromList(man.getAllUpgrades());
        assertEquals(upgrades.size(), 0);
    }

    public void testDoUpgradeIfNeededNoExport() throws IllegalXMLCharactersException
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        man.doUpgradeIfNeededAndAllowed(null);
    }

    public void testDoUpgradeIfNeededWithExportErrors() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final String tempDir = "somedir";

        final AutoExport mockAutoExport = createMock(AutoExport.class);
        expect(mockAutoExport.exportData()).andThrow(new Exception("There was an error."));
        replay(mockAutoExport);

        final UpgradeManagerImpl man = createForTest(mockAutoExport);
        final Collection<String> errors = man.doUpgradeIfNeededAndAllowed(tempDir);
        assertEquals(1, errors.size());

        final String message = errors.iterator().next();
        assertTrue(message.startsWith("Error occurred during export before upgrade:"));
        verify(mockAutoExport);
    }

    public void testDoUpgradeIfNeededWithExportIllegalCharacters() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final String expectedExportPath = "somedir";

        final AutoExport mockAutoExport = createMock(AutoExport.class);
        expect(mockAutoExport.exportData()).andThrow(new IllegalXMLCharactersException("Bad characters."));
        replay(mockAutoExport);

        final UpgradeManagerImpl man = createForTest(mockAutoExport);
        Collection<String> errors = null;
        try
        {
            errors = man.doUpgradeIfNeededAndAllowed(expectedExportPath);
            fail("IllegalXMLCharactersException should have been thrown.");
        }
        catch (final IllegalXMLCharactersException e)
        {
            assertNull(errors);
            assertEquals("Bad characters.", e.getMessage());
        }

        verify(mockAutoExport);
    }

    public void testDoUpgradeIfNeededWithExport() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        replay(mockDelegator);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final String expectedExportPath = "somedir";

        final AutoExport mockAutoExport = createMock(AutoExport.class);
        expect(mockAutoExport.exportData()).andReturn(expectedExportPath);
        replay(mockAutoExport);

        final UpgradeManagerImpl man = createForTest(mockAutoExport);
        final Collection<String> errors = man.doUpgradeIfNeededAndAllowed(expectedExportPath);
        assertTrue(errors.isEmpty());
        assertEquals(expectedExportPath, man.getExportFilePath());

        verify(mockAutoExport);
    }

    public void testGetUpgradeHistoryItemFromTasksNone() throws Exception
    {
        mockController.replay();
        reset(mockDelegator);
        expect(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollection(String.class),
                anyList(String.class))).andReturn(Collections.<GenericValue>emptyList());
        replay(mockDelegator);
        final UpgradeManagerImpl man = createForTest();

        final UpgradeHistoryItem result = man.getUpgradeHistoryItemFromTasks();
        assertNull(result);

        mockController.verify();
    }

    public void testGetUpgradeHistoryItemFromTasksHappyPath() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollection(String.class),
                anyList(String.class))).andReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeHistoryLastClassForTargetBuild", ImmutableMap.of(
                        "upgradeclass", "UpgradeTask_Build106"
                )))
        );
        replay(mockDelegator);

        final BuildVersionImpl buildVersion = new BuildVersionImpl("106", "XYZ");
        expect(buildVersionRegistry.getVersionForBuildNumber("106")).andReturn(buildVersion);

        mockController.replay();

        final UpgradeManagerImpl man = createForTest();

        final UpgradeHistoryItem result = man.getUpgradeHistoryItemFromTasks();
        final UpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "106", "XYZ", "106", null, true);
        assertEquals(result, expected);

        mockController.verify();
    }

    public void testGetUpgradeHistoryItemFromTasksBadClass() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollection(String.class),
                anyList(String.class))).andReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeHistoryLastClassForTargetBuild", ImmutableMap.of(
                        "upgradeclass", "BADCLASS"
                )))
        );
        replay(mockDelegator);

        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        final UpgradeHistoryItem result = man.getUpgradeHistoryItemFromTasks();
        assertNull(result);

        mockController.verify();
    }

    public void testGetUpgradeHistoryNoPrevious() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll(anyString(), anyList(String.class))).andReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeVersionHistory", MapBuilder.build(
                        "timeperformed", null,
                        "targetbuild", "400",
                        "targetversion", VERSION
                )))
        );
        replay(mockDelegator);

        mockController.replay();

        final UpgradeManagerImpl man = createForTest((UpgradeHistoryItem) null);

        final List<UpgradeHistoryItem> result = man.getUpgradeHistory();
        final UpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "400", VERSION, null, null);
        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));

        mockController.verify();
    }

    public void testGetUpgradeHistoryPrevious() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll(anyString(), anyList(String.class))).andReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeVersionHistory", MapBuilder.build(
                        "timeperformed", null,
                        "targetbuild", "400",
                        "targetversion", VERSION
                )))
        );
        replay(mockDelegator);

        mockController.replay();

        final UpgradeHistoryItem expected1 = new UpgradeHistoryItemImpl(null, "400", VERSION, "300", "3.0");
        final UpgradeHistoryItem expected2 = new UpgradeHistoryItemImpl(null, "300", "3.0", null, null);

        final UpgradeManagerImpl man = createForTest(expected2);

        final List<UpgradeHistoryItem> result = man.getUpgradeHistory();
        assertEquals(2, result.size());
        assertEquals(expected1, result.get(0));
        assertEquals(expected2, result.get(1));

        mockController.verify();
    }


    public void testSequencerIsRefreshedAfterStandardUpgrade() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        mockDelegator.refreshSequencer();
        expectLastCall();
        replay(mockDelegator);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        UpgradeManager tested = createForTest();
        Collection<String> errors = tested.doUpgradeIfNeededAndAllowed(null);
        assertTrue("Unexpected errors: " + errors, errors.isEmpty());
        verify(mockDelegator);
    }

    public void testSequencerIsRefreshedAfterSetupUpgrade() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        mockDelegator.refreshSequencer();
        expectLastCall();
        replay(mockDelegator);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        UpgradeManager tested = createForTest();
        Collection<String> errors = tested.doSetupUpgrade();
        assertTrue("Unexpected errors: " + errors, errors.isEmpty());
        verify(mockDelegator);
    }

    public void testSequencerIsRefreshedAfterUpgradeWithErrors() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        mockDelegator.refreshSequencer();
        expectLastCall();
        replay(mockDelegator);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final UpgradeManager tested = createForTest(ImmutableList.of(taskWithError(false)), Collections.<UpgradeTask>emptyList());
        Collection<String> errors = tested.doUpgradeIfNeededAndAllowed(null);
        assertEquals(1, errors.size());
        verify(mockDelegator);
    }

    public void testSequencerIsRefreshedAfterSetupUpgradeWithErrors() throws Exception
    {
        reset(mockDelegator);
        expect(mockDelegator.findAll("UpgradeHistory")).andReturn(Collections.<GenericValue>emptyList());
        mockDelegator.refreshSequencer();
        expectLastCall();
        replay(mockDelegator);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final UpgradeManager tested = createForTest(Collections.<UpgradeTask>emptyList(), ImmutableList.of(taskWithError(true)));
        Collection<String> errors = tested.doSetupUpgrade();
        assertEquals(1, errors.size());
        verify(mockDelegator);
    }

    private UpgradeTask taskWithError(boolean isSetup) throws Exception
    {
        final UpgradeTask answer = createMock(UpgradeTask.class);
        expect(answer.getBuildNumber()).andReturn("150").times((isSetup ? 1 : 3));
        expect(answer.getShortDescription()).andReturn("Testing task");
        answer.doUpgrade(isSetup);
        expectLastCall().andThrow(new Exception("Surprise!!!"));
        replay(answer);
        return answer;
    }
}