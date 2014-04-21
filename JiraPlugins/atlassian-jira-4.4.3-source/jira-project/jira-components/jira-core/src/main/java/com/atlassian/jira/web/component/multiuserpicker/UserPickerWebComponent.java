package com.atlassian.jira.web.component.multiuserpicker;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.jira.web.component.WebComponentUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.profiling.UtilTimerStack;
import com.atlassian.velocity.VelocityManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPickerWebComponent extends AbstractWebComponent
{
    private final JiraAuthenticationContext authenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);
    private final UserPickerSearchService searchService;

    public UserPickerWebComponent(final VelocityManager velocityManager, final ApplicationProperties applicationProperties, final UserPickerSearchService searchService)
    {
        super(velocityManager, applicationProperties);
        this.searchService = searchService;
    }

    public String getHtml(final UserPickerLayoutBean layoutBean, final Collection<User> currentUsers, final boolean canEdit, final Long id)
    {
        final List userBeans = UserBean.convertUsersToUserBeans(authenticationContext.getLocale(), currentUsers);
        return getHtml(layoutBean, userBeans, canEdit, id, getI18nBean());
    }

    public String getHtmlForUsernames(final UserPickerLayoutBean layoutBean, final List<String>usernames, final boolean canEdit, final Long id)
    {
        final List userBeans = UserBean.convertUsernamesToUserBeans(authenticationContext.getLocale(), usernames);
        return getHtml(layoutBean, userBeans, canEdit, id, getI18nBean());
    }

    private String getHtml(final UserPickerLayoutBean layoutBean, final List currentUsers, final boolean canEdit, final Long id, final I18nHelper i18n)
    {
        try
        {
            UtilTimerStack.push("UserPickerHtml");

            final boolean canPickUsers = ManagerFactory.getPermissionManager().hasPermission(Permissions.USER_PICKER, authenticationContext.getUser());
            final Map startingParams = new HashMap();
            startingParams.put("userUtil", ComponentAccessor.getUserUtil());
            startingParams.put("layout", layoutBean);
            startingParams.put("currentSelections", currentUsers);
            startingParams.put("i18n", i18n);
            startingParams.put("canEdit", Boolean.valueOf(canEdit));
            startingParams.put("id", id);
            startingParams.put("canPick", Boolean.valueOf(canPickUsers));
            startingParams.put("windowName", "UserPicker");
            final Map velocityParams = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);

            final JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());

            final boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
            if (canPerformAjaxSearch)
            {
                velocityParams.put("canPerformAjaxSearch", "true");
                velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
            }
            final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
            webResourceManager.requireResource("jira.webresources:autocomplete");
            return getHtml("templates/jira/multipicker/pickertable.vm", velocityParams);
        }
        finally
        {
            UtilTimerStack.pop("UserPickerHtml");
        }
    }

    public static Collection/*<String>*/getUserNamesToRemove(final Map params, final String paramPrefix)
    {
        return WebComponentUtils.getRemovalValues(params, paramPrefix);
    }

    public static Collection/*<String>*/getUserNamesToAdd(final String rawUserNames)
    {
        return WebComponentUtils.convertStringToCollection(rawUserNames);
    }

    private I18nHelper getI18nBean()
    {
        return authenticationContext.getI18nHelper();
    }

}
