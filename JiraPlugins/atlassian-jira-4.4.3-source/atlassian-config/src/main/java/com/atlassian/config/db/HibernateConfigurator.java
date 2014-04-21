package com.atlassian.config.db;

import com.atlassian.config.ConfigurationException;

/**
 * User: nickf
 * Date: Nov 10, 2004
 * Time: 5:16:18 PM
 */
public interface HibernateConfigurator
{
    String DATABASE_TYPE_EMBEDDED = "embedded";
    String DATABASE_TYPE_STANDARD = "standard";
    String DATABASE_TYPE_DATASOURCE = "datasource";

    void configureDatabase(DatabaseDetails dbDetails, boolean embedded)
        throws ConfigurationException;

    void configureDatasource(String datasourceName, String dialect) throws ConfigurationException;

    void unconfigureDatabase();
}
