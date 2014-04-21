package com.atlassian.jira.admin.quicknav;

import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Set;

/**
 * Provides localized aliases for JIRA simple links, that can be used to match given link in UI in addition to the label.
 *
 * @since v4.4
 */
public interface SimpleLinkAliasProvider
{
    /**
     * <p/>
     * Get aliases for given simple link in given context.
     *
     * <p/>
     * If given link is not supported in given context, an empty set should be returned.
     *
     * @param mainSection main section of the link, may be <code>null</code>
     * @param link link
     * @param ctx authentication context
     * @return set of aliases for given link, or empty set if link not supported by this provider
     */
    Set<String> aliasesFor(SimpleLinkSection mainSection, SimpleLink link, JiraAuthenticationContext ctx);
}
