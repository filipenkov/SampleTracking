package com.atlassian.applinks.host.util;

import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Implements the Atlassian Name Generation Algorithm(TM) (taking the first
 * character of each part of the hostname -- like JAC for jira.atlassian.com).
 *
 * @since   3.0
 */
public class InstanceNameGenerator
{
    /**
     * @param baseURL the base url to generate an instance name from
     * @return an abbreviated instance 'name' generated from the base url
     * @throws MalformedURLException    when the specified URL is not a valid
     * URL string.
     */
    public String generateInstanceName(final String baseURL) throws MalformedURLException
    {
        String hostname = new URL(baseURL).getHost();
        if ("localhost".equals(hostname))
        {
            try
            {
                hostname = InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException se)
            {
                // ignore
            }
        }
        return StringUtils.split(hostname, ".")[0];
    }
}
