package com.atlassian.jira.bc.dataimport;

import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.activeobjects.spi.NullRestoreProgressMonitor;
import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.admin.OfbizImportHandler;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.jira.license.LicenseStringFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.ProgressMonitoringFileInputStream;
import com.atlassian.jira.task.StepTaskProgressSink;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.XmlReader;
import com.atlassian.jira.util.concurrent.BoundedExecutor;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.xml.JiraFileInputStream;
import com.atlassian.jira.util.xml.XMLCleaningReader;
import com.atlassian.jira.web.action.setup.DevModeSecretSauce;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.scheduler.SchedulerUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.model.ModelViewEntity;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @see DataImportService for details about what this does.
 * @since 4.4
 */
public class DefaultDataImportService implements DataImportService
{
    private static final Logger log = Logger.getLogger(DefaultDataImportService.class);

    private final PermissionManager permissionManager;
    private final JiraHome jiraHome;
    private final JiraLicenseUpdaterService jiraLicenseService;
    private final I18nHelper.BeanFactory beanFactory;
    private final OfBizDelegator ofBizDelegator;
    private final LicenseStringFactory licenseStringFactory;
    private final IndexPathManager indexPathManager;
    private final AttachmentPathManager attachmentPathManager;
    private final ExternalLinkUtil externalLinkUtil;
    private final ApplicationProperties applicationProperties;
    private final BuildUtilsInfo buildUtilsInfo;
    private final TaskManager taskManager;
    private final MailQueue mailQueue;
    private final DataImportProductionDependencies dependencies;
    private final DevModeSecretSauce devModeSecretSauce;

    public DefaultDataImportService(final DataImportProductionDependencies dependencies, final PermissionManager permissionManager,
            final JiraHome jiraHome, final JiraLicenseUpdaterService jiraLicenseService, final I18nHelper.BeanFactory beanFactory,
            final OfBizDelegator ofBizDelegator, final LicenseStringFactory licenseStringFactory,
            final IndexPathManager indexPathManager, final AttachmentPathManager attachmentPathManager,
            final ExternalLinkUtil externalLinkUtil, final ApplicationProperties applicationProperties,
            final BuildUtilsInfo buildUtilsInfo, final TaskManager taskManager, final MailQueue mailQueue)
    {
        this.dependencies = dependencies;
        this.permissionManager = permissionManager;
        this.jiraHome = jiraHome;
        this.jiraLicenseService = jiraLicenseService;
        this.beanFactory = beanFactory;
        this.ofBizDelegator = ofBizDelegator;
        this.licenseStringFactory = licenseStringFactory;
        this.indexPathManager = indexPathManager;
        this.attachmentPathManager = attachmentPathManager;
        this.externalLinkUtil = externalLinkUtil;
        this.applicationProperties = applicationProperties;
        this.buildUtilsInfo = buildUtilsInfo;
        this.taskManager = taskManager;
        this.mailQueue = mailQueue;
        this.devModeSecretSauce = new DevModeSecretSauce();
    }

    @Override
    public ImportValidationResult validateImport(User loggedInUser, DataImportParams params)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(loggedInUser);
        if (!params.isSetup() && !permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, loggedInUser))
        {
            errors.addErrorMessage(i18n.getText("admin.errors.import.permission"));
        }

        //did we try to do a setup import in an instance that's already been setup?
        if (params.isSetup() && applicationProperties.getString(APKeys.JIRA_SETUP) != null)
        {
            errors.addErrorMessage(i18n.getText("admin.errors.import.already.setup"));
        }

        if (StringUtils.isBlank(params.getFilename()))
        {
            errors.addErrorMessage(i18n.getText("admin.errors.must.enter.xml2"));
        }
        else
        {
            final File file = getSafeFile(params);

            if (!file.exists())
            {
                errors.addError("filename", i18n.getText("admin.errors.could.not.find.file", file.getAbsolutePath()));
            }
            else
            {
                if (!file.isFile())
                {
                    errors.addError("filename", i18n.getText("admin.errors.file.is.directory", file.getAbsolutePath()));
                }
                if (!file.canRead())
                {
                    errors.addError("filename", i18n.getText("admin.errors.file.readable", file.getAbsolutePath()));
                }
            }
        }

        if (StringUtils.isNotBlank(params.getLicenseString()))
        {
            final JiraLicenseService.ValidationResult validationResult = jiraLicenseService.validate(i18n, params.getLicenseString());
            errors.addErrorCollection(validationResult.getErrorCollection());
        }

        return new ImportValidationResult(errors, params);
    }

    @Override
    public ImportResult doImport(User loggedInUser, ImportValidationResult result, TaskProgressSink taskProgressSink)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You cannot perform an import with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You cannot perform an import with an invalid validation result.");
        }

        final I18nHelper i18n = beanFactory.getInstance(loggedInUser);

        log.info("Running JIRA Data Import...");
        final ImportResult.Builder importResultBuilder = new ImportResult.Builder(result.getParams());

        // Create the executor pool and the import handler that we will use to parse/import the backup XML
        final BoundedExecutor pool = new BoundedExecutor(Executors.newFixedThreadPool(10), 10000);
        final OfbizImportHandler ofbizImportHandler = new OfbizImportHandler(ofBizDelegator, pool, licenseStringFactory, indexPathManager, attachmentPathManager, result.getParams().isUseDefaultPaths());

        // First, before we shut anything down, lets try to parse the XML backup to make sure its valid and that we
        // are really going to go through with this import.
        final JiraLicenseService.ValidationResult licenseValidationResult = parseXmlAndValidateBackupData(result.getParams(), importResultBuilder, i18n, ofbizImportHandler, taskProgressSink);

        // We should not go forward if there has been errors
        if (!importResultBuilder.isValid())
        {
            return importResultBuilder.build();
        }

        // Now that we have validated that the provided XML is cool we can prep JIRA for importing the XML and
        // shutdown all our async services (task manager and scheduler).
        shutdownAndFlushAsyncServices(result.getParams());

        // Now that we have parsed the XML and made sure its valid AND we have shutdown all our async services
        // lets get to importing the data
        final boolean success = performImport(result.getParams(), importResultBuilder, i18n, pool, ofbizImportHandler, licenseValidationResult, taskProgressSink);

        // initialize the new scheduler that was created because of the successful import
        if (success && importResultBuilder.isValid())
        {
            startAsyncServices();
        }
        taskProgressSink.makeProgress(100, i18n.getText("data.import.completed"), i18n.getText("data.import.completed.imported", ofbizImportHandler.getEntityCount()));

        log.info("JIRA Data Import has finished.");
        return importResultBuilder.build();
    }

    private JiraLicenseService.ValidationResult parseXmlAndValidateBackupData(final DataImportParams params,
            final ImportResult.Builder importResult, final I18nHelper i18n, final OfbizImportHandler ofbizImportHandler,
            final TaskProgressSink taskProgressSink)
    {
        // Validate that we have been provided a valid input source, if there is not one then this method will
        // add error messages which will stop the calling method from moving forward.
        final ErrorCollection errors = importResult.getErrorCollection();
        final File safeFileName = getSafeFile(params);
        final InputSource inputSource = getInputSource(safeFileName, errors, i18n, taskProgressSink);
        if (inputSource == null)
        {
            return null;
        }

        JiraLicenseService.ValidationResult licenseValidationResult = null;
        try
        {
            final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            log.info("Importing XML data...");

            // Indicate to the parser that we just want to parse the XML file without touching the db - so that we can
            // be sure the XML is valid
            ofbizImportHandler.setCreateEntities(false);

            log.info("Start parsing XML with SAX Parser.");
            saxParser.parse(inputSource, ofbizImportHandler);
            log.info("XML successfully parsed.");

            final String buildNumber = ofbizImportHandler.getBuildNumber();
            if (isXmlNewerThanThisVersion(buildNumber))
            {
                errors.addErrorMessage(i18n.getText("data.import.error.xml.newer"));
            }

            // Note: build number might be null in blank XML - we need to allow this
            if (buildNumber != null && !doesXmlMeetMinimumVersionRequirement(buildNumber))
            {
                errors.addErrorMessage(i18n.getText("data.import.error.xml.too.old",
                        externalLinkUtil.getProperty("external.link.jira.confluence.upgrade.guide.for.old.versions")));
            }

            // validating the license, this is the license from the form if any, the license from the XML otherwise
            final String licenseString = StringUtils.isNotBlank(params.getLicenseString()) ? params.getLicenseString() : ofbizImportHandler.getLicenseString();
            licenseValidationResult = jiraLicenseService.validate(i18n, licenseString);
            if (licenseValidationResult.getLicenseVersion() == 1)
            {
                importResult.setSpecificError(ImportError.V1_LICENSE_EXCEPTION, licenseString);
            }
            else
            {
                errors.addErrors(licenseValidationResult.getErrorCollection().getErrors());
            }

            final String indexPath = ofbizImportHandler.getIndexPath();
            if (indexPath != null)
            {
                File indexDir = new File(indexPath);

                if (!indexDir.exists())
                {
                    errors.addErrorMessage(i18n.getText("setup.error.index.filepath", indexDir.getAbsolutePath()));
                    importResult.setSpecificError(ImportError.CUSTOM_PATH_EXCEPTION, "path not found");
                }
                else if (!indexDir.isDirectory() || !indexDir.canWrite())
                {
                    errors.addErrorMessage(i18n.getText("setup.error.index.filepath.writeerror", indexDir.getAbsolutePath()));
                }
            }

            final String attachmentPath = ofbizImportHandler.getAttachmentPath();
            if (attachmentPath != null)
            {
                File attachmentDir = new File(attachmentPath);

                if (!attachmentDir.exists())
                {
                    errors.addErrorMessage(i18n.getText("attachfile.error.invalid", attachmentDir.getAbsolutePath()));
                    importResult.setSpecificError(ImportError.CUSTOM_PATH_EXCEPTION, "path not found");
                }
                else if (!attachmentDir.isDirectory() || !attachmentDir.canWrite())
                {
                    errors.addErrorMessage(i18n.getText("attachfile.error.writeerror", attachmentDir.getAbsolutePath()));
                }
            }
        }
        catch (final SAXParseException e)
        {
            log.error("Error parsing export file: " + e, e);
            errors.addErrorMessage(i18n.getText("data.import.error.parsing.export.file", e));
        }
        catch (final Exception e)
        {
            log.error("Error importing data: " + e, e);
            errors.addErrorMessage(i18n.getText("admin.errors.error.importing.data", e));
        }
        finally
        {
            closeInputSource(inputSource);
        }
        return licenseValidationResult;
    }

    private void restoreActiveObjects(final File safeFileName, final I18nHelper i18n, final ErrorCollection errors)
    {
        ZipFile zipFile = null;
        try
        {
             zipFile = new ZipFile(safeFileName);
            // Otherwise we expect them to be using the "new" (ca 2011) naming convention.
            final InputStream inputStream = zipFile.getInputStream(new ZipEntry(DefaultExportService.ACTIVEOBJECTS_XML));
            if (inputStream == null)
            {
                log.info(String.format("Unable to find ActiveObjects backup (%s) inside of zip file: %s", DefaultExportService.ACTIVEOBJECTS_XML, safeFileName));
            }
            else
            {
                final Backup activeObjectsBackup = ComponentManager.getOSGiComponentInstanceOfType(Backup.class);
                if (activeObjectsBackup == null)
                {
                    final String message = "Unable to find ActiveObjects in OSGi fairy land to perform restoration of plugin entities.";
                    log.error(message);
                    errors.addErrorMessage(i18n.getText("admin.errors.error.importing.data", message));
                }
                else
                {
                    activeObjectsBackup.restore(inputStream, NullRestoreProgressMonitor.INSTANCE);
                }
            }
        }
        catch (IOException e)
        {
            log.error("Error attempting to import ActiveObjects backup: " + e, e);
            errors.addErrorMessage(i18n.getText("admin.errors.error.importing.data", e));
        }
        finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                }
                catch (IOException e)
                {
                    log.error("Unable to close zip stream during ActiveObjects restore", e);
                }
            }
        }
    }

    private void shutdownAndFlushAsyncServices(final DataImportParams params)
    {
        // Stop the scheduler
        final Scheduler scheduler = dependencies.getScheduler();
        if (params.isQuickImport())
        {
            // Pause the scheduler; we will keep it and restart it later.
            pauseAndFlushScheduler(scheduler);
        }
        else
        {
            // Shutdown the scheduler - we throw it away and create a new one when we recreate Pico Container.
            final SchedulerUtil schedUtil = new SchedulerUtil();
            schedUtil.shutdownScheduler(scheduler);
        }

        //We need to clean up the task manager now. Leaving tasks running during import may cause a deadlock.
        //The task manager is restarted in globalRefresh.
        cleanUpTaskManager(params);

        // Send the emails on the Mail Queue
        try
        {
            mailQueue.sendBuffer();
        }
        catch (final Exception e)
        {
            log.warn("Sending buffer failed: " + e.getMessage(), e);
        }
    }

    private void cleanUpTaskManager(final DataImportParams params)
    {
        if (params.isQuickImport())
        {
            //Wait until all long running tasks have completed. In quick import we don't replace the task manager.
            if (!taskManager.awaitUntilActiveTasksComplete(20))
            {
                log.error("There were still running tasks during the live import:");
                for (TaskDescriptor<?> descriptor : taskManager.getLiveTasks())
                {
                    log.error(" -\t" + descriptor.getDescription());
                }
            }
        }
        else
        {
            //Shutdown the task manager before the import. Having a Long Running Task running can cause deadlock during
            //an import.
            taskManager.shutdownAndWait(5);
        }
    }

    private void pauseAndFlushScheduler(final Scheduler scheduler)
    {
        try
        {
            scheduler.standby();
            // Remove all existing jobs - they get added back in on restart.
            final String[] jobGroupNames = scheduler.getJobGroupNames();
            for (String groupName : jobGroupNames)
            {
                final String[] jobNames = scheduler.getJobNames(groupName);
                for (String jobName : jobNames)
                {
                    scheduler.deleteJob(jobName, groupName);
                }
            }
        }
        catch (SchedulerException e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings ( { "ThrowableResultOfMethodCallIgnored" })
    private boolean performImport(final DataImportParams params, ImportResult.Builder importResult, final I18nHelper i18n,
            final BoundedExecutor pool, final OfbizImportHandler ofbizImportHandler, final JiraLicenseService.ValidationResult licenseValidationResult, TaskProgressSink taskProgressSink)
    {
        InputSource inputSource = null;
        final ErrorCollection errors = importResult.getErrorCollection();
        try
        {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser saxParser = factory.newSAXParser();
            // If we got here (no exception was thrown then we can proceed with the import)
            // Now remove all the existing entities
            removeAllEntities();
            // And parse the XML file again and store the values
            final long entityCount = ofbizImportHandler.getEntityCount();
            if (log.isInfoEnabled())
            {
                log.info("Started storing " + entityCount + " Generic Values.");
            }
            ofbizImportHandler.setTaskProgressSink(new I18nTaskProgressSink(new StepTaskProgressSink(20, 90, entityCount, taskProgressSink), i18n, entityCount));
            if (restoreData(importResult, i18n, pool, ofbizImportHandler, saxParser, params, errors))
            {
                log.info("Finished storing Generic Values.");
            }
            else
            {
                return false;
            }

            final File safeFile = getSafeFile(params);
            if ("zip".equalsIgnoreCase(FilenameUtils.getExtension(safeFile.getAbsolutePath())))
            {
                restoreActiveObjects(safeFile, i18n, errors);
            }

            restartJira(params, licenseValidationResult);
            checkConsistency(i18n, taskProgressSink);
            final Collection<String> upgradeErrors = upgradeJira(i18n, taskProgressSink);
            if (!upgradeErrors.isEmpty())
            {
                setErrorMessage(importResult, upgradeErrors);
            }
            else
            {
                updateLookAndFeel();
            }
            // Need to reindex after all the upgrades are complete
            reindex();
            upgradePlugins();
            return true; // we can restart the scheduler.
        }
        catch (final SAXParseException e)
        {
            // NOTE: this should never happen since we have already parsed the XML before and found it to be well formed
            log.error("Error parsing export file: " + e, e);
            errors.addErrorMessage(i18n.getText("data.import.error.parsing.export.file", e));
        }
        catch (final Exception e)
        {
            log.error("Error importing data: " + e, e);
            errors.addErrorMessage(i18n.getText("admin.errors.error.importing.data", e));
        }
        finally
        {
            pool.shutdownAndWait();

            closeInputSource(inputSource);
        }
        return false;
    }

    private void upgradePlugins()
    {
        //JRA  23876 - need to do plugin uogrades after setup tasks
        dependencies.getPluginEventManager().broadcast(new JiraStartedEvent());
    }

    private void updateLookAndFeel()
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        final long oldLookAndFeelversion = lookAndFeelBean.getVersion();
        //JRA-11680: Need to increment the LF version in case the imported data contains LF changes.  This is so that
        //           the css does not get cached.
        lookAndFeelBean.updateVersion(oldLookAndFeelversion);
    }

    private void setErrorMessage(final ImportResult.Builder importResult, final Collection<String> upgradeErrors)
    {
        final StringBuilder errorString = new StringBuilder();
        for (String upgradeError : upgradeErrors)
        {
            errorString.append(upgradeError).append("\n");
        }
        importResult.setSpecificError(ImportError.UPGRADE_EXCEPTION, errorString.toString());
    }

    private Collection<String> upgradeJira(final I18nHelper i18n,
            final TaskProgressSink taskProgressSink)
            throws IllegalXMLCharactersException
    {
        // now upgrade to make sure we're at the latest version without doing an export - as we just imported the
        // data
        final Collection<String> upgradeErrors = dependencies.getUpgradeManager().doUpgradeIfNeededAndAllowed(null);
        taskProgressSink.makeProgress(96, i18n.getText("data.import.indexing"), i18n.getText("data.import.indexing.progress"));
        return upgradeErrors;
    }

    private void checkConsistency(final I18nHelper i18n, final TaskProgressSink taskProgressSink) throws Exception
    {// check the consistency of all imported data
        dependencies.getConsistencyChecker().checkDataConsistency();
        taskProgressSink.makeProgress(92, i18n.getText("data.import.upgrade"), i18n.getText("data.import.upgrade.progress"));
    }

    private void restartJira(final DataImportParams params, final JiraLicenseService.ValidationResult licenseValidationResult)
            throws Exception
    {
        // synchronise the sequencer with the database
        // storing a new index path can result in the creation of a new property entry so we want to ensure
        // that the ofbiz sequencer gets refreshed to prevent us from mis-allocating sequence ids.
        dependencies.refreshSequencer();
        // Reset the license if required, if we're there the license has already been validated
        if (StringUtils.isNotBlank(params.getLicenseString()) && licenseValidationResult != null)
        {
            jiraLicenseService.setLicense(licenseValidationResult);
        }
        // Restart JIRAs PICO container and plugins system (amongst other things)
        globalRefresh(params.isQuickImport());
    }

    private boolean restoreData(final ImportResult.Builder importResult, final I18nHelper i18n, BoundedExecutor pool,
            final OfbizImportHandler ofbizImportHandler, final SAXParser saxParser,
            final DataImportParams params, final ErrorCollection errors)
            throws SAXException, IOException
    {
        // Retrieve the input source again for the parsing. This time we wont record any progress
        // when reading the file since we're only interested in how many entities have been stored
        // in the database.
        InputSource inputSource = getInputSource(getSafeFile(params), errors, i18n, TaskProgressSink.NULL_SINK);
        // Indicate to the parser that it should actually create entities in the db
        ofbizImportHandler.setCreateEntities(true);
        saxParser.parse(inputSource, ofbizImportHandler);

        pool.shutdownAndWait();

        if (ofbizImportHandler.getImportError() != null)
        {
            importResult.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.unknown.error.during.import", ofbizImportHandler.getImportError().getMessage()));
            return false;
        }
        return true;
    }

    private void startAsyncServices()
    {
        final SchedulerUtil schedUtil = new SchedulerUtil();
        schedUtil.initializeAndStart(dependencies.getScheduler());
    }

    private void closeInputSource(InputSource is)
    {
        if (is != null)
        {
            IOUtils.closeQuietly(is.getByteStream());
            IOUtils.closeQuietly(is.getCharacterStream());
        }
    }

    private void globalRefresh(boolean quickImport) throws Exception
    {
        try
        {
            dependencies.globalRefresh(quickImport);
        }
        catch (final RuntimeException ex)
        {
            log.error(ex.getMessage(), ex);
        }
    }

    private boolean isXmlNewerThanThisVersion(final String xmlBuildNumber)
    {
        try
        {
            final int currentBuildNumber = Integer.parseInt(buildUtilsInfo.getCurrentBuildNumber());
            return Integer.parseInt(xmlBuildNumber) > currentBuildNumber;
        }
        catch (final NumberFormatException nfe)
        {
            log.warn("Could not parse the build number from XML import data. The build number was <" + xmlBuildNumber + ">");
            // we just assume it will work if the build number is unparseable. is this good?
            return false;
        }
    }

    private boolean doesXmlMeetMinimumVersionRequirement(final String xmlBuildNumber)
    {
        try
        {
            final int minimumVersionBuildNumber = Integer.parseInt(buildUtilsInfo.getMinimumUpgradableBuildNumber());
            return Integer.parseInt(xmlBuildNumber) >= minimumVersionBuildNumber;
        }
        catch (final NumberFormatException nfe)
        {
            log.warn("problems parsing build number", nfe);
            return false;
        }
    }

    private InputSource getInputSource(final File file, ErrorCollection errors, I18nHelper i18n, TaskProgressSink taskProgressSink)
    {
        InputStream inputStream;
        try
        {
            JiraFileInputStream stream = new JiraFileInputStream(file);
            inputStream = new ProgressMonitoringFileInputStream(stream,
                    new StepTaskProgressSink(0, 20, stream.getSize(), taskProgressSink),
                    i18n.getText("data.import.parse.xml"), i18n.getText("data.import.parse.progress", "{0}", FileSize.format(stream.getSize())));
        }
        catch (final FileNotFoundException e)
        {
            errors.addErrorMessage(i18n.getText("data.import.could.not.find.file.at", file.getAbsolutePath()));
            return null;
        }
        catch (final IOException e)
        {
            log.error("Error importing from zip file: \"" + file.getAbsolutePath() + "\"", e);
            errors.addErrorMessage(i18n.getText("data.import.error.importing.from.zip", "\"" + file.getAbsolutePath() + "\"", e.getMessage()));
            return null;
        }

        if (applicationProperties.getOption(APKeys.JIRA_IMPORT_CLEAN_XML))
        {
            final Reader reader = getFilteredReader(inputStream);
            return new InputSource(reader);
        }
        else
        {
            return new InputSource(inputStream);
        }
    }

    private void reindex() throws Exception
    {
        final IndexLifecycleManager indexManager = dependencies.getIndexLifecycleManager();
        indexManager.deactivate();
        indexManager.activate(Contexts.percentageLogger(indexManager, log));
    }

    private void removeAllEntities() throws GenericEntityException
    {
        log.info("Removing all entries from the database.");

        final ModelReader reader = ofBizDelegator.getModelReader();
        final Collection<String> ec = reader.getEntityNames();
        final TreeSet<String> entityNames = new TreeSet<String>(ec);

        for (final String entityName : entityNames)
        {
            final ModelEntity modelEntity = reader.getModelEntity(entityName);
            if (modelEntity != null)
            {
                if (!(modelEntity instanceof ModelViewEntity))
                {
                    // We have a normal entity so remove all of its records.
                    ofBizDelegator.removeByAnd(entityName, Collections.<String, Object>emptyMap());
                }
                else if (log.isDebugEnabled())
                {
                    log.debug("No need to remove records from View entity '" + entityName + "'");
                }
            }
            else
            {
                log.warn("Nothing known about entity '" + entityName + "' - cannot delete.");
            }

        }
        log.info("All entries removed.");
    }

    private Reader getFilteredReader(final InputStream is)
    {
        try
        {
            final Reader reader = XmlReader.createReader(is);
            if (reader instanceof XmlReader)
            {
                final XmlReader xmlReader = (XmlReader) reader;
                return new XMLCleaningReader(new InputStreamReader(xmlReader.getInputStream(), xmlReader.getEncoding()));
            }
            else
            {
                final String message = "Expected XMLReader but got '" + reader.getClass().getName() + ". Cannot determine the encoding of the XML file.";
                log.error(message);
                throw new InvalidSourceException(message);
            }
        }
        catch (final UnsupportedEncodingException e)
        {
            log.error(e, e);
            throw new InvalidSourceException("Unsupported encoding.", e);
        }
        catch (final IOException e)
        {
            log.error(e, e);
            throw new InvalidSourceException("IO error has occurred.", e);
        }
    }

    private File getSafeFile(final DataImportParams params)
    {
        //during setup we can import from anywhere. Otherwise only from the home directory should be allowed.
        final File file = new File(params.getFilename());
        if (params.isSetup() || hasSecretSauce(file))
        {
            if (file.exists())
            {
                return file;
            }
        }

        return new File(jiraHome.getImportDirectory().getAbsolutePath(), file.getName());
    }


    /**
     * We have the secret developer sauce if this mode is on and the file name has a full path in it
     *
     * @param file the file in play
     * @return true if the secret sauce should be applied
     */
    private boolean hasSecretSauce(File file)
    {
        return devModeSecretSauce.isBoneFideJiraDeveloper() && file.getParentFile() != null;
    }

    class InvalidSourceException extends RuntimeException
    {
        public InvalidSourceException(final String s)
        {
            super(s);
        }

        public InvalidSourceException(final String s, final Throwable throwable)
        {
            super(s, throwable);
        }
    }

    /**
     * A task progress sink that can be used to log i18nized messages in its makeProgress method.
     */
    static class I18nTaskProgressSink implements TaskProgressSink
    {
        private final TaskProgressSink delegate;
        private final I18nHelper i18n;
        private final long totalSize;

        I18nTaskProgressSink(TaskProgressSink delegate, I18nHelper i18n, long totalSize)
        {
            this.delegate = delegate;
            this.i18n = i18n;
            this.totalSize = totalSize;
        }

        @Override
        public void makeProgress(long taskProgress, String currentSubTask, String message)
        {
            delegate.makeProgress(taskProgress, i18n.getText(currentSubTask),
                    i18n.getText(message, Long.toString(taskProgress), Long.toString(totalSize)));
        }
    }

}
