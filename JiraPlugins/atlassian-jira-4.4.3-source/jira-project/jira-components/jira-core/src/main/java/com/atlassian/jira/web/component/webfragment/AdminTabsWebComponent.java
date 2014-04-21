package com.atlassian.jira.web.component.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @since v4.4
 */
public class AdminTabsWebComponent extends AbstractWebComponent
{
    public static final String SYSTEM_ADMIN_TOP_NAVIGATION_BAR = "system.admin.top.navigation.bar";
    private static final String TEMPLATE = "templates/plugins/webfragments/system-admin-tabs.vm";

    private final SimpleLinkManager linkManager;
    private final JiraAuthenticationContext jac;

    public AdminTabsWebComponent(VelocityManager velocityManager, ApplicationProperties applicationProperties,
            SimpleLinkManager linkManager, JiraAuthenticationContext jac)
    {
        super(velocityManager, applicationProperties);
        this.linkManager = linkManager;
        this.jac = jac;
    }

    public Pair<String, Integer> render(Project project, String currentSection, String currentTabId)
    {
        final Pair<List<TabGroup>, Integer> tabGroups = getTabs(project, currentSection, currentTabId);

        final MapBuilder<String, Object> context = MapBuilder.newBuilder();
        context.add("project", project);
        if (project != null)
        {
            context.add("projectKeyEncoded", encode(project.getKey()));
        }
        context.add("tabGroups", tabGroups.first());
        context.add("numberOfTabs", tabGroups.second());

        return Pair.of(getHtml(TEMPLATE, context.toMap()), tabGroups.second());
    }

    private Pair<List<TabGroup>, Integer> getTabs(Project project, String currentSectionId, String currentTabId)
    {
        final MapBuilder<String, Object> context = MapBuilder.newBuilder();
        context.add("project", project);
        if (project != null)
        {
            context.add("projectKeyEncoded", encode(project.getKey()));
        }
        final JiraHelper jiraHelper = new JiraHelper(getHttpRequest(), project, context.toMap());
        final User loggedInUser = jac.getLoggedInUser();
        final String requestURI = getHttpRequest().getRequestURI();

        //first lets find sections that match where we are in the admin section currently.  For a project this could
        //mean any sub section under 'atl.jira.proj.config'.  Otherwise we try to find a subsection with a matching
        //section key based on the provided currentSectionid.
        final List<SimpleLinkSection> linkSections = findRelevantSections(project, currentSectionId, jiraHelper, loggedInUser, requestURI);

        final List<TabGroup> groups = new ArrayList<TabGroup>(linkSections.size());

        boolean foundSelected = false;
        Tab firstTab = null;
        int tabCount = 0;

        for (final SimpleLinkSection section : linkSections)
        {
            String sectionId;

            if (project != null)
            {
                sectionId = String.format("atl.jira.proj.config/%s", section.getId());
            }
            else
            {
                sectionId = section.getId();
            }

            List<SimpleLink> linksForSection = linkManager.getLinksForSection(sectionId, loggedInUser, jiraHelper);
            if (!linkSections.isEmpty())
            {
                TabGroup group = new TabGroup();
                for (SimpleLink simpleLink : linksForSection)
                {
                    final String tabId = simpleLink.getId();

                    boolean selected = tabId != null && tabId.equalsIgnoreCase(currentTabId);

                    if (!selected && StringUtils.isBlank(currentTabId))
                    {
                        if (requestURI.endsWith(JiraUrl.extractActionFromURL(simpleLink.getUrl())))
                        {
                            selected = true;
                        }
                    }

                    foundSelected = foundSelected || selected;

                    Tab tab = new Tab(simpleLink, selected);
                    tabCount++;
                    group.addTab(tab);

                    if (firstTab == null)
                    {
                        firstTab = tab;
                    }
                }
                groups.add(group);
            }
        }
        return Pair.of(groups, tabCount);
    }

    private List<SimpleLinkSection> findRelevantSections(Project project, String currentSectionId, JiraHelper jiraHelper, User loggedInUser, String requestURI)
    {
        // hopefully this doesn't stay empty
        List<SimpleLinkSection> linkSections = Collections.emptyList();

        // if we didn't find a meta tag
        if (currentSectionId == null)
        {
            SimpleLinkSection sectionForURL = linkManager.getSectionForURL(SYSTEM_ADMIN_TOP_NAVIGATION_BAR, requestURI, loggedInUser, jiraHelper);

            if (sectionForURL != null)
            {
                linkSections = Collections.singletonList(sectionForURL);
            }
        }
        else
        {
            if (project != null)
            {
                linkSections = linkManager.getSectionsForLocation(currentSectionId, loggedInUser, jiraHelper);
            }
            else
            {
                SimpleLinkSection linkSection = getSectionFromKey(currentSectionId, loggedInUser, jiraHelper);
                if (linkSection != null)
                {
                    linkSections = Collections.singletonList(linkSection);
                }
            }
        }
        return linkSections;
    }

    private SimpleLinkSection getSectionFromKey(String currentSectionId, User loggedInUser, JiraHelper jiraHelper)
    {
        String[] cells = StringUtils.split(currentSectionId, "/");

        if (cells != null && cells.length > 1)
        {
            int bottomOfHierarchy = cells.length - 1;
            String sectionLocation = cells[bottomOfHierarchy - 1];
            String sectionKey = cells[bottomOfHierarchy];

            for (SimpleLinkSection linkSection : linkManager.getSectionsForLocation(sectionLocation, loggedInUser, jiraHelper))
            {
                //we'll only render tabs if we have a parent section and that parent section has a label to display.
                if (sectionKey.equals(linkSection.getId()) && linkSection.getLabel() != null)
                {
                    return new SimpleLinkSectionImpl(sectionLocation + "/" + sectionKey, (SimpleLinkSectionImpl) linkSection);
                }
            }
        }

        return null;
    }

    HttpServletRequest getHttpRequest()
    {
        return ExecutingHttpRequest.get();
    }

    String encode(String string)
    {
        return JiraUrlCodec.encode(string, true);
    }

    public static class TabGroup implements Iterable<Tab>
    {
        private final List<Tab> tabs = new ArrayList<Tab>();

        TabGroup()
        {
        }

        void addTab(Tab tab)
        {
            tabs.add(tab);
        }

        @Override
        public Iterator<Tab> iterator()
        {
            return tabs.iterator();
        }

        public List<Tab> getTabs()
        {
            return tabs;
        }

        public int size()
        {
            return tabs.size();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TabGroup tabGroup = (TabGroup) o;

            if (!tabs.equals(tabGroup.tabs)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return tabs.hashCode();
        }
    }

    public static class Tab
    {
        private final SimpleLink delegate;
        private boolean selected;

        Tab(SimpleLink delegate, boolean selected)
        {
            this.delegate = delegate;
            this.selected = selected;
        }

        public String getTitle()
        {
            return delegate.getTitle();
        }

        public String getLabel()
        {
            return delegate.getLabel();
        }

        public String getId()
        {
            return delegate.getId();
        }

        @NotNull
        public String getUrl()
        {
            return delegate.getUrl();
        }

        public boolean isSelected()
        {
            return selected;
        }

        void setSelected()
        {
            selected = true;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Tab tab = (Tab) o;

            if (selected != tab.selected) { return false; }
            if (!delegate.equals(tab.delegate)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = delegate.hashCode();
            result = 31 * result + (selected ? 1 : 0);
            return result;
        }
    }
}
