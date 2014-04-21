package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Provides velocity contexts for rendering admin header panels.
 *
 * @since v4.4
 */
public class AdminContextProvider implements CacheableContextProvider
{
    private final JiraAuthenticationContext authenticationContext;
    private final SimpleLinkManager simpleLinkManager;

    public AdminContextProvider(final JiraAuthenticationContext authenticationContext, final SimpleLinkManager simpleLinkManager)
    {
        this.authenticationContext = authenticationContext;
        this.simpleLinkManager = simpleLinkManager;
    }

    @Override
    public void init(Map<String, String> stringStringMap) throws PluginParseException
    {
        //Nothing to do.
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder(JiraVelocityUtils.getDefaultVelocityParams(context, authenticationContext));
        final JiraHelper jiraHelper = new JiraHelper(ExecutingHttpRequest.get());
        final String currentHeading = extractHeading(context, jiraHelper);
        if (currentHeading != null)
        {
            contextBuilder.add("currentHeading", currentHeading);
        }

        return contextBuilder.toMap();
    }

    //package local for testing
    String extractHeading(Map<String, Object> context, final JiraHelper helper)
    {
        final String activeSection = (String) context.get("admin.active.section");
        final String activeTab = (String) context.get("admin.active.tab");
        final String[] sections = StringUtils.split(activeSection, "/");

        String label = null;
        //if we're in a hierarchy deeper than 1 (i.e. the link in the dropdown was a grouping of other links, then show that as the heading)
        if (sections != null)
        {
            if (sections.length >= 2)
            {
                final String levelUp = sections[sections.length - 2];
                final String theSection = sections[sections.length - 1];

                final List<SimpleLinkSection> sectionItems = simpleLinkManager.getSectionsForLocation(levelUp, authenticationContext.getLoggedInUser(), helper);
                if (!sectionItems.isEmpty())
                {
                    for (SimpleLinkSection section : sectionItems)
                    {
                        if (section.getId().equals(theSection))
                        {
                            //we use the title for sections with sub-items to give us the name without the '...' at the end
                            label = section.getTitle() != null ? section.getTitle() : section.getLabel();
                            break;
                        }
                    }
                }
            }
            //Didn't find a label up in the hierarchy.  let's just get the current tab as the label!
            if(label == null && StringUtils.isNotBlank(activeTab))
            {
                final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection(activeSection, authenticationContext.getLoggedInUser(), helper);
                for (SimpleLink simpleLink : linksForSection)
                {
                    if (simpleLink.getId().equals(activeTab))
                    {
                        //we use the title for links with sub-items to give us the name without the '...' at the end
                        label = simpleLink.getTitle() != null ? simpleLink.getTitle() : simpleLink.getLabel();
                        break;
                    }
                }
            }
        }
        return label;
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }
}
