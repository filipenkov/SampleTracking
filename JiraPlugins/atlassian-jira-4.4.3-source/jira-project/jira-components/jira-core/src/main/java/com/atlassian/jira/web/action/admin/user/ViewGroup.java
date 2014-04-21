package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.filter.SearchRequestAdminService;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.security.util.GroupToIssueSecuritySchemeMapper;
import com.atlassian.jira.security.util.GroupToNotificationSchemeMapper;
import com.atlassian.jira.security.util.GroupToPermissionSchemeMapper;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Collections;

@WebSudoRequired
public class ViewGroup extends JiraWebActionSupport
{
    private String name;
    private Group group;
    private GroupToPermissionSchemeMapper groupPermissionSchemeMapper;
    private GroupToNotificationSchemeMapper groupNotificationSchemeMapper;
    private GroupToIssueSecuritySchemeMapper groupIssueSecuritySchemeMapper;
    private final SearchRequestAdminService searchRequestService;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;
    private final UserFormatManager userFormatManager;
    private CrowdService crowdService;

    public ViewGroup(final SearchRequestAdminService searchRequestManager, final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil, final UserFormatManager userFormatManager, final PermissionSchemeManager permissionSchemeManager, final SchemePermissions schemePermissions, final NotificationSchemeManager notificationSchemeManager,
                final CrowdService crowdService)
            throws GenericEntityException
    {
        searchRequestService = searchRequestManager;
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.userFormatManager = userFormatManager;
        this.crowdService = crowdService;

        this.groupPermissionSchemeMapper = new GroupToPermissionSchemeMapper(permissionSchemeManager, schemePermissions);
        this.groupNotificationSchemeMapper = new GroupToNotificationSchemeMapper(notificationSchemeManager);

        final IssueSecuritySchemeManager securitySchemeManager = ComponentManager.getComponentInstanceOfType(IssueSecuritySchemeManager.class);
        final IssueSecurityLevelManager securityLevelManager = ComponentManager.getComponentInstanceOfType(IssueSecurityLevelManager.class);
        try
        {
            groupIssueSecuritySchemeMapper = new GroupToIssueSecuritySchemeMapper(securitySchemeManager, securityLevelManager);
        }
        catch (final GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

    }

    public String execute()
    {
        group = crowdService.getGroup(getName());
        if (group == null)
        {
            addErrorMessage("Group not found.");
        }
        return getResult();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public Group getGroup()
    {
        return group;
    }

    public Collection getPermissionSchemes(final String groupName)
    {
        if (groupPermissionSchemeMapper != null)
        {
            return groupPermissionSchemeMapper.getMappedValues(groupName);
        }
        return Collections.EMPTY_LIST;
    }

    public Collection getNotificationSchemes(final String groupName)
    {
        if (groupNotificationSchemeMapper != null)
        {
            return groupNotificationSchemeMapper.getMappedValues(groupName);
        }
        return Collections.EMPTY_LIST;
    }

    public Collection getIssueSecuritySchemes(final String groupName)
    {
        if (groupIssueSecuritySchemeMapper != null)
        {
            return groupIssueSecuritySchemeMapper.getMappedValues(groupName);
        }
        return Collections.EMPTY_LIST;
    }

    public Collection getSavedFilters(final Group group)
    {
        final Collection filters = searchRequestService.getFiltersSharedWithGroup(group);
        return filters == null ? Collections.EMPTY_LIST : filters;
    }

    public String getEscapeAmpersand(final String str)
    {
        return StringUtils.replaceAll(str, "&", "%26");
    }

    public boolean isUserAbleToDeleteGroup(final String groupName)
    {
        return globalPermissionGroupAssociationUtil.isUserAbleToDeleteGroup(getRemoteUser(), groupName);
    }

    public String getFullUserName(final String userName)
    {
        if (userName != null)
        {
            return userFormatManager.formatUser(userName, FullNameUserFormat.TYPE, "view_group");
        }
        return null;
    }
}
