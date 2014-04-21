package com.atlassian.config.db;

import com.atlassian.config.lifecycle.LifecycleItem;
import com.atlassian.config.lifecycle.LifecycleContext;
import com.atlassian.config.lifecycle.LifecycleManager;
import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;

import java.util.Properties;

import org.apache.log4j.Logger;

public class DatabaseLowerPropertyCheck implements LifecycleItem
{
    private static final Logger log = Logger.getLogger(LifecycleManager.class);

    private AtlassianBootstrapManager bootstrapManager;

    public void setBootstrapManager(AtlassianBootstrapManager bootstrapManager)
    {
        this.bootstrapManager = bootstrapManager;
    }

    public void startup(LifecycleContext context) throws Exception
    {
        Properties databaseProperties = bootstrapManager.getHibernateProperties();
        DatabaseHelper databaseHelper = new DatabaseHelper();
        try
        {
            databaseHelper.setDatabaseLowerProperty(databaseProperties, bootstrapManager.getApplicationConfig());
        }
        catch (BootstrapException e)
        {
            log.error("Exception while checking for lowercasing support of database for non ascii characters: " + e, e);
        }
    }

    public void shutdown(LifecycleContext context) throws Exception
    {
        // t'aint nothin' need doin'
    }
}
