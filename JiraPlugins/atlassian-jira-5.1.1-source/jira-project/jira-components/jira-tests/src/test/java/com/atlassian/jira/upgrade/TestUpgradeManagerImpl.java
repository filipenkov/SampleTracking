package com.atlassian.jira.upgrade;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bean.export.AutoExport;
import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.upgrade.MockUpgradeTask;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUpgradeManagerImpl
{
    private static final String VERSION = "4.0";

    private static final Collection<MockUpgradeTask> ALL_UPGRADES = ImmutableList.of(
            new MockUpgradeTask("1.0", "short desc", false),
            new MockUpgradeTask("1.0.1", "short desc2", true),
            new MockUpgradeTask("1.2", "short desc3", false),
            new MockUpgradeTask("1.2.3", "short desc4", false),
            new MockUpgradeTask("1.3", "short desc5", false),
            new MockUpgradeTask("27", "short desc6", false),
            new MockUpgradeTask("0.9", "short desc7", false),
            new MockUpgradeTask("1.1", "short desc8", false)
    );

    // move all those guys to mockito
    private MockController mockController;
    private JiraLicenseService jiraLicenseService;
    private BuildUtilsInfo buildUtilsInfo;
    private I18nHelper.BeanFactory i18HelperFactory;
    private ApplicationProperties applicationProperties;
    private LicenseDetails licenseDetails;
    private BuildVersionRegistry buildVersionRegistry;
    private IndexLifecycleManager indexManager;

    @Mock
    private OfBizDelegator mockDelegator;

    @Mock
    private EventPublisher mockEventPublisher;

    @Mock
    private OutlookDateManager mockDateManager;

    @Mock
    private FeatureManager mockFeatureManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        mockController = new MockController();
        jiraLicenseService = mockController.getMock(JiraLicenseService.class);
        licenseDetails = mockController.getMock(LicenseDetails.class);

        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andStubReturn(false); // we bypass license checks if it is not set.

        buildUtilsInfo = mockController.getMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getCurrentBuildNumber()).andStubReturn("99999");

        i18HelperFactory = mockController.getMock(I18nHelper.BeanFactory.class);

        applicationProperties = mockController.getNiceMock(ApplicationProperties.class);
        applicationProperties.setString(EasyMock.eq(APKeys.JIRA_PATCHED_VERSION), EasyMock.<String>anyObject());
        expectLastCall().asStub();

        buildVersionRegistry = mockController.getMock(BuildVersionRegistry.class);
        indexManager = mockController.getStrictMock(IndexLifecycleManager.class);
    }

    private UpgradeManagerImpl createForTest()
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, mockEventPublisher,
                indexManager, mockDateManager, mockFeatureManager, ALL_UPGRADES, ALL_UPGRADES);
    }

    private UpgradeManagerImpl createForTest(Iterable<UpgradeTask> upgradeTasks, Iterable<UpgradeTask> setupUpgradeTasks)
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, mockEventPublisher,
                indexManager, mockDateManager, mockFeatureManager, upgradeTasks, setupUpgradeTasks);
    }

    private UpgradeManagerImpl createForTest(final AutoExport mockAutoExport)
    {
        return new UpgradeManagerImpl(jiraLicenseService, buildUtilsInfo, i18HelperFactory,
                applicationProperties, buildVersionRegistry, mockDelegator, mockEventPublisher,
                indexManager, mockDateManager, mockFeatureManager, ALL_UPGRADES, ALL_UPGRADES)
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
                applicationProperties, buildVersionRegistry, mockDelegator, mockEventPublisher, indexManager,
                mockDateManager, mockFeatureManager, ALL_UPGRADES, ALL_UPGRADES)
        {
            @Override
            UpgradeHistoryItem getUpgradeHistoryItemFromTasks() throws GenericEntityException
            {
                return upgradeHistoryItem;
            }
        };
    }

    @Test
    public void testDoUpdate() throws IllegalXMLCharactersException, IndexException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final UpgradeManager tested = createForTest();
        final Collection<String> actualResult = tested.doUpgradeIfNeededAndAllowed(null);

        // we should have no errors
        assertEquals(0, actualResult.size());
    }

    @Test
    public void testDoUpgradeIfNeededAndAllowed_BadLicense_TooOldForBuild() throws IllegalXMLCharactersException, IndexException
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

        final UpgradeManager tested = createForTest();
        final Collection<String> actualResult = tested.doUpgradeIfNeededAndAllowed(null);

        // we should have one error
        assertEquals(1, actualResult.size());
    }

    @Test
    public void testDoUpgradeIfNeededAndAllowed_BadLicense_ValidationFailed() throws IllegalXMLCharactersException, IndexException
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

        final UpgradeManager tested = createForTest();
        final Collection<String> actualResult = tested.doUpgradeIfNeededAndAllowed(null);
        assertEquals(1, actualResult.size());
    }

    @Test
    public void testUpgradesInOrder()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0.1");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
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

    @Test
    public void testUpgradesSubset0_9() throws IndexException
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0.9");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
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

    @Test
    public void testUpgradesSubset1_1()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("1.1");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();

        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
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

    @Test
    public void testUpgradesSubset1_3()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("1.3");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        final UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset27()
    {
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("27");
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        assertEquals(upgrades.size(), 0);
    }

    @Test
    public void testDoUpgradeIfNeededNoExport() throws IllegalXMLCharactersException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        tested.doUpgradeIfNeededAndAllowed(null);
    }

    @Test
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

        final UpgradeManagerImpl tested = createForTest(mockAutoExport);
        final Collection<String> errors = tested.doUpgradeIfNeededAndAllowed(tempDir);
        assertEquals(1, errors.size());

        final String message = errors.iterator().next();
        assertTrue(message.startsWith("Error occurred during export before upgrade:"));
        verify(mockAutoExport);
    }

    @Test
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

        final UpgradeManagerImpl tested = createForTest(mockAutoExport);
        Collection<String> errors = null;
        try
        {
            errors = tested.doUpgradeIfNeededAndAllowed(expectedExportPath);
            fail("IllegalXMLCharactersException should have been thrown.");
        }
        catch (final IllegalXMLCharactersException e)
        {
            assertNull(errors);
            assertEquals("Bad characters.", e.getMessage());
        }

        verify(mockAutoExport);
    }

    @Test
    public void testDoUpdateNoAutoExportInOnDemandJRADEV11718() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());
        when(mockFeatureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);

        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("100");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        mockController.replay();

        final AutoExport mockAutoExport = createMock(AutoExport.class);
        replay(mockAutoExport);

        final UpgradeManager tested = createForTest(mockAutoExport);
        final Collection<String> actualResult = tested.doUpgradeIfNeededAndAllowed("somedir");

        // we should have no errors
        assertEquals(0, actualResult.size());
    }

    @Test
    public void testDoUpgradeIfNeededWithExport() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

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

        final UpgradeManagerImpl tested = createForTest(mockAutoExport);
        final Collection<String> errors = tested.doUpgradeIfNeededAndAllowed(expectedExportPath);
        assertTrue(errors.isEmpty());
        assertEquals(expectedExportPath, tested.getExportFilePath());

        verify(mockAutoExport);
    }

    @Test
    public void testGetUpgradeHistoryItemFromTasksNone() throws Exception
    {
        mockController.replay();
        when(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollectionOf(String.class),
                anyListOf(String.class))).thenReturn(Collections.<GenericValue>emptyList());
        final UpgradeManagerImpl tested = createForTest();
        final UpgradeHistoryItem result = tested.getUpgradeHistoryItemFromTasks();
        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testGetUpgradeHistoryItemFromTasksHappyPath() throws Exception
    {
        when(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollectionOf(String.class),
                anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeHistoryLastClassForTargetBuild", ImmutableMap.of(
                        "upgradeclass", "UpgradeTask_Build106"
                )))
        );

        final BuildVersionImpl buildVersion = new BuildVersionImpl("106", "XYZ");
        expect(buildVersionRegistry.getVersionForBuildNumber("106")).andReturn(buildVersion);

        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();

        final UpgradeHistoryItem result = tested.getUpgradeHistoryItemFromTasks();
        final UpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "106", "XYZ", "106", null, true);
        assertEquals(result, expected);

        mockController.verify();
    }

    @Test
    public void testGetUpgradeHistoryItemFromTasksBadClass() throws Exception
    {
        when(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollectionOf(String.class),
                anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeHistoryLastClassForTargetBuild", ImmutableMap.of(
                        "upgradeclass", "BADCLASS"
                )))
        );

        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        final UpgradeHistoryItem result = tested.getUpgradeHistoryItemFromTasks();
        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testGetUpgradeHistoryNoPrevious() throws Exception
    {
        when(mockDelegator.findAll(Mockito.anyString(), anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeVersionHistory", MapBuilder.build(
                        "timeperformed", null,
                        "targetbuild", "400",
                        "targetversion", VERSION
                )))
        );

        mockController.replay();

        final UpgradeManagerImpl tested = createForTest((UpgradeHistoryItem) null);

        final List<UpgradeHistoryItem> result = tested.getUpgradeHistory();
        final UpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "400", VERSION, null, null);
        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));

        mockController.verify();
    }

    @Test
    public void testGetUpgradeHistoryPrevious() throws Exception
    {
        when(mockDelegator.findAll(anyString(), anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeVersionHistory", MapBuilder.build(
                        "timeperformed", null,
                        "targetbuild", "400",
                        "targetversion", VERSION
                )))
        );

        mockController.replay();

        final UpgradeHistoryItem expected1 = new UpgradeHistoryItemImpl(null, "400", VERSION, "300", "3.0");
        final UpgradeHistoryItem expected2 = new UpgradeHistoryItemImpl(null, "300", "3.0", null, null);

        final UpgradeManagerImpl tested = createForTest(expected2);

        final List<UpgradeHistoryItem> result = tested.getUpgradeHistory();
        assertEquals(2, result.size());
        assertEquals(expected1, result.get(0));
        assertEquals(expected2, result.get(1));

        mockController.verify();
    }

    @Test
    public void testUpgradesWithReindex() throws IllegalXMLCharactersException, IndexException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("0.9");
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_INDEXING)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        expect(indexManager.size()).andReturn(8).once();
        expect(indexManager.reIndexAll(EasyMock.<Context>anyObject())).andReturn(1l).once();
        mockController.replay();

        final UpgradeManagerImpl man = createForTest();
        Collection<String> errors = man.doUpgradeIfNeededAndAllowed(null);

        // Need to assert the errors collection here as the upgrade task swallows Throwable
        assertEquals(0, errors.size());
        mockController.verify();
    }

    @Test
    public void testUpgradesWithoutReindex() throws IllegalXMLCharactersException, IndexException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        expect(applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION)).andStubReturn("1.2");
        expect(applicationProperties.getString(APKeys.JIRA_SETUP)).andStubReturn("true");
        expect(applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).andStubReturn(true);
        expect(buildUtilsInfo.getVersion()).andStubReturn(VERSION);
        expect(buildUtilsInfo.getApplicationBuildNumber()).andStubReturn(400);
        expect(buildUtilsInfo.getDatabaseBuildNumber()).andStubReturn(100);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_INDEXING)).andStubReturn(true);
        mockController.replay();

        final UpgradeManagerImpl tested = createForTest();
        tested.doUpgradeIfNeededAndAllowed(null);
        mockController.verify();
    }

    @Test
    public void testSequencerIsRefreshedAfterStandardUpgrade() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

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
        Mockito.verify(mockDelegator).refreshSequencer();
    }

    @Test
    public void testSequencerIsRefreshedAfterSetupUpgrade() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

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
        Mockito.verify(mockDelegator).refreshSequencer();
    }

    @Test
    public void testSequencerIsRefreshedAfterUpgradeWithErrors() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

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
        Mockito.verify(mockDelegator).refreshSequencer();
    }

    @Test
    public void testSequencerIsRefreshedAfterSetupUpgradeWithErrors() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

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
        Mockito.verify(mockDelegator).refreshSequencer();
    }

    private UpgradeTask taskWithError(boolean isSetup) throws Exception
    {
        final UpgradeTask answer = mock(UpgradeTask.class);
        when(answer.getBuildNumber()).thenReturn("150");
        when(answer.getShortDescription()).thenReturn("Testing task");
        doThrow(new Exception("Surprise!!!")).when(answer).doUpgrade(isSetup);
        return answer;
    }
}