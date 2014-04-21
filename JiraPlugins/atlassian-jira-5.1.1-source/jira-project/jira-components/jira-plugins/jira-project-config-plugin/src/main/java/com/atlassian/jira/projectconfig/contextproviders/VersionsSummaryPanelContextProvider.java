package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides the context to velocity needed to display the versions summary panel on the view project page.
 *
 * @since v4.4
 */
public class VersionsSummaryPanelContextProvider implements CacheableContextProvider
{
    private static final int MAX_VERSIONS_DISPLAYED = 5;

    private final ContextProviderUtils providerUtils;
    private final VersionService service;
    private final JiraAuthenticationContext authContext;
    private final TabUrlFactory tabUrlFactory;
    private final DateFieldFormat dateFormat;

    public VersionsSummaryPanelContextProvider(final ContextProviderUtils utils, final VersionService service,
            final JiraAuthenticationContext authContext,
            final TabUrlFactory tabUrlFactory, DateFieldFormat dateFormat)
    {
        this.providerUtils = utils;
        this.service = service;
        this.authContext = authContext;
        this.tabUrlFactory = tabUrlFactory;
        this.dateFormat = dateFormat;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
        // Nothing to do.
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Project project = providerUtils.getProject();

        final VersionService.VersionsResult result = service.getVersionsByProject(authContext.getLoggedInUser(), project);
        final List<Version> versions = result.getVersions() == null ? Collections.<Version>emptyList() : new ArrayList<Version>(result.getVersions());
        final List<SimpleVersion> simpleVersions = new ArrayList<SimpleVersion>(MAX_VERSIONS_DISPLAYED);

        Collections.reverse(versions);

        int count = 0;
        for (Version version : versions)
        {
            if (!version.isArchived())
            {
                if (count < MAX_VERSIONS_DISPLAYED)
                {
                    String releaseDate = version.getReleaseDate() == null ? null : dateFormat.format(version.getReleaseDate());
                    simpleVersions.add(new SimpleVersion(version.getName(), version.isReleased(), version.isArchived(),
                            service.isOverdue(version), releaseDate));
                }
                count++;
            }
        }

        MapBuilder<String, Object> newContext = MapBuilder.newBuilder(context)
                .add("versions", simpleVersions)
                .add("errors", providerUtils.flattenErrors(result.getErrorCollection()))
                .add("totalSize", count)
                .add("actualSize", simpleVersions.size())
                .add("manageVersionLink", tabUrlFactory.forVersions());

        return newContext.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleVersion
    {
        private final String name;
        private final String releaseDate;
        private final boolean released;
        private final boolean archived;
        private final boolean overdue;

        SimpleVersion(String name, boolean released, boolean archived, boolean overdue, String releaseDate)
        {
            this.name = name;
            this.released = released;
            this.archived = archived;
            this.overdue = overdue;
            this.releaseDate = releaseDate;
        }

        public String getName()
        {
            return name;
        }

        public boolean isReleased()
        {
            return released;
        }

        public boolean isArchived()
        {
            return archived;
        }

        public boolean isOverdue()
        {
            return overdue;
        }

        public String getReleaseDate()
        {
            return releaseDate;
        }

        public boolean isHasReleaseDate()
        {
            return releaseDate != null;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleVersion that = (SimpleVersion) o;

            if (archived != that.archived) { return false; }
            if (overdue != that.overdue) { return false; }
            if (released != that.released) { return false; }
            if (!name.equals(that.name)) { return false; }
            if (releaseDate != null ? !releaseDate.equals(that.releaseDate) : that.releaseDate != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name.hashCode();
            result = 31 * result + (releaseDate != null ? releaseDate.hashCode() : 0);
            result = 31 * result + (released ? 1 : 0);
            result = 31 * result + (archived ? 1 : 0);
            result = 31 * result + (overdue ? 1 : 0);
            return result;
        }
    }
}
