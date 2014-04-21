package com.atlassian.jira.user.preferences;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.core.util.PropertyUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.HashSet;
import java.util.Set;

/**
 * This is the JIRA version of atlassian-core UserPreferences. The difference is where we get the default values from.
 * It was decided to re-implement here as changing atlassian-core and updating dependencies etc was too hard.
 */
public class JiraUserPreferences implements Preferences //extends UserPreferences 
{
    private Supplier<PropertySet> backingPSSupplier = Suppliers.ofInstance(null);
    // stores the keys for preferences that the default values should be used
    private final Set<String> defaultKeys = new HashSet<String>();

    public JiraUserPreferences()
    {
    }

    public JiraUserPreferences(final User pUser)
    {
        if (pUser != null)
        {
            backingPSSupplier = new Supplier<PropertySet>()
            {
                @Override
                public PropertySet get()
                {
                    return ComponentAccessor.getUserPropertyManager().getPropertySet(pUser);
                }
            };
        }
    }

    public JiraUserPreferences(PropertySet userPs)
    {
        if (userPs != null)
        {
            // JRA-16762 - there was a bug that existed with cloning the users property set, where when client
            // code updated the property set directly we could not see the change. We were winning nothing by
            // creating a clone (the user property set is cached to begin with). We will now keep the reference
            // to the actual user property set so we will see any modifications that may occur.
            backingPSSupplier = Suppliers.ofInstance(userPs);
        }
    }

    public long getLong(String key)
    {
        // Check if the default value should be used for this key
        if (defaultKeys.contains(key))
        {
            // If the default value is used for the key just return it
            return Long.parseLong(getApplicationProperties().getDefaultBackedString(key));
        }
        else
        {
            PropertySet backingPS = backingPSSupplier.get();
            if (backingPS != null && backingPS.exists(key))
            {
                return backingPS.getLong(key);
            }
            else
            {
                // Remember that the default value for this key is used for the user
                // So that we do not have look it up again
                defaultKeys.add(key);
                // Return the default value
                return Long.parseLong(getApplicationProperties().getDefaultBackedString(key));
            }
        }
    }

    public String getString(String key)
    {
        // Check if the default value should be used for this key
        if (defaultKeys.contains(key))
        {
            // If the default value is used for the key just return it
            return getApplicationProperties().getDefaultBackedString(key);
        }
        else
        {
            PropertySet backingPS = backingPSSupplier.get();
            if (backingPS != null && backingPS.exists(key))
            {
                return backingPS.getString(key);
            }
            else
            {
                defaultKeys.add(key);
                return getApplicationProperties().getDefaultBackedString(key);
            }
        }
    }

    public boolean getBoolean(String key)
    {
        // Check if the default value should be used for this key
        if (defaultKeys.contains(key))
        {
            // If the default value is used for the key just return it
            return getApplicationProperties().getOption(key);
        }
        else
        {
            PropertySet backingPS = backingPSSupplier.get();
            if (backingPS != null && backingPS.exists(key))
            {
                return backingPS.getBoolean(key);
            }
            else
            {
                // Remember that the default value for this key is used for the user
                defaultKeys.add(key);
                return getApplicationProperties().getOption(key);
            }
        }
    }

    public void setLong(String key, long i) throws AtlassianCoreException
    {
        PropertySet backingPS = backingPSSupplier.get();
        if (backingPS == null)
        {
            throw new AtlassianCoreException("Trying to set a property on a null user this is not allowed");
        }
        else
        {
            // Do not use the default value (if one was not used before, it does not matter)
            defaultKeys.remove(key);
            backingPS.setLong(key, i);
        }
    }

    public void setString(String key, String value) throws AtlassianCoreException
    {
        PropertySet backingPS = backingPSSupplier.get();
        if (backingPS == null)
        {
            throw new AtlassianCoreException("Trying to set a property on a null user this is not allowed");
        }
        else
        {
            // Do not use the default value (if one was not used before, it does not matter)
            defaultKeys.remove(key);
            backingPS.setString(key, value);
        }
    }

    public void setBoolean(String key, boolean b) throws AtlassianCoreException
    {
        PropertySet backingPS = backingPSSupplier.get();
        if (backingPS == null)
        {
            throw new AtlassianCoreException("Trying to set a property on a null user this is not allowed");
        }
        else
        {
            // Do not use the default value (if one was not used before, it does not matter)
            defaultKeys.remove(key);
            backingPS.setBoolean(key, b);
        }
    }

    public void remove(String key) throws AtlassianCoreException
    {
        PropertySet backingPS = backingPSSupplier.get();
        if (backingPS == null)
        {
            throw new AtlassianCoreException("Trying to remove a property on a null user this is not allowed");
        }
        else
        {
            if (backingPS.exists(key))
            {
                // Do not use the default value (if one was not used before, it does not matter)
                defaultKeys.remove(key);
                backingPS.remove(key);
            }
            else
            {
                throw new AtlassianCoreException("The property with key '" + key + "' does not exist.");
            }
        }
    }

    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    public boolean equals(Object o)
    {
        // NOTE: the defaultKeys is not used to determine equality of this object.
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof JiraUserPreferences))
        {
            return false;
        }

        final JiraUserPreferences jiraUserPreferences = (JiraUserPreferences) o;
        PropertySet backingPS = backingPSSupplier.get();
        if (backingPS == null)
        {
            return jiraUserPreferences.backingPSSupplier.get() == null;
        }
        else
        {
            return PropertyUtils.identical(backingPS, jiraUserPreferences.backingPSSupplier.get());
        }
    }

    public int hashCode()
    {
        // Note that this method was copied from jira-core's UserPreferences and is not likely to work correctly in all
        // possible scenarios. Firstly it is reliant on the backing property set implementing  hashCode() correctly.
        // Secondly, in theory 2 of these objects could be using different implementations of PropertySet whose
        // hashCode()'s are incompatible.
        // Having said that, this is the way the logic has been for a while, so I guess it is maybe not required?


        // NOTE: the defaultKeys is not used to determine the hashCode of this object.
        PropertySet backingPS = backingPSSupplier.get();
        return (backingPS != null ? backingPS.hashCode() : 0);
    }
}
