package com.atlassian.jira.bc.dataimport;

import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.activeobjects.spi.NullBackupProgressMonitor;
import com.atlassian.core.util.DataUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.admin.export.EntitiesExporter;
import com.atlassian.jira.action.admin.export.EntityXmlWriter;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelReader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @since v4.4
 */
public class DefaultExportService implements ExportService
{
    static private final Logger LOG = Logger.getLogger(DefaultExportService.class);
    static public final String ACTIVEOBJECTS_XML = "activeobjects.xml";
    static public final String ENTITIES_XML = "entities.xml";

    private final DelegatorInterface genericDelegator;
    private final EntitiesExporter entitiesExporter;
    private final I18nHelper.BeanFactory i18nFactory;

    public DefaultExportService(final DelegatorInterface genericDelegator, final EntitiesExporter entitiesExporter, I18nHelper.BeanFactory i18nFactory)
    {
        this.genericDelegator = genericDelegator;
        this.entitiesExporter = entitiesExporter;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public ServiceOutcome<Void> export(User loggedInUser, String filename, TaskProgressSink taskProgressSink)
    {
        return export(loggedInUser, filename, ExportService.Style.NORMAL, taskProgressSink);
    }

    @Override
    public ServiceOutcome<Void> export(User loggedInUser, String filename, Style style, TaskProgressSink taskProgressSink)
    {
        final I18nHelper i18n = i18nFactory.getInstance(loggedInUser);

        ZipOutputStream zip = null;
        try
        {
            final ModelReader reader = genericDelegator.getModelReader();
            final Collection<String> ec = reader.getEntityNames();
            final TreeSet<String> entityNames = new TreeSet<String>(ec);

            final int numberOfEntities = entityNames.size();
            LOG.debug("numberOfEntities = " + numberOfEntities);

            zip = getZipOutputStream(filename);

            final EntityXmlWriter entityWriter = style.getEntityXmlWriter();

            final long start = System.currentTimeMillis();
            final long entitiesWritten = entitiesExporter.exportEntities(zip, entityNames, entityWriter, (com.opensymphony.user.User) loggedInUser);
            LOG.info("Data export completed in " + (System.currentTimeMillis() - start) + "ms. Wrote " + entitiesWritten + " entities to export in memory.");

            final Backup activeObjects = getActiveObjectsBackup();
            if (activeObjects == null)
            {
                // we don't want this to stop all JIRA backups.
                LOG.error("Could not find ActiveObjects in OSGi fairy land. Plugins using ActiveObjects have not been backed up.");
            }
            else
            {
                zip.putNextEntry(new ZipEntry(ACTIVEOBJECTS_XML));
                LOG.info("Attempting to save the Active Objects Backup");
                try
                {
                    activeObjects.save(zip, NullBackupProgressMonitor.INSTANCE);
                }
                catch (NoSuchMethodError ex)
                {
                    // JRADEV-5986: Sounds like we are on an old version of Java.
                    final String javaRuntimeVersion = System.getProperty("java.runtime.version");
                    final String message = "Error exporting Active Objects. You must run JRE 1.6_18 or higher. java.runtime.version: " + javaRuntimeVersion;
                    LOG.error(message, ex);
                    throw new NoSuchMethodError(message);
                }
                LOG.info("Finished saving the Active Objects Backup");
            }

            return ServiceOutcomeImpl.ok(null);
        }
        catch (IOException e)
        {
            LOG.error("Error during XML backup.", e);
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.export.ioerror", filename));
        }
        catch (GenericEntityException e)
        {
            if ((e.getMessage() != null) && (e.getMessage().indexOf("invalid XML character") != -1))
            {
                final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
                errorCollection.addErrorMessage(i18n.getText("admin.export.backup.data.invalid.characters"), ErrorCollection.Reason.VALIDATION_FAILED);
                return ServiceOutcomeImpl.from(errorCollection, null);
            }
            else
            {
                return ServiceOutcomeImpl.error(i18n.getText("admin.errors.dataexport.error.exporting.data", e));
            }
        }
        finally
        {
            IOUtils.closeQuietly(zip);
        }
    }

    protected Backup getActiveObjectsBackup()
    {
        return ComponentManager.getOSGiComponentInstanceOfType(Backup.class);
    }

    protected ZipOutputStream getZipOutputStream(final String filename) throws IOException
    {
        final String zipFileName = DataUtils.getZipFilename(filename);
        final ZipOutputStream out = new ZipOutputStream(FileUtils.openOutputStream(new File(zipFileName)));

        // Add ZIP entry to output stream.
        out.putNextEntry(new ZipEntry(ENTITIES_XML));
        return out;
    }
}