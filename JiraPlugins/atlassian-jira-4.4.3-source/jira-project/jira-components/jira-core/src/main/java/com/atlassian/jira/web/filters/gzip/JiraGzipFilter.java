/*
 * Copyright (c) 2002-2007
 * All rights reserved.
 */

package com.atlassian.jira.web.filters.gzip;

import com.atlassian.gzipfilter.GzipFilter;
import com.atlassian.gzipfilter.integration.GzipFilterIntegration;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

public class JiraGzipFilter extends GzipFilter
{
    private static final Logger log = Logger.getLogger(JiraGzipFilter.class);

    public JiraGzipFilter()
    {
        super(new JiraGzipFilterIntegration());
    }

    private static class JiraGzipFilterIntegration implements GzipFilterIntegration
    {
        public boolean useGzip()
        {

            try
            {
                return ManagerFactory.getApplicationProperties().getOption(APKeys.JIRA_OPTION_WEB_USEGZIP);
            }
            catch (RuntimeException e)
            {
                log.debug("Cannot get application properties, defaulting to no GZip filter");
                return false;
            }
        }

        public String getResponseEncoding(HttpServletRequest httpServletRequest)
        {
            return ManagerFactory.getApplicationProperties().getEncoding();
        }
    }
}
