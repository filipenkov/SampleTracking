package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

/**
 * Represents a saved filter.
 *
 * @since v5.0
 */
@XmlRootElement (name = "filter")
public class FilterBean
{
    public static final FilterBean DOC_EXAMPLE_1 = new FilterBean(
            Examples.restURI("filter/10000"),
            "10000",
            "All Open Bugs",
            "A sample filter description",
            UserBean.SHORT_DOC_EXAMPLE,
            "type = Bug and resolution is empty",
            Examples.jiraURI("secure/IssueNavigator.jspa?mode=hide&requestId=10000"),
            Examples.restURI("search?jql=type%20%3D%20Bug%20and%20resolutino%20is%20empty"),
            true);

     public static final FilterBean DOC_EXAMPLE_2 = new FilterBean(
            Examples.restURI("filter/10010"),
            "10010",
            "My issues",
            "Issues assigned to me",
            UserBean.SHORT_DOC_EXAMPLE,
            "assignee = currentUser() and resolution is empty",
            Examples.jiraURI("secure/IssueNavigator.jspa?mode=hide&requestId=10010"),
            Examples.restURI("search?jql=assignee+%3D+currentUser%28%29+and+resolution+is+empty"),
            true);

    public static final List<FilterBean> DOC_FILTER_LIST_EXAMPLE = Lists.newArrayList(DOC_EXAMPLE_1, DOC_EXAMPLE_2);

    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private UserBean owner;

    @XmlElement
    private String jql;

    @XmlElement
    private URI viewUrl;

    @XmlElement
    private URI searchUrl;

    @XmlElement
    private boolean favourite;

    public FilterBean() { }

    public FilterBean(URI self, String id, String name, String description, UserBean owner, String jql, URI viewUrl, URI searchUrl, boolean isFavourite)
    {
        this.self = self;
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.jql = jql;
        this.viewUrl = viewUrl;
        this.searchUrl = searchUrl;
        this.favourite = isFavourite;
    }

    public URI getSelf()
    {
        return self;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public UserBean getOwner()
    {
        return owner;
    }

    public String getJql()
    {
        return jql;
    }

    public URI getViewUrl()
    {
        return viewUrl;
    }

    public URI getSearchUrl()
    {
        return searchUrl;
    }

    public boolean isFavourite()
    {
        return favourite;
    }
}
