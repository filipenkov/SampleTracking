package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 */
@WebSudoRequired
public class ViewUserProjectRoles extends JiraWebActionSupport
{
    protected ProjectManager projectManager;
    protected ProjectRoleService projectRoleService;
    protected ProjectFactory projectFactory;
    private CrowdService crowdService;
    protected String name;
    private HashMap projectsByCategory;
    protected Collection currentVisibleProjects;
    private Collection projectRoles;
    private Map visibleProjectsByCategory;
    private User projectRoleEditUser;
    private Map projectsUserInForByProjectRole;
    private ProjectRoleGroupsProjectMapper projectRoleGroupsProjectMapper;

    public ViewUserProjectRoles(ProjectManager projectManager, ProjectRoleService projectRoleService, ProjectFactory projectFactory,
                final CrowdService crowdService)
    {
        this.projectManager = projectManager;
        this.projectRoleService = projectRoleService;
        this.projectFactory = projectFactory;
        this.crowdService = crowdService;
    }

    public Collection getAllProjectRoles()
    {
        if (projectRoles == null)
        {
            projectRoles = projectRoleService.getProjectRoles(getRemoteUser(), this);
        }
        return projectRoles;
    }

    public Collection getAllProjectCategories()
    {
        return projectManager.getProjectCategories();
    }

    public Collection getAllProjectsForCategory(GenericValue projectCategory)
    {
        Collection projects = (Collection) getProjectsByCategory().get(projectCategory.getLong("id"));
        if (projects == null)
        {
            projects = projectFactory.getProjects(projectManager.getProjectsFromProjectCategory(projectCategory));
            getProjectsByCategory().put(projectCategory.getLong("id"), projects);
        }
        return projects;
    }

    public boolean isUserInProjectRoleTypeUser(ProjectRole projectRole, Project project)
    {
        if (projectsUserInForByProjectRole == null)
        {
            projectsUserInForByProjectRole = new HashMap();
        }

        List projectsUserInForProjectRole = (List) projectsUserInForByProjectRole.get(projectRole.getId());

        // We only ever want to perform one database query for each projectRole so we get back all the projects for
        // a given role that the userToEdit is a member of.
        if (projectsUserInForProjectRole == null)
        {
            projectsUserInForProjectRole = projectRoleService.roleActorOfTypeExistsForProjects(getRemoteUser(), getVisibleProjectIds(), projectRole, UserRoleActorFactory.TYPE, name, this);
            projectsUserInForByProjectRole.put(projectRole.getId(), projectsUserInForProjectRole);
        }
        return projectsUserInForProjectRole.contains(project.getId());
    }

    public String getUserInProjectRoleOtherType(ProjectRole projectRole, Project project)
    {
        return getRoleMapper().getGroupNameString(projectRole, project);
    }

    public boolean isRoleForProjectSelected(ProjectRole role, Project project)
    {
        Map parameters = ActionContext.getParameters();
        String paramKey = project.getId() + "_" + role.getId();
        String paramKeyOrig = paramKey + "_orig";
        if (parameters.containsKey(paramKeyOrig))
        {
            String[] newValueParam = (String[]) parameters.get(paramKey);
            return newValueParam != null;
        }
        else
        {
            return isUserInProjectRoleTypeUser(role, project);
        }
    }

    public Collection getCurrentVisibleProjects()
    {
        if (currentVisibleProjects == null)
        {
            currentVisibleProjects = getRoleMapper().getProjects();
        }

        return currentVisibleProjects;
    }

    private ProjectRoleGroupsProjectMapper getRoleMapper()
    {
        if (projectRoleGroupsProjectMapper == null)
        {
            projectRoleGroupsProjectMapper = new ProjectRoleGroupsProjectMapper();
        }
        return projectRoleGroupsProjectMapper;
    }

    public Map getVisibleProjectsByCategory()
    {
        if (visibleProjectsByCategory == null)
        {
            visibleProjectsByCategory = new TreeMap(OfBizComparators.NAME_COMPARATOR);
            for (Iterator iterator = getCurrentVisibleProjects().iterator(); iterator.hasNext();)
            {
                Project project = (Project) iterator.next();
                List projects = (List) visibleProjectsByCategory.get(project.getProjectCategory());
                if (projects == null)
                {
                    projects = new ArrayList();
                    visibleProjectsByCategory.put(project.getProjectCategory(), projects);
                }
                projects.add(project);
            }
            for (Iterator i = visibleProjectsByCategory.values().iterator(); i.hasNext();)
            {
                List list = (List) i.next();
                Collections.sort(list, ProjectNameComparator.COMPARATOR);
            }
        }
        return visibleProjectsByCategory;
    }

    public int getProjectRoleColumnWidth()
    {
        return 75 / getAllProjectRoles().size();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public User getProjectRoleEditUser()
    {
        if (projectRoleEditUser == null)
        {
            projectRoleEditUser = crowdService.getUser(name);
        }
        return projectRoleEditUser;
    }

    private List getVisibleProjectIds()
    {
        List visibleProjectIds = new ArrayList();
        for (Iterator iterator = getCurrentVisibleProjects().iterator(); iterator.hasNext();)
        {
            Project project = (Project) iterator.next();
            visibleProjectIds.add(project.getId());
        }
        return visibleProjectIds;
    }

    private HashMap getProjectsByCategory()
    {
        if (projectsByCategory == null)
        {
            projectsByCategory = new HashMap();
        }
        return projectsByCategory;
    }

    public String doDefault() throws Exception
    {
        super.doDefault();
        User user = getProjectRoleEditUser();
        if (user == null)
        {
            this.addErrorMessage("No user exists");
            return ERROR;

        }
        return SUCCESS;
    }

    public String getReturnUrl()
    {
        String returnUrl = super.getReturnUrl();
        if (StringUtils.isEmpty(returnUrl))
        {
            return "ViewUser.jspa";
        }
        return returnUrl;
    }

    private class ProjectRoleGroupsProjectMapper
    {
        // map the roleID to a map of projectIDs that map to a lisrt of group names
        private final Map /*<Long, MultiMap<Long, List<String>>>*/ roleProjectMap = new HashMap(getAllProjectRoles().size());
        private final Set /*<Long>*/ projects = new HashSet();

        ProjectRoleGroupsProjectMapper()
        {
            Set /*<Long>*/ ids = new HashSet();
            for (Iterator iterator = getAllProjectRoles().iterator(); iterator.hasNext();)
            {
                ProjectRole projectRole = (ProjectRole) iterator.next();
                // We don't want to initialize the groups by project if the user has already defined the list of
                // visible projects.
                List projectIds = (currentVisibleProjects == null) ? Collections.EMPTY_LIST : getCurrentVisibleProjectIds();
                Map groupsByProject = projectRoleService.getProjectIdsForUserInGroupsBecauseOfRole(getRemoteUser(), projectIds, projectRole, GroupRoleActorFactory.TYPE, name, ViewUserProjectRoles.this);
                roleProjectMap.put(projectRole.getId(), groupsByProject);
                ids.addAll(groupsByProject.keySet());
            }

            if (!ids.isEmpty())
            {
                projects.addAll(projectFactory.getProjects(projectManager.convertToProjects(ids)));
            }
            projects.addAll(projectRoleService.getProjectsContainingRoleActorByNameAndType(getRemoteUser(), name, UserRoleActorFactory.TYPE, ViewUserProjectRoles.this));
        }

        private List getCurrentVisibleProjectIds()
        {
            List currentVisibleProjectIds = new ArrayList();
            for (Iterator iterator = currentVisibleProjects.iterator(); iterator.hasNext();)
            {
                Project project = (Project) iterator.next();
                currentVisibleProjectIds.add(project.getId());
            }
            return currentVisibleProjectIds;
        }

        String getGroupNameString(ProjectRole projectRole, Project project)
        {
            Collection groupNamesUserInForProject = getGroupNames(projectRole.getId(), project.getId());

            if (groupNamesUserInForProject != null && !groupNamesUserInForProject.isEmpty())
            {
                return getListAsString(groupNamesUserInForProject);
            }
            return null;
        }

        Collection getGroupNames(Long roleId, Long projectId)
        {
            Map groupNamesByProject = (Map) roleProjectMap.get(roleId);
            if (groupNamesByProject == null)
            {
                return Collections.EMPTY_LIST;
            }
            return (Collection) groupNamesByProject.get(projectId);
        }

        Collection /*<Project>*/ getProjects()
        {
            return projects;
        }

        String getListAsString(Collection groupNamesUserInForProject)
        {
            StringBuffer groups = new StringBuffer();
            for (Iterator iterator = groupNamesUserInForProject.iterator(); iterator.hasNext();)
            {
                groups.append((String) iterator.next());
                if (iterator.hasNext())
                {
                    groups.append(", ");
                }
            }
            return groups.toString();
        }
    }
}
