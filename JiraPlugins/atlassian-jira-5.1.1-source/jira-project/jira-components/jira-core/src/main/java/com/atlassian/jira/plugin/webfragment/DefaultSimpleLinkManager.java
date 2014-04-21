package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebSectionModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.plugin.web.model.WebLink;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the SimpleLinkManager This actually uses combines SimpleLinkFactory lists with {@link
 * com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} links and {@link com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor}
 * sections, respecting weights.
 *
 * @since v4.0
 */
public class DefaultSimpleLinkManager implements SimpleLinkManager
{
    private static final Logger log = Logger.getLogger(DefaultSimpleLinkManager.class);

    private final JiraWebInterfaceManager webInterfaceManager;
    private final SimpleLinkFactoryModuleDescriptors simpleLinkFactoryModuleDescriptors;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public DefaultSimpleLinkManager(final JiraWebInterfaceManager webInterfaceManager, final SimpleLinkFactoryModuleDescriptors simpleLinkFactoryModuleDescriptors, final JiraAuthenticationContext authenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.simpleLinkFactoryModuleDescriptors = simpleLinkFactoryModuleDescriptors;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * This determines whether a location should be loaded lazily if possible. This loops through all sections for the
     * location and then retrieves all {@link com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor}
     * for those sections and sees whether any of the factories say they should be lazy.  If any say true, return true.
     *
     * @param location The location to check for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return true if any of the underlying factories for this location say they should be lazy, false otherwise
     */
    public boolean shouldLocationBeLazy(@NotNull final String location, final User remoteUser, @NotNull final JiraHelper jiraHelper)
    {
        @SuppressWarnings ( { "unchecked" }) final List<JiraWebSectionModuleDescriptor> sections = webInterfaceManager.getDisplayableSections(location, remoteUser, jiraHelper);
        final Iterable<SimpleLinkFactoryModuleDescriptor> linkFactories = simpleLinkFactoryModuleDescriptors.get();

        for (final JiraWebSectionModuleDescriptor section : sections)
        {
            // don't do String concatenation here
            class SectionPredicate implements Predicate<SimpleLinkFactoryModuleDescriptor>
            {
                final int locLength = location.length();
                final String key = section.getKey();
                final int length = locLength + 1 + key.length();

                public boolean apply(final SimpleLinkFactoryModuleDescriptor linkFactory)
                {
                    return nameMatches(linkFactory.getSection()) && linkFactory.shouldBeLazy();
                }

                boolean nameMatches(final String name)
                {
                    return (name.length() == length) && name.startsWith(location) && (name.charAt(locLength) == '/') && name.endsWith(key);
                }
            }
            if (Iterables.any(linkFactories, new SectionPredicate()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * This determines whether a section should be loaded lazily if possible.
     *
     * @param section The section to check for
     * @return true if any of the underlying factories for this section say they should be lazy, false otherwise
     */
    public boolean shouldSectionBeLazy(final String section)
    {
        for (final SimpleLinkFactoryModuleDescriptor linkFactory : simpleLinkFactoryModuleDescriptors.get())
        {
            if (section.equals(linkFactory.getSection()))
            {
                if (linkFactory.shouldBeLazy())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public List<SimpleLink> getLinksForSection(final String section, final User remoteUser, final JiraHelper jiraHelper)
    {
        //noinspection unchecked
        return getLinks(section, webInterfaceManager.getDisplayableItems(section, remoteUser, jiraHelper), remoteUser, jiraHelper);
    }

    public List<SimpleLink> getLinksForSectionIgnoreConditions(@NotNull final String section, final User remoteUser, @NotNull final JiraHelper jiraHelper)
    {
        //noinspection unchecked
        return getLinks(section, webInterfaceManager.getItems(section), remoteUser, jiraHelper);
    }

    private List<SimpleLink> getLinks(final String section, final List<JiraWebItemModuleDescriptor> items, final User user, final JiraHelper jiraHelper)
    {
        final List<SimpleLink> returnLinks = new ArrayList<SimpleLink>();

        final List<SimpleLinkFactoryModuleDescriptor> matchingFactories = new ArrayList<SimpleLinkFactoryModuleDescriptor>();
        for (final SimpleLinkFactoryModuleDescriptor linkFactory : simpleLinkFactoryModuleDescriptors.get())
        {
            if (section.equals(linkFactory.getSection()))
            {
                matchingFactories.add(linkFactory);
            }
        }

        final Iterator<SimpleLinkFactoryModuleDescriptor> factoryIterator = matchingFactories.iterator();
        final Iterator<JiraWebItemModuleDescriptor> itemIterator = items.iterator();
        SimpleLinkFactoryModuleDescriptor factory = factoryIterator.hasNext() ? factoryIterator.next() : null;
        JiraWebItemModuleDescriptor item = itemIterator.hasNext() ? itemIterator.next() : null;

        while ((factory != null) || (item != null))
        {

            if ((factory != null) && (item != null))
            {
                if (factory.getWeight() < item.getWeight())
                {
                    final SimpleLinkFactory factoryModule = factory.getModule();
                    if (factoryModule != null)
                    {
                        returnLinks.addAll(factoryModule.getLinks(user, jiraHelper.getContextParams()));
                    }
                    factory = factoryIterator.hasNext() ? factoryIterator.next() : null;
                }
                else
                {
                    final SimpleLink link = convertWebItemToSimpleLink(item, user, jiraHelper);
                    if (link != null)
                    {
                        returnLinks.add(link);
                    }
                    item = itemIterator.hasNext() ? itemIterator.next() : null;
                }
            }
            else if (factory == null)
            {
                final SimpleLink link = convertWebItemToSimpleLink(item, user, jiraHelper);
                if (link != null)
                {
                    returnLinks.add(link);
                }
                item = itemIterator.hasNext() ? itemIterator.next() : null;
            }
            else
            //item == null
            {
                final SimpleLinkFactory factoryModule = factory.getModule();
                if (factoryModule != null)
                {
                    returnLinks.addAll(factoryModule.getLinks(user, jiraHelper.getContextParams()));
                }
                factory = factoryIterator.hasNext() ? factoryIterator.next() : null;
            }
        }
        return returnLinks;
    }

    // Converts JiraWebItemModuleDescriptors to SimpleLinks
    private SimpleLink convertWebItemToSimpleLink(final JiraWebItemModuleDescriptor item, final User user, final JiraHelper helper)
    {
        try
        {
            final WebLabel label = item.getLabel();
            String labelStr = null;
            final Map<String, Object> ctx = MapBuilder.<String, Object>newBuilder().add(JiraWebInterfaceManager.CONTEXT_KEY_USER, user)
                    .add(JiraWebInterfaceManager.CONTEXT_KEY_HELPER, helper)
                    .add(JiraWebInterfaceManager.CONTEXT_KEY_I18N, authenticationContext.getI18nHelper()).toMutableMap();
            ctx.putAll(helper.getContextParams());
            if (label != null)
            {
                if (label instanceof JiraWebLabel)
                {
                    labelStr = ((JiraWebLabel) label).getDisplayableLabel(user, helper);
                }
                else
                {
                    labelStr = label.getDisplayableLabel(helper.getRequest(), ctx);
                }
            }

            final WebLabel tooltip = item.getTooltip();
            String tooltipStr = null;
            if (tooltip != null)
            {
                if (tooltip instanceof JiraWebLabel)
                {
                    tooltipStr = ((JiraWebLabel) tooltip).getDisplayableLabel(user, helper);
                }
                else
                {
                    tooltipStr = tooltip.getDisplayableLabel(helper.getRequest(), ctx);
                }
            }
            final WebLink iconUrl = item.getIcon() == null ? null : item.getIcon().getUrl();
            String iconUrlStr = null;
            if (iconUrl != null)
            {
                if (iconUrl instanceof JiraWebLink)
                {
                    iconUrlStr = ((JiraWebLink) iconUrl).getDisplayableUrl(user, helper);
                }
                else
                {
                    iconUrlStr = iconUrl.getDisplayableUrl(helper.getRequest(), ctx);
                }
            }

            final WebLink url = item.getLink();
            String urlStr = null;
            String accessKey = null;
            String id = null;
            if (url != null)
            {
                id = url.getId();
                if (url instanceof JiraWebLink)
                {
                    urlStr = ((JiraWebLink) url).getRenderedUrl(user, helper);
                    accessKey = ((JiraWebLink) url).getAccessKey(user, helper);
                }
                else
                {
                    urlStr = url.getRenderedUrl(ctx);
                    accessKey = url.getAccessKey(ctx);
                }
                if (StringUtils.isNotBlank(urlStr) && !(urlStr.startsWith("http://") || urlStr.startsWith("https://")))
                {
                    final VelocityRequestContext velocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
                    urlStr = velocityRequestContext.getBaseUrl() + urlStr;
                }
            }
            if (id == null)
            {
                id = item.getKey();
            }
            return new SimpleLinkImpl(id, labelStr, tooltipStr, iconUrlStr, item.getStyleClass(),
                    item.getParams(), urlStr, accessKey);
        }
        catch (RuntimeException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("RuntimeException converting item '" + item.getCompleteKey() + "' to Simple link. This link will be skipped.", e);
            }
            else
            {
                log.error("RuntimeException converting item '" + item.getCompleteKey() + "' to Simple link. This link will be skipped. " + e.getMessage());
            }
        }

        return null;
    }

    public List<SimpleLinkSection> getSectionsForLocation(final String location, final User remoteUser, final JiraHelper jiraHelper)
    {
        @SuppressWarnings ( { "unchecked" })
        final List<JiraWebSectionModuleDescriptor> sections = webInterfaceManager.getDisplayableSections(location, remoteUser, jiraHelper);

        final List<SimpleLinkSection> returnSections = new ArrayList<SimpleLinkSection>(sections.size());

        for (final JiraWebSectionModuleDescriptor section : sections)
        {
            returnSections.add(convertWebSectionToSimpleLinkSection(section, remoteUser, jiraHelper));
        }
        return returnSections;
    }

    public List<SimpleLinkSection> getNotEmptySectionsForLocation(@NotNull String location, User remoteUser, @NotNull JiraHelper jiraHelper)
    {
       List<SimpleLinkSection> allSections = getSectionsForLocation(location,remoteUser,jiraHelper);
       List<SimpleLinkSection> notEmptySections = new ArrayList<SimpleLinkSection>(allSections.size());
       for (SimpleLinkSection section : allSections)
       {
           List<SimpleLink> links = getLinksForSection(location+"/"+section.getId(),remoteUser,jiraHelper);

           if (links.size() > 0)
           {
               notEmptySections.add(section);
               continue;  // trolling through all subsections could be intensive.
           }

           List<SimpleLinkSection> subSections = getNotEmptySectionsForLocation(section.getId(),remoteUser,jiraHelper);

           if (subSections.size() > 0)
           {
               notEmptySections.add(section);
           }
       }
        return notEmptySections;
    }

    // Converts JiraWebSectionModuleDescriptors to SimpleLinkSections
    private SimpleLinkSection convertWebSectionToSimpleLinkSection(final JiraWebSectionModuleDescriptor descriptor, final User user, final JiraHelper helper)
    {
        final WebLabel label = descriptor.getLabel();
        String labelStr = null;
        final Map<String, Object> ctx = MapBuilder.<String, Object>newBuilder().add(JiraWebInterfaceManager.CONTEXT_KEY_USER, user)
                .add(JiraWebInterfaceManager.CONTEXT_KEY_HELPER, helper)
                .add(JiraWebInterfaceManager.CONTEXT_KEY_I18N, authenticationContext.getI18nHelper()).toMutableMap();
        ctx.putAll(helper.getContextParams());
        if (label != null)
        {
            if (label instanceof JiraWebLabel)
            {
                labelStr = ((JiraWebLabel) label).getDisplayableLabel(user, helper);
            }
            else
            {
                labelStr = label.getDisplayableLabel(helper.getRequest(), ctx);
            }
        }
        final WebLabel tooltip = descriptor.getTooltip();
        String tooltipStr = null;
        if (tooltip != null)
        {
            if (tooltip instanceof JiraWebLabel)
            {
                tooltipStr = ((JiraWebLabel) tooltip).getDisplayableLabel(user, helper);
            }
            else
            {
                tooltipStr = tooltip.getDisplayableLabel(helper.getRequest(), ctx);
            }
        }

        return new SimpleLinkSectionImpl(descriptor.getKey(), labelStr, tooltipStr, null, null, descriptor.getParams());
    }

    public SimpleLinkSection getSectionForURL (@NotNull String topLevelSection, @NotNull String URL, User remoteUser, JiraHelper jiraHelper)
    {
        return findWebSectionForURL (topLevelSection, URL, remoteUser, jiraHelper);
    }

    private SimpleLinkSection findWebSectionForURL (String currentLocation, String targetURL, User remoteUser, JiraHelper jiraHelper) {
        List<SimpleLinkSection> sections = getSectionsForLocation(currentLocation, remoteUser, jiraHelper);

        SimpleLinkSection sectionForURL = null;

        OuterLoop:
        for (SimpleLinkSection section : sections)
        {
            // check items
            String subSection = currentLocation + "/" + section.getId();
            List<SimpleLink> links = getLinksForSection (subSection, remoteUser, jiraHelper);
            for (SimpleLink link : links)
            {
                if (targetURL.endsWith(JiraUrl.extractActionFromURL(link.getUrl())))
                {
                    // yay!
                    sectionForURL = new SimpleLinkSectionImpl(subSection, (SimpleLinkSectionImpl) section);
                    break OuterLoop;
                }
            }

            // check subsections
            if (sectionForURL == null)
            {
                sectionForURL = findWebSectionForURL (section.getId(), targetURL, remoteUser, jiraHelper);
                if (sectionForURL != null)
                {
                    break OuterLoop;
                }
            }
        }

        return sectionForURL;
    }
}
