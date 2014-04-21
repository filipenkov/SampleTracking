package com.atlassian.jira.plugin.ext.bamboo.service;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Helper class to transform Bamboo HTML into JIRA friendly HTML
 */
public class BambooContentRewriter
{
    private static final Logger log = Logger.getLogger(BambooContentRewriter.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public String rewriteHtml(String relativeUrl, String serverBaseUrl)
    {
        if(relativeUrl == null || serverBaseUrl == null)
        {
            return null;
        }
        
        String context = getContextPathWithSlash(serverBaseUrl);

        if (!serverBaseUrl.endsWith("/"))
        {
            serverBaseUrl = serverBaseUrl + "/";
        }

        relativeUrl = relativeUrl.replaceAll("src=\"" + context, "src=\"" + serverBaseUrl);
        relativeUrl = relativeUrl.replaceAll("href=\"" + context, "href=\"" + serverBaseUrl);
        return relativeUrl;
    }


    String getContextPathWithSlash(String host)
    {
        try
        {
            URI hostURI = new URI(host, false);
            final String escapedPath = StringUtils.trimToEmpty(hostURI.getEscapedPath());
            if (!escapedPath.endsWith("/"))
            {
                return escapedPath + "/";
            }
            else
            {
                return escapedPath;
            }
        }
        catch (URIException e)
        {
            final String message = "Unable to parse URL " + host;
            log.error(message, e);
            throw new IllegalArgumentException(message);
        }
    }


    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators


}
