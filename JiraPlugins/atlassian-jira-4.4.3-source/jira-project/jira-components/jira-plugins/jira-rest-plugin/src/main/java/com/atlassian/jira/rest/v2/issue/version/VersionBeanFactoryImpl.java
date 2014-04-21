package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.ExecutingHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link VersionBeanFactory}.
 *
 * @since v4.4
 */
public class VersionBeanFactoryImpl implements VersionBeanFactory
{
    private final VersionService versionService;
    private final UriInfo info;
    private final DateFieldFormat dateFieldFormat;
    private final JiraAuthenticationContext authContext;
    private final SimpleLinkManager simpleLinkManager;

    public VersionBeanFactoryImpl(VersionService versionService, UriInfo info, DateFieldFormat dateFieldFormat, JiraAuthenticationContext authContext, SimpleLinkManager simpleLinkManager)
    {
        this.versionService = versionService;
        this.authContext = authContext;
        this.simpleLinkManager = simpleLinkManager;

        //these two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
        this.dateFieldFormat = dateFieldFormat;
    }

    public VersionBean createVersionBean(Version version)
    {
        return createVersionBean(version, false);
    }
    public VersionBean createVersionBean(Version version, boolean expandOps)
    {
        Assertions.notNull("version", version);

        final Date releaseDate = version.getReleaseDate();
        Boolean versionOverDue = null;
        String prettyDate = null;
        if (releaseDate != null)
        {
            if (!version.isReleased())
            {
                versionOverDue = versionService.isOverdue(version);
            }

            prettyDate = dateFieldFormat.format(releaseDate);
        }

        if (expandOps)
        {
            final VersionBean returnBean = new VersionBean(version, versionOverDue, prettyDate, createSelfURI(version), getOperations(version));
            returnBean.setExpand("operations");
            return returnBean;
        }

        return new VersionBean(version, versionOverDue, prettyDate, createSelfURI(version));
    }

    private ArrayList<SimpleLinkBean> getOperations(Version version)
    {
        HttpServletRequest httpServletRequest = ExecutingHttpRequest.get();

        User loggedInUser = authContext.getLoggedInUser();
        Project project = version.getProjectObject();
        Map<String, Object> params = MapBuilder.build("version", version, "user", loggedInUser, "project", project);
        List<SimpleLink> links = simpleLinkManager.getLinksForSection(VERSION_OPERATIONS_WEB_LOCATION, loggedInUser, new JiraHelper(httpServletRequest, project, params));

        ArrayList<SimpleLinkBean> linkBeans = new ArrayList<SimpleLinkBean>();
        for (SimpleLink link : links)
        {
            linkBeans.add(new SimpleLinkBean(link));
        }
        return linkBeans;
    }

    public List<VersionBean> createVersionBeans(Collection<? extends Version> versions)
    {
        return createVersionBeans(versions, false);
    }
    public List<VersionBean> createVersionBeans(Collection<? extends Version> versions, boolean expandOps)
    {
        Assertions.containsNoNulls("versions", versions);

        final List<VersionBean> beans = new ArrayList<VersionBean>(versions.size());
        for (Version version : versions)
        {
            beans.add(createVersionBean(version, expandOps));
        }

        return beans;
    }

    private URI createSelfURI(Version version)
    {
        return info.getBaseUriBuilder().path(VersionResource.class).path(version.getId().toString()).build();
    }
}
