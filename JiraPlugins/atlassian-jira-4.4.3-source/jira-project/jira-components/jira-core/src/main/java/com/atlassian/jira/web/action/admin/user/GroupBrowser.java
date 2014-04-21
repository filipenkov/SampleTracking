/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.util.GroupToPermissionSchemeMapper;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.AbstractBrowser;
import com.atlassian.jira.web.bean.GroupBrowserFilter;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;
import webwork.util.BeanUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class GroupBrowser extends AbstractBrowser
{
    private List groups;
    private String addName;
    private GroupToPermissionSchemeMapper groupPermissionSchemeMapper;
    private ApplicationProperties applicationProperties;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;
    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;

    public GroupBrowser(GroupToPermissionSchemeMapper groupToPermissionSchemeMapper, ApplicationProperties applicationProperties, GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil,
            CrowdService crowdService, CrowdDirectoryService crowdDirectoryService)
    {
        this.applicationProperties = applicationProperties;
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        if (groupToPermissionSchemeMapper != null)
        {
            this.groupPermissionSchemeMapper = groupToPermissionSchemeMapper;
        }
        else
        {
            addErrorMessage(getText("groupbrowser.error.retrieve.group"));
        }
    }

    public GroupBrowser(CrowdDirectoryService crowdDirectoryService) throws GenericEntityException
    {
        this(new GroupToPermissionSchemeMapper(
                        ManagerFactory.getPermissionSchemeManager(),
                        ComponentManager.getInstance().getSchemePermissions()),
                ComponentAccessor.getApplicationProperties(),
                ComponentManager.getComponentInstanceOfType(GlobalPermissionGroupAssociationUtil.class),
                ComponentManager.getComponentInstanceOfType(CrowdService.class), crowdDirectoryService);
    }

    protected String doExecute() throws Exception
    {
        resetPager();

        BeanUtil.setProperties(params, getFilter());

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAdd() throws Exception
    {
        if (! addNewGroup())
        {
            return ERROR;
        }
        return doExecute();
    }

    private boolean addNewGroup()
    {
        if (org.apache.commons.lang.StringUtils.isEmpty(addName))
        {
            addError("addName", getText("admin.errors.cannot.add.groups.invalid.group.name"));
            return false;
        }

        //JRA-12112: If external *user* management is enabled, we do not allow the addtion of a new user.
        if(getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            addErrorMessage(getText("admin.errors.cannot.add.groups.external.managment", getJiraContactHelper().getAdministratorContactMessage(getI18nHelper())));
            return false;
        }

        if (crowdService.getGroup(addName) == null)
        {
            try
            {
                crowdService.addGroup(new ImmutableGroup(addName));
            }
            catch (OperationNotPermittedException e)
            {
                addError("addName", getText("groupbrowser.error.add", addName));
                log.error("Error occurred adding group : " + addName, e);
            }
            catch (InvalidGroupException e)
            {
                addError("addName", getText("groupbrowser.error.add", addName));
                log.error("Error occurred adding group : " + addName, e);
            }
            catch (OperationFailedException e)
            {
                addError("addName", getText("groupbrowser.error.add", addName));
                log.error("Error occurred adding group : " + addName, e);
            }
            addName = null;
        }
        else
        {
            addError("addName", getText("groupbrowser.error.group.exists"));
        }
        return true;
    }

    public PagerFilter getPager()
    {
        return getFilter();
    }

    public void resetPager()
    {
        ActionContext.getSession().put(SessionKeys.GROUP_FILTER, null);
    }

    public GroupBrowserFilter getFilter()
    {
        GroupBrowserFilter filter = (GroupBrowserFilter) ActionContext.getSession().get(SessionKeys.GROUP_FILTER);

        if (filter == null)
        {
            filter = new GroupBrowserFilter();
            ActionContext.getSession().put(SessionKeys.GROUP_FILTER, filter);
        }

        return filter;
    }

    /**
     * Return the current 'page' of issues (given max and start) for the current filter
     */
    public List getCurrentPage()
    {
        return getFilter().getCurrentPage(getBrowsableItems());
    }

    public List getBrowsableItems()
    {
        if (groups == null)
        {
            try
            {
                groups = getFilter().getFilteredGroups();
            }
            catch (Exception e)
            {
                log.error("Exception getting groups: " + e, e);
                return Collections.EMPTY_LIST;
            }
        }

        return groups;
    }

    public String getAddName()
    {
        return addName;
    }

    public void setAddName(String addName)
    {
        this.addName = addName.trim();
    }

    public String escapeAmpersand(String str)
    {
        return StringUtils.replaceAll(str, "&", "%26");
    }

    public Collection getPermissionSchemes(String groupName)
    {
        if (groupPermissionSchemeMapper != null)
        {
            return groupPermissionSchemeMapper.getMappedValues(groupName);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public boolean isExternalUserManagementEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }

    public boolean isUserAbleToDeleteGroup(String groupName)
    {
        return globalPermissionGroupAssociationUtil.isUserAbleToDeleteGroup(getRemoteUser(), groupName);
    }

    /**
     * Return true if any directory supports nested groups.
     * @return true if any directory supports nested groups.
     */
    public boolean isNestedGroupsEnabledForAnyDirectory()
    {
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (crowdDirectoryService.supportsNestedGroups(directory.getId()))
            {
                return true;
            }
        }
        return false;
    }

}
