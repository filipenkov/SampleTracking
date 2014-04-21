package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.List;
import java.util.Map;

/**
 * An search renderer for the user fields.
 *
 * @since v4.0
 */
public abstract class AbstractUserSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    public static final String SELECT_LIST_NONE = "select.list.none";
    public static final String SELECT_LIST_USER = "select.list.user";
    public static final String SELECT_LIST_GROUP = "select.list.group";

    private final UserFieldSearchConstants searchConstants;
    private final String emptySelectFlag;
    private final String nameKey;
    private final ApplicationProperties applicationProperties;
    private final UserPickerSearchService searchService;

    public AbstractUserSearchRenderer(UserFieldSearchConstantsWithEmpty searchConstants, String nameKey, VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, UserPickerSearchService searchService)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, searchConstants.getSearcherId(), nameKey);
        this.emptySelectFlag = searchConstants.getEmptySelectFlag();
        this.searchConstants = searchConstants;
        this.nameKey = nameKey;
        this.applicationProperties = applicationProperties;
        this.searchService = searchService;
    }

    public AbstractUserSearchRenderer(UserFieldSearchConstants searchConstants, String nameKey, VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, UserPickerSearchService searchService)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, searchConstants.getSearcherId(), nameKey);
        this.emptySelectFlag = null;
        this.searchConstants = searchConstants;
        this.nameKey = nameKey;
        this.applicationProperties = applicationProperties;
        this.searchService = searchService;
    }

    public String getEditHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);

        final int permissionId = Permissions.getType("pickusers");
        boolean pickPermission = ManagerFactory.getPermissionManager().hasPermission(permissionId, searcher);

        velocityParams.put("hasPermissionToPickUsers", pickPermission);
        velocityParams.put("selectListOptions", getSelectedListOptions(searcher));

        return renderEditTemplate("user-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
         
        velocityParams.put("textForuser", getTextForuser(searcher, fieldValuesHolder));
        velocityParams.put("linkedGroup", getLinkedGroup(searcher, fieldValuesHolder));
        velocityParams.put("linkedUser", getLinkedUser(searcher, fieldValuesHolder));

        return renderViewTemplate("user-searcher-view.vm", velocityParams);
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        return isRelevantForQuery(searchConstants.getJqlClauseNames(), query);
    }
    // ----------------------------------------------------------------------------------------------------- View Helper

    /**
     * @param searcher performing the action.
     * @return the select list options that are displayed for this user searcher (e.g. SpecificUser, CurrentUser...)
     */
    protected abstract List<Map<String, String>> getSelectedListOptions(final User searcher);

    /**
     * @return the i18n key for the text that describes an empty value for this searcher.
     */
    protected abstract String getEmptyValueKey();

    protected String getTextForuser(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String selectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
        if (emptySelectFlag != null && emptySelectFlag.equals(selectList))
        {
            return getI18n(searcher).getText(getEmptyValueKey());
        }
        else if (searchConstants.getCurrentUserSelectFlag().equals(selectList))
        {
            return getI18n(searcher).getText("reporter.types.currentuser");
        }
        else
        {
            return (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
        }
    }

    private String getLinkedUser(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String selectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
        if (searchConstants.getCurrentUserSelectFlag().equals(selectList))
        {
            if (searcher != null)
            {
                return searcher.getName();
            }
        }
        else if (searchConstants.getSpecificUserSelectFlag().equals(selectList))
        {
            return (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
        }

        return null;
    }

    private String getLinkedGroup(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String selectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
        if (searchConstants.getSpecificGroupSelectFlag().equals(selectList))
        {
            if (ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, searcher))
            {
                return (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
            }
        }

        return null;
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User searcher, final SearchContext searchContext, final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        final JiraServiceContext ctx = new JiraServiceContextImpl(searcher);
        final boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);

        velocityParams.put("name", getI18n(searcher).getText(nameKey));
        velocityParams.put("userField", searchConstants.getFieldUrlParameter());
        velocityParams.put("userSelect", searchConstants.getSelectUrlParameter());

        final WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");

        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        return velocityParams;
    }
}
