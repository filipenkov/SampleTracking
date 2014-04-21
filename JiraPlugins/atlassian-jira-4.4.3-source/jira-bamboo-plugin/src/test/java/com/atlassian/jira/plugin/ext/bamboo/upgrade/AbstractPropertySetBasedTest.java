package com.atlassian.jira.plugin.ext.bamboo.upgrade;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager.LegacyBambooServerManagerImpl;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractPropertySetBasedTest
{
    private static final String PROPERTIES_PATH = AbstractPropertySetBasedTest.class.getPackage().getName().replace('.', File.separatorChar);

    protected PropertySet propertySet;

    protected void setUpPropertySet()
    {
        HashMap psArgs = new HashMap();
        psArgs.put("delegator.name", "default");
        psArgs.put("entityName", "BambooServerProperties");
        psArgs.put("entityId", (long) 1);
        propertySet = PropertySetManager.getInstance("memory", psArgs);
    }

    protected void init(final String propertiesFileName) throws Exception
    {
        final String qualifiedPropertiesFileName = PROPERTIES_PATH + File.separator + propertiesFileName;
        Configuration configuration = new PropertiesConfiguration(ConfigurationUtils.locate(qualifiedPropertiesFileName));

        List intProperties = configuration.getList("global.int-properties");

        for (Iterator<String> iterator = configuration.getKeys(LegacyBambooServerManagerImpl.CFG_ROOT); iterator.hasNext();)
        {
            String key = iterator.next();
            if (intProperties.contains(key))
            {
                propertySet.setInt(key, configuration.getInt(key));
            }
            else
            {
                propertySet.setString(key, configuration.getString(key));
            }
        }
    }

    protected void verifyPropertySetWithProperties(final String propertiesFileName) throws Exception
    {
        final String qualifiedPropertiesFileName = PROPERTIES_PATH + File.separator + propertiesFileName;
        Configuration configuration = new PropertiesConfiguration(ConfigurationUtils.locate(qualifiedPropertiesFileName));

        Collection<String> configurationKeys = new HashSet();
        Collection<String> propertySetKeys = propertySet.getKeys(LegacyBambooServerManagerImpl.CFG_ROOT);

        // check if all entries from property file are present (and equal) in the property set
        for (Iterator<String> iterator = configuration.getKeys(LegacyBambooServerManagerImpl.CFG_ROOT); iterator.hasNext();)
        {
            String key = iterator.next();
            configurationKeys.add(key);
            assertTrue("propertyset property [" + key + "] shall exist", propertySet.exists(key));

            switch (propertySet.getType(key))
            {
                case PropertySet.INT:
                    assertEquals("INT property from config [" + key + "]", configuration.getInt(key), propertySet.getInt(key));
                    break;
                case PropertySet.STRING:
                    assertEquals("STRING property from config [" + key + "]", configuration.getString(key), StringUtils.defaultString(propertySet.getString(key)));
                    break;
            }
        }

        propertySetKeys.removeAll(configurationKeys);
    }
}
