package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;

/**
 * Provides the search renderer for issue constants (priority, status, resolution).
 *
 * @since v4.0
 */
public abstract class IssueConstantsSearchRenderer<T extends IssueConstant> extends AbstractSearchRenderer implements SearchRenderer
{
    private final SimpleFieldSearchConstants constants;
    private final ConstantsManager constantsManager;
    private final FieldVisibilityManager fieldVisibilityManager;

    public IssueConstantsSearchRenderer(SimpleFieldSearchConstants constants, String searcherNameKey, ConstantsManager constantsManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityManager velocityManager, FieldVisibilityManager fieldVisibilityManager)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, constants, searcherNameKey);
        this.constants = constants;
        this.constantsManager = constantsManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public abstract Collection<T> getSelectListOptions(SearchContext searchContext);

    public String getEditHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("selectedValues", fieldValuesHolder.get(constants.getUrlParameter()));
        velocityParams.put("selectListOptions", getSelectListOptions(searchContext));
        return renderEditTemplate("constants-searcher-edit.vm", velocityParams);
    }

    public boolean isShown(final User searcher, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(constants.getFieldId(), searchContext, searcher);
    }

    public String getViewHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        final Collection selectedValues = (Collection) fieldValuesHolder.get(constants.getUrlParameter());
        velocityParams.put("selectedObjects", constantsManager.convertToConstantObjects(constants.getUrlParameter(), selectedValues));
        return renderViewTemplate("constants-searcher-view.vm", velocityParams);
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }
}
