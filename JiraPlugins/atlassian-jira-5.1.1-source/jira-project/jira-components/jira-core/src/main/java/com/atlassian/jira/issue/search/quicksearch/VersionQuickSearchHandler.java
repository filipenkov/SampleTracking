package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.RegexpUtils;
import org.apache.commons.collections.MultiHashMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Note that this handler needs to run after the Project Handler has run.
 * <p>You can use wildcards <code>'*'</code> and <code>'?'</code> to match multiple versions.  Example:
 * <code>'2*'</code> will match versions <code>'2'</code>, <code>'2.1'</code>, <code>'2.2'</code> and <code>'2.2.2'</code>
 */
public abstract class VersionQuickSearchHandler extends PrefixedSingleWordQuickSearchHandler
{
    private final VersionManager versionManager;
    private final ProjectAwareQuickSearchHandler projectAwareQuickSearchHandler;

    public VersionQuickSearchHandler(VersionManager versionManager, ProjectManager projectManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext)
    {
        this.versionManager = versionManager;
        this.projectAwareQuickSearchHandler = new ProjectAwareQuickSearchHandlerImpl(projectManager, permissionManager, authenticationContext);
    }

    protected Map/*<String, String>*/ handleWordSuffix(String versionRegexp, QuickSearchResult searchResult)
    {
        MultiHashMap/*<String, String>*/ versions = new MultiHashMap();
        List/*<GenericValue>*/ possibleProjects = projectAwareQuickSearchHandler.getProjects(searchResult);
        for (Iterator iterator = possibleProjects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();
            versions.putAll(getSearchParamName(), getAllVersionIdsMatchingSubstring(versionManager.getVersions(project), versionRegexp));
        }
        return versions;
    }

    /**
     * Return
     * @param versions  The collection of versions to limit from
     * @param name  A name which can include wildcards (eg <code>'*'</code> or <code>'?'</code>)
     * @return  A list of versionIds (as Strings) that match this parameter
     */
    public static List getAllVersionIdsMatchingSubstring(final Collection<Version> versions, String name)
    {
        List<String> versionIdsThatMatch = new ArrayList<String>();
        Pattern p = null;
        if (name.contains("*") || name.contains("?"))
        {
            p = Pattern.compile(RegexpUtils.wildcardToRegex(name));
        }

        for (Iterator iterator = versions.iterator(); iterator.hasNext();)
        {
            Version version = (Version) iterator.next();
            String versionName = version.getName();
            if (versionName != null)
            {
                if (p != null)
                {
                    if (p.matcher(versionName).matches())
                    {
                        versionIdsThatMatch.add(version.getId().toString());
                    }
                }
                else
                {
                    // this is the old matching of version names that have spaces in it.  So searching for "2.0" would match "2.0 Enterprise" and "2.0 Professional"
                    StringTokenizer st = new StringTokenizer(versionName, " ");
                    while (st.hasMoreTokens())
                    {
                        String word = st.nextToken();
                        if (name.equalsIgnoreCase(word))
                        {
                            versionIdsThatMatch.add(version.getId().toString());
                        }
                    }
                }
            }
        }

        return versionIdsThatMatch;
    }

    protected abstract String getPrefix();

    protected abstract String getSearchParamName();
}
