package com.atlassian.jira.issue.search;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple Link Factory for creating links to favourite filters
 *
 * @since v4.0
 */
public class FavouriteFilterLinkFactory implements SimpleLinkFactory
{
    private static final Logger log = Logger.getLogger(FavouriteFilterLinkFactory.class);

    private static final int DEFAULT_FILTER_DROPDOWN_ITEMS = 10;
    private static final int MAX_LABEL_LENGTH = 30;

    private final SearchRequestService searchRequestService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory i18nFactory;

    public FavouriteFilterLinkFactory(SearchRequestService searchRequestService, VelocityRequestContextFactory velocityRequestContextFactory,
                                      ApplicationProperties applicationProperties, I18nHelper.BeanFactory i18nFactory)
    {
        this.searchRequestService = searchRequestService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
        this.i18nFactory = i18nFactory;
    }

    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    public List<SimpleLink> getLinks(com.opensymphony.user.User user, Map<String, Object> params)
    {
        final Collection<SearchRequest> filters = searchRequestService.getFavouriteFilters(user);
        final List<SimpleLink> links = new ArrayList<SimpleLink>();

        if (filters != null && !filters.isEmpty())
        {
            final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
            final I18nHelper i18n = i18nFactory.getInstance(user);

            // Need ot ensure they contain the baseurl in case they are loaded via ajax/rest
            final String baseUrl = requestContext.getBaseUrl();
            final int maxItems = getMaxDropdownItems();

            final Iterator<SearchRequest> filterIterator = filters.iterator();
            for (int i = 0; i < maxItems && filterIterator.hasNext(); i++)
            {
                final SearchRequest filter = filterIterator.next();

                final String name = filter.getName();
                String shortName = name;
                if (shortName.length() > MAX_LABEL_LENGTH)
                {
                    shortName = shortName.substring(0, MAX_LABEL_LENGTH) + "...";
                }
                final String title = StringUtils.isBlank(filter.getDescription()) ? name : i18n.getText("menu.issues.filter.title", name, filter.getDescription());


                links.add(new SimpleLinkImpl("filter_lnk_" + filter.getId(), shortName, title, null, null, null,
                        baseUrl + "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId(), null));
            }

            if (filters.size() > maxItems)
            {
                final String url = baseUrl + "/secure/ManageFilters.jspa?filterView=favourites";
                links.add(new SimpleLinkImpl("filter_lnk_more", i18n.getText("menu.issues.filter.more"),
                        i18n.getText("menu.issues.filter.more.desc"), null, null, null, url, null));
            }
        }
        return links;
    }

    private int getMaxDropdownItems()
    {
        int maxItems = DEFAULT_FILTER_DROPDOWN_ITEMS;
        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS));
        }
        catch (NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.issue.filter.dropdown.items'.  Should be a number.");
        }

        return maxItems;
    }
}
