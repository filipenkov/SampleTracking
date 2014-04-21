package com.atlassian.jira.rest.v2.issue.component;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.rest.v2.issue.ComponentResource;
import com.atlassian.jira.util.I18nHelper;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Implementation of {@link com.atlassian.jira.rest.v2.issue.component.ComponentIssueCountsBeanFactory}.
 *
 * @since v4.4
 */
public class ComponentIssueCountsBeanFactoryImpl implements ComponentIssueCountsBeanFactory
{
    private final UriInfo info;
    private final I18nHelper helper;

    public ComponentIssueCountsBeanFactoryImpl(UriInfo info, I18nHelper helper)
    {
        //these two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
        this.helper = helper;
    }

    public ComponentIssueCountsBean createComponentBean(ProjectComponent component, long issueCount)
    {
        return new ComponentIssueCountsBean(issueCount, createSelfURI(component));
    }

    private URI createSelfURI(ProjectComponent component)
    {
        return info.getBaseUriBuilder().path(ComponentResource.class).path(component.getId().toString()).build();
    }
}
