package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.impl.LabelsCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.label.AlphabeticalLabelRenderer;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Implements a heatmap project tab panel modeled after the one found in Confluence labels. The tab has a number of
 * different views including:
 * <p/>
 * Popular view (alphabetic or heatmap) View All Index.
 */
public class LabelsHeatmapProjectTabPanel extends GenericProjectTabPanel
{
    private static final String LABELS_VIEW_PARAMETER = "labels.view";
    private static final String SELECTED_LABEL_PARAMETER = "selected.field";
    private static final String POPULAR_LABELS_VIEW = "popular";
    private static final String LABELS_ORDER_PARAMETER = "labels.order";
    private static final String ALPHA_ORDER = "alpha";

    private final CustomFieldManager customFieldManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final FieldManager fieldManager;
    private final LabelUtil labelUtil;
    private final AlphabeticalLabelRenderer alphabeticalLabelRenderer;
    private static final int MAX_FONT_SIZE = 14;


    public LabelsHeatmapProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext,
            final CustomFieldManager customFieldManager, final FieldVisibilityManager fieldVisibilityManager,
            final FieldManager fieldManager, final LabelUtil labelUtil, final AlphabeticalLabelRenderer alphabeticalLabelRenderer)
    {
        super(jiraAuthenticationContext, fieldVisibilityManager);
        this.customFieldManager = customFieldManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.fieldManager = fieldManager;
        this.labelUtil = labelUtil;
        this.alphabeticalLabelRenderer = alphabeticalLabelRenderer;
    }

    /**
     * Returns HTML for the project tab.  This is overriden here to test for the view type process the proper velocity
     * template and set appropriate velocity context parameters.
     */
    public String getHtml(BrowseContext browseContext)
    {
        try
        {
            final HttpServletRequest req = ServletActionContext.getRequest();
            String labelsView = req.getParameter(LABELS_VIEW_PARAMETER);
            String selectedFieldId = req.getParameter(SELECTED_LABEL_PARAMETER);

            final List<Field> labels = getLabelFields(browseContext.getProject().getId());
            Field selectedField;
            if (StringUtils.isBlank(selectedFieldId) && !labels.isEmpty())
            {
                selectedField = labels.get(0);
            }
            else
            {
                selectedField = fieldManager.getField(selectedFieldId);
            }

            String result;
            if (StringUtils.isEmpty(labelsView) || POPULAR_LABELS_VIEW.equals(labelsView))
            {
                result = getPopularView(browseContext, req, selectedField, labels);
            }
            else
            {
                result = getAllView(browseContext, req, selectedField, labels);
            }
            return result;
        }
        catch (Exception e)
        {
            throw new NestableRuntimeException(e);
        }
    }

    //Return a view consisting of an index of all labels.
    private String getAllView(BrowseContext browseContext, HttpServletRequest req, Field field, Collection<Field> labelFields)
            throws Exception
    {
        final Long projectId = browseContext.getProject().getId();
        final Map<String, Object> startingParams = getVelocityParams(browseContext, req, field, labelFields);
        startingParams.put("alphabeticalLabelsHtml", alphabeticalLabelRenderer.getHtml(browseContext.getUser(), projectId, field.getId()));

        return descriptor.getHtml("view-all", startingParams);
    }

    //Returns a list of most popular labels arranged alphabetically or Ranked by usage with varying font size (ie
    private String getPopularView(BrowseContext browseContext, HttpServletRequest req, Field field, Collection<Field> labelFields)
            throws Exception
    {
        final String labelOrder = req.getParameter(LABELS_ORDER_PARAMETER);
        final Long projectId = browseContext.getProject().getId();
        final StatisticAccessorBean statBean = new StatisticAccessorBean(browseContext.getUser(), getProjectFilter(projectId));
        try
        {
            StatisticAccessorBean.OrderBy order = StatisticAccessorBean.OrderBy.TOTAL;
            StatisticAccessorBean.Direction direction = StatisticAccessorBean.Direction.DESC;
            if (StringUtils.isEmpty(labelOrder) || ALPHA_ORDER.equals(labelOrder))
            {
                order = StatisticAccessorBean.OrderBy.NATURAL;
                direction = StatisticAccessorBean.Direction.ASC;
            }

            @SuppressWarnings ("unchecked")
            final StatisticMapWrapper<Label, Number> statWrapper = statBean.getAllFilterBy(field.getId(), order, direction);

            Map<String, Object> startingParams = getVelocityParams(browseContext, req, field, labelFields);
            startingParams.put("labelOrder", labelOrder);
            startingParams.put("labelsHeatMap", normalizeMap(statWrapper));
            return descriptor.getHtml("view", startingParams);
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Map<Label, Number> normalizeMap(Map<Label, Number> input)
    {
        final Map<Label, Number> ret = new LinkedHashMap<Label, Number>();
        //remove empty values!
        input.remove(null);

        if (!input.isEmpty())
        {
            //first find the max value from the input
            final Collection<Number> values = input.values();
            Number max = null;
            for (Number value : values)
            {
                if (max == null)
                {
                    max = value;
                }
                else if (value.intValue() > max.intValue())
                {
                    max = value;
                }
            }
            if (max != null)
            {
                double multiplier = MAX_FONT_SIZE / max.doubleValue();
                for (Map.Entry<Label, Number> labelNumberEntry : input.entrySet())
                {
                    ret.put(labelNumberEntry.getKey(), (int) (labelNumberEntry.getValue().intValue() * multiplier));
                }
            }
        }
        return ret;
    }

    private Map<String, Object> getVelocityParams(final BrowseContext browseContext, final HttpServletRequest req,
            final Field field, final Collection<Field> labelFields)
    {
        Map<String, Object> startingParams = new HashMap<String, Object>();
        startingParams.put("field", field);
        startingParams.put("labelFields", labelFields);
        startingParams.put("labelUtils", labelUtil);
        startingParams.put("isCustomField", field.getId().startsWith(CustomFieldUtils.CUSTOM_FIELD_PREFIX));
        startingParams.put("projectId", browseContext.getProject().getId());
        startingParams.put("projectKey", browseContext.getProject().getKey());
        startingParams.put("currentView", getCurrentView(req));
        startingParams.put("remoteUser", browseContext.getUser());
        return startingParams;
    }

    private SearchRequest getProjectFilter(Long projectId)
    {
        final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder jqlClauseBuilder = jqlQueryBuilder.where();
        jqlClauseBuilder.project(projectId);
        return new SearchRequest(jqlClauseBuilder.buildQuery());
    }

    private String getCurrentView(HttpServletRequest req)
    {
        final String labelsView = req.getParameter(LABELS_VIEW_PARAMETER);
        if (StringUtils.isEmpty(labelsView))
        {
            return POPULAR_LABELS_VIEW;
        }
        else
        {
            return labelsView;
        }
    }

    public boolean showPanel(BrowseContext browseContext)
    {
        final List<Field> labelFields = getLabelFields(browseContext.getProject().getId());
        return !labelFields.isEmpty();
    }

    public List<Field> getLabelFields
            (
                    final Long projectId)
    {
        final List<Field> ret = new ArrayList<Field>();
        if (!fieldVisibilityManager.isFieldHiddenInAllSchemes(projectId, IssueFieldConstants.LABELS))
        {
            ret.add(fieldManager.getField(IssueFieldConstants.LABELS));
        }
        final List<CustomField> customFieldList = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : customFieldList)
        {
            if (customField.getCustomFieldType() instanceof LabelsCFType)
            {
                if (!fieldVisibilityManager.isFieldHiddenInAllSchemes(projectId, customField.getId()))
                {
                    ret.add(customField);
                }
            }
        }
        return ret;
    }
}