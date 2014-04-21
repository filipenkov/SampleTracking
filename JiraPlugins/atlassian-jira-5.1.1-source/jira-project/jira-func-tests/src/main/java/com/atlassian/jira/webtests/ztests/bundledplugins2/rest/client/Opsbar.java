package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

/**
 * @since v5.0
 */
public class Opsbar
{
    private List<LinkGroup> linkGroups;

    public Opsbar()
    {
        
    }

    public List<LinkGroup> getLinkGroups()
    {
        return linkGroups;
    }

    public void setLinkGroups(List<LinkGroup> linkGroups)
    {
        this.linkGroups = linkGroups;
    }

}
