package com.atlassian.jira.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A SimpleLinkFactory that produces links to Browse Project for recently viewed project except for the current Project
 *
 * @since v4.0
 */
public class ProjectHistoryLinkFactory implements SimpleLinkFactory
{
    private final UserProjectHistoryManager userHistoryManager;
    private final I18nBean.BeanFactory beanFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private String relativeUrl = null;
    private final String adminSection = "admin_project_menu/recent_project_section";
    private Boolean showCurrentProject = false;

    private boolean isInAdminSection = false;

    public static final int MAX_RECENT_PROJECTS_TO_SHOW = 5;

    public ProjectHistoryLinkFactory(VelocityRequestContextFactory velocityRequestContextFactory, UserProjectHistoryManager userHistoryManager,
                                     I18nBean.BeanFactory beanFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.userHistoryManager = userHistoryManager;

        this.beanFactory = beanFactory;
    }

    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
        if (descriptor.getSection().equals(adminSection))
        {
            relativeUrl = "/plugins/servlet/project-config/";
            showCurrentProject = true;
            isInAdminSection = true;
        }
        else
        {
            relativeUrl = "/browse/";
            showCurrentProject = false;
            isInAdminSection = false;
        }
    }

    public List<SimpleLink> getLinks(com.opensymphony.user.User user, Map<String, Object> params)
    {

        final ProjectAction projectAction;

        if (isInAdminSection)
        {
            projectAction = ProjectAction.EDIT_PROJECT_CONFIG;
        }
        else
        {
            projectAction = ProjectAction.VIEW_ISSUES;
        }

        final List<Project> history = userHistoryManager.getProjectHistoryWithPermissionChecks(projectAction, user);

        final List<SimpleLink> links = new ArrayList<SimpleLink>();

        if (history != null && !history.isEmpty())
        {
            final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
            // Need ot ensure they contain the baseurl in case they are loaded via ajax/rest
            final String baseUrl = requestContext.getBaseUrl();

            final Project currentProject = userHistoryManager.getCurrentProject(Permissions.BROWSE, user);
            final I18nHelper i18n = beanFactory.getInstance(user);


            for (Project project : history)
            {
                if (showCurrentProject || !project.equals(currentProject))
                {
                    final Long projectId = project.getId();
                    final String name = project.getName();
                    final String key = project.getKey();

                    String iconUrl = null;
                    if (project.getGenericValue().getLong("avatar") != null)
                    {
                        final Avatar avatar = project.getAvatar();
                        iconUrl = baseUrl + "/secure/projectavatar?pid=" + projectId + "&avatarId=" + avatar.getId() + "&size=small";
                    }
                    links.add(new SimpleLinkImpl("proj_lnk_" + projectId, name + " (" + key + ")",
                            i18n.getText("tooltip.browseproject.specified", name), iconUrl, null, null,
                            baseUrl + relativeUrl + key, null));
                }
            }
        }

        return links.subList(0, Math.min(MAX_RECENT_PROJECTS_TO_SHOW, links.size()));
    }

}
