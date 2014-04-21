package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @since v3.13
 */
public class BackupOverviewImpl implements BackupOverview
{
    private final Map fullProjectsByKey;
    private final BackupSystemInformation backupSystemInformation;

    public BackupOverviewImpl(final BackupSystemInformation backupSystemInformation, final List backupProjects)
    {
        Null.not("backupSystemInformation", backupSystemInformation);
        Null.not("backupProjects", backupProjects);

        this.backupSystemInformation = backupSystemInformation;

        fullProjectsByKey = new ListOrderedMap();

        // Sort the projects by name so they are ordered
        Collections.sort(backupProjects, new BackupProjectNameComparator());
        for (final Iterator iterator = backupProjects.iterator(); iterator.hasNext();)
        {
            final BackupProject backupProject = (BackupProject) iterator.next();
            fullProjectsByKey.put(backupProject.getProject().getKey(), backupProject);
        }
    }

    public BackupProject getProject(final String projectKey)
    {
        return (BackupProject) fullProjectsByKey.get(projectKey);
    }

    public List /*<BackupProject>*/getProjects()
    {
        return new ArrayList(fullProjectsByKey.values());
    }

    public BackupSystemInformation getBackupSystemInformation()
    {
        return backupSystemInformation;
    }

    ///CLOVER:OFF - this will be removed before we go into production, this is just for testing
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        for (final Iterator iterator = getProjects().iterator(); iterator.hasNext();)
        {
            final BackupProject backupProject = (BackupProject) iterator.next();
            sb.append("--").append(backupProject.getProject().getKey()).append("--");
            sb.append(backupProject);
            sb.append("\n");
        }

        sb.append(backupSystemInformation);
        return sb.toString();
    }
    ///CLOVER:ON

}
