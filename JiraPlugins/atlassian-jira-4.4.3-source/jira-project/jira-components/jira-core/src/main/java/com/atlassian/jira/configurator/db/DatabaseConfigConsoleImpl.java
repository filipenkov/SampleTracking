package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

public class DatabaseConfigConsoleImpl extends AbstractConnectionConfig implements DatabaseConfigConsole
{
    private final ConfigField cfHostname = new ConfigField("Hostname");
    private final ConfigField cfPort = new ConfigField("Port");
    private final ConfigField cfInstance = new ConfigField("Instance");
    private final ConfigField cfUsername = new ConfigField("Username");
    private final ConfigField cfPassword = new ConfigField("Pasword");
    private ConfigField[] fields = {cfHostname, cfPort, cfInstance, cfUsername, cfPassword};

    private DatabaseConfig databaseConfig;

    public DatabaseConfigConsoleImpl(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
        cfInstance.setLabel(databaseConfig.getInstanceFieldName());
    }

    public String getDatabaseType()
    {
        return databaseConfig.getDatabaseType();
    }

    public ConfigField[] getFields()
    {
        return fields;
    }
    
    public void setSettings(final Settings settings) throws ParseException
    {
        cfUsername.setValue(settings.getDbUsername());
        cfPassword.setValue(settings.getDbPassword());
        // parse the URL.
        DatabaseInstance databaseInstance = databaseConfig.parseUrl(settings.getJdbcUrl());
        cfHostname.setValue(databaseInstance.getHostname());
        cfPort.setValue(databaseInstance.getPort());
        cfInstance.setValue(databaseInstance.getInstance());
    }

    public String getInstanceName()
    {
        return cfHostname.getValue() + ':' + cfPort.getValue() + '/' + cfInstance.getValue();
    }

    public String getUsername()
    {
        return cfUsername.getValue();
    }

    public String getPassword()
    {
        return cfPassword.getValue();
    }

    public String getUrl() throws ValidationException
    {
        return databaseConfig.getUrl(cfHostname.getValue(), cfPort.getValue(), cfInstance.getValue());
    }

    public String getClassName()
    {
        return databaseConfig.getClassName();
    }

}
