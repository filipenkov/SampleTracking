package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.profile.OptionalUserProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.conditions.UserIsTheLoggedInUserCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.plugin.webresource.SuperBatchFilteringWriter;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings ({ "UnusedDeclaration" })
public class ViewProfile extends IssueActionSupport
{
    private static final String CONTENTONLY = "contentonly";

    private String name;
    private User profileUser;
    private String selectedTab;
    private static final String USER_NOT_FOUND_VIEW = "usernotfound";
    private final SimpleLinkManager simpleLinkManager;
    private final WebResourceManager webResourceManager;
    private final PluginAccessor pluginAccessor;
    private final UserPropertyManager userPropertyManager;
    protected final CrowdService crowdService;
    private final AvatarService avatarService;

    private boolean contentOnly = false;
    private boolean noTitle = false;

    private List<ViewProfilePanelModuleDescriptor> moduleDescriptors;
    private ViewProfilePanelModuleDescriptor selectedDescriptor = null;

    public ViewProfile(final UserPropertyManager userPropertyManager)
    {
        this(ComponentManager.getComponentInstanceOfType(SimpleLinkManager.class),
                ComponentManager.getComponentInstanceOfType(WebResourceManager.class),
                ComponentManager.getComponentInstanceOfType(PluginAccessor.class),
                ComponentManager.getComponentInstanceOfType(CrowdService.class), userPropertyManager,
                ComponentManager.getComponentInstanceOfType(AvatarService.class));
    }

    public ViewProfile(final SimpleLinkManager SimpleLinkManager, final WebResourceManager webResourceManager,
            final PluginAccessor pluginAccessor, final CrowdService crowdService,
            final UserPropertyManager userPropertyManager, AvatarService avatarService)
    {
        super();
        simpleLinkManager = SimpleLinkManager;
        this.webResourceManager = webResourceManager;
        this.pluginAccessor = pluginAccessor;
        this.crowdService = crowdService;
        this.userPropertyManager = userPropertyManager;
        this.avatarService = avatarService;

        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.userprofile"
        webResourceManager.requireResourcesForContext("atl.userprofile");
        webResourceManager.requireResourcesForContext("jira.userprofile");
    }

    protected String doExecute() throws Exception
    {
        if (getLoggedInUser() == null)
        {
            if (contentOnly)
            {
                ServletActionContext.getResponse().setStatus(401);
                return NONE;
            }

            return "securitybreach";
        }

        if (getUser() == null)
        {
            if (contentOnly)
            {
                ServletActionContext.getResponse().setStatus(401);
                return NONE;
            }

            return USER_NOT_FOUND_VIEW;
        }

        if (contentOnly)
        {
            String specifiedTab = selectedTab;
            if (StringUtils.isBlank(specifiedTab))
            {
                specifiedTab = (String) getSession().get(SessionKeys.VIEW_PROFILE_TAB);
            }
            if (canSeeTab(specifiedTab))
            {
                return CONTENTONLY;
            }
            else
            {
                ServletActionContext.getResponse().setStatus(401);
                return NONE;                
            }
        }

        webResourceManager.requireResource("jira.webresources:userprofile");

        return super.doExecute();
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public void setContentOnly(boolean contentOnly)
    {
        this.contentOnly = contentOnly;
    }

    public boolean isNoTitle()
    {
        final String NO_TITLE = "noTitle";
        boolean descriptorNoTitle = getSelectedProfilePanelDescriptor().getParams().containsKey(NO_TITLE) && "true".equalsIgnoreCase(getSelectedProfilePanelDescriptor().getParams().get(NO_TITLE));

        return noTitle || descriptorNoTitle;
    }

    public void setNoTitle(boolean noTitle)
    {
        this.noTitle = noTitle;
    }

    public String getAvatarUrl(User user)
    {
        if(user != null)
        {
            return avatarService.getAvatarURL(getLoggedInUser(), user.getName()).toString();
        }
        return null;
    }

    public User getUser()
    {
        if (profileUser == null)
        {
            if (name == null)
            {
                profileUser = getLoggedInUser();
            }
            else
            {
                profileUser = crowdService.getUser(name);
            }
        }
        return profileUser;
    }

    public ViewProfilePanelModuleDescriptor getSelectedProfilePanelDescriptor()
    {
        if (selectedDescriptor == null)
        {
            final String selected = getSelectedTab();
            for (ViewProfilePanelModuleDescriptor descriptor : getTabDescriptors())
            {
                if (descriptor.getCompleteKey().equals(selected))
                {
                    selectedDescriptor = descriptor;
                    return selectedDescriptor;
                }
            }
        }
        return selectedDescriptor;
    }

    public String getLabelForSelectedTab()
    {
        final ViewProfilePanelModuleDescriptor moduleDescriptor = getSelectedProfilePanelDescriptor();

        return moduleDescriptor.getName();
    }

    public String getHtmlForSelectedTab()
    {
        final ViewProfilePanelModuleDescriptor moduleDescriptor = getSelectedProfilePanelDescriptor();

        final String tabHtml = moduleDescriptor.getModule().getHtml(profileUser);
        final StringBuilder strBuilder = new StringBuilder();

        if (contentOnly)
        {
            if (!isNoTitle())
            {
                strBuilder.append("<h2 id=\"up-tab-title\">");
                strBuilder.append(getLabelForSelectedTab());
                strBuilder.append("</h2>\n");
            }

            strBuilder.append(tabHtml);

            // Put this after the HTML so that we don't have to come up with crazy css
            // selector to skip over resources that may or may not get dynamically injected
            // into the page.
            final SuperBatchFilteringWriter writer = new SuperBatchFilteringWriter();
            webResourceManager.includeResources(writer, UrlMode.AUTO);
            strBuilder.append(writer.toString());

        }
        else
        {
            strBuilder.append(tabHtml);
        }

        return strBuilder.toString();
    }

    public boolean isHasMoreThanOneProfileTabs()
    {
        return getTabDescriptors().size() > 1;
    }

    public String getSelectedTab()
    {
        // Set the default selectedTab if none has been specified
        if (selectedTab == null)
        {
            String sessionSelectedTab = (String) getSession().get(SessionKeys.VIEW_PROFILE_TAB);
            if (sessionSelectedTab != null && canSeeTab(sessionSelectedTab))
            {
                selectedTab = sessionSelectedTab;
            }
            else
            {
                for (ViewProfilePanelModuleDescriptor descriptor : getTabDescriptors())
                {
                    selectedTab = descriptor.getCompleteKey();
                    break;
                }
            }
        }
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab)
    {
        getSession().put(SessionKeys.VIEW_PROFILE_TAB, selectedTab);
    }

    private boolean canSeeTab(String key)
    {
        for (ViewProfilePanelModuleDescriptor descriptor : getTabDescriptors())
        {
            if (descriptor.getCompleteKey().equals(key))
            {
                return true;
            }
        }

        return false;
    }

    public List<ViewProfilePanelModuleDescriptor> getTabDescriptors()
    {
        if (moduleDescriptors == null)
        {
            final List<ViewProfilePanelModuleDescriptor> allDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(ViewProfilePanelModuleDescriptor.class);

            moduleDescriptors = new ArrayList<ViewProfilePanelModuleDescriptor>();

            for (ViewProfilePanelModuleDescriptor descriptor : allDescriptors)
            {
                final ViewProfilePanel profilePanel = descriptor.getModule();
                if (profilePanel instanceof OptionalUserProfilePanel)
                {
                    final OptionalUserProfilePanel optionalPanel = (OptionalUserProfilePanel) profilePanel;
                    if (optionalPanel.showPanel(getUser(), getLoggedInUser()))
                    {
                        moduleDescriptors.add(descriptor);
                    }
                }
                else
                {
                    moduleDescriptors.add(descriptor);
                }
            }
            Collections.sort(moduleDescriptors, ModuleDescriptorComparator.COMPARATOR);
        }

        return moduleDescriptors;

    }

    public List<SimpleLinkSection> getSectionsForMenu()
    {
        final List<SimpleLinkSection> sections = new ArrayList<SimpleLinkSection>();
        sections.add(new SimpleLinkSectionImpl("operations", getText("common.concepts.tools"), null, null, "icon-tools", null));

        return sections;
    }

    public List<SimpleLink> getSectionLinks(String key)
    {
        final User remoteUser = getLoggedInUser();
        final HttpServletRequest servletRequest = ServletActionContext.getRequest();
        servletRequest.setAttribute(UserIsTheLoggedInUserCondition.PROFILE_USER, getUser());

        final Map<String, Object> params = MapBuilder.<String, Object>build(UserIsTheLoggedInUserCondition.PROFILE_USER, getUser());

        final JiraHelper helper = new JiraHelper(servletRequest, null, params);

        return simpleLinkManager.getLinksForSection("system.user.profile.links/" + key, remoteUser, helper);
    }

    private Map getSession()
    {
        return ActionContext.getSession();
    }
}
