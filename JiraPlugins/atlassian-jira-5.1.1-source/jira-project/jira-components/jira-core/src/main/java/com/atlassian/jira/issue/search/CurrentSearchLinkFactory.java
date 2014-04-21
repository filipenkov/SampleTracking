package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Link factory for creating a link to the current search.  If we ever do search history, we can use this to provide links
 *
 * @since v4.0
 */
public class CurrentSearchLinkFactory implements SimpleLinkFactory
{
    private static final int MAX_MENU_LABEL_LENGTH = 30;

    private static final Logger log = Logger.getLogger(CurrentSearchLinkFactory.class);

    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final I18nHelper.BeanFactory i18nFactory;
    private final SearchProvider searchProvider;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    public CurrentSearchLinkFactory(VelocityRequestContextFactory velocityRequestContextFactory, I18nHelper.BeanFactory i18nFactory,
            SearchProvider searchProvider, final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18nFactory = i18nFactory;
        this.searchProvider = searchProvider;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
    {
        final VelocityRequestContext velocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final List<SimpleLink> links = new ArrayList<SimpleLink>(1);

        final SearchRequest searchRequest = getSearchRequest(velocityRequestContext);

        if (searchRequest != null)
        {

            final I18nHelper i18n = i18nFactory.getInstance(user);
            long count = -1;
            try
            {
                count = searchProvider.searchCount(searchRequest.getQuery(), user);
            }
            catch (SearchException e)
            {
                log.warn("Error thrown while getting count for current search.", e);
                // ignoring as count is just a nice to have.
            }

            final String baseUrl = velocityRequestContext.getBaseUrl();

            final String name = getName(i18n, searchRequest);
            String shortName = name;
            if (name.length() > MAX_MENU_LABEL_LENGTH)
            {
                shortName = shortName.substring(0, MAX_MENU_LABEL_LENGTH) + "...";
            }
            final String display = getFilterDisplay(i18n, shortName, searchRequest, count);

            final String title = StringUtils.isBlank(searchRequest.getDescription()) ? name : i18n.getText("menu.issues.current.search.title", name, searchRequest.getDescription());

            links.add(new SimpleLinkImpl("curr_search_lnk_" + getId(searchRequest), display, title, null, null, null,
                    baseUrl + "/secure/IssueNavigator.jspa?mode=hide", null));
        }

        return links;
    }

    private String getId(SearchRequest searchRequest)
    {
        return searchRequest.isLoaded() ? searchRequest.getId().toString() : "unsaved";
    }

    private String getName(I18nHelper i18n, SearchRequest searchRequest)
    {
        return searchRequest.isLoaded() ? searchRequest.getName() : i18n.getText("menu.issues.current.search.unsaved");
    }


    private String getFilterDisplay(I18nHelper i18n, String name, SearchRequest searchRequest, long count)
    {

        if (count == 0)
        {
            return i18n.getText("menu.issues.current.search.no.issues", name);
        }
        else if (count == 1)
        {
            return i18n.getText("menu.issues.current.search.one.issue", name);
        }
        else if (count > 1)
        {
            return i18n.getText("menu.issues.current.search.issues", name, count + "");
        }
        return name;
    }

    private SearchRequest getSearchRequest(final VelocityRequestContext velocityRequestContext)
    {
        if (velocityRequestContext != null)
        {
            final VelocityRequestSession session = velocityRequestContext.getSession();
            if (session != null)
            {
                SessionSearchRequestManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createSearchRequestManager(session);
                return sessionSearchRequestManager.getCurrentObject();
            }
        }

        return null;
    }
}
