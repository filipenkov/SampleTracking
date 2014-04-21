package com.atlassian.applinks.core.upgrade;


import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Upgrades stored Authentication Provider configuration properties to their new MD5 hashed format (see APL-533 for
 * more context).
 *
 * @since 3.4
 */
public class FishEyeCrucibleHashAuthenticatorPropertiesUpgradeTask implements PluginUpgradeTask
{
    private static final Logger log = LoggerFactory.getLogger(FishEyeCrucibleHashAuthenticatorPropertiesUpgradeTask.class);

    private final PropertyService propertyService;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final ApplicationLinkService applicationLinkService;
    private final I18nResolver i18nResolver;

    public FishEyeCrucibleHashAuthenticatorPropertiesUpgradeTask(final PropertyService propertyService,
                                                                 final PluginSettingsFactory pluginSettingsFactory,
                                                                 final ApplicationLinkService applicationLinkService,
                                                                 final I18nResolver i18nResolver)
    {
        this.propertyService = propertyService;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.applicationLinkService = applicationLinkService;
        this.i18nResolver = i18nResolver;
    }

    public int getBuildNumber()
    {
        return 10;
    }

    public String getShortDescription()
    {
        return "Hash Authentication Provider config properties";
    }

    /**
     * @return an empty collection, warnings are logged directly by this upgrade task
     * TODO return populated collection when https://studio.atlassian.com/browse/SAL-152 is fixed
     * @throws Exception if an irrecoverable error occurs
     */
    public Collection<Message> doUpgrade() throws Exception
    {
        final PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        for (final ApplicationLink applicationLink : applicationLinkService.getApplicationLinks())
        {
            final ApplicationLinkProperties props = propertyService.getApplicationLinkProperties(applicationLink.getId());
            for (final String originalProviderKey : props.getProviderKeys())
            {
                final String oldPropertyKey = formatDeprecatedKey(applicationLink.getId(), originalProviderKey);
                try
                {
                    /**
                     * for each key, retrieve the stored config and store it against a hashed version of the key
                     */
                    final Map<String, String> providerConfig = (Map<String, String>) pluginSettings.get(oldPropertyKey);
                    if (providerConfig != null) {
                        // setProviderConfig handles hashing the key for us
                        props.setProviderConfig(originalProviderKey, providerConfig);
                    } else {
                        log.warn(i18nResolver.getText("applinks.upgrade.warn.no.auth.provider.config.found",
                                originalProviderKey, applicationLink.toString()));
                    }
                }
                // this is just a defensive catch, the contract for a PluginUpgradeTask is to never throw an exception
                // unless you want to halt the upgrade process with a FATAL error. Any non-fatal problems should be
                // reported via the Collection<Message> passed back, but until SAL-152 is addressed, I'm just logging
                // messages for the user.
                catch (Exception e)
                {
                    log.error(i18nResolver.getText("applinks.upgrade.warn.upgrade.auth.provider.hash.failed",
                            originalProviderKey, applicationLink.toString()), e);
                }
                finally
                {
                    /**
                     * delete the stored provider config for the old key even if the upgrade failed, they're no longer
                     * readable by UAL and may contain sensitive information
                     */
                    try
                    {
                        pluginSettings.remove(oldPropertyKey);
                    }
                    // see comment on the above catch clause for why we're catching Exception
                    catch (Exception e)
                    {
                        log.error(i18nResolver.getText("applinks.upgrade.warn.upgrade.auth.provider.delete.failed",
                                originalProviderKey, applicationLink.toString()), e);
                    }
                }
            }
        }
        return Lists.newArrayList();
    }
    /*
     * Old Format: applinks.admin.[APPLICATION_ID].auth.[AUTHENTICATION_PROVIDER_CLASS]
     *
     * e.g. applinks.admin.92004b08-5657-3048-b5dc-f886e662ba15.auth.com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider
     */
    private String formatDeprecatedKey(final ApplicationId id, final String originalProviderKey)
    {
        return String.format("applinks.admin.%s.auth.%s", id.toString(), originalProviderKey);
    }

    public String getPluginKey()
    {
        return "com.atlassian.applinks.applinks-plugin";
    }
}
