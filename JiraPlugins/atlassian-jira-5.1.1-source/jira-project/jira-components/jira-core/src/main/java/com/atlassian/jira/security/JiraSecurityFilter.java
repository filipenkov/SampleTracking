package com.atlassian.jira.security;

import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.seraph.filter.SecurityFilter;
import org.apache.log4j.Logger;

import javax.servlet.FilterConfig;

/**
 * A wrapper around the Seraph SecurityFilter.
 */
public class JiraSecurityFilter extends SecurityFilter
{
    private static final Logger log = Logger.getLogger(JiraSecurityFilter.class);

    public void init(FilterConfig config)
    {
        log.debug("Initing JIRA security filter");
        if (JiraStartupChecklist.startupOK())
        {
            super.init(config);
        }
        log.debug("JIRA security filter inited");
    }
}
