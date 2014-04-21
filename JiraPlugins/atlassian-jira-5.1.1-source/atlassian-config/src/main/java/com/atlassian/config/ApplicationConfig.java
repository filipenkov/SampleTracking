package com.atlassian.config;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ApplicationConfig implements ApplicationConfiguration
{
    public static final Logger log = Logger.getLogger(ApplicationConfig.class);

    public static final boolean NULL_BOOLEAN_VALUE = false;
    public static final int NULL_INTEGER_VALUE = Integer.MIN_VALUE;
    public static final String DEFAULT_CONFIG_FILE_NAME = "atlassian-config.xml";
    public static final String DEFAULT_APPLICATION_HOME = ".";

    // @TODO Make constant on inject with spring
    private String setupStepNode    = "setupStep";
    private String setupTypeNode    = "setupType";
    private String buildNumberNode  = "buildNumber";

    private String applicationHome = DEFAULT_APPLICATION_HOME;
    private boolean homeOk = false;
    private Map properties = new TreeMap();
    private String buildNumber = "0";
    private int majorVersion = 0;
    private int minorVersion = 0;
    private boolean setupComplete = false;

    private String currentSetupStep;
    private String setupType;
    private String configurationFileName;

    protected ConfigurationPersister configurationPersister;

    public ApplicationConfig()
    {
    }

    public void reset()
    {
        homeOk = false;
        applicationHome = DEFAULT_APPLICATION_HOME;
        properties.clear();
        buildNumber = "0";
        majorVersion = 0;
        minorVersion = 0;
        setupComplete = false;
        configurationPersister = null;
    }

    public void setApplicationHome(String home) throws ConfigurationException
    {
        File homeDir = new File(home);
        if (homeDir.isDirectory() == false)
        {
            log.warn("Application home does not exist. Creating directory: " + homeDir.getAbsolutePath());
            homeOk = homeDir.mkdirs();
            if (!homeOk)
            {
                throw new ConfigurationException("Could not make directory/ies: " + homeDir.getAbsolutePath());
            }
        }
        try
        {
            this.applicationHome = homeDir.getCanonicalPath();
            homeOk = true;
        }
        catch (IOException e)
        {
            homeOk = false;
            throw new ConfigurationException("Failed to locate application home: " + home, e);
        }
    }

    public String getApplicationHome()
    {
        return applicationHome;
    }

    public boolean isApplicationHomeValid()
    {
        return homeOk;
    }

    public void setProperty(Object key, Object value)
    {
        properties.put(key, value);
    }

    public Object removeProperty(Object key)
    {
        return properties.remove(key);
    }

    public Object getProperty(Object key)
    {
        return properties.get(key);
    }

    public Map getProperties()
    {
        return properties;
    }

    public void setBuildNumber(String build)
    {
        buildNumber = build;
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public int getMajorVersion()
    {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion)
    {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion)
    {
        this.minorVersion = minorVersion;
    }

    public String getApplicationVersion()
    {
        return getMajorVersion() + "." + getMinorVersion() + " build: " + getBuildNumber();
    }

    public Map getPropertiesWithPrefix(String prefix)
    {
        Map newProps = new HashMap();
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (key.startsWith(prefix))
            {
                newProps.put(key, entry.getValue());
            }
        }
        return newProps;
    }

    public boolean isSetupComplete()
    {
        return setupComplete;
    }

    public void setSetupComplete(boolean setupComplete)
    {
        this.setupComplete = setupComplete;
    }

    public void setProperty(Object key, int value)
    {
        properties.put(key, new Integer(value));
    }

    public void setProperty(Object key, boolean value)
    {
        properties.put(key, new Boolean(value));
    }

    public boolean getBooleanProperty(Object key)
    {
        Object temp = properties.get(key);
        if (temp == null)
        {
            return NULL_BOOLEAN_VALUE;
        }
        else if (temp instanceof Boolean)
        {
            return ((Boolean) temp).booleanValue();
        }
        else
        {
            return Boolean.valueOf(temp.toString()).booleanValue();
        }
    }

    public int getIntegerProperty(Object key)
    {
        Object temp = properties.get(key);
        if (temp == null)
        {
            return NULL_INTEGER_VALUE;
        }
        else if (temp instanceof Integer)
        {
            return ((Integer) temp).intValue();
        }
        else
        {
            return Integer.valueOf(temp.toString()).intValue();
        }
    }



    public void setConfigurationPersister(ConfigurationPersister configurationPersister)
    {
        this.configurationPersister = configurationPersister;
    }

    /**
     * Support the adding of a batch of properties via a map.
     *
     * @param initalProperties
     */
    public void setInitialProperties(Map initalProperties)
    {
        properties.putAll(initalProperties);
    }

    protected String getConfigurationFileName()
    {
        return configurationFileName;
    }

    public void setConfigurationFileName(String configurationFileName)
    {
        this.configurationFileName = configurationFileName;
    }

    public String getSetupType()
    {
        return setupType;
    }

    public void setSetupType(String setupType)
    {
        this.setupType = setupType;
    }

    public String getCurrentSetupStep()
    {
        return currentSetupStep;
    }

    public void setCurrentSetupStep(String currentSetupStep)
    {
        this.currentSetupStep = currentSetupStep;
    }

    public void load() throws ConfigurationException
    {
        configurationPersister.load(getApplicationHome(), getConfigurationFileName());
        setBuildNumber(configurationPersister.getStringConfigElement(buildNumberNode));
        setSetupType(configurationPersister.getStringConfigElement(setupTypeNode));
        setCurrentSetupStep(configurationPersister.getStringConfigElement(setupStepNode));
        Map props = (Map) configurationPersister.getConfigElement(Map.class, "properties");
        getProperties().putAll(props);
    }

    public boolean configFileExists()
    {
        File file = new File(getApplicationHome(), getConfigurationFileName());
        return file.exists();
    }

    public void save() throws ConfigurationException
    {
        configurationPersister.clear();
        configurationPersister.addConfigElement(getCurrentSetupStep(), setupStepNode);
        configurationPersister.addConfigElement(getSetupType(), setupTypeNode);
        configurationPersister.addConfigElement(getBuildNumber(), buildNumberNode);
        configurationPersister.addConfigElement(getProperties(), "properties");
        configurationPersister.save(getApplicationHome(), getConfigurationFileName());
    }
}
