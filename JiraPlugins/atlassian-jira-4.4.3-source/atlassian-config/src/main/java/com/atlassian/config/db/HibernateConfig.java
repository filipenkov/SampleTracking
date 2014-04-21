package com.atlassian.config.db;

import com.atlassian.config.ApplicationConfiguration;

import java.util.Properties;

/**
 * Wrapper around ApplicationConfiguration to provide easy access to Hibernate configuration info
 */
public class HibernateConfig
{
    public static final String HIBERNATE_SETUP = "hibernate.setup";
    public static final String HIBERNATE_CONFIG_PREFIX = "hibernate.";

    private ApplicationConfiguration applicationConfig;

    /**
     * Do not construct. Do not take orally. Consult a doctor if pain persists.
     */
    public HibernateConfig() {
    }

    public void setApplicationConfig(ApplicationConfiguration applicationConfiguration)
    {
        this.applicationConfig = applicationConfiguration;
    }

    public boolean isHibernateSetup() {
        return (applicationConfig.getBooleanProperty(HibernateConfig.HIBERNATE_SETUP));
    }

    /**
     * Gets all hibernate properties from the config starting 'hibernate.'
     * @return all hibernate properties in a Properties map
     */
    public Properties getHibernateProperties()
    {
        Properties props = new Properties();
        props.putAll(applicationConfig.getPropertiesWithPrefix(HIBERNATE_CONFIG_PREFIX));
        return props;
    }

    /**
     * Since MySQL needs to be isolated from _real_ databases.
     */
    public boolean isMySql()
    {
        return isHibernateSetup() &&
                ((String) applicationConfig.getProperty("hibernate.dialect")).endsWith("MySQLDialect");
    }

    public boolean isOracle()
    {
        return isHibernateSetup() &&
                ((String) applicationConfig.getProperty("hibernate.dialect")).endsWith("OracleDialect");
    }

    public boolean isHSQL()
    {
        return isHibernateSetup() &&
                ((String) applicationConfig.getProperty("hibernate.dialect")).endsWith("HSQLDialect");
    }

}
