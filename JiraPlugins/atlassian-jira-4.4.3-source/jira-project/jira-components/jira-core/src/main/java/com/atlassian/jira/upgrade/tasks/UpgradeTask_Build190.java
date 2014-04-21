package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This upgrade task checks to see if an export service exists without a backup path set.
 * If this is the case, we check for the {@link com.atlassian.jira.config.properties.APKeys#JIRA_PATH_BACKUP} property and if it
 * is set, we set the export service's path to this property.
 * <p/>
 * This is to fix all JIRA instances affected by JRA-12242
 */
public class UpgradeTask_Build190 extends AbstractUpgradeTask
{
    public static final String EXPORT_SERVICE_CLASS = "com.atlassian.jira.service.services.export.ExportService";
    private static final Logger log = Logger.getLogger(UpgradeTask_Build190.class);

    private final ServiceManager serviceManager;
    private final ApplicationProperties applicationProperties;
    private static final String DIR_NAME_KEY = "DIR_NAME";

    /**
     * Returns 190
     *
     * @return 190
     */
    public String getBuildNumber()
    {
        return "190";
    }


    public UpgradeTask_Build190(ServiceManager serviceManager, ApplicationProperties applicationProperties)
    {
        this.serviceManager = serviceManager;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Loops through all the services until one with the right service class is found.  We then
     * check if it has a directory set and if not, we set it to the ap property.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        String backupPath = applicationProperties.getString(APKeys.JIRA_PATH_BACKUP);
        //if the backupPath property is not set, there's no point to running this
        //upgrade task.
        if (TextUtils.stringSet(backupPath))
        {
            //now check if the export service has a path set.
            Collection services = serviceManager.getServices();
            for (Iterator iterator = services.iterator(); iterator.hasNext();)
            {
                JiraServiceContainer jiraServiceContainer = (JiraServiceContainer) iterator.next();
                if (ExportService.class.getName().equals(jiraServiceContainer.getServiceClass()))
                {
                    if(updateServiceWhenPathEmpty(jiraServiceContainer, backupPath))
                    {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Updates the export service.
     * @param jiraServiceContainer
     * @param backupPath
     * @return True if the service was updated false otherwise.
     */
    private boolean updateServiceWhenPathEmpty(JiraServiceContainer jiraServiceContainer, String backupPath)
    {
        try
        {
            String serviceBackupDirProperty = jiraServiceContainer.getProperty(DIR_NAME_KEY);

            //if the DIR_NAME property is not set, update the service with the backuppath stored
            //in the ap property and exit from the loop.
            if (!TextUtils.stringSet(serviceBackupDirProperty))
            {
                //need to convert the map to key -> String[] rather than key -> String (see ConfigurableObjectUtil)
                Map params = convertPropertiesToMap(jiraServiceContainer.getProperties());
                params.put(DIR_NAME_KEY, new String[]{backupPath});
                serviceManager.editService(jiraServiceContainer.getId(), jiraServiceContainer.getDelay(), params);
                return true;
            }
        }
        catch (Exception e)
        {
            log.error("Error updating property map for export service: " + e, e);
        }
        return false;
    }

    private Map convertPropertiesToMap(PropertySet props)
    {
        Map params = new HashMap();
        for (Iterator iterator = props.getKeys().iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            params.put(key, new String[] {props.getString(key)});
        }
        return params;
    }

    /**
     * Returns a short description of this upgrade task
     *
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "Set the backup path for the export service if it doesn't exist using the APKeys.JIRA_PATH_BACKUP property.";
    }
}
