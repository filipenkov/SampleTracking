package com.atlassian.jira.plugin.ext.bamboo.web;

import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class ViewBambooApplicationLinks extends BambooWebActionSupport
{
    public static final String JSPA_PATH = "ViewBambooApplicationLinks.jspa";
    
    public ViewBambooApplicationLinks(BambooApplicationLinkManager applicationLinkManager, WebResourceManager webResourceManager)
    {
        super(applicationLinkManager, webResourceManager);
    }
}