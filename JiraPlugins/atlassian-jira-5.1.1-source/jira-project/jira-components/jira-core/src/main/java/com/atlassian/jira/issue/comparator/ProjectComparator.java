package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.util.JiraKeyUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class ProjectComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        GenericValue i1 = (GenericValue) o1;
        GenericValue i2 = (GenericValue) o2;

        if (i1 == null && i2 == null)
            return 0;
        else if (i2 == null) // any value is less than null
            return -1;
        else if (i1 == null) // null is greater than any value
            return 1;

        String key1 = i1.getString("key");
        String key2 = i2.getString("key");

        return compareKeys(key1, key2);

    }

    public static int compareKeys(String key1, String key2)
    {
        if (key1 == null && key2 == null)
            return 0;
        else if (key1 == null)
            return 1;
        else if (key2 == null)
            return -1;

        String projectKey1 = JiraKeyUtils.getFastProjectKeyFromIssueKey(key1);
        String projectKey2 = JiraKeyUtils.getFastProjectKeyFromIssueKey(key2);

        // issue key may not have project key
        // data imported from Bugzilla may not comply with atlassian issue key format
        // this added to make jira more fault tolerant
        if (projectKey1 == null && projectKey2 == null)
            return 0;
        else if (projectKey1 == null)
            return 1;
        else if (projectKey2 == null)
            return -1;

        return projectKey1.compareTo(projectKey2);

    }
}
