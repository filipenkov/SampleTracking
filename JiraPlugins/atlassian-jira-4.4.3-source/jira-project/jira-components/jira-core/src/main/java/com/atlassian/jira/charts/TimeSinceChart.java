package com.atlassian.jira.charts;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.HistogramChartGenerator;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.QueryOptimizer;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.DatePeriodStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.Query;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Searcher;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.urls.TimeSeriesURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Produces a chart showing the number of issues based on a particular datefield.
 *
 * @since v4.0
 */
class TimeSinceChart
{
    private final FieldManager fieldManager;
    private final SearchProvider searchProvider;
    private final IssueIndexManager issueIndexManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final TimeZone chartTimeZone;

    public TimeSinceChart(FieldManager fieldManager, SearchProvider searchProvider, IssueIndexManager issueIndexManager,
            SearchService searchService, ApplicationProperties applicationProperties, TimeZone chartTimeZone)
    {
        this.fieldManager = fieldManager;
        this.searchProvider = searchProvider;
        this.issueIndexManager = issueIndexManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.chartTimeZone = chartTimeZone;
    }

    public Chart generateChart(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height, boolean cumulative, final String dateFieldId)
    {
        Assertions.notNull("searchRequest", searchRequest);
        Assertions.notNull("dateFieldId", dateFieldId);

        try
        {
            final Field dateField = fieldManager.getField(dateFieldId);

            days = DataUtils.normalizeDaysValue(days, periodName);

            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            final JqlClauseBuilder whereClauseBuilder = queryBuilder.where().defaultAnd();
            whereClauseBuilder.addStringCondition(dateFieldId, Operator.GREATER_THAN_EQUALS, "-" + days + "d");

            final Map<RegularTimePeriod, Number> matchingIssues = new TreeMap<RegularTimePeriod, Number>();
            final Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
            final HitCollector hitCollector = new GenericDateFieldIssuesHitCollector(matchingIssues, issueIndexManager.getIssueSearcher(), timePeriodClass, dateFieldId, chartTimeZone);
            final Query query = whereClauseBuilder.buildQuery();
            searchProvider.search(query, remoteUser, hitCollector);
            DataUtils.normaliseDateRangeCount(matchingIssues, days - 1, timePeriodClass, chartTimeZone);

            final I18nBean i18nBean = new I18nBean(remoteUser);
            final TimeSeriesCollection originalDataset = DataUtils.getTimeSeriesCollection(EasyList.build(matchingIssues),
                    new String[] { dateField.getName() }, timePeriodClass);

            //need to calculate the number of total issues before potentially making the values cumulative, which
            //would yield the wrong total.
            int numIssues = DataUtils.getTotalNumber(matchingIssues);
            if (cumulative)
            {
                DataUtils.makeCumulative(matchingIssues);
            }


            final TimeSeriesCollection dataset = DataUtils.getTimeSeriesCollection(EasyList.build(matchingIssues),
                    new String[] { i18nBean.getText("datacollector.createdresolved") }, timePeriodClass);
            ChartHelper helper = new HistogramChartGenerator(dataset, i18nBean.getText("common.concepts.issues"), i18nBean).generateChart();
            JFreeChart chart = helper.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
            renderer.setToolTipGenerator(new XYToolTipGenerator()
            {
                public String generateToolTip(XYDataset xyDataset, int row, int col)
                {
                    final TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) xyDataset;
                    final TimeSeries timeSeries = timeSeriesCollection.getSeries(row);
                    final TimeSeriesDataItem item = timeSeries.getDataItem(col);
                    final RegularTimePeriod period = item.getPeriod();

                    int total = matchingIssues.get(period).intValue();

                    return period.toString() + ": " + total + " " + StringEscapeUtils.escapeHtml(dateField.getName()) + " " + i18nBean.getText("datacollector.tooltip.issues") + ".";
                }
            });
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            XYURLGenerator urlGenerator = new TimeSeriesURLGenerator()
            {
                public String generateURL(XYDataset xyDataset, int row, int col)
                {
                    final TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) xyDataset;
                    final TimeSeries timeSeries = timeSeriesCollection.getSeries(row);
                    final TimeSeriesDataItem item = timeSeries.getDataItem(col);
                    final RegularTimePeriod period = item.getPeriod();

                    StatisticsMapper createdMapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), dateFieldId, chartTimeZone);
                    SearchRequest searchUrlSuffix = createdMapper.getSearchUrlSuffix(period, searchRequest);
                    final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
                    jqlQueryBuilder.where().addClause(searchUrlSuffix.getQuery().getWhereClause());
                    jqlQueryBuilder.orderBy().setSorts(searchUrlSuffix.getQuery().getOrderByClause());
                    jqlQueryBuilder.orderBy().addSortForFieldName(dateFieldId, SortOrder.DESC, true);
                    QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();
                    final Query query = optimizer.optimizeQuery(jqlQueryBuilder.buildQuery());
                    return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query);
                }
            };
            renderer.setURLGenerator(urlGenerator);
            plot.setRenderer(renderer);
            helper.generate(width, height);
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("chart", helper.getLocation());
            params.put("chartDataset", dataset);
            params.put("completeDataset", originalDataset);
            params.put("completeDatasetUrlGenerator", urlGenerator);
            params.put("numIssues", numIssues);
            params.put("period", periodName.toString());
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());
            params.put("field", dateField);
            params.put("daysPrevious", days);
            params.put("cumulative", cumulative);
            params.put("dateField", dateFieldId);
            params.put("chartFilterUrl", "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query));

            return new Chart(helper.getLocation(), helper.getImageMap(), helper.getImageMapName(), params);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error generating chart", e);
        }
        catch (SearchException e)
        {
            throw new RuntimeException("Error generating chart", e);
        }
    }

    /**
     * Creates a mapping of RegularTimePeriod -> Issuecount.
     */
    static class GenericDateFieldIssuesHitCollector extends DocumentHitCollector
    {
        private final Map<RegularTimePeriod, Number> resolvedMap;
        private final Class timePeriodClass;
        private final String dateFieldId;
        private final TimeZone timeZone;

        public GenericDateFieldIssuesHitCollector(Map<RegularTimePeriod, Number> resolvedMap, Searcher searcher,
                Class timePeriodClass, String dateFieldId, TimeZone timeZone)
        {
            super(searcher);
            this.resolvedMap = resolvedMap;
            this.timePeriodClass = timePeriodClass;
            this.dateFieldId = dateFieldId;
            this.timeZone = timeZone;
        }

        public void collect(Document d)
        {
            final Date date = LuceneUtils.stringToDate(d.get(dateFieldId));
            final RegularTimePeriod period = RegularTimePeriod.createInstance(timePeriodClass, date, timeZone);

            incrementMap(resolvedMap, period);
        }

        private void incrementMap(Map<RegularTimePeriod, Number> map, RegularTimePeriod period)
        {
            Number count = map.get(period);
            if (count == null)
            {
                count = 0;
            }

            map.put(period, count.intValue() + 1);
        }
    }
}
