/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 15, 2002
 * Time: 11:30:01 AM
 * CVS Revision: $Revision: 1.10 $
 * Last CVS Commit: $Date: 2005/10/04 02:41:53 $
 * Author of last CVS Commit: $Author: nfaiz $
 */
package com.atlassian.core.user.preferences;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.util.PropertyUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.user.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class UserPreferences implements Preferences, Serializable
{
    private PropertySet backingPS = null;

    // stores the keys for preferences that the default values should be used
    private Set defaultKeys = null;

    public UserPreferences()
    {
        this((PropertySet) null, true);
    }

    public UserPreferences(User pUser)
    {
        this(pUser, true);
    }
    
    public UserPreferences(PropertySet propertySet)
    {
        backingPS = propertySet;
        defaultKeys = new HashSet();
    }

    public UserPreferences(PropertySet propertySet, boolean bulkload)
    {
        // Use this set even if the propertySet is null
        // This will save some operations on checking if the userPs is null before
        // looking at the default properties, and look into the default properties straight away
        defaultKeys = new HashSet();

        if (propertySet != null)
        {
            PropertySet userPs = propertySet;

            Map params = new HashMap(2);
            params.put("PropertySet", userPs);
            params.put("bulkload", new Boolean(bulkload));

            backingPS = PropertySetManager.getInstance("cached", params);
        }
    }

    public UserPreferences(User pUser, boolean bulkload)
    {
        // Use this set even if the pUser is null
        // This will save some operations on checking if the userPs is null before
        // looking at the default properties, and look into the default properties straight away
        defaultKeys = new HashSet();

        if (pUser != null)
        {
            PropertySet userPs = pUser.getPropertySet();

            Map params = new HashMap(2);
            params.put("PropertySet", userPs);
            params.put("bulkload", new Boolean(bulkload));

            backingPS = PropertySetManager.getInstance("cached", params);
        }
    }

    public long getLong(String key)
    {
        // Check if the default value should be used for this key
        if (defaultKeys.contains(key))
        {
            // If the default value is used for the key just return it
            return DefaultPreferences.getPreferences().getLong(key);
        }
        else
        {
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
                return DefaultPreferences.getPreferences().getLong(key);
            }
        }
    }

    public void setLong(String key, long i) throws AtlassianCoreException
    {
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

    public String getString(String key)
    {
        // Check if the default value should be used for this key
        if (defaultKeys.contains(key))
        {
            // If the default value is used for the key just return it
            return DefaultPreferences.getPreferences().getString(key);
        }
        else
        {
            if (backingPS != null && backingPS.exists(key))
            {
                return backingPS.getString(key);
            }
            else
            {
                // Remember that the default value for this key is used for the user
                defaultKeys.add(key);
                return DefaultPreferences.getPreferences().getString(key);
            }
        }
    }

    public void setString(String key, String value) throws AtlassianCoreException
    {
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

    public boolean getBoolean(String key)
    {
        // Check if the default value should be used for this key
        if (defaultKeys.contains(key))
        {
            // If the default value is used for the key just return it
            return DefaultPreferences.getPreferences().getBoolean(key);
        }
        else
        {
            if (backingPS != null && backingPS.exists(key))
            {
                return backingPS.getBoolean(key);
            }
            else
            {
                // Remember that the default value for this key is used for the user
                defaultKeys.add(key);
                return DefaultPreferences.getPreferences().getBoolean(key);
            }
        }
    }

    public void setBoolean(String key, boolean b) throws AtlassianCoreException
    {
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

    public boolean equals(Object o)
    {
        // NOTE: the defaultKeys is not used to determine equality of this object.
        if (this == o) return true;
        if (!(o instanceof UserPreferences)) return false;

        final UserPreferences userPreferences = (UserPreferences) o;

        if (backingPS != null ? !PropertyUtils.identical(backingPS, userPreferences.backingPS) : userPreferences.backingPS != null) return false;

        return true;
    }

    public int hashCode()
    {
        // NOTE: the defaultKeys is not used to determine the hashCode of this object.
        return (backingPS != null ? backingPS.hashCode() : 0);
    }
}
