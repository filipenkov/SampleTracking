/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 11:47:04
 * To change this template use File | Settings | File Templates.
 */
package com.atlassian.config;

import java.util.Map;

public interface ApplicationConfiguration
{
    public void setApplicationHome(String home) throws ConfigurationException;

    public String getApplicationHome();

    public boolean isApplicationHomeValid();

    public void setProperty(Object key, Object value);

    public void setProperty(Object key, int value);

    public void setProperty(Object key, boolean value);

    public Object getProperty(Object key);

    public boolean getBooleanProperty(Object key);

    public int getIntegerProperty(Object key);

    public Object removeProperty(Object key);

    public Map getProperties();

    public void setBuildNumber(String build);

    public String getBuildNumber();

    public int getMajorVersion();

    public void setMajorVersion(int majorVersion);

    public int getMinorVersion();

    public void setMinorVersion(int minorVersion);

    public String getApplicationVersion();

    Map getPropertiesWithPrefix(String prefix);

    public boolean isSetupComplete();

    public void setSetupComplete(boolean setupComplete);

    public void setConfigurationPersister(ConfigurationPersister config);

    public void save() throws ConfigurationException;

    public void reset();

    String getSetupType();

    void setSetupType(String setupType);

    String getCurrentSetupStep();

    void setCurrentSetupStep(String currentSetupStep);

    void load() throws ConfigurationException;

    boolean configFileExists();

    void setConfigurationFileName(String configurationFileName);
}
