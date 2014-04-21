package com.atlassian.jira.admin.quicknav;

import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * <p/>
 * {@link com.atlassian.jira.admin.quicknav.SimpleLinkAliasProvider} that adds section name to the aliases for
 * given link.
 *
 * @since v4.4
 */
public class SectionNameAliasProvider implements SimpleLinkAliasProvider
{
    @Override
    public Set<String> aliasesFor(SimpleLinkSection mainSection, SimpleLink link, JiraAuthenticationContext ctx)
    {
        if (mainSection != null && isNotBlank(mainSection.getLabel()))
        {
            return ImmutableSet.of(mainSection.getLabel());
        }
        return emptySet();
    }
}
