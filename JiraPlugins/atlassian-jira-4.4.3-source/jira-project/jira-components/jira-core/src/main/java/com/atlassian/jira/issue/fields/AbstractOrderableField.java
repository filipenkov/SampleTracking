package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.BulkFieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraKeyUtilsBean;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MemoizingMap.Master;
import com.atlassian.jira.util.velocity.CommonVelocityKeys;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractOrderableField extends AbstractField implements OrderableField
{
    private static final Logger log = Logger.getLogger(AbstractOrderableField.class);
    private static final Master<String, Object> MASTER;
    static
    {
        final Master.Builder<String, Object> builder = Master.builder();
        builder.addLazy("req", new Supplier<HttpServletRequest>()
        {
            public HttpServletRequest get()
            {
                return ServletActionContext.getRequest();
            }
        });
        builder.add("stringutils", new StringUtils());
        builder.add("jirakeyutils", new JiraKeyUtilsBean());
        builder.add("calendarIncluder", new CalendarResourceIncluder());
        MASTER = builder.master();
    }

    private final VelocityManager velocityManager;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final SearchHandlerFactory searcherHandlerFactory;

    public AbstractOrderableField(final String id, final String name, final VelocityManager velocityManager, final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final SearchHandlerFactory searcherHandlerFactory)
    {
        super(id, name, authenticationContext);
        this.velocityManager = velocityManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.searcherHandlerFactory = searcherHandlerFactory;
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        return getCreateHtml(fieldLayoutItem, operationContext, action, issue, new HashMap());
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, new HashMap());
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        if (searcherHandlerFactory == null)
        {
            return null;
        }
        else
        {
            return searcherHandlerFactory.createHandler(this);
        }
    }

    /**
     * Returns HTML that should be shown when the issue is being bulk edited.
     * By default calls the  {@link #getEditHtml(com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem, com.atlassian.jira.issue.customfields.OperationContext, webwork.action.Action, com.atlassian.jira.issue.Issue, java.util.Map)}
     * method with null for {@link FieldLayoutItem} and the first issue in the collection}
     */
    public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        if ((bulkEditBean == null) || (bulkEditBean.getSelectedIssues() == null) || bulkEditBean.getSelectedIssues().isEmpty())
        {
            throw new IllegalArgumentException("At least one issue must be passed.");
        }

        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            final FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);
            return getEditHtml(fieldLayoutItem, operationContext, action, bulkEditBean.getFirstTargetIssueObject(), displayParameters);
        }
        else if (BulkWorkflowTransitionOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            final FieldScreenRenderer fieldScreenRenderer = bulkEditBean.getFieldScreenRenderer();

            for (final Object element : fieldScreenRenderer.getFieldScreenRenderTabs())
            {
                final FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) element;
                for (final Object element2 : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
                {
                    final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = (BulkFieldScreenRenderLayoutItemImpl) element2;

                    for (final Object element3 : bulkFieldScreenRenderLayoutItem.getFieldLayoutItems())
                    {
                        final FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) element3;
                        if (getId().equals(fieldLayoutItem.getOrderableField().getId()))
                        {
                            return bulkFieldScreenRenderLayoutItem.getEditHtml(action, operationContext, bulkEditBean.getSelectedIssues(),
                                displayParameters);
                        }
                    }

                }
            }
        }
        else
        {
            final MutableIssue issue = (MutableIssue) bulkEditBean.getSelectedIssues().iterator().next();
            FieldLayoutItem fieldLayoutItem = null;
            try
            {
                fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue.getProject(),
                    issue.getIssueTypeObject().getId()).getFieldLayoutItem(getId());
            }
            catch (final DataAccessException e)
            {
                log.warn("Unable to resolve the FieldLayoutItem for project: " + issue.getProject().getString("id") + " and issue type: " + issue.getIssueTypeObject().getId());
            }

            return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
        }
        return "";
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue)
    {
        return getViewHtml(fieldLayoutItem, action, issue, new HashMap());
    }

    public void populateFromParams(final Map fieldValuesHolder, final Map parameters)
    {
        fieldValuesHolder.put(getId(), getRelevantParams(parameters));
    }

    protected String renderTemplate(final String template, final Map velocityParams)
    {
        try
        {
            return velocityManager.getEncodedBody(TEMPLATE_DIRECTORY_PATH, template, applicationProperties.getEncoding(), velocityParams);
        }
        catch (final VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + TEMPLATE_DIRECTORY_PATH + "/" + template + "'.", e);
        }

        return "";
    }

    protected ApplicationProperties getApplicationProperties()
    {
        return applicationProperties;
    }

    protected abstract Object getRelevantParams(Map params);

    protected Map<String, Object> getVelocityParams(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Map displayParams)
    {
        final Map<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put(CommonVelocityKeys.DISPLAY_PARAMS, displayParams);
        velocityParams.put(CommonVelocityKeys.DISPLAY_PARAMETERS, displayParams);
        if (displayParams != null)
        {
            velocityParams.put(CommonVelocityKeys.READ_ONLY, (displayParams.get("readonly") == null ? Boolean.FALSE: displayParams.get("readonly")));
            velocityParams.put(CommonVelocityKeys.TEXT_ONLY, (displayParams.get("textOnly") == null ? Boolean.FALSE: displayParams.get("textOnly")));
            velocityParams.put(CommonVelocityKeys.EXCEL_VIEW, (displayParams.get("excel_view") == null ? Boolean.FALSE: displayParams.get("excel_view")));
            velocityParams.put(CommonVelocityKeys.NO_LINK, (displayParams.get("nolink") == null ? Boolean.FALSE: displayParams.get("nolink")));
            velocityParams.put(CommonVelocityKeys.PREFIX, (displayParams.get("prefix") == null ? "": displayParams.get("prefix")));

        }
        else
        {
            velocityParams.put(CommonVelocityKeys.READ_ONLY, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.TEXT_ONLY, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.EXCEL_VIEW, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.NO_LINK, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.PREFIX, "");
        }


        velocityParams.put("fieldLayoutItem", fieldLayoutItem);
        velocityParams.put("action", action);
        velocityParams.put("i18n", action);
        velocityParams.put("auiparams", new HashMap<String, Object>());
        velocityParams.put("helpUtil", HelpUtil.getInstance());

        return CompositeMap.of(velocityParams, getVelocityParams(issue));
    }

    protected Map<String, Object> getVelocityParams(final Issue issue)
    {
        final Map<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put("field", this);
        velocityParams.put("issue", issue);
        // express if there is a calendar translation file for the current language
        final CalendarLanguageUtil calLangUtil = (CalendarLanguageUtil) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(
            CalendarLanguageUtil.class);
        final String language = authenticationContext.getI18nHelper().getLocale().getLanguage();
        velocityParams.put("hasCalendarTranslation", calLangUtil.hasTranslationForLanguage(language));
        return CompositeMap.of(velocityParams, MASTER.toMap(JiraVelocityUtils.createVelocityParams(authenticationContext)));
    }

    protected boolean valuesEqual(final Object value, final Object currentValue)
    {
        if (value == null)
        {
            return (currentValue == null);
        }
        else
        {
            return value.equals(currentValue);
        }
    }

    /**
     * Determines if the field is hidden in <b>at least one</b> field layout.
     *
     * @param fieldLayouts the field layouts to check
     * @return true if the field is hidden in at least one of the passed field layouts,
     *         false otherwise
     */
    protected boolean isHidden(final Collection fieldLayouts)
    {
        for (final Iterator iterator = fieldLayouts.iterator(); iterator.hasNext();)
        {
            final FieldLayout fieldLayout = (FieldLayout) iterator.next();
            if (fieldLayout.isFieldHidden(getId()))
            {
                return true;
            }
        }

        return false;
    }

    protected boolean rendererTypesEqual(final String oldRendererType, final String newRendererType)
    {
        return ((oldRendererType == null) && (newRendererType == null)) || ((oldRendererType != null) && oldRendererType.equals(newRendererType));
    }

    protected boolean hasPermission(final Issue issue, final int permissionId)
    {
        if (issue.getGenericValue() == null)
        {
            return hasPermission(issue.getProject(), permissionId, !issue.isCreated());
        }
        else
        {
            return hasPermission(issue.getGenericValue(), permissionId, !issue.isCreated());
        }
    }

    private boolean hasPermission(final GenericValue entity, final int permissionId, final boolean issueCreation)
    {
        return permissionManager.hasPermission(permissionId, entity, getAuthenticationContext().getUser(), issueCreation);
    }

    protected boolean hasBulkUpdatePermission(final BulkEditBean bulkEditBean, final Issue issue)
    {
        // Do not check the permission if we are doing a bulk workflow transition. Bulk Workflow
        // transition is only protected by the workflow conditions of the transition and should not
        // hardcode a check for a permission here.
        // For bulk edit we should check whether the user has the edit permission for the issue
        return BulkWorkflowTransitionOperation.NAME.equals(bulkEditBean.getOperationName()) || hasPermission(issue, Permissions.EDIT_ISSUE);
    }

    protected PermissionManager getPermissionManager()
    {
        return permissionManager;
    }
}
