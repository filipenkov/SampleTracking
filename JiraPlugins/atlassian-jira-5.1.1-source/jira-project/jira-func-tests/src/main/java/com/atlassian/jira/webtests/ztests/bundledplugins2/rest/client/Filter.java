package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.GenericType;

import java.util.List;

/**
 * Representation of a comment in the JIRA REST API.
 *
 * @since v4.3
 */
public class Filter
{
    public static final GenericType<List<Filter>> FILTER_TYPE = new GenericType<List<Filter>>(){};

    public String self;
    public String id;
    public String name;
    public String description;
    public User owner;
    public String jql;
    public String viewUrl;
    public String searchUrl;
    public boolean favourite;

    public Filter()
    {
    }

}
