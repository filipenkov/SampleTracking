package com.atlassian.jira.bc.dataimport;

import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.jira.license.LicenseStringFactory;
import com.atlassian.jira.local.MockedComponentManagerTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.upgrade.ConsistencyChecker;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.DirectorySynchroniserBarrier;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeManager;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultDataImportService extends MockedComponentManagerTestCase
{
    private UpgradeManager mockUpgradeManager;
    private ConsistencyChecker mockConsistencyChecker;
    private MailQueue mockMailQueue;
    private Scheduler mockScheduler;
    private IndexLifecycleManager mockIndexManager;
    private IndexPathManager mockIndexPathManager;
    private AttachmentPathManager mockAttachmentPathManager;
    private ExternalLinkUtil mockExternalLinkUtil;
    private JiraLicenseUpdaterService mockJiraLicenseService;
    private LicenseStringFactory mockLicenseStringFactory;
    private BuildUtilsInfo mockBuildUtilsInfo;
    private List<File> directories = new ArrayList<File>();
    private I18nHelper.BeanFactory mockBeanFactory;
    private I18nHelper mockI18nHelper;
    private PermissionManager mockPermissionManager;
    private JiraHome mockJiraHome;
    private OfBizDelegator mockOfBizDelegator;
    private TaskManager mockTaskManager;
    private DataImportProductionDependencies mockDependencies;
    private PluginEventManager mockPluginEventManager;
    private ComponentFactory mockFactory;
    private DirectorySynchroniserBarrier mockBarrier;

    private User currentUser = new MockUser("admin");

    private JiraLicenseService.ValidationResult mockValidationResult;
    private ModelReader mockModelReader;
    private MockApplicationProperties mockProperties;
    private Backup backup;
    private EventPublisher eventPublisher;
    private PluginUpgradeManager pluginUpgradeManager = new PluginUpgradeManager()
    {
        @Override
        public List<Message> upgrade()
        {
            return Collections.emptyList();
        }
    };

    @Before
    public void setUpTest() throws Exception
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        setupMockManagers();
        setupDirectory("jira-attachments");
        setupDirectory("jira-indexes");
    }

    @After
    public void tearDown()
    {
        tearDownDirectory("jira-attachments");
        tearDownDirectory("jira-indexes");
    }

    private void setupMockManagers()
    {
        mockUpgradeManager = createStrictMock(UpgradeManager.class);
        mockConsistencyChecker = createStrictMock(ConsistencyChecker.class);
        mockMailQueue = createStrictMock(MailQueue.class);
        mockScheduler = createStrictMock(Scheduler.class);
        mockIndexManager = createStrictMock(IndexLifecycleManager.class);
        mockIndexPathManager = createStrictMock(IndexPathManager.class);
        mockAttachmentPathManager = createStrictMock(AttachmentPathManager.class);
        mockExternalLinkUtil = createMock(ExternalLinkUtil.class);
        mockJiraLicenseService = createStrictMock(JiraLicenseUpdaterService.class);
        mockLicenseStringFactory = createStrictMock(LicenseStringFactory.class);
        mockBuildUtilsInfo = createStrictMock(BuildUtilsInfo.class);
        mockBeanFactory = createStrictMock(I18nHelper.BeanFactory.class);
        mockI18nHelper = createStrictMock(I18nHelper.class);
        mockPermissionManager = createStrictMock(PermissionManager.class);
        mockJiraHome = createStrictMock(JiraHome.class);
        mockOfBizDelegator = createMock(OfBizDelegator.class);
        mockTaskManager = createStrictMock(TaskManager.class);
        mockModelReader = createStrictMock(ModelReader.class);
        mockProperties = new MockApplicationProperties();
        mockPluginEventManager = createStrictMock(PluginEventManager.class);
        mockFactory = createNiceMock(ComponentFactory.class);
        mockValidationResult = createMock(JiraLicenseService.ValidationResult.class);
        mockDependencies = new MockDataImportDependencies(mockConsistencyChecker, mockPluginEventManager, pluginUpgradeManager);
        mockBarrier = createMock(DirectorySynchroniserBarrier.class);
        backup = createStrictMock(Backup.class);
        eventPublisher = createStrictMock(EventPublisher.class);

        //These are deps that will come from pico via the MockDataImportDependencies class above.
        addMock(Scheduler.class, mockScheduler);
        addMock(OfBizDelegator.class, mockOfBizDelegator);
        addMock(ConsistencyChecker.class, mockConsistencyChecker);
        addMock(UpgradeManager.class, mockUpgradeManager);
        addMock(IndexLifecycleManager.class, mockIndexManager);

        expect(mockFactory.createObject(DirectorySynchroniserBarrier.class)).andStubReturn(mockBarrier);
    }
    
    @Test
    public void testExecuteGoodVersion() throws Exception
    {
        expect(mockBarrier.await(20, TimeUnit.SECONDS)).andReturn(true);

        expect(mockBeanFactory.getInstance(currentUser)).andReturn(new MockI18nHelper()).anyTimes();

        //called during validation!
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        expect(mockOfBizDelegator.makeValue(EasyMock.<String>anyObject())).andReturn(mockGv).anyTimes();
        expect(mockAttachmentPathManager.getDefaultAttachmentPath()).andReturn(directories.get(0).getAbsolutePath()).anyTimes();
        expect(mockIndexPathManager.getDefaultIndexRootPath()).andReturn(directories.get(1).getAbsolutePath()).anyTimes();
        expect(mockLicenseStringFactory.create(EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andStubReturn("");

        //after the first parse check the build number.
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andStubReturn("99999999");
        expect(mockBuildUtilsInfo.getMinimumUpgradableBuildNumber()).andStubReturn("0");

        //after the first parse we also verify the license is good.
        expect(mockJiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andStubReturn(2);
        expect(mockValidationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        expect(mockScheduler.isShutdown()).andReturn(false);
        mockScheduler.shutdown();
        mockMailQueue.sendBuffer();
        expect(mockTaskManager.shutdownAndWait(5)).andReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        expect(mockOfBizDelegator.getModelReader()).andReturn(mockModelReader);
        expect(mockModelReader.getEntityNames()).andReturn(CollectionBuilder.<String>list("Issue", "User"));
        expect(mockModelReader.getModelEntity("Issue")).andReturn(new ModelEntity());
        expect(mockOfBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).andReturn(10);
        expect(mockModelReader.getModelEntity("User")).andReturn(new ModelEntity());
        expect(mockOfBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).andReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        mockOfBizDelegator.refreshSequencer();
        mockConsistencyChecker.checkDataConsistency();

        //after the consistency check lets do the upgrade
        expect(mockUpgradeManager.doUpgradeIfNeededAndAllowed(null)).andReturn(Collections.<String>emptyList());

        //now do a reindex
        mockIndexManager.deactivate();
        expect(mockIndexManager.size()).andReturn(5);
        expect(mockIndexManager.activate((Context) notNull())).andReturn(1L);

        //raise the JiraStartedEvent
        mockPluginEventManager.broadcast(EasyMock.<JiraStartedEvent>anyObject());

        //finally we can restart the scheduler!
        expect(mockScheduler.scheduleJob(EasyMock.<JobDetail>anyObject(), EasyMock.<Trigger>anyObject())).andReturn(new Date()).anyTimes();
        mockScheduler.start();


        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) mockDependencies).globalRefreshCalled);
    }

    @Test
    public void testExecuteQuickImport() throws Exception
    {
        expect(mockBarrier.await(20, TimeUnit.SECONDS)).andReturn(true);

        expect(mockBeanFactory.getInstance(currentUser)).andReturn(new MockI18nHelper()).anyTimes();
        //called during validation!
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        expect(mockOfBizDelegator.makeValue(EasyMock.<String>anyObject())).andReturn(mockGv).anyTimes();
        expect(mockAttachmentPathManager.getDefaultAttachmentPath()).andReturn(directories.get(0).getAbsolutePath()).anyTimes();
        expect(mockIndexPathManager.getDefaultIndexRootPath()).andReturn(directories.get(1).getAbsolutePath()).anyTimes();
        expect(mockLicenseStringFactory.create(EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andStubReturn("");

        //after the first parse check the build number.
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andStubReturn("99999999");
        expect(mockBuildUtilsInfo.getMinimumUpgradableBuildNumber()).andStubReturn("0");

        //after the first parse we also verify the license is good.
        expect(mockJiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andStubReturn(2);
        expect(mockValidationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        mockScheduler.standby();
        expect(mockScheduler.getJobGroupNames()).andReturn(new String[] { "group1" });
        expect(mockScheduler.getJobNames("group1")).andReturn(new String[] { "job1", "job2" });
        expect(mockScheduler.deleteJob("job1", "group1")).andReturn(true);
        expect(mockScheduler.deleteJob("job2", "group1")).andReturn(true);
        mockMailQueue.sendBuffer();
        expect(mockTaskManager.awaitUntilActiveTasksComplete(20)).andReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        expect(mockOfBizDelegator.getModelReader()).andReturn(mockModelReader);
        expect(mockModelReader.getEntityNames()).andReturn(CollectionBuilder.<String>list("Issue", "User"));
        expect(mockModelReader.getModelEntity("Issue")).andReturn(new ModelEntity());
        expect(mockOfBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).andReturn(10);
        expect(mockModelReader.getModelEntity("User")).andReturn(new ModelEntity());
        expect(mockOfBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).andReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        mockOfBizDelegator.refreshSequencer();
        mockConsistencyChecker.checkDataConsistency();

        //after the consistency check lets do the upgrade
        expect(mockUpgradeManager.doUpgradeIfNeededAndAllowed(null)).andReturn(Collections.<String>emptyList());

        //now do a reindex
        mockIndexManager.deactivate();
        expect(mockIndexManager.size()).andReturn(5);
        expect(mockIndexManager.activate((Context) notNull())).andReturn(1L);

        //raise the JiraStartedEvent
        mockPluginEventManager.broadcast(EasyMock.<JiraStartedEvent>anyObject());

        //finally we can restart the scheduler!
        expect(mockScheduler.scheduleJob(EasyMock.<JobDetail>anyObject(), EasyMock.<Trigger>anyObject())).andReturn(new Date()).anyTimes();
        mockScheduler.start();


        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).setQuickImport(true).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) mockDependencies).globalRefreshCalled);
    }

    @Test
    public void testNoPermission() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(false);
        expect(mockI18nHelper.getText("admin.errors.import.permission")).andReturn("No Permission to import data!");

        try
        {
            final String filePath = getDataFilePath("jira-export-test.xml");
            final DataImportParams params = new DataImportParams.Builder(filePath).build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testNoFileProvided() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockI18nHelper.getText("admin.errors.must.enter.xml2")).andReturn("Must provide file");

        try
        {
            final DataImportParams params = new DataImportParams.Builder("").build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testSetupImportWhenAlreadySetup() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        mockProperties.setString(APKeys.JIRA_SETUP, "true");
        expect(mockI18nHelper.getText("admin.errors.import.already.setup")).andReturn("Already setup. Should do xml restore");
        expect(mockI18nHelper.getText("admin.errors.must.enter.xml2")).andReturn("Must provide file");

        try
        {
            final DataImportParams params = new DataImportParams.Builder("").setupImport().build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testFileNonExistent() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockJiraHome.getImportDirectory()).andReturn(new File("somewhere")).anyTimes();
        expect(mockI18nHelper.getText(EasyMock.eq("admin.errors.could.not.find.file"), EasyMock.<String>anyObject())).andReturn("File does not exist.");

        try
        {
            final DataImportParams params = new DataImportParams.Builder("idontexisthopefully.txt").build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }
    
    @Test
    public void testUnsafeFileNonExistent() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockJiraHome.getImportDirectory()).andReturn(new File("somewhere")).anyTimes();
        expect(mockI18nHelper.getText(EasyMock.eq("admin.errors.could.not.find.file"), EasyMock.<String>anyObject())).andReturn("File does not exist.");

        try
        {
            final DataImportParams params = new DataImportParams.Builder(null)
                    .setUnsafeJiraBackup(new File("idontexist.really.really.not")).build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }
    
    @Test
    public void testUnsafeAOFileNonExistent() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockI18nHelper.getText(EasyMock.eq("admin.errors.could.not.find.file"), EasyMock.<String>anyObject())).andReturn("File does not exist.");

        final File file = File.createTempFile("testUnsafeAOFileNonExistent", "txt");
        file.deleteOnExit();

        try
        {
            final DataImportParams params = new DataImportParams.Builder(null)
                    .setUnsafeJiraBackup(file)
                    .setUnsafeAOBackup(new File("I.really.really.don't.exist.and.if.i.did.it.would.be.very.unlucky"))
                    .build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
        finally
        {
            file.delete();
        }
    }

    public void testGetJiraBackupFilesWithFileNameAndNoAOFile() throws IOException
    {
        String f = getDataFilePath("jira-export-test.xml");
        replayMocks();

        final DefaultDataImportService defaultDataImportService = createImportService();
        final DataImportParams params = new DataImportParams.Builder("jira-export-test.xml").build();
        final File backupFile = defaultDataImportService.getJiraBackupFile(params);
        final File aoBackupFile = defaultDataImportService.getAOBackupFile(params);

        final File expectedFile = new File(f).getCanonicalFile();
        assertEquals(expectedFile, backupFile.getCanonicalFile());
        assertEquals(expectedFile, aoBackupFile.getCanonicalFile());
        verifyMocks();
    }

    public void testGetJiraBackupFileWithFileAndNoAOFile() throws IOException
    {
        File tempFile = File.createTempFile("jira-temp-file", "txt");
        try
        {
            getDataFilePath("jira-export-test.xml");
            replayMocks();

            final DefaultDataImportService defaultDataImportService = createImportService();
            final DataImportParams params = new DataImportParams.Builder("jira-export-test.xml").build();
            final File backupFile = defaultDataImportService.getJiraBackupFile(params);
            final File aoBackupFile = defaultDataImportService.getAOBackupFile(params);

            assertEquals(tempFile.getCanonicalFile(), backupFile.getCanonicalFile());
            assertEquals(tempFile.getCanonicalFile(), aoBackupFile.getCanonicalFile());

            verifyMocks();

        }
        finally
        {
            tempFile.delete();
        }
    }

    public void testGetJiraBackupFileAOFile() throws IOException
    {
        File tempFile = File.createTempFile("jira-temp-file", "txt");
        try
        {
            final String dataFilePath = getDataFilePath("jira-export-test.xml");
            replayMocks();

            final DefaultDataImportService defaultDataImportService = createImportService();
            final DataImportParams params = new DataImportParams.Builder("jira-export-test.xml").setUnsafeAOBackup(tempFile).build();
            final File backupFile = defaultDataImportService.getJiraBackupFile(params);
            final File aoBackupFile = defaultDataImportService.getAOBackupFile(params);

            assertEquals(new File(dataFilePath).getCanonicalFile(), backupFile.getCanonicalFile());
            assertEquals(tempFile.getCanonicalFile(), aoBackupFile.getCanonicalFile());

            verifyMocks();

        }
        finally
        {
            tempFile.delete();
        }
    }

    @Test
    public void testNoAO() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockJiraHome.getImportDirectory()).andReturn(new File("somewhere")).anyTimes();
        expect(mockI18nHelper.getText("data.import.error.no.ao")).andReturn("Data Import.");

        backup = null;

        try
        {
            final String filePath = getDataFilePath("jira-export-test.xml");
            final DataImportParams params = new DataImportParams.Builder(filePath).build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testInvalidLicenseProvided() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockJiraLicenseService.validate(mockI18nHelper, "thisisnotavalidlicensestring")).andReturn(mockValidationResult);
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Not a valid license");
        expect(mockValidationResult.getErrorCollection()).andReturn(errors);

        try
        {
            final String filePath = getDataFilePath("jira-export-test.xml");
            final DataImportParams params = new DataImportParams.Builder(filePath).setLicenseString("thisisnotavalidlicensestring").build();
            executeTest(params, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }

    }

    @Test
    public void testVersion1License() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockI18nHelper.getText("data.import.parse.xml")).andReturn("Parsing XML");
        expect(mockI18nHelper.getText(EasyMock.eq("data.import.parse.progress"), EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andReturn("Parsing progress");

        //called during validation!
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);
        expect(mockJiraLicenseService.validate(mockI18nHelper, "version1license")).andReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andReturn(1);
        expect(mockValidationResult.getErrorCollection()).andReturn(new SimpleErrorCollection());

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        expect(mockOfBizDelegator.makeValue(EasyMock.<String>anyObject())).andReturn(mockGv).anyTimes();
        expect(mockAttachmentPathManager.getDefaultAttachmentPath()).andReturn(directories.get(0).getAbsolutePath()).anyTimes();
        expect(mockIndexPathManager.getDefaultIndexRootPath()).andReturn(directories.get(1).getAbsolutePath()).anyTimes();
        expect(mockLicenseStringFactory.create(EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andStubReturn("");

        //after the first parse check the build number.
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andStubReturn("99999999");
        expect(mockBuildUtilsInfo.getMinimumUpgradableBuildNumber()).andStubReturn("0");

        //after the first parse we also verify the license is good.
        expect(mockJiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andStubReturn(2);
        expect(mockValidationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());

        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).setLicenseString("version1license").build();
        executeTest(params, false, DataImportService.ImportError.V1_LICENSE_EXCEPTION);
    }


    @Test
    public void testExecuteWithBuildNumberTooNewInXml() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockI18nHelper.getText("data.import.parse.xml")).andReturn("Parsing XML");
        expect(mockI18nHelper.getText(EasyMock.eq("data.import.parse.progress"), EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andReturn("Parsing progress");

        //called during validation!
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        expect(mockOfBizDelegator.makeValue(EasyMock.<String>anyObject())).andReturn(mockGv).anyTimes();
        expect(mockAttachmentPathManager.getDefaultAttachmentPath()).andReturn(directories.get(0).getAbsolutePath()).anyTimes();
        expect(mockIndexPathManager.getDefaultIndexRootPath()).andReturn(directories.get(1).getAbsolutePath()).anyTimes();
        expect(mockLicenseStringFactory.create(EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andStubReturn("");

        //after the first parse check the build number.
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andStubReturn("1");
        expect(mockBuildUtilsInfo.getMinimumUpgradableBuildNumber()).andStubReturn("1");

        //after the first parse we also verify the license is good.
        expect(mockJiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andStubReturn(2);
        expect(mockValidationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());
        expect(mockI18nHelper.getText("data.import.error.xml.newer")).andReturn("Data is from a newer version of JIRA");

        final String filePath = getDataFilePath("jira-export-test-too-new.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        executeTest(params, false, DataImportService.ImportError.NONE);
    }

    @Test
    public void testExecuteBuildNumberTooOldInXml() throws Exception
    {
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(mockI18nHelper).anyTimes();
        expect(mockI18nHelper.getText("data.import.parse.xml")).andReturn("Parsing XML");
        expect(mockI18nHelper.getText(EasyMock.eq("data.import.parse.progress"), EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andReturn("Parsing progress");

        //called during validation!
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        expect(mockOfBizDelegator.makeValue(EasyMock.<String>anyObject())).andReturn(mockGv).anyTimes();
        expect(mockAttachmentPathManager.getDefaultAttachmentPath()).andReturn(directories.get(0).getAbsolutePath()).anyTimes();
        expect(mockIndexPathManager.getDefaultIndexRootPath()).andReturn(directories.get(1).getAbsolutePath()).anyTimes();
        expect(mockLicenseStringFactory.create(EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andStubReturn("");

        //after the first parse check the build number.
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andStubReturn("400");
        expect(mockBuildUtilsInfo.getMinimumUpgradableBuildNumber()).andStubReturn("18");

        //after the first parse we also verify the license is good.
        expect(mockJiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andStubReturn(2);
        expect(mockValidationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());

        expect(mockExternalLinkUtil.getProperty("external.link.jira.confluence.upgrade.guide.for.old.versions")).andReturn(
                "http://www.atlassian.com");

        expect(mockI18nHelper.getText("data.import.error.xml.too.old", "http://www.atlassian.com")).andReturn("Data is too old visit http://www.atlassian.com/");

        final String filePath = getDataFilePath("jira-export-test-too-old.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();
        executeTest(params, false, DataImportService.ImportError.NONE);
    }

    @Test
    public void testExecuteBuildNumberMissing() throws Exception
    {
        expect(mockBarrier.await(20, TimeUnit.SECONDS)).andReturn(true);
        expect(mockBeanFactory.getInstance(currentUser)).andReturn(new MockI18nHelper()).anyTimes();

        //called during validation!
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        expect(mockOfBizDelegator.makeValue(EasyMock.<String>anyObject())).andReturn(mockGv).anyTimes();
        expect(mockAttachmentPathManager.getDefaultAttachmentPath()).andReturn(directories.get(0).getAbsolutePath()).anyTimes();
        expect(mockIndexPathManager.getDefaultIndexRootPath()).andReturn(directories.get(1).getAbsolutePath()).anyTimes();
        expect(mockLicenseStringFactory.create(EasyMock.<String>anyObject(), EasyMock.<String>anyObject())).andStubReturn("");

        //after the first parse check the build number.
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andStubReturn("1");
        expect(mockBuildUtilsInfo.getMinimumUpgradableBuildNumber()).andStubReturn("0");

        //after the first parse we also verify the license is good.
        expect(mockJiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), EasyMock.<String>anyObject())).andStubReturn(mockValidationResult);
        expect(mockValidationResult.getLicenseVersion()).andStubReturn(2);
        expect(mockValidationResult.getErrorCollection()).andStubReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        expect(mockScheduler.isShutdown()).andReturn(false);
        mockScheduler.shutdown();
        mockMailQueue.sendBuffer();
        expect(mockTaskManager.shutdownAndWait(5)).andReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        expect(mockOfBizDelegator.getModelReader()).andReturn(mockModelReader);
        expect(mockModelReader.getEntityNames()).andReturn(CollectionBuilder.<String>list("Issue", "User"));
        expect(mockModelReader.getModelEntity("Issue")).andReturn(new ModelEntity());
        expect(mockOfBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).andReturn(10);
        expect(mockModelReader.getModelEntity("User")).andReturn(new ModelEntity());
        expect(mockOfBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).andReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        mockOfBizDelegator.refreshSequencer();
        mockConsistencyChecker.checkDataConsistency();

        //after the consistency check lets do the upgrade
        expect(mockUpgradeManager.doUpgradeIfNeededAndAllowed(null)).andReturn(Collections.<String>emptyList());

        //now do a reindex
        mockIndexManager.deactivate();
        expect(mockIndexManager.size()).andReturn(5);
        expect(mockIndexManager.activate((Context) notNull())).andReturn(1L);

        //raise the JiraStartedEvent
        mockPluginEventManager.broadcast(EasyMock.<JiraStartedEvent>anyObject());

        //finally we can restart the scheduler!
        expect(mockScheduler.scheduleJob(EasyMock.<JobDetail>anyObject(), EasyMock.<Trigger>anyObject())).andReturn(new Date()).anyTimes();
        mockScheduler.start();


        final String filePath = getDataFilePath("jira-export-test-no-build-number.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) mockDependencies).globalRefreshCalled);
    }

    private void executeTest(final DataImportParams params, final boolean success, DataImportService.ImportError specificError)
            throws Exception
    {
        replayMocks();

        mockProperties.setOption(APKeys.JIRA_IMPORT_CLEAN_XML, false);
        final DefaultDataImportService service = createImportService();

        final DataImportService.ImportValidationResult validationResult = service.validateImport(currentUser, params);
        final DataImportService.ImportResult importResult = service.doImport(currentUser, validationResult, TaskProgressSink.NULL_SINK);

        assertEquals(success, importResult.isValid());
        assertEquals(specificError, importResult.getImportError());
        verifyMocks();
    }

    private DefaultDataImportService createImportService()
    {
        return new DefaultDataImportService(mockDependencies, mockPermissionManager,
                    mockJiraHome, mockJiraLicenseService, mockBeanFactory, mockOfBizDelegator, mockLicenseStringFactory,
                    mockIndexPathManager, mockAttachmentPathManager, mockExternalLinkUtil, mockProperties, mockBuildUtilsInfo,
                mockTaskManager, mockMailQueue, mockFactory)
        {
            @Override
            Backup getAOBackup()
            {
                return backup;
            }

            @Override
            protected EventPublisher getEventPublisher()
            {
                return eventPublisher;
            }
        };
    }

    private void verifyMocks()
    {
        verify(mockExternalLinkUtil, mockBuildUtilsInfo, mockLicenseStringFactory, mockJiraLicenseService, mockValidationResult,
                mockIndexManager, mockAttachmentPathManager, mockIndexPathManager, mockUpgradeManager,
                mockConsistencyChecker, mockMailQueue, mockScheduler, mockBeanFactory, mockI18nHelper, mockPermissionManager,
                mockJiraHome, mockTaskManager, mockOfBizDelegator, mockModelReader, mockPluginEventManager, mockFactory, 
                mockBarrier, backup);
    }

    private void replayMocks()
    {
        replay(mockExternalLinkUtil, mockBuildUtilsInfo, mockLicenseStringFactory, mockJiraLicenseService, mockValidationResult,
                mockIndexManager, mockAttachmentPathManager, mockIndexPathManager, mockUpgradeManager,
                mockConsistencyChecker, mockMailQueue, mockScheduler, mockBeanFactory, mockI18nHelper, mockPermissionManager,
                mockJiraHome, mockTaskManager, mockOfBizDelegator, mockModelReader, mockPluginEventManager, mockFactory,
                mockBarrier, backup);
    }

    private void setupDirectory(String directoryName) throws Exception
    {
        File directory = new File(directoryName);
        if (!directory.exists())
        {
            if (directory.mkdirs())
            {
                directory.deleteOnExit();
            }
        }
        directories.add(directory);
    }

    private void tearDownDirectory(String directoryName)
    {
        File directory = new File(directoryName);
        if (directory.exists())
        {
            deleteDir(directory);
        }
    }

    private boolean deleteDir(File dir)
    {
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String child : children)
            {
                boolean success = deleteDir(new File(dir, child));
                if (!success)
                {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    private String getDataFilePath(String dataFileName)
    {
        // let's do some funky URL stuff to find the real path of this file
        final URL url = ClassLoaderUtils.getResource(JiraTestUtil.TESTS_BASE + "/action/admin/" + dataFileName, TestDefaultDataImportService.class);
        final File f = new File(url.getPath());
        expect(mockJiraHome.getImportDirectory()).andReturn(new File(f.getParent())).anyTimes();
        return f.getAbsolutePath();
    }

    static class MockDataImportDependencies extends DataImportProductionDependencies
    {
        private boolean globalRefreshCalled = false;
        private final ConsistencyChecker consistencyChecker;
        private final PluginEventManager pluginEventManager;
        private final PluginUpgradeManager pluginUpgradeManager;

        MockDataImportDependencies(ConsistencyChecker consistencyChecker, PluginEventManager pluginEventManager,
                PluginUpgradeManager pluginUpgradeManager)
        {
            this.consistencyChecker = consistencyChecker;
            this.pluginEventManager = pluginEventManager;
            this.pluginUpgradeManager = pluginUpgradeManager;
        }

        @Override
        void globalRefresh(boolean quickImport)
        {
            globalRefreshCalled = true;
        }

        @Override
        ConsistencyChecker getConsistencyChecker()
        {
            return consistencyChecker;
        }

        @Override
        PluginEventManager getPluginEventManager()
        {
            return pluginEventManager;
        }

        @Override
        PluginUpgradeManager getPluginUpgradeManager()
        {
            return pluginUpgradeManager;
        }
    }
}
