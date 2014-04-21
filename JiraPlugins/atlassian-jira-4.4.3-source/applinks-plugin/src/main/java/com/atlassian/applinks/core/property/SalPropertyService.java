package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.PropertySet;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import static com.atlassian.applinks.spi.application.TypeId.getTypeId;

/**
 * Default {@link PropertyService} implementation that delegates to SAL
 * <p/>
 * NOTE - this implementation means that all {@link SalPropertySet}s are backed by the same global {@link PluginSettings}
 * object.
 */
public class SalPropertyService implements PropertyService
{
    private final PluginSettingsFactory pluginSettingsFactory;

    /*
    * The String prefixes below are used to namespace properties stored the global PluginSettings object. To ensure
    * that key clashes do not occur, each prefix must be:
    *  a) unique; and
    *  b) NOT a prefix or suffix substring of any other prefixes.
    */
    private static final String APPLINKS                        = "applinks.";
    private static final String APPLINKS_GLOBAL_PREFIX          = APPLINKS + "global";
    private static final String APPLICATION_ADMIN_PREFIX        = APPLINKS + "admin";
    private static final String APPLICATION_PREFIX              = APPLINKS + "application";

    private static final String ENTITY_PREFIX                   = APPLINKS + "entity";
    private static final String LOCAL_ENTITY_PREFIX             = APPLINKS + "local";

    public SalPropertyService(final PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public PropertySet getProperties(final ApplicationLink application)
    {
        return getPropertySet(key(application.getId()));
    }

    public EntityLinkProperties getProperties(final EntityLink entity)
    {
        return new EntityLinkProperties(getPropertySet(key(entity)));
    }

    protected PropertySet getPropertySet(final String key)
    {
        return new SalPropertySet(new HashingLongPropertyKeysPluginSettings(pluginSettingsFactory.createGlobalSettings()), key);
    }

    private String key(final ApplicationId applicationId)
    {
        return String.format("%s.%s", APPLICATION_PREFIX, escape(applicationId.get()));
    }

    private String key(final EntityLink entity)
    {
        return String.format("%s.%s.%s.%s", ENTITY_PREFIX,
                escape(entity.getApplicationLink().getId().get()),
                escape(getTypeId(entity.getType()).get()),
                escape(entity.getKey())
        );
    }

    public ApplicationLinkProperties getApplicationLinkProperties(final ApplicationId id)
    {
        return new ApplicationLinkProperties(getPropertySet(String.format("%s.%s", APPLICATION_ADMIN_PREFIX,
                escape(id.get()))),
                getPropertySet(key(id))
        );
    }

    public PropertySet getGlobalAdminProperties()
    {
        return getPropertySet(APPLINKS_GLOBAL_PREFIX);
    }

    public PropertySet getLocalEntityProperties(final String localEntityKey, final TypeId localEntityTypeId)
    {
        return getPropertySet(String.format("%s.%s.%s", LOCAL_ENTITY_PREFIX,
                escape(localEntityKey),
                escape(localEntityTypeId.get()))
        );
    }

    private static String escape(final String s)
    {
        return s.replaceAll("_", "__").replaceAll("\\.", "_");
    }
}
