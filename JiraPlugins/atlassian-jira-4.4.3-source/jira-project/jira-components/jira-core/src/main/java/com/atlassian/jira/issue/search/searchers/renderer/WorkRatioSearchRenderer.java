package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import webwork.action.Action;

import java.util.Map;

/**
 * Searcher Renderer for the {@link com.atlassian.jira.issue.search.searchers.impl.WorkRatioSearcher}
 *
 * @since v4.0
 */
@NonInjectableComponent
public class WorkRatioSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final SimpleFieldSearchConstants constants;
    private final WorkRatioSearcherConfig config;

    public WorkRatioSearchRenderer(VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties, VelocityManager velocityManager, SimpleFieldSearchConstants searchConstants, String searcherNameKey, final FieldVisibilityManager fieldVisibilityManager, final WorkRatioSearcherConfig config)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, searchConstants, searcherNameKey);
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.constants = searchConstants;
        this.config = config;
    }

    public boolean isShown(final User searcher, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(constants.getFieldId(), searchContext, searcher);
    }

    public String getEditHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderEditTemplate("workratio-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderViewTemplate("workratio-searcher-view.vm", velocityParams);
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User searcher, final SearchContext searchContext, final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        velocityParams.put("minField", config.getMinField());
        velocityParams.put("maxField", config.getMaxField());
        return velocityParams;
    }
}
