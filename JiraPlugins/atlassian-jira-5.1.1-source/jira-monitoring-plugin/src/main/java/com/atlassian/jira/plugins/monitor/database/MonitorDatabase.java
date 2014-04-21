package com.atlassian.jira.plugins.monitor.database;

import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.plugins.monitor.MonitorAction;
import com.atlassian.jira.plugins.monitor.MonitoringFeature;
import com.atlassian.jira.plugins.monitor.rrd4j.Graph;
import com.atlassian.jira.plugins.monitor.rrd4j.GraphRegistry;
import com.atlassian.jira.plugins.monitor.rrd4j.RrdUpdater;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.sal.api.message.HelpPath;
import com.atlassian.sal.api.message.HelpPathResolver;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jfree.chart.servlet.ServletUtilities;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.util.TextUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.rrd4j.graph.RrdGraph.ALIGN_LEFTNONL_MARKER;
import static org.rrd4j.graph.RrdGraph.ALIGN_LEFT_MARKER;
import static org.rrd4j.graph.RrdGraph.ALIGN_RIGHT_MARKER;

/**
 * View for monitoring JIRA's database connections.
 *
 * @since v5.0.3
 */
@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class MonitorDatabase extends MonitorAction
{
    private static final Logger log = LoggerFactory.getLogger(MonitorDatabase.class);

    private final RrdUpdater databaseRrdUpdater;
    private final JiraBaseUrls jiraBaseUrls;
    private final GraphRegistry graphRegistry;
    private final HelpPathResolver helpPathResolver;

    private ImmutableList<GraphBean> graphs;
    private int hours = 6;

    public MonitorDatabase(RrdUpdater databaseRrdUpdater, PermissionManager permissionManager,
                           JiraAuthenticationContext jiraAuthenticationContext, JiraBaseUrls jiraBaseUrls,
                           GraphRegistry graphRegistry, MonitoringFeature monitoringFeature,
                           HelpPathResolver helpPathResolver)
    {
        super(permissionManager, jiraAuthenticationContext, monitoringFeature);
        this.databaseRrdUpdater = databaseRrdUpdater;
        this.jiraBaseUrls = jiraBaseUrls;
        this.graphRegistry = graphRegistry;
        this.helpPathResolver = helpPathResolver;
    }

    public ImmutableList<GraphBean> getGraphs()
    {
        if (graphs == null)
        {
            graphs = createGraphs();
        }
        return graphs;
    }

    public String getDatabaseConnectionHelpLinkHtml()
    {
        final HelpPath helpPath = helpPathResolver.getHelpPath("dbconfig.generic");
        return "<a href=\"" + helpPath.getUrl() + "\" target=\"_blank\">" + TextUtil.escapeHTML(helpPath.getTitle()) + "</a>";
    }

    public void setHours(int hours)
    {
        this.hours = hours;
    }

    private ImmutableList<GraphBean> createGraphs()
    {
        List<GraphBean> graphBeans = Lists.newArrayList();
        for (Graph graph : graphRegistry.getGraphs())
        {
            // create the graph
            try
            {
                String filename = createImageFile(graph);
                log.debug("Created image '{}' for: {}", filename, graph);

                graphBeans.add(new GraphBean(getText(graph.getTitleI18nKey()), getImageUrl(filename)));
            }
            catch (IOException e)
            {
                log.error("Error creating graph for: " + graph, e);
            }
        }

        return ImmutableList.copyOf(graphBeans);
    }

    private String getImageUrl(String filename)
    {
        // reuse the JFreeChart servlet
        UrlBuilder imageUrl = new UrlBuilder(jiraBaseUrls.baseUrl())
                .addPath("/charts")
                .addParameter("filename", filename);

        return imageUrl.asUrlString();
    }

    private File createTemporaryFile()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");

        // subvert the JFreeChart servlet to serve our images too
        return new File(tmpDir, ServletUtilities.getTempOneTimeFilePrefix() + UUID.randomUUID().toString() + ".png");
    }

    private RrdGraphDef createGraphDefinition(File file)
    {
        RrdGraphDef gDef = new RrdGraphDef();
        gDef.setFilename(file.getAbsolutePath());
        gDef.setImageFormat("png");
        gDef.setWidth(800);
        gDef.setHeight(300);
        gDef.setValueAxis(1, 1);
        gDef.setMinValue(0);

        return gDef;
    }

    private String createImageFile(Graph graph) throws IOException
    {
        // create the image
        File file = createTemporaryFile();
        RrdGraphDef gDef = createGraphDefinition(file);
        graph.addGraphElements(gDef, databaseRrdUpdater.getRrdDbPathFor(graph).getAbsolutePath());

        // set the time
        DateTime now = new DateTime();
        gDef.setTitle(getText(graph.getTitleI18nKey()));
        gDef.setTimeSpan(now.minus(Hours.hours(hours)).getMillis() / 1000, now.getMillis() / 1000);
        gDef.setLargeFont(new Font("sansserif", Font.PLAIN, 14));
        gDef.setSmallFont(new Font("sansserif", Font.PLAIN, 12));

        // insert the common footer
        gDef.comment("" + ALIGN_LEFT_MARKER);
        gDef.comment(jiraBaseUrls.baseUrl() + ALIGN_LEFTNONL_MARKER);
        gDef.comment(now + ALIGN_RIGHT_MARKER);

        // this creates the graph
        @SuppressWarnings ("UnusedDeclaration")
        RrdGraph rrdGraph = new RrdGraph(gDef);

        // now we can publish the filename
        return file.getName();
    }

    public static class GraphBean
    {
        private final String title;
        private final String imageUrl;

        private GraphBean(String title, String imageUrl)
        {
            this.title = title;
            this.imageUrl = imageUrl;
        }

        public String getTitle()
        {
            return title;
        }

        public String getImageUrl()
        {
            return imageUrl;
        }
    }
}
