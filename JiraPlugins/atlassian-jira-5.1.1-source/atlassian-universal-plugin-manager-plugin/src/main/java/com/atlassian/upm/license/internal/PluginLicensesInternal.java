package com.atlassian.upm.license.internal;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.atlassian.upm.Plugins.fromPluginKey;
import static com.atlassian.upm.api.license.entity.LicenseError.EXPIRED;
import static com.atlassian.upm.api.license.entity.LicenseError.USER_MISMATCH;
import static com.atlassian.upm.license.PluginLicenses.isEvaluation;
import static com.atlassian.upm.license.PluginLicenses.isNearlyExpired;
import static com.atlassian.upm.license.PluginLicenses.isNearlyMaintenanceExpired;
import static com.atlassian.upm.license.PluginLicenses.isRecentlyExpired;
import static com.atlassian.upm.license.PluginLicenses.isRecentlyMaintenanceExpired;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

/**
 * Utility methods for {@link PluginLicense}s. Exists in this module instead of licensing-lib due to class dependencies.
 * Similar to {@link com.atlassian.upm.license.PluginLicenses} except that this class only deals with UPM-specific tasks
 * which do not need to be exposed in {@code upm-common}.
 */
public class PluginLicensesInternal
{
    public static final Function<PluginLicense, String> toPluginKey()
    {
        return new Function<PluginLicense, String>()
        {
            public String apply(PluginLicense pluginLicense)
            {
                return pluginLicense.getPluginKey();
            }
        };
    };

    public static final Function<String, Option<PluginLicense>> toPluginLicense(final PluginLicenseRepository licenseRepository)
    {
        return new Function<String, Option<PluginLicense>>()
        {
            public Option<PluginLicense> apply(String pluginKey)
            {
                return licenseRepository.getPluginLicense(pluginKey);
            }
        };
    };

    public static Function<PluginLicense, Plugin> toPlugin(PluginAccessorAndController pluginAccessorAndController)
    {
        return new ToPlugin(fromPluginKey(pluginAccessorAndController));
    }

    private static class ToPlugin implements Function<PluginLicense, Plugin>
    {
        private final Function<String, Plugin> fromPluginKey;

        ToPlugin(Function<String, Plugin> fromPluginKey)
        {
            this.fromPluginKey = checkNotNull(fromPluginKey, "fromPluginKey");
        }

        @Override
        public Plugin apply(PluginLicense license)
        {
            return fromPluginKey.apply(license.getPluginKey());
        }
    };

    public static Iterable<PluginLicense> getUserMismatchPluginLicenses(PluginLicenseRepository licenseRepository)
    {
        return licenseRepository.getPluginLicenses(USER_MISMATCH);
    }

    public static Iterable<PluginLicense> getRecentlyExpiredEvaluationPluginLicenses(
        PluginLicenseRepository licenseRepository,
        PluginAccessorAndController pluginAccessorAndController)
    {
        return filter(licenseRepository.getPluginLicenses(EXPIRED), and(isRecentlyExpired(),
                                                                        isEvaluation(),
                                                                        not(isUninstalledLegacyPlugin(pluginAccessorAndController))));
    }

    public static Iterable<PluginLicense> getNearlyExpiredEvaluationPluginLicenses(
        PluginLicenseRepository licenseRepository,
        PluginAccessorAndController pluginAccessorAndController)
    {
        return filter(licenseRepository.getPluginLicenses(), and(isNearlyExpired(),
                                                                 isEvaluation(),
                                                                 not(isUninstalledLegacyPlugin(pluginAccessorAndController))));
    }

    public static Iterable<PluginLicense> getMaintenanceRecentlyExpiredPluginLicenses(
        PluginLicenseRepository licenseRepository,
        PluginAccessorAndController pluginAccessorAndController)
    {
        return filter(licenseRepository.getPluginLicenses(), and(isRecentlyMaintenanceExpired(),
                                                                 not(isEvaluation()),
                                                                 not(isUninstalledLegacyPlugin(pluginAccessorAndController))));
    }

    public static Iterable<PluginLicense> getMaintenanceNearlyExpiredPluginLicenses(
        PluginLicenseRepository licenseRepository,
        PluginAccessorAndController pluginAccessorAndController)
    {
        return filter(licenseRepository.getPluginLicenses(), and(isNearlyMaintenanceExpired(),
                                                                 not(isEvaluation()),
                                                                 not(isUninstalledLegacyPlugin(pluginAccessorAndController))));
    }

    /**
     * A {@link Predicate} that checks whether or not a {@link PluginLicense} is for an uninstalled legacy plugin.
     */
    public static Predicate<PluginLicense> isUninstalledLegacyPlugin(PluginAccessorAndController pluginAccessorAndController)
    {
        return new IsUninstalledLegacyPlugin(pluginAccessorAndController);
    }

    private static class IsUninstalledLegacyPlugin implements Predicate<PluginLicense>
    {
        private final PluginAccessorAndController pluginAccessorAndController;

        public IsUninstalledLegacyPlugin(PluginAccessorAndController pluginAccessorAndController)
        {
            this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        }

        public boolean apply(PluginLicense license)
        {
            String pluginKey = license.getPluginKey();
            if (!pluginAccessorAndController.isLegacyLicensePlugin(pluginKey))
            {
                //the license is not for a legacy licensing plugin - always return false.
                return false;
            }

            return !pluginAccessorAndController.isPluginInstalled(pluginKey);
        }
    }
}
