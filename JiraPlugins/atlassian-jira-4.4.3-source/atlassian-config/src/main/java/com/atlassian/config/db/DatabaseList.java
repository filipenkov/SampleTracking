package com.atlassian.config.db;

import com.atlassian.core.util.PairType;
import com.atlassian.core.util.PropertyUtils;

import java.util.*;

/**
 * This class is responsible for loading a list of possible databases
 */
public class DatabaseList
{

    private List databases;

    /**
     * By default this will load the supportedDatabases.propertie file
     */
    public DatabaseList()
    {
        this("supportedDatabases.properties");
    }

    /**
     * Generate database list from given file
     * @param supportedDbFile path to properties file
     */
    public DatabaseList(String supportedDbFile)
    {
        databases = new ArrayList(7);
        Properties dbProps = PropertyUtils.getProperties(supportedDbFile, DatabaseList.class);

        List c = new ArrayList(dbProps.keySet());
        Collections.sort(c);

        for (Iterator it = c.iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            if (key.startsWith("key."))
                databases.add(new PairType(dbProps.getProperty(key), dbProps.getProperty("value." + key.substring(4))));
        }
    }

    /**
     *
     * @return List of {@link PairType} containing database key and description pairs
     */
    public List getDatabases()
    {
        return databases;
    }

}
