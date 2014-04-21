package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides the context to velocity needed to display the components summary panel on the view project page.
 *
 * @since v4.4
 */
public class ComponentsSummaryPanelContextProvider implements CacheableContextProvider
{
    private static final String COMPONENT_SUMMARY_ID = "component-summary-panel-lead";
    private static final int MAX_COMPONENTS_DISPLAYED = 5;

    private final ContextProviderUtils providerUtils;
    private final ProjectComponentService projectComponentService;
    private final UserFormats userFormatManager;
    private final TabUrlFactory tabUrlFactory;

    public ComponentsSummaryPanelContextProvider(ContextProviderUtils providerUtils,
            ProjectComponentService projectComponentService, UserFormats userFormatManager,
            TabUrlFactory tabUrlFactory)
    {
        this.providerUtils = providerUtils;
        this.projectComponentService = projectComponentService;
        this.userFormatManager = userFormatManager;
        this.tabUrlFactory = tabUrlFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
        // Nothing to do.
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Project project = providerUtils.getProject();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        
        final List<ProjectComponent> components = Lists.newArrayList(projectComponentService.findAllForProject(errorCollection, project.getId()));
        final List<SimpleComponent> simpleComponents = new ArrayList<SimpleComponent>(MAX_COMPONENTS_DISPLAYED);
        int totalSize = components.size();

        final UserFormat format = userFormatManager.forType(ProfileLinkUserFormat.TYPE);
        
        int count = 0;
        for (Iterator<ProjectComponent> iterator = components.iterator(); count < MAX_COMPONENTS_DISPLAYED && iterator.hasNext(); count++)
        {
            ProjectComponent component = iterator.next();

            final String userHover;
            if (component.getLead() != null)
            {
                userHover = format.format(component.getLead(), COMPONENT_SUMMARY_ID);
            }
            else
            {
                userHover = null;
            }
            simpleComponents.add(new SimpleComponent(component.getName(), userHover));
        }

        MapBuilder<String, Object> newContext = MapBuilder.newBuilder(context)
                .add("components", simpleComponents)
                .add("errors", providerUtils.flattenErrors(errorCollection))
                .add("totalSize", totalSize)
                .add("actualSize", count)
                .add("manageUrl", tabUrlFactory.forComponents());

        return newContext.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleComponent
    {
        private final String name;
        private final String userHover;

        SimpleComponent(String name, String userHover)
        {
            this.name = name;
            this.userHover = userHover;
        }

        public String getName()
        {
            return name;
        }

        public String getUserHoverHtml()
        {
            return userHover;
        }

        public boolean isHasLead()
        {
            return userHover != null;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleComponent that = (SimpleComponent) o;

            return name.equals(that.name) && !(userHover != null ? !userHover.equals(that.userHover) : that.userHover != null);

        }

        @Override
        public int hashCode()
        {
            int result = name.hashCode();
            result = 31 * result + (userHover != null ? userHover.hashCode() : 0);
            return result;
        }
    }
}
