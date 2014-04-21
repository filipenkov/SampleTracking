/*
 * Copyright (c) 2002-2007
 * All rights reserved.
 */

package com.atlassian.jira.web.filters.gzip;

import com.atlassian.gzipfilter.GzipFilter;
import com.atlassian.gzipfilter.integration.GzipFilterIntegration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.jira.config.CoreFeatures.ON_DEMAND;

public class JiraGzipFilter extends GzipFilter
{
    private static final Logger log = Logger.getLogger(JiraGzipFilter.class);

    public JiraGzipFilter()
    {
        super(createGzipIntegration());
    }

    private static GzipFilterIntegration createGzipIntegration()
    {
        if (ON_DEMAND.isSystemPropertyEnabled())
        {
            return new JiraOnDemandGzipFilterIntegration();
        }

        return new JiraGzipFilterIntegration();
    }

    private static class JiraGzipFilterIntegration implements GzipFilterIntegration
    {
        public boolean useGzip()
        {
            try
            {
                // normally we would use GzipCompression here, but if we do that then we end up deadlocking inside
                // ComponentAccessor when a web request comes in and JIRA is being restarted after XML import. sooooo,
                // instead we do a bit of copy & paste to achieve the same effect.
                //
                // basically I fought the ComponentAccessor and the ComponentAccessor won.
                return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_WEB_USEGZIP);
            }
            catch (RuntimeException e)
            {
                log.debug("Cannot get application properties, defaulting to no GZip compression");
                return false;
            }
        }

        public String getResponseEncoding(HttpServletRequest httpServletRequest)
        {
            return ComponentAccessor.getApplicationProperties().getEncoding();
        }
    }

    /**
     * Forces GZIP to off in OnDemand.
     */
    private static class JiraOnDemandGzipFilterIntegration extends JiraGzipFilterIntegration
    {
        @Override
        public boolean useGzip()
        {
            return false;
        }
    }
}
