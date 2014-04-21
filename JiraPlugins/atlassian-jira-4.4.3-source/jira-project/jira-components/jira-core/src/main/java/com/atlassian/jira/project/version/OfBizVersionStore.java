/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OfBizVersionStore implements VersionStore
{
    private final OfBizDelegator delegator;

    public OfBizVersionStore(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    // Return a list of all version GVs
    public List getAllVersions()
    {
        return new ArrayList(delegator.findAll("Version", EasyList.build("sequence")));
    }

    public GenericValue createVersion(Map versionParams)
    {
        return delegator.createValue(OfBizDelegator.VERSION, versionParams);
    }

    public void storeVersion(Version version)
    {
        delegator.store(version.getGenericValue());
    }

    public void storeVersions(final Collection<Version> versions)
    {
        for (Version version : versions)
        {
            if (version != null)
            {
                storeVersion(version);
            }
        }
    }

    public GenericValue getVersion(Long id)
    {
        return delegator.findByPrimaryKey("Version", EasyMap.build("id", id));
    }

    public void deleteVersion(GenericValue versionGV)
    {
        delegator.removeValue(versionGV);
    }

}
