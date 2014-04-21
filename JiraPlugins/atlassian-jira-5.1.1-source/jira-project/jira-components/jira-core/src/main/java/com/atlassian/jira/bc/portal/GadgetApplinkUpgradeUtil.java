package com.atlassian.jira.bc.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.collect.Iterables;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility that helps determine if there are any external gadget specs that need to have applinks created for them!
 *
 * @since v4.3
 */
@EventComponent
public class GadgetApplinkUpgradeUtil
{
    private final ExternalGadgetSpecStore externalGadgetSpecStore;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;

    private final ResettableLazyReference<Boolean> ref = new ResettableLazyReference<Boolean>()
    {
        @Override
        protected Boolean create() throws Exception
        {
            if (!applicationProperties.getOption(APKeys.JIRA_GADGET_APPLINK_UPGRADE_FINISHED))
            {
                final Iterable<ExternalGadgetSpec> entries = externalGadgetSpecStore.entries();
                return !Iterables.isEmpty(entries);
            }
            return false;
        }
    };

    public GadgetApplinkUpgradeUtil(final ExternalGadgetSpecStore externalGadgetSpecStore, final ApplicationProperties applicationProperties,
            final PermissionManager permissionManager)
    {
        this.externalGadgetSpecStore = externalGadgetSpecStore;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
    }

    public boolean isUpgradeRequired(final User user)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
        {
            return ref.get();
        }
        return false;
    }

    /**
     * Returns a mapping of a baseurl that doesn't have an applink configured yet to a list of gadget specs starting
     * with that base url
     *
     * @return Map of baseurl -> List of gadget specs starting with that baseurl
     */
    public Map<URI, List<ExternalGadgetSpec>> getExternalGadgetsRequiringUpgrade()
    {
        final Map<URI, List<ExternalGadgetSpec>> ret = new LinkedHashMap<URI, List<ExternalGadgetSpec>>();

        for (ExternalGadgetSpec spec : externalGadgetSpecStore.entries())
        {
            final URI specUri = spec.getSpecUri();
            final URI host = URI.create(specUri.getScheme() + "://" + specUri.getAuthority());
            if (!ret.containsKey(host))
            {
                ret.put(host, new ArrayList<ExternalGadgetSpec>());
            }
            ret.get(host).add(spec);
        }
        return ret;
    }

    public void disableUpgradeCheck()
    {
        applicationProperties.setOption(APKeys.JIRA_GADGET_APPLINK_UPGRADE_FINISHED, true);
        ref.reset();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        ref.reset();
    }
}
