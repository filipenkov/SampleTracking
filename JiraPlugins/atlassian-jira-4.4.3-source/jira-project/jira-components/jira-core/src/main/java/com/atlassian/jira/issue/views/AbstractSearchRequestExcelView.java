package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.IssueTableWriter;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.opensymphony.user.User;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

public abstract class AbstractSearchRequestExcelView extends AbstractSearchRequestIssueTableView
{
    protected final TableLayoutFactory tableLayoutFactory;
    private SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;


    protected AbstractSearchRequestExcelView(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ApplicationProperties appProperties, TableLayoutFactory tableLayoutFactory, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil)
    {
        super(authenticationContext, searchProvider, appProperties, searchRequestViewBodyWriterUtil);
        this.tableLayoutFactory = tableLayoutFactory;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
    }

    @Override
    public void writeSearchResults(SearchRequest searchRequest, SearchRequestParams searchRequestParams, Writer writer)
    {
        final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        final IssueTableLayoutBean columnLayout = getColumnLayout(searchRequest, authenticationContext.getUser());

        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("encoding", applicationProperties.getEncoding());
        params.put("colCount", columnLayout.getColumns().size());
        final VelocityRequestContext velocityRequestContext = (VelocityRequestContext) params.get("requestContext");
        params.put("link", SearchRequestViewUtils.getLink(searchRequest, velocityRequestContext.getBaseUrl(), authenticationContext.getUser()));

        try
        {
            long numberOfIssues = searchProvider.searchCount(searchRequest.getQuery(), authenticationContext.getUser());
            // the pager might not let us display all issues...so don't lie to the user. tell them how many are in the result.
            numberOfIssues = Math.min(numberOfIssues, searchRequestParams.getPagerFilter().getMax());
            if (numberOfIssues == 0)
            {
                params.put("noissues", Boolean.TRUE);
            }

            params.put("generatedInfo", SearchRequestViewUtils.getGeneratedInfo(authenticationContext.getUser()));
            params.put("resultsDescription", getResultsDescription(numberOfIssues));

            addLayoutProperties(params);
            params.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE)));

            writer.write(descriptor.getHtml("header", params));
            final IssueTableWriter issueTableWriter = new IssueTableWebComponent().getHtmlIssueWriter(writer, columnLayout, null, null);
            searchRequestViewBodyWriterUtil.writeTableBody(writer, issueTableWriter, searchRequest, searchRequestParams.getPagerFilter());
            writer.write(descriptor.getHtml("footer", params));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SearchException e)
        {
            e.printStackTrace();
        }
    }

    private String getResultsDescription(long searchResultsSize)
    {
        return authenticationContext.getI18nHelper().getText("navigator.excel.results.displayissues", String.valueOf(searchResultsSize), authenticationContext.getOutlookDate().formatDMYHMS(new Date()));
    }

    private void addLayoutProperties(Map<String, Object> map)
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        final String topBgColour = lookAndFeelBean.getTopBackgroundColour();

        String jiraLogo = lookAndFeelBean.getLogoUrl();
        final String jiraLogoWidth = lookAndFeelBean.getLogoWidth();
        final String jiraLogoHeight = lookAndFeelBean.getLogoHeight();

        if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = (new DefaultVelocityRequestContextFactory(applicationProperties)).getJiraVelocityRequestContext().getBaseUrl() + jiraLogo;
        }

        map.put("topBgColour", topBgColour);
        map.put("jiraLogo", jiraLogo);
        map.put("jiraLogoWidth", jiraLogoWidth);
        map.put("jiraLogoHeight", jiraLogoHeight);
    }

    @Override
    public void writeHeaders(final SearchRequest searchRequest, final RequestHeaders requestHeaders, final SearchRequestParams searchRequestParams)
    {
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
        WordViewUtils.writeEncodedAttachmentFilenameHeader(
                requestHeaders,
                JiraUrlCodec.encode(SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE))) + ".xls",
                searchRequestParams.getUserAgent(),
                applicationProperties.getEncoding());
    }

    protected abstract IssueTableLayoutBean getColumnLayout(SearchRequest searchRequest, User user);


}
