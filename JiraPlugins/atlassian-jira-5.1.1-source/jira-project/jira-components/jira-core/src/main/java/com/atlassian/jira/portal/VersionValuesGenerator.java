/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VersionValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(VersionValuesGenerator.class);

    public Map getValues(Map params)
    {
        GenericValue projectGV = (GenericValue) params.get("project");
        User remoteUser = (User) params.get("User");

        try
        {
            VersionManager versionManager = ComponentAccessor.getVersionManager();
            I18nHelper i18n = new I18nBean(remoteUser);

            Collection unreleasedVersions = versionManager.getVersionsUnreleased(projectGV.getLong("id"), false);
            Map unreleased = ListOrderedMap.decorate(new HashMap(unreleasedVersions.size()));
            Iterator unreleasedIter = unreleasedVersions.iterator();
            if (unreleasedIter.hasNext())
            {
                unreleased.put(new Long(-1), i18n.getText("common.filters.unreleasedversions"));
                while (unreleasedIter.hasNext())
                {
                    Version version = (Version) unreleasedIter.next();
                    unreleased.put(version.getId(), "- " + version.getName());
                }
            }

            Map released = ListOrderedMap.decorate(new HashMap(unreleasedVersions.size()));
            List releasedIter = new ArrayList(versionManager.getVersionsReleased(projectGV.getLong("id"), false));
            if (!releasedIter.isEmpty())
            {
                released.put(new Long(-2), i18n.getText("common.filters.releasedversions"));
                Collections.reverse(releasedIter);
                for (Iterator iterator = releasedIter.iterator(); iterator.hasNext();)
                {
                    Version version = (Version) iterator.next();
                    released.put(version.getId(), "- " + version.getName());
                }
            }

            int size = unreleased.size() + released.size();
            Map versions = ListOrderedMap.decorate(new HashMap(size));

            versions.putAll(unreleased);
            versions.putAll(released);
            return versions;
        }
        catch (Exception e)
        {
            log.error("Could not retrieve versions for the project: " + projectGV.getString("id"), e);
            return null;
        }
    }
}
