package com.atlassian.jira.plugin.versionpanel;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.Query;
import com.opensymphony.user.User;

import java.util.Map;

/**
 * A narrowing of context from {@link BrowseProjectContext} that also includes a {@link Version}.
 *
 * @since v4.0
 */
public class BrowseVersionContextImpl extends BrowseProjectContext implements BrowseVersionContext
{
    private final Version version;

    /**
     * Constructs an instance for this class.
     *
     * @param version The version at the center of this context
     * @param user    The user browsing the version
     * @see IssueFieldConstants
     */
    public BrowseVersionContextImpl(Version version, User user)
    {
        super(user, version.getProjectObject());
        this.version = version;
    }

    /**
     * Returns a query string parameter to append to the IssueNavigator URL for field id equal to
     * {@link IssueFieldConstants#FIX_FOR_VERSIONS}. Also includes the project parameter.
     *
     * @return a query string parameter to append to the IssueNavigator URL
     */
    public String getQueryString()
    {
        final Query query = JqlQueryBuilder.newBuilder().where().fixVersion().eq(version.getName()).and().addClause(getProjectClause()).buildQuery();
        return getSearchService().getQueryString(getUser(), query);
    }

    public Map<String, Object> createParameterMap()
    {
        final Map<String, Object> map = super.createParameterMap();
        map.put("version", getVersion());
        return map;
    }

    public Version getVersion()
    {
        return version;
    }

    @Override
    public Query createQuery()
    {
        final Query superQuery = super.createQuery();
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(superQuery);
        builder.where().defaultAnd().fixVersion().eq(version.getName());
        return builder.buildQuery();
    }

    public String getContextKey()
    {
        return super.getContextKey() + "_version" + version.getId();
    }
}
