package com.atlassian.jira.admin.quicknav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * REST endpoint for providing meta-information about available web fragments.
 *
 * @since v4.4
 */
@AnonymousAllowed
@Path("/fragments")
@Produces (MediaType.APPLICATION_JSON)
public class WebFragmentsResource
{
    private final SimpleLinkManager linkManager;
    private final JiraAuthenticationContext authContext;
    private final SimpleLinkAliasProvider keywordsProvider;

    public WebFragmentsResource(SimpleLinkManager webManager, JiraAuthenticationContext authContext,
            SimpleLinkAliasProvider keywordsProvider)
    {
        this.linkManager = checkNotNull(webManager);
        this.authContext = authContext;
        this.keywordsProvider = keywordsProvider;
    }

    /**
     * Get web fragments for given location and section.
     *
     * @param request web request
     * @param location location to look in, must be specified
     * @param section section to look in, may be <code>null</code>
     * @return list of web fragments for given location
     */
    @GET
    @Path("/{location}")
    public Response getWebFragments(@Context HttpServletRequest request, @PathParam("location") String location,
            @QueryParam("section") String section)
    {
        User user = authContext.getLoggedInUser();
        checkUser(user);
        checkLocation(location);
        JiraHelper helper = new JiraHelper(request);
        List<WebItem> webItems = getWebItems(fullSectionKey(location, section), null, user, helper);
        List<WebSection> webSections = getWebSections(null, section != null ? section : location, user, helper);
        return Response.ok(new WebFragmentLocation(fullSectionKey(location, section), webItems, webSections)).build();
    }



    private void checkUser(User user)
    {
        if (user == null)
        {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    private void checkLocation(final String location)
    {
        if (location == null)
        {
            throw new WebApplicationException(badRequest("Parameter 'location' required"));
        }
    }

    private Response badRequest(String message)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).cacheControl(never()).build();
    }

    private List<WebItem> getWebItems(final String location, @Nullable final SimpleLinkSection mainSection,
            final User user, final JiraHelper helper)
    {
        List<SimpleLink> items = getItemDescriptors(location, user, helper);
        return Lists.transform(items, new Function<SimpleLink, WebItem>()
        {
            public WebItem apply(final SimpleLink input)
            {
                return toWebItemBean(input, location, mainSection);
            }
        });
    }

    private List<WebSection> getWebSections(@Nullable final SimpleLinkSection parent, final String location,
            final User user, final JiraHelper helper)
    {
        final List<SimpleLinkSection> descriptors = getSectionDescriptors(location, user, helper);
        return Lists.transform(descriptors, new Function<SimpleLinkSection, WebSection>()
        {
            public WebSection apply(final SimpleLinkSection input)
            {
                return toWebSectionBean(parent != null ? parent : input, input, location, user, helper);
            }
        });

    }

    private List<SimpleLink> getItemDescriptors(String location, User user, JiraHelper helper)
    {
        return linkManager.getLinksForSection(location, user, helper);
    }

    private List<SimpleLinkSection> getSectionDescriptors(String location, User user, JiraHelper helper)
    {
        return linkManager.getSectionsForLocation(location, user, helper);
    }

    private WebItem toWebItemBean(SimpleLink source, String section, SimpleLinkSection mainSection)
    {
        return new WebItem(source.getId(), source.getUrl(), source.getLabel(), section,
                keywordsProvider.aliasesFor(mainSection, source, authContext));
    }

    private WebSection toWebSectionBean(SimpleLinkSection mainSection, SimpleLinkSection source, String location, User user, JiraHelper helper)
    {
        List<WebSection> innerSections = getWebSections(mainSection, source.getId(), user, helper);
        List<WebItem> innerItems = getWebItems(fullSectionKey(location, source.getId()), mainSection, user, helper);
        return new WebSection(source.getId(), source.getLabel(), location, innerSections, innerItems);
    }

    private CacheControl never()
    {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoStore(true);
        cacheControl.setNoCache(true);
        return cacheControl;
    }

    private String fullSectionKey(final String location, final String sectionKey)
    {
        checkNotNull(location);
        if (sectionKey == null)
        {
            return location;
        }
        return location + "/" + sectionKey;
    }

    @XmlRootElement
    public static class WebFragmentLocation
    {
        @XmlAttribute
        private String key;

        @XmlElement
        private List<WebItem> items;

        @XmlElement
        private List<WebSection> sections;

        public WebFragmentLocation(String key, List<WebItem> items, List<WebSection> sections)
        {
            this.key = key;
            this.items = items;
            this.sections = sections;
        }

        public String key()
        {
            return key;
        }

        public List<WebItem> webItems()
        {
            return ImmutableList.copyOf(items);
        }

        public List<WebSection> webSections()
        {
            return ImmutableList.copyOf(sections);
        }
    }

    @XmlRootElement
    public static class WebSection extends WebFragmentLocation
    {
        @XmlAttribute private String label;
        @XmlAttribute private String location;

        public WebSection(String key, String label, String location, List<WebSection> sections, List<WebItem> items) {
            super(key, items, sections);
            this.label = label;
            this.location = location;
        }

        public String label()
        {
            return label;
        }

        public String location()
        {
            return location;
        }


    }

    @XmlRootElement
    public static class WebItem
    {
        @XmlAttribute private String key;
        @XmlAttribute private String linkUrl;
        @XmlAttribute private String label;
        @XmlAttribute private String section;
        @XmlElement private Set<String> keywords;

        public WebItem(String key, String linkUrl, String label, String section, Set<String> keywords) {
            this.key = key;
            this.linkUrl = linkUrl;
            this.label = label;
            this.keywords = keywords;
            this.section = section;
        }

        public String key()
        {
            return key;
        }

        public String linkUrl()
        {
            return linkUrl;
        }

        public String label()
        {
            return label;
        }

        public String section()
        {
            return section;
        }

        public Set<String> keywords()
        {
            return new LinkedHashSet<String>(keywords);
        }

    }
}
