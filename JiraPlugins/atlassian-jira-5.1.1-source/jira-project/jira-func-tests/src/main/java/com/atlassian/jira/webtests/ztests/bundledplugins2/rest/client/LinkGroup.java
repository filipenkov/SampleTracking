package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

public class LinkGroup
{
    private String id;
    private SimpleLink header;
    private List<SimpleLink> links;
    private List<LinkGroup> groups;

    public LinkGroup()
    {

    }

    public String getId()
    {
        return id;
    }

    public SimpleLink getHeader()
    {
        return header;
    }

    public List<SimpleLink> getLinks()
    {
        return links;
    }

    public List<LinkGroup> getGroups()
    {
        return groups;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setHeader(SimpleLink header)
    {
        this.header = header;
    }

    public void setLinks(List<SimpleLink> links)
    {
        this.links = links;
    }

    public void setGroups(List<LinkGroup> groups)
    {
        this.groups = groups;
    }
}
