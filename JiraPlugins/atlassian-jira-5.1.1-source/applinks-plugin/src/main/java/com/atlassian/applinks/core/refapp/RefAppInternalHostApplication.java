package com.atlassian.applinks.core.refapp;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.application.refapp.RefAppApplicationType;
import com.atlassian.applinks.api.application.refapp.RefAppCharlieEntityType;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.AbstractInternalHostApplication;
import com.atlassian.applinks.host.spi.DefaultEntityReference;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.util.InstanceNameGenerator;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.project.ProjectManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The refapp does not support the concept of an "instance name", so our
 * application-specific modules include an admin page that allows the user to
 * specify a unique name for its instance. This name is then stored using SAL's
 * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
 * <p/>
 * When the refapp is first started up and the user has not yet provided an
 * instance name, a name is automatically generated.
 * 
 * NB:
 * Has to implement {@link com.atlassian.sal.api.lifecycle.LifecycleAware} again, this is due to a possible
 * bug https://studio.atlassian.com/browse/PLUG-635 in the plugins framework, where the byte code scanning doesn't detect this interface as
 * required and will lead to a ClassNotFoundException, when trying to auto-wire this class.
 */
public class RefAppInternalHostApplication extends AbstractInternalHostApplication implements LifecycleAware
{
    public static final String REFAPP_PREFIX = "com.atlassian.applinks.host.refapp";
    public static final String INSTANCE_NAME_KEY = REFAPP_PREFIX + ".instanceName";
    public static final String SERVER_ID = REFAPP_PREFIX + ".serverId";
    private final ApplicationProperties applicationProperties;
    private final ProjectManager projectManager;

    private final PluginSettingsFactory pluginSettingsFactory;
    private final UserManager userManager;
    private final InternalTypeAccessor typeAccessor;
    private final PluginSettings pluginSettings;
    private static final Logger logger = Logger.getLogger(RefAppInternalHostApplication.class);

    public RefAppInternalHostApplication(final PluginSettingsFactory pluginSettingsFactory,
                                         final ApplicationProperties applicationProperties,
                                         final PluginAccessor pluginAccessor,
                                         final ProjectManager projectManager,
                                         final InternalTypeAccessor typeAccessor,
                                         final UserManager userManager)
    {
        super(pluginAccessor);
        this.applicationProperties = applicationProperties;
        this.projectManager = projectManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.userManager = userManager;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        this.typeAccessor = typeAccessor;
    }

    //cache the base url as we may want to resolve the hostname and this may not be quick
    private final LazyReference<URI> baseUrl = new LazyReference<URI>()
    {
        @Override
        protected URI create() throws Exception
        {
            final String storedBaseUrl = applicationProperties.getBaseUrl();
            URI url;
            try
            {
                url = new URI(storedBaseUrl);
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException(String.format(
                        "ApplicationProperties.getBaseUrl() returned invalid URI (%s). Reason: %s", storedBaseUrl,
                        e.getReason()));
            }

            try
            {
                final URL baseUrl = new URL(storedBaseUrl);

                if ("localhost".equalsIgnoreCase(baseUrl.getHost()))

                {
                    url = new URL(
                            baseUrl.getProtocol(),
                            InetAddress.getLocalHost().getHostName(), //attempt to resolve the hostname
                            baseUrl.getPort(),
                            baseUrl.getFile()).toURI();
                }
            }
            catch (Exception e) //fall back to original behaviour if any problem encountered
            {
                logger.error("Failed to resolve local hostname. Returning localhost.", e);
            }

            return url;
        }
    };

    public URI getBaseUrl()
    {
        return baseUrl.get();
    }

    /**
     * Checks to see if an instance name has already been provided. If not,
     * auto-generate it.
     */
    public void onStart()
    {
        /**
         * Checks to see if the server id has already been provided. If not,
         * auto-generate it.
         */
        if (StringUtils.isEmpty((String) pluginSettings.get(SERVER_ID)))
        {
            pluginSettings.put(SERVER_ID, UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty((String) pluginSettings.get(INSTANCE_NAME_KEY)))
        {
            final String baseUrl = applicationProperties.getBaseUrl();

            String instanceName = "RefApp";
            if (!StringUtils.isEmpty(baseUrl))
            {
                try
                {
                    instanceName += (" - " + new InstanceNameGenerator().generateInstanceName(
                            applicationProperties.getBaseUrl()));
                }
                catch (MalformedURLException me)
                {
                    //ignore
                }
            }
            pluginSettings.put(INSTANCE_NAME_KEY, instanceName);
        }
    }

    public String getName()
    {
        return (String) pluginSettings.get(INSTANCE_NAME_KEY);
    }

    public ApplicationType getType()
    {
        return checkNotNull(typeAccessor.getApplicationType(RefAppApplicationType.class), "RefAppApplicationType not installed!");
    }

    public boolean doesEntityExist(final String key, final Class<? extends EntityType> type)
    {
        return RefAppCharlieEntityType.class.isAssignableFrom(type) && projectManager.getAllProjectKeys().contains(key);
    }

    public boolean doesEntityExistNoPermissionCheck(final String key, final Class<? extends EntityType> type)
    {
        return doesEntityExist(key, type);
    }

    public EntityReference toEntityReference(final Object domainObject)
    {
        if (!(domainObject instanceof String))
        {
            throw new IllegalArgumentException("RefApp has no domain object, use a String key");
        }

        final String key = (String) domainObject;

        if (!projectManager.getAllProjectKeys().contains(key))
        {
            throw new IllegalArgumentException("Entity with key " + key + " does not exist");
        }

        return toEntityReference(key, RefAppCharlieEntityType.class);
    }

    public EntityReference toEntityReference(final String key, final Class<? extends EntityType> type)
    {
        final String name = (String) pluginSettingsFactory.createSettingsForKey(key).get("charlie.name");

        return new DefaultEntityReference(key, name,
                checkNotNull(typeAccessor.getEntityType(RefAppCharlieEntityType.class), "Couldn't load RefAppCharlieEntityType"));
    }

    public Iterable<EntityReference> getLocalEntities()
    {
        return Iterables.transform(projectManager.getAllProjectKeys(), new Function<String, EntityReference>()
        {
            public EntityReference apply(final String key)
            {
                return toEntityReference(key, RefAppCharlieEntityType.class);
            }
        });
    }

    public URI getDocumentationBaseUrl()
    {
        return URIUtil.uncheckedCreate("http://confluence.atlassian.com/display/APPLINKS");
    }

    public boolean canManageEntityLinksFor(final EntityReference entityReference)
    {
        final String username = userManager.getRemoteUsername();
 	 	return username != null && userManager.isSystemAdmin(username);
    }

    public ApplicationId getId()
    {
        return new ApplicationId((String) pluginSettings.get(SERVER_ID));
    }

    public boolean hasPublicSignup()
    {
        return false;
    }
}
