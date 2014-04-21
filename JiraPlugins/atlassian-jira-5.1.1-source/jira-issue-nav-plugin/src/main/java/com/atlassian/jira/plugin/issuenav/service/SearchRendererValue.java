package com.atlassian.jira.plugin.issuenav.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* Value returned by search renderer
* @since v5.1
*/
@XmlRootElement
public class SearchRendererValue
{
    @XmlElement
    public final String name;
    @XmlElement
    public final String viewHtml;
    @XmlElement
    public final String editHtml;
    @XmlElement
    public final String jql;
    @XmlElement
    public final boolean validSearcher;

    public SearchRendererValue()
    {
        this.name = null;
        this.jql = null;
        this.viewHtml = null;
        this.editHtml = null;
        this.validSearcher = false;
    }

    public SearchRendererValue(String name, String jql, String viewHtml, String editHtml, boolean validSearcher)
    {
        this.name = name;
        this.editHtml = editHtml;
        this.jql = jql;
        this.validSearcher = validSearcher;
        this.viewHtml = viewHtml;
    }
}
