package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import webwork.action.Action;

import java.util.List;
import java.util.Map;

/**
 * A search renderer for the Labels searcher
 *
 * @since v4.2
 */
public class LabelsSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{

    private final SimpleFieldSearchConstantsWithEmpty constants;
    private FieldVisibilityManager fieldVisibilityManager;

    public LabelsSearchRenderer(SimpleFieldSearchConstantsWithEmpty constants, VelocityRequestContextFactory velocityRequestContextFactory,
            FieldVisibilityManager fieldVisibilityManager, ApplicationProperties applicationProperties, VelocityManager velocityManager,
            String nameKey)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, constants.getSearcherId(), nameKey);
        this.constants = constants;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public String getEditHtml(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("labelString", getLabelString(fieldValuesHolder));
        return renderEditTemplate("labels-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("labelString", getLabelString(fieldValuesHolder));
        return renderViewTemplate("labels-searcher-view.vm", velocityParams);
    }

    private String getLabelString(final FieldValuesHolder fieldValuesHolder)
    {
        @SuppressWarnings("unchecked")
        final List<String> labelList = (List<String>) fieldValuesHolder.get("labels");
        final StringBuilder labelString = new StringBuilder();
        if(labelList != null && !labelList.isEmpty())
        {
            for (String label : labelList)
            {
                labelString.append(label).append(LabelsSystemField.SEPARATOR_CHAR);
            }
        }
        return labelString.toString().trim();
    }

    public boolean isShown(final User searcher, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(constants.getFieldId(), searchContext, searcher);
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }
}
