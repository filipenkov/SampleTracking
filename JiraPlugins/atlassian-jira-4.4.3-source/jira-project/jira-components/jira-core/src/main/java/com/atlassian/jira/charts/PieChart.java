package com.atlassian.jira.charts;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.PieChartGenerator;
import com.atlassian.jira.charts.jfreechart.util.PieDatasetUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.QueryOptimizer;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Produces a pie chart based on all issues for a certain field.
 *
 * @since v4.0
 */
class PieChart
{
    private static final Logger log = Logger.getLogger(PieChart.class);
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;

    public PieChart(ConstantsManager constantsManager, CustomFieldManager customFieldManager,
            SearchService searchService, ApplicationProperties applicationProperties)
    {
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
    }

    public Chart generateChart(final User remoteUser, SearchRequest searchRequest, String statisticType, int width, int height)
    {
        Assertions.notNull("searchRequest", searchRequest);
        Assertions.notNull("statisticType", statisticType);

        final I18nBean i18nBean = getI18nBean(remoteUser);

        try
        {
            log.debug("DataCollector.populatePieChart: Cloning the SearchRequest.");
            final SearchRequest clonedSearchRequest = new SearchRequest(searchRequest.getQuery());
            final StatisticAccessorBean statBean = new StatisticAccessorBean(remoteUser, clonedSearchRequest);
            final StatisticMapWrapper statWrapper = statBean.getAllFilterBy(statisticType);
            final StatisticsMapper statMapper = statBean.getMapper(statisticType);

            log.debug("DataCollector.populatePieChart: Creating initial PieDataset.");
            final DefaultPieDataset dataset = new DefaultPieDataset();
            int numIssues = 0;
            for (final Object o : statWrapper.entrySet())
            {
                final Map.Entry statistic = (Map.Entry) o;
                final Object key = statistic.getKey();
                PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(key, i18nBean, statisticType, constantsManager, customFieldManager);
                Number number = (Number) statistic.getValue();
                dataset.setValue(pieSegmentWrapper, number);
                numIssues += number.intValue();
            }
            
            if (statWrapper.getIrrelevantCount() > 0)
            {
                // Add a pie slice for Irrelevant
                PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(FilterStatisticsValuesGenerator.IRRELEVANT, i18nBean, statisticType, constantsManager, customFieldManager);
                Number number = statWrapper.getIrrelevantCount();
                dataset.setValue(pieSegmentWrapper, number);
                numIssues += number.intValue();
            }
            
            if (numIssues == 0)
            {
                height = 35;
            }

            log.debug("DataCollector.populatePieChart: Processing the PieDataset.");

            // sort the dataset in order of lowest to highest
            PieDataset sortedDataset = PieDatasetUtil.createSortedPieDataset(dataset);
            PieDataset consolidatedDataset = PieDatasetUtil.createConsolidatedSortedPieDataset(sortedDataset, "Other", false, 0.02, 10);

            // now create the complete dataset
            DefaultCategoryDataset completeDataset = new DefaultCategoryDataset();
            for (Iterator iterator = sortedDataset.getKeys().iterator(); iterator.hasNext();)
            {
                Comparable key = (Comparable) iterator.next();
                Number value = sortedDataset.getValue(key);
                completeDataset.addValue(value, i18nBean.getText("common.concepts.issues"), key);
                completeDataset.addValue((100 * value.intValue() / numIssues), "%", key);
            }

            log.debug("DataCollector.populatePieChart: Generate the ChartHelper.");
            final ChartHelper helper = new PieChartGenerator(consolidatedDataset, i18nBean).generateChart();
            log.debug("DataCollector.populatePieChart: ChartHelper generated. Add Tooltips and URL Generators.");
            JFreeChart chart = helper.getChart();
            PiePlot plot = (PiePlot) chart.getPlot();
            final int numIssues1 = numIssues;
            plot.setToolTipGenerator(new PieToolTipGenerator()
            {
                public String generateToolTip(PieDataset dataset, Comparable key)
                {
                    Number number = dataset.getValue(key);
                    return StringEscapeUtils.escapeHtml(key.toString()) + ": " + number + " " + i18nBean.getText("datacollector.tooltip.issues") + " (" + (100 * number.intValue() / numIssues1) + "%)";
                }
            });
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            plot.setURLGenerator(new PieURLGenerator()
            {
                public String generateURL(PieDataset dataset, Comparable key, int section)
                {
                    if (key instanceof PieSegmentWrapper && ((PieSegmentWrapper)(key)).isGenerateUrl())
                    {
                        SearchRequest searchUrlSuffix = statMapper.getSearchUrlSuffix(((PieSegmentWrapper) key).getKey(), clonedSearchRequest);
                        Query query;
                        if (searchUrlSuffix == null)
                        {
                            query = new QueryImpl();
                        }
                        else
                        {
                            QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();
                            query = optimizer.optimizeQuery(searchUrlSuffix.getQuery());
                        }
                        return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query);
                    }
                    else
                    {
                        return null;
                    }
                }
            });

            CategoryURLGenerator completeUrlGenerator = new CategoryURLGenerator()
            {
                public String generateURL(CategoryDataset categoryDataset, int row, int col)
                {
                    Comparable key = categoryDataset.getColumnKey(col);
                    if (key instanceof PieSegmentWrapper)
                    {
                        SearchRequest searchUrlSuffix = statMapper.getSearchUrlSuffix(((PieSegmentWrapper) key).getKey(), clonedSearchRequest);
                        return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, (searchUrlSuffix == null) ? new QueryImpl() : searchUrlSuffix.getQuery());
                    }

                    return null;
                }
            };

            log.debug("DataCollector.populatePieChart: Have the ChartHelper generate the image.");
            helper.generate(width, height);
            log.debug("DataCollector.populatePieChart: ChartHelper finished generating.");

            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("chart", helper.getLocation());
            params.put("chartDataset", consolidatedDataset);
            params.put("completeDataset", completeDataset);
            params.put("completeDatasetUrlGenerator", completeUrlGenerator);
            params.put("numIssues", numIssues);
            params.put("statisticType", statisticType);
            params.put("statisticTypeI18nName", getStatisticsTypeI18nName(i18nBean, statisticType));
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());
            params.put("width", width);
            params.put("height", height);

            return new Chart(helper.getLocation(), helper.getImageMap(), helper.getImageMapName(), params);
        }
        catch (SearchException e)
        {
            throw new RuntimeException("Error generating pie chart", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error generating pie chart", e);
        }
    }

    String getStatisticsTypeI18nName(final I18nHelper i18nBean, final String statisticType)
    {
        //check if it's a custom field and look up the custom field name if that's the case
        if (statisticType.startsWith(FieldManager.CUSTOM_FIELD_PREFIX))
        {
            CustomField customField = customFieldManager.getCustomFieldObject(statisticType);
            if (customField == null)
            {
                throw new RuntimeException("No custom field with id '" + statisticType + "'");
            }
            if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
            {
                return customField.getName();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return i18nBean.getText("gadget.filterstats.field.statistictype." + statisticType.toLowerCase());
        }
    }

    private I18nBean getI18nBean(User remoteUser)
    {
        return new I18nBean(remoteUser);
    }

}
