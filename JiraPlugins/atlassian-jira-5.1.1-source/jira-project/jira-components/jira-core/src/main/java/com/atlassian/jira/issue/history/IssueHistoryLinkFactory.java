package com.atlassian.jira.issue.history;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.query.Query;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple Link Factory for creating links to recently view issues.
 *
 * @since v4.0
 */
public class IssueHistoryLinkFactory implements SimpleLinkFactory
{
    private static final Logger log = Logger.getLogger(IssueHistoryLinkFactory.class);

    private final UserIssueHistoryManager userHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final SearchService searchService;
    private final I18nHelper.BeanFactory i18nFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private static final int MAX_LABEL_LENGTH = 30;

    public IssueHistoryLinkFactory(VelocityRequestContextFactory velocityRequestContextFactory, UserIssueHistoryManager userHistoryManager,
                                   ApplicationProperties applicationProperties, SearchService searchService, I18nHelper.BeanFactory i18nFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.userHistoryManager = userHistoryManager;
        this.applicationProperties = applicationProperties;
        this.searchService = searchService;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
    {
        final List<Issue> history = userHistoryManager.getShortIssueHistory(user);
        final List<SimpleLink> links = new ArrayList<SimpleLink>();

        if (history != null && !history.isEmpty())
        {
            final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
            // Need ot ensure they contain the baseurl in case they are loaded via ajax/rest
            final String baseUrl = requestContext.getBaseUrl();

            final int maxItems = getMaxDropdownItems();

            // we actually display one less so we know when to add a more link
            for (int i = 0; i < maxItems - 1 && i < history.size(); i++)
            {
                final Issue issue = history.get(i);
                final String label = issue.getKey() + " " + issue.getSummary();
                String shortLabel = label;
                if (shortLabel.length() > MAX_LABEL_LENGTH)
                {
                    shortLabel = shortLabel.substring(0, MAX_LABEL_LENGTH) + "...";
                }
                String iconUrl = issue.getIssueTypeObject().getIconUrl();
                if (!iconUrl.startsWith("http://") && !iconUrl.startsWith("https://"))
                {
                    iconUrl = baseUrl + iconUrl;
                }
                links.add(new SimpleLinkImpl("issue_lnk_" + issue.getId(), shortLabel, label, iconUrl, null, null,
                        baseUrl + "/browse/" + issue.getKey(), null));
            }

            if (history.size() >= maxItems)
            {
                final I18nHelper i18n = i18nFactory.getInstance(user);
                final Query query = JqlQueryBuilder.newBuilder().where().issueInHistory().buildQuery();

                final String url = baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide" + searchService.getQueryString(user, query);


                links.add(new SimpleLinkImpl("issue_lnk_more", i18n.getText("menu.issues.history.more"),
                        i18n.getText("menu.issues.history.more.desc"), null, null, null, url, null));
            }
        }
        return links;
    }

    private int getMaxDropdownItems()
    {
        int maxItems = UserIssueHistoryManager.DEFAULT_ISSUE_HISTORY_DROPDOWN_ITEMS;

        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS));
        }
        catch (NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.dropdown.items'.  Should be a number.");
        }

        return maxItems;
    }

}
