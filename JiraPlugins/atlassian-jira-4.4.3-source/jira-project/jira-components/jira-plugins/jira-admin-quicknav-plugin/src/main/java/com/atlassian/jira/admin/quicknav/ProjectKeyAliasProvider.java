package com.atlassian.jira.admin.quicknav;

import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p/>
 * Provides alias for simple links to projects containing their name/key.
 *
 * @since v4.4
 */
public class ProjectKeyAliasProvider implements SimpleLinkAliasProvider
{
    private static final String PROJECT_LINK_ID_PREFIX = "proj_lnk_";

    private static final Pattern REVERSE_ENGINEER = Pattern.compile("(.*)\\((.*)\\)");

    @Override
    public Set<String> aliasesFor(SimpleLinkSection section, SimpleLink link, JiraAuthenticationContext ctx)
    {
        if (link.getId() != null && link.getId().startsWith(PROJECT_LINK_ID_PREFIX))
        {
            final String label = link.getLabel();
            if (label != null)
            {
                final Matcher matcher = REVERSE_ENGINEER.matcher(link.getLabel());
                if (matcher.matches())
                {
                    return ImmutableSet.of(matcher.group(1), matcher.group(2));
                }
            }
        }
        return Collections.emptySet();
    }
}
