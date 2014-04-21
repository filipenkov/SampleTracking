package com.atlassian.jira.user.util;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.OfbizExternalEntityStore;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides methods for counting both internal users and external users (if external user mgmt is enabled) It
 * also provides methods for updating global preferences.
 */
public class GlobalUserPreferencesUtilImpl implements GlobalUserPreferencesUtil
{
    private final OfBizDelegator ofBizDelegator;
    private final UserPreferencesManager userPreferencesManager;

    public GlobalUserPreferencesUtilImpl(OfBizDelegator ofBizDelegator, final UserPreferencesManager userPreferencesManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.userPreferencesManager = userPreferencesManager;
    }

    public long getTotalUpdateUserCountMailMimeType(String mimetype)
    {
        List<GenericValue> valExternalEntity = getEntriesForMailWithMimetype(mimetype, OfbizExternalEntityStore.ENTITY_NAME_EXTERNAL_ENTITY);
        return valExternalEntity.size();
    }

    public void updateUserMailMimetypePreference(String mimetype) throws GenericEntityException
    {
        // get all the ids of the values that need updating.
        List vals = getEntriesForMailWithMimetype(mimetype, OfbizExternalEntityStore.ENTITY_NAME_EXTERNAL_ENTITY);
        List keys = new ArrayList();
        for (Iterator iterator = vals.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            keys.add(genericValue.getLong("id"));
        }

        // update the values
        ofBizDelegator.bulkUpdateByPrimaryKey("OSPropertyString", EasyMap.build("value", mimetype), keys);

        // make sure we clear the user property cache.
        userPreferencesManager.clearCache();
    }

    public long getUserLocalePreferenceCount(String localeCode)
    {
        Collection vals = getUserLocalePreferenceEntries(localeCode);
        return vals.size();
    }

    /**
     * Retrieve a list of usernames that have selected the specified locale in the user preferences
     *
     */
    public Collection getUserLocalePreferenceList(String localeCode)
    {
        Collection userNameList = new ArrayList();
        Collection matches = getUserLocalePreferenceEntries(localeCode);
        for (Iterator iterator = matches.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            GenericValue user = ofBizDelegator.findByPrimaryKey("OSPropertyEntry", EasyMap.build("id", genericValue.getString("id")));
            GenericValue userGV = ofBizDelegator.findByPrimaryKey(OfbizExternalEntityStore.ENTITY_NAME_EXTERNAL_ENTITY, EasyMap.build("id", user.getString("entityId")));

            userNameList.add(userGV.getString("name"));
        }
        return userNameList;
    }

    private Collection getUserLocalePreferenceEntries(String localeCode)
    {
        List matches = new ArrayList();
        // JRA-8526 - this can not be as efficient as it should be cause some db's (mssql) can not do an ='s compare
        // on the propertyValue's data type
        List vals = ofBizDelegator.findByAnd("OSUserPropertySetView", EasyMap.build("propertyKey", "jira.user.locale", "entityName", OfbizExternalEntityStore.ENTITY_NAME_EXTERNAL_ENTITY));
        for (Iterator iterator = vals.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            if (localeCode.equals(genericValue.getString("propertyValue")))
            {
                matches.add(genericValue);
            }
        }
        return matches;
    }

    private List<GenericValue> getEntriesForMailWithMimetype(String mimetype, String entityType)
    {
        String updateMimetype = (mimetype.equalsIgnoreCase("html")) ? "text" : "html";
        List<GenericValue> matches = new ArrayList();
        // JRA-8526 - this can not be as efficient as it should be cause some db's (mssql) can not do an ='s compare
        // on the propertyValue's data type
        List vals = ofBizDelegator.findByAnd("OSUserPropertySetView", EasyMap.build("propertyKey", "user.notifications.mimetype", "entityName",
                entityType));
        for (Iterator iterator = vals.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            if (updateMimetype.equals(genericValue.getString("propertyValue")))
            {
                matches.add(genericValue);
            }
        }
        return matches;
    }
}
