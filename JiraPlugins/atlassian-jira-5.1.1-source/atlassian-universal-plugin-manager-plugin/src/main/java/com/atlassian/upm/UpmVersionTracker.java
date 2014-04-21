package com.atlassian.upm;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
import com.atlassian.plugin.event.events.PluginUpgradedEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.impl.NamespacedPluginSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.ArrayUtils.contains;

/**
 * Component to keep track of the UPM's version and various attributes about it.
 * More specifically, this is a workaround for UPM-1405.
 */
public class UpmVersionTracker implements InitializingBean, DisposableBean
{
    private static final Logger logger = LoggerFactory.getLogger(UpmVersionTracker.class);

    private static final String KEY_PREFIX = Sys.class.getName() + ":upm-settings:";
    private static final String MOST_RECENT_UPM_VERSION_KEY = "updated-upm-version";

    private final PluginSettingsFactory pluginSettingsFactory;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;

    /**
     * Represents whether or not UPM has self-updated since the last system restart.
     * By default, this value will be set to true in case this {@code UpmVersionTracker} component was initialized during plugin upgrade.
     * If it is during a system startup then the value will be soon set to false.
     */
    private boolean upmUpdatedSinceLastRestart = true;

    public UpmVersionTracker(PluginSettingsFactory pluginSettingsFactory,
                             PluginAccessorAndController pluginAccessorAndController,
                             EventPublisher eventPublisher,
                             ApplicationProperties applicationProperties)
    {
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory, "pluginSettingsFactory");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    }

    /**
     * Stores the current UPM version as the "most recently updated" UPM version.
     * This should be called just prior to UPM self-updating itself.
     */
    public void setCurrentUpmVersionAsMostRecentlyUpdated()
    {
        getPluginSettings().put(MOST_RECENT_UPM_VERSION_KEY, pluginAccessorAndController.getUpmVersion().toString());
    }

    /**
     * Returns true if plugins using UPM's legacy licensing compatibility SPI will be UPM 2.0-aware, false if not.
     * If these plugins are not UPM 2.0-aware then license updates entered in UPM's UI will not be reflected in the plugins
     * until the next system restart. See UPM-1405 for more information.
     *
     * @return true if plugins using UPM's legacy licensing compatibility SPI will be UPM 2.0-aware, false if not.
     */
    public boolean isLegacyLicensingCompatibilitySpiUpm20Aware()
    {
        // UPM-1539: This method isn't guaranteed to work for FeCru so just claim licensing-awareness.
        if (contains(new String[] {"fisheye", "refimpl"}, applicationProperties.getDisplayName().toLowerCase()))
        {
            return true;
        }

        for (String upmVersion : getMostRecentlyUpdatedUpmVersion())
        {
            //UPM 2.0+ has self-updated. Since the previous version was also it existed previously, this is licensing-aware.
            //NOTE: This will NOT work in an environment where you upgraded, for example, from UPM 1.6 to 2.0 to 2.1 without a system restart.
            //      However, without this fix in place, it wouldn't work in that scenario or many others.
            return isMostRecentlyUpdatedUpmVersionLicensingAware();
        }

        //UPM 2.0+ has never self-updated to a later version (but UPM 1.6 may have self-updated to UPM 2.0).
        //If no UPM self-update has occurred since the system was started up then it is licensing-aware.
        return !upmUpdatedSinceLastRestart;
    }

    /**
     * Returns the most recent version of UPM which UPM self-updated itself FROM. For example, if UPM self-updated
     * from UPM 2.0 to 2.1, then this value would be set to "2.0". none() will be returned in the situation that UPM
     * has never self-updated or if the only self-updates have occurred from UPM 1.6.x.
     *
     * @return the most recent version of UPM which UPM self-updated itself FROM.
     */
    private Option<String> getMostRecentlyUpdatedUpmVersion()
    {
        Object upmVersion = getPluginSettings().get(MOST_RECENT_UPM_VERSION_KEY);
        if (upmVersion == null || !(upmVersion instanceof String))
        {
            return none(String.class);
        }
        return some((String)upmVersion);
    }

    private boolean isMostRecentlyUpdatedUpmVersionLicensingAware()
    {
        for (String upmVersion : getMostRecentlyUpdatedUpmVersion())
        {
            try
            {
                //licensing was first added in UPM 2.0
                int majorVersion = Integer.parseInt(upmVersion.split("\\.")[0]);
                return majorVersion >= 2;
            }
            catch (NumberFormatException e)
            {
                logger.warn("Number format exception while parsing UPM version: " + upmVersion);
                return false;
            }
        }
        return false;
    }

    private PluginSettings getPluginSettings()
    {
        //never cache our plugin settings
        return new NamespacedPluginSettings(pluginSettingsFactory.createGlobalSettings(), KEY_PREFIX);
    }

    @EventListener
    public void handleUpmPluginUpgrade(PluginUpgradedEvent event)
    {
        if (pluginAccessorAndController.getUpmPluginKey().equals(event.getPlugin().getKey()))
        {
            //UPM has been updated since the most recent restart. This value will be cleared upon restarting the system.
            upmUpdatedSinceLastRestart = true;
        }
    }

    @EventListener
    public void handleApplicationStartedEvent(Object event)
    {
        String eventName = event.getClass().getName();
        if (eventName.equals("com.atlassian.config.lifecycle.events.ApplicationStartedEvent")
            || eventName.equals("com.atlassian.bamboo.event.ServerStartedEvent"))
        {
            applicationStartup();
        }
    }

    @EventListener
    public void handlePluginFrameworkStartedEvent(PluginFrameworkStartedEvent event)
    {
        applicationStartup();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    private void applicationStartup()
    {
        // The host app is just starting up, meaning that this plugin existed early enough
        // that the legacy licensing compatibility SPI should be UPM 2.0-aware.
        // This event will be fired after all bundled plugins and all user-installed plugins are enabled.
        upmUpdatedSinceLastRestart = false;
    }
}
