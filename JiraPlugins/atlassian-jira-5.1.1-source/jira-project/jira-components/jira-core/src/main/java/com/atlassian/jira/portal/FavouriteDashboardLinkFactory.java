package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A SimpleLinkFactory that generates a list of SimpleLinks that point to the Favourite Filters of a user.
 *
 * @since v4.0
 */
public class FavouriteDashboardLinkFactory implements SimpleLinkFactory
{
    private static final int MAX_LABEL_LENGTH = 30;

    private final PortalPageService portalPageService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final I18nHelper.BeanFactory i18nFactory;
    private final UserHistoryManager userHistoryManager;

    public FavouriteDashboardLinkFactory(PortalPageService portalPageService, VelocityRequestContextFactory velocityRequestContextFactory,
                                         I18nHelper.BeanFactory i18nFactory, final UserHistoryManager userHistoryManager)
    {
        this.portalPageService = portalPageService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18nFactory = i18nFactory;
        this.userHistoryManager = userHistoryManager;
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

        final Collection<PortalPage> portalPages = portalPageService.getFavouritePortalPages(user);
        // Need to ensure they contain the baseurl in case they are loaded via ajax/rest
        final String baseUrl = requestContext.getBaseUrl();
        final I18nHelper i18n = i18nFactory.getInstance(user);
        final List<SimpleLink> links = new ArrayList<SimpleLink>();

        if (portalPages == null || portalPages.isEmpty())
        {

            links.add(new SimpleLinkImpl("dash_lnk_system", i18n.getText("menu.dashboard.view.system"),
                    i18n.getText("menu.dashboard.view.system.title"), null, null, null,
                    baseUrl + "/secure/Dashboard.jspa", null));
        }
        else
        {
            final Long currentDash = getCurrentDashboard(user);

            for (PortalPage portalPage : portalPages)
            {
                String style = null;
                final Long pageId = portalPage.getId();
                final String description = portalPage.getDescription();
                final String name = portalPage.getName();
                String shortName = name;
                if (shortName.length() > MAX_LABEL_LENGTH)
                {
                    shortName = shortName.substring(0, MAX_LABEL_LENGTH) + "...";
                }

                final String title = StringUtils.isBlank(description) ? name : i18n.getText("menu.dashboard.title", name, description);

                if (portalPages.size() > 1 && pageId.equals(currentDash))
                {
                    style = "bolded";
                }
                links.add(new SimpleLinkImpl("dash_lnk_" + pageId, shortName, title, null, style, null,
                         baseUrl + "/secure/Dashboard.jspa?selectPageId=" + pageId, null));
            }
        }
        return links;
    }

    private Long getCurrentDashboard(final User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        if (!history.isEmpty())
        {
            return Long.valueOf(history.get(0).getEntityId());
        }

        return null;
    }
}
