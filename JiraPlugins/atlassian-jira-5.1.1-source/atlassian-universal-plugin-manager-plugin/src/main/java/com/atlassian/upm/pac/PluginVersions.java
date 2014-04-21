package com.atlassian.upm.pac;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.internal.PluginLicenseRepository;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.joda.time.DateTime;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

public abstract class PluginVersions
{
    /**
     * Returns a {@link Predicate} to check if {@link PluginVersion}s are compatible with configured licenses
     * @param licenseRepository license repository
     * @return a {@link Predicate} to check if {@link PluginVersion}s are compatible with configured licenses
     */
    public static Predicate<PluginVersion> isLicensedToBeUpdated(PluginLicenseRepository licenseRepository)
    {
        return new IsLicensedToBeUpdated(licenseRepository);
    }

    /**
     * Returns true if the {@link PluginVersion} is compatible with configured licenses, false if not
     * @param pluginVersion plugin version
     * @param licenseRepository license repository
     * @return true if the {@link PluginVersion} is compatible with configured licenses, false if not
     */
    public static boolean isLicensedToBeUpdated(PluginVersion pluginVersion, PluginLicenseRepository licenseRepository)
    {
        return isLicensedToBeUpdated(licenseRepository).apply(pluginVersion);
    }

    private static class IsLicensedToBeUpdated implements Predicate<PluginVersion>
    {
        private final PluginLicenseRepository licenseRepository;

        public IsLicensedToBeUpdated(PluginLicenseRepository licenseRepository)
        {
            this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
        }

        public boolean apply(PluginVersion pluginVersion)
        {
            for (PluginLicense pluginLicense : licenseRepository.getPluginLicense(pluginVersion.getPlugin().getPluginKey()))
            {
                // if the license isn't valid anyway, we allow the user to update the plugin (shouldn't make things worse)
                if (!pluginLicense.isValid())
                {
                    return true;
                }

                // check release date from PAC (presumed build date) against the date that the current license supports upgrades until
                for (DateTime pluginMaintenanceExpiryDate : pluginLicense.getMaintenanceExpiryDate())
                {
                    return pluginMaintenanceExpiryDate.isAfter(new DateTime(pluginVersion.getReleaseDate()));
                }
            }
            return true; // allow the user to update any plugins that don't have licenses
        }
    };

    /**
     * Returns a {@link Predicate} to check if a {@link PluginVersion}s is for UPM
     * @param pluginAccessorAndController pluginAccessorAndController
     * @return a {@link Predicate} to check if a {@link PluginVersion}s is for UPM
     */
    public static Predicate<PluginVersion> notUpm(PluginAccessorAndController pluginAccessorAndController)
    {
        return new NotUpm(pluginAccessorAndController);
    }

    private static class NotUpm implements Predicate<PluginVersion>
    {
        private final PluginAccessorAndController pluginAccessorAndController;

        public NotUpm(PluginAccessorAndController pluginAccessorAndController)
        {
            this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        }

        public boolean apply(PluginVersion pluginVersion)
        {
            return !pluginAccessorAndController.getUpmPluginKey().equals(pluginVersion.getPlugin().getPluginKey());
        }
    }

    /**
     * Converts {@link PluginVersion}s to plugin keys
     * @param pluginVersions plugin versions
     * @return plugin keys
     */
    public static Iterable<String> toPluginKeys(Iterable<PluginVersion> pluginVersions)
    {
        return transform(pluginVersions, new Function<PluginVersion, String>()
        {
            @Override
            public String apply(PluginVersion pluginVersion)
            {
                return pluginVersion.getPlugin().getPluginKey();
            }
        });
    }
}
