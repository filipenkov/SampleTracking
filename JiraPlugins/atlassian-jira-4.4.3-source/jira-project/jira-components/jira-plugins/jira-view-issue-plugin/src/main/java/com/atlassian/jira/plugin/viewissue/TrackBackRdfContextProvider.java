package com.atlassian.jira.plugin.viewissue;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.util.TextUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the Trackback RDF block
 *
 * @since v4.4
 */
public class TrackBackRdfContextProvider implements CacheableContextProvider
{
    private static final String CONTEXT_TRACKBACKS = "trackbacks";
    private static final String CONTEXT_BASEURL = "baseurl";
    private static final String CONTEXT_ISSUE = "issue";

    private final VelocityRequestContextFactory requestContextFactory;

    public TrackBackRdfContextProvider(VelocityRequestContextFactory requestContextFactory)
    {
        this.requestContextFactory = requestContextFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get(CONTEXT_ISSUE);
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();

        final String baseUrl = requestContext.getCanonicalBaseUrl();

        final String issueUrl = baseUrl + "/browse/" + issue.getKey();
        final String issueById = baseUrl + "/secure/ViewIssue.jspa?id=" + issue.getId();
        final String issueByKey = baseUrl + "/secure/ViewIssue.jspa?key=" + issue.getKey();

        final List<SimpleTrackBack> trackbacks = CollectionBuilder.list(createTrackback(issue, issueUrl), createTrackback(issue, issueById), createTrackback(issue, issueByKey));

        paramsBuilder.add(CONTEXT_TRACKBACKS, trackbacks);
        paramsBuilder.add(CONTEXT_BASEURL, baseUrl);

        return paramsBuilder.toMap();
    }

    private SimpleTrackBack createTrackback(Issue issue, String about)
    {
        final String projectName = issue.getProjectObject().getName();
        final String rawDesc = issue.getDescription();
        final String description = rawDesc == null ? "" : StringUtils.crop(rawDesc, 100, "...");
        final Timestamp created = issue.getCreated();
        return new SimpleTrackBack(about, issue.getSummary(), projectName, description, created);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleTrackBack
    {
        private final String about;
        private final String title;
        private final String subject;
        private final String description;
        private final String date;


        public SimpleTrackBack(String about, String title, String subject, String description, Timestamp date)
        {
            this.about = about;
            this.title = encodeAndReplace(title);
            this.subject = encodeAndReplace(subject);
            this.description = encodeAndReplace(description);
            this.date = DateUtils.formatDateISO8601(date);
        }

        private String encodeAndReplace(String raw)
        {
            final String encoded = TextUtils.htmlEncode(raw);
            final String encodedAndReplaced = StringUtils.replaceAll(encoded, "--", "&#45;&#45;");

            return encodedAndReplaced;
        }

        public String getAbout()
        {
            return about;
        }

        public String getTitleHtml()
        {
            return title;
        }

        public String getSubjectHtml()
        {
            return subject;
        }

        public String getDescriptionHtml()
        {
            return description;
        }

        public String getDate()
        {
            return date;
        }
    }
}
