package com.atlassian.jira.gadgets.system;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.configurable.ObjectConfigurationTypes;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.dashboard.LegacyGadgetUrlProvider;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletAccessManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.component.DashboardPageConfigUrlFactory;
import com.atlassian.jira.web.component.DashboardPageConfigUrlFactoryImpl;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

/**
 * REST endpoint to retrieve the URL to display a legacy portlet.
 *
 * @since v4.0
 */
@Path ("legacy")
@AnonymousAllowed
public class LegacyPortletResource
{
    private static final Logger log = Logger.getLogger(LegacyPortletResource.class);
    private static final String LEGACY_BRIDGE_TEMPLATE = "gadgets/legacy/legacy-portlet-bridge.vm";

    private final PortletConfigurationManager portletConfigurationManager;
    private final JiraAuthenticationContext authenticationContext;
    private final PortletAccessManager portletAccessManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final WebResourceManager webResourceManager;
    private final DashboardPermissionService permissionService;
    private final LegacyGadgetUrlProvider legacyGadgetUrlFactory;
    private final PortalPageService portalPageService;
    private final TemplateRenderer renderer;

    public LegacyPortletResource(final PortletConfigurationManager portletConfigurationManager, final JiraAuthenticationContext authenticationContext,
            final PortletAccessManager portletAccessManager, final TemplateRenderer renderer, final VelocityRequestContextFactory velocityRequestContextFactory,
            final WebResourceManager webResourceManager, final DashboardPermissionService permissionService, final LegacyGadgetUrlProvider legacyGadgetUrlProvider, final PortalPageService portalPageService)
    {
        this.portletConfigurationManager = portletConfigurationManager;
        this.authenticationContext = authenticationContext;
        this.portletAccessManager = portletAccessManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.webResourceManager = webResourceManager;
        this.permissionService = permissionService;
        this.legacyGadgetUrlFactory = legacyGadgetUrlProvider;
        this.portalPageService = portalPageService;
        this.renderer = renderer;
    }

    @GET
    @Path ("{portletid}")
    @Produces (MediaType.APPLICATION_JSON)
    public Response getLegacyPortlet(@PathParam ("portletid") Long portletId)
    {
        final PortletConfiguration portletConfiguration = portletConfigurationManager.getByPortletId(portletId);
        //either no portlet with this id exists, or the portlet is not a legacy portlet.
        if (portletConfiguration == null)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final Long dashboardPageId = portletConfiguration.getDashboardPageId();

        //make sure we have a legacy portlet!
        if (portletConfiguration.getPortlet() == null && !legacyGadgetUrlFactory.isLegacyGadget(portletConfiguration.getGadgetURI()))
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final User user = authenticationContext.getLoggedInUser();
        boolean isConfigurable = false;
        boolean requiresInitialConfiguration = false;
        if (user != null)
        {
            final boolean isConfigurationNeeded = isConfigurationNeeded(user, portletConfiguration);
            isConfigurable = permissionService.isWritableBy(DashboardId.valueOf(dashboardPageId.toString()), user.getName()) && isConfigurationNeeded;
            final Long defaultDashboardId = portalPageService.getSystemDefaultPortalPage().getId();
            try
            {
                //if a legacy portlet requires configuration, but nothing has been stored yet, we should do some
                //initial configuration.  However if this is the default dashboard, don't show configuration screens.
                if (isConfigurable && portletConfiguration.getProperties().getKeys().size() == 0 &&
                        !defaultDashboardId.equals(portletConfiguration.getDashboardPageId()))
                {
                    requiresInitialConfiguration = true;
                }
            }
            catch (ObjectConfigurationException e)
            {
                throw new RuntimeException(e);
            }
        }

        final DashboardPageConfigUrlFactory urlFactory = new DashboardPageConfigUrlFactoryImpl(dashboardPageId);
        final String editUrl = urlFactory.getEditPortletUrl(portletConfiguration.getId()) + "&decorator=none&displayUserSummary=false";
        final String viewUrl = urlFactory.getRunPortletUrl(new PortletConfigurationAdaptorImpl(portletConfiguration, getPortletKey(portletConfiguration)));

        return Response.ok(new LegacyPortlet(isConfigurable, viewUrl, editUrl, requiresInitialConfiguration, authenticationContext.getI18nHelper().getText("gadget.common.configure"))).build();
    }

    @GET
    @Path ("spec/{portletKey}")
    @Produces (MediaType.APPLICATION_XML)
    public Response getLegacyPortletSpec(@PathParam ("portletKey") String portletKey)
    {
        if (StringUtils.isEmpty(portletKey))
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final User user = authenticationContext.getLoggedInUser();
        final I18nHelper i18n = authenticationContext.getI18nHelper();

        final Portlet portlet = user == null ? portletAccessManager.getPortlet(portletKey) : portletAccessManager.getPortlet(user, portletKey);

        final StringWriter requiredResource = new StringWriter();
        webResourceManager.includeResources(ImmutableList.<String>of("com.atlassian.jira.gadgets:common"), requiredResource, UrlMode.ABSOLUTE);
        final MapBuilder<String, Object> mapBuilder = MapBuilder.<String, Object>newBuilder().
                add("i18n", i18n).
                add("baseurlHtml", velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl()).
                add("requiredResourceHtml", requiredResource.toString());

        final String template = LEGACY_BRIDGE_TEMPLATE;
        if (portlet != null)
        {
            mapBuilder.add("portletTitle", portlet.getName()).
                    add("portletDescription", portlet.getDescription());
        }
        else
        {
            mapBuilder.add("portletTitle", i18n.getText("gadget.common.unknown.legacy.portlet")).
                    add("portletDescription", i18n.getText("gadget.common.unknown.legacy.portlet.description", portletKey));
        }

        final StringWriter gadgetXml = new StringWriter();
        try
        {
            renderer.render(template, mapBuilder.toMap(), gadgetXml);
        }
        catch (IOException e)
        {
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok(gadgetXml.toString()).build();
    }

    private boolean isConfigurationNeeded(final User remoteUser, final PortletConfiguration portletConfiguration)
    {
        final Map<String, User> remoteuserAsMap = MapBuilder.<String, User>newBuilder().add("User", remoteUser).toMap();
        Portlet portlet = portletConfiguration.getPortlet();
        if (portlet == null)
        {
            final String portletKey = legacyGadgetUrlFactory.extractPortletKey(portletConfiguration.getGadgetURI());
            portlet = portletAccessManager.getPortlet(portletKey);
        }

        if (portlet == null)
        {
            throw new RuntimeException("Null portlet for portletconfiguration with id '" + portletConfiguration.getId() + "', user " + remoteUser);
        }
        final ObjectConfiguration oc;
        try
        {
            oc = portlet.getObjectConfiguration(remoteuserAsMap);
        }
        catch (ObjectConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        return !oc.allFieldsHidden();
    }

    private String getPortletKey(final PortletConfiguration portletConfiguration)
    {
        if (portletConfiguration.getPortlet() == null)
        {
            //need to properly lookup the portlet the get the right id, since the RunPortlet action
            //doesn't like legacy style portlet ids like INTRODUCTION
            final String portletKey = legacyGadgetUrlFactory.extractPortletKey(portletConfiguration.getGadgetURI());
            final Portlet portlet = portletAccessManager.getPortlet(portletKey);
            if (portlet != null)
            {
                return portlet.getId();
            }
            else
            {
                throw new RuntimeException("No portlet for portletKey '" + portletKey + "' found!");
            }
        }
        else
        {
            return portletConfiguration.getPortlet().getId();
        }
    }

    /**
     * Implementation that uses the portletKey provided rather than trying to look it up via the portlet, since that may
     * result in a NPE if this is a portlet with a gadget url rather than portlet key.
     */
    static class PortletConfigurationAdaptorImpl implements DashboardPageConfigUrlFactory.PortletConfigurationAdaptor
    {
        private final PortletConfiguration portletConfiguration;
        private final String portletKey;

        PortletConfigurationAdaptorImpl(final PortletConfiguration portletConfiguration, final String portletKey)
        {
            this.portletConfiguration = portletConfiguration;
            this.portletKey = portletKey;
        }

        public String getPropertyAsString(final String key)
        {
            try
            {
                if (portletConfiguration.getObjectConfiguration().getFieldType(key) == ObjectConfigurationTypes.TEXT)
                {
                    return portletConfiguration.getTextProperty(key);
                }
                return portletConfiguration.getProperty(key);
            }
            catch (final ObjectConfigurationException e)
            {
                //the most likely case for this to happen is that a portlet had some configuration stored, then
                //the portlet was changed to no longer have a particular field. JRA-18825 
                final String logMessage = new StringBuilder().
                        append("Found portlet property with key '").
                        append(key).
                        append("' that shouldn't exist for portlet '").
                        append(portletConfiguration.getId()).
                        append("' of type '").
                        append(portletKey).
                        append("'. This could be due to JRA-18825.").toString();
                if (log.isDebugEnabled())
                {
                    log.debug(logMessage, e);
                }
                else
                {
                    log.warn(logMessage);
                }
                return "";
            }
        }

        public String getPortletId()
        {
            return portletKey;
        }

        public Collection getKeys()
        {
            try
            {
                return portletConfiguration.getProperties().getKeys();
            }
            catch (final ObjectConfigurationException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class LegacyPortlet
    {
        @XmlElement
        private String url;
        @XmlElement
        private String editUrl;
        @XmlElement
        private boolean isConfigurable;
        @XmlElement
        private boolean requiresInitialConfiguration;
        @XmlElement
        private String editName;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private LegacyPortlet()
        { }

        public LegacyPortlet(final boolean configurable, final String url, final String editUrl, final boolean requiresInitialConfiguration, final String editName)
        {
            this.isConfigurable = configurable;
            this.url = url;
            this.editUrl = editUrl;
            this.requiresInitialConfiguration = requiresInitialConfiguration;
            this.editName = editName;
        }

        public boolean isConfigurable()
        {
            return isConfigurable;
        }

        public String getUrl()
        {
            return url;
        }

        public String getEditUrl()
        {
            return editUrl;
        }

        public boolean isRequiresInitialConfiguration()
        {
            return requiresInitialConfiguration;
        }
    }
    ///CLOVER:ON
}

