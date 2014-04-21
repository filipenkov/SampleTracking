package com.atlassian.upm.license;

import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;

import static com.atlassian.upm.api.license.entity.LicenseError.EXPIRED;
import static com.atlassian.upm.api.license.entity.LicenseError.VERSION_MISMATCH;
import static com.atlassian.upm.api.license.entity.LicenseType.ACADEMIC;
import static com.atlassian.upm.api.license.entity.LicenseType.COMMERCIAL;
import static com.atlassian.upm.api.license.entity.LicenseType.STARTER;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;

/**
 * Utility methods for {@link PluginLicense}s. Exists in this module instead of licensing-lib due to class dependencies
 * and to increase accessibility/reusability.
 */
public class PluginLicenses
{
    /**
     * Number of days before expiration when a license is considered to be "nearly expired".
     */
    public static final Integer NEARLY_EXPIRED_DAYS = 7;

    /**
     * Number of days during which an expired license is considered "recently expired".
     */
    public static final Integer RECENTLY_EXPIRED_DAYS = 7;
   
    public static Predicate<PluginLicense> isNearlyExpired()
    {
        return new Predicate<PluginLicense>()
        {
            @Override
            public boolean apply(PluginLicense license)
            {
                for (DateTime expiryDate : license.getExpiryDate())
                {
                    return new Interval(expiryDate.minusDays(NEARLY_EXPIRED_DAYS), expiryDate).contains(new DateTime());
                }
                return false; //no expiration date means it is never "nearly expired"
            }
        };
    }

    public static Predicate<PluginLicense> isNearlyMaintenanceExpired()
    {
        return new Predicate<PluginLicense>()
        {
            @Override
            public boolean apply(PluginLicense license)
            {
                for (DateTime maintenanceExpiryDate : license.getMaintenanceExpiryDate())
                {
                    return new Interval(maintenanceExpiryDate.minusDays(NEARLY_EXPIRED_DAYS), maintenanceExpiryDate).contains(new DateTime());
                }
                return false; //no maintenance expiration date means it is never "nearly maintenance expired"
            }
        };
    }

    public static Predicate<PluginLicense> isRecentlyExpired()
    {
        return new IsRecentlyExpired();
    }

    private static class IsRecentlyExpired implements Predicate<PluginLicense>
    {
        private final DateTime sevenDaysAgo;

        public IsRecentlyExpired()
        {
            this.sevenDaysAgo = new DateTime().minusDays(RECENTLY_EXPIRED_DAYS);
        }

        public boolean apply(PluginLicense license)
        {
            for (DateTime expiryDate : license.getExpiryDate())
            {
                return expiryDate.isAfter(sevenDaysAgo) && expiryDate.isBefore(new DateTime());
            }
            return false; //this plugin does not expire - return false.
        }
    }

    public static Predicate<PluginLicense> isRecentlyMaintenanceExpired()
    {
        return new IsRecentlyMaintenanceExpired();
    }

    private static class IsRecentlyMaintenanceExpired implements Predicate<PluginLicense>
    {
        private final DateTime sevenDaysAgo;

        public IsRecentlyMaintenanceExpired()
        {
            this.sevenDaysAgo = new DateTime().minusDays(RECENTLY_EXPIRED_DAYS);
        }

        public boolean apply(PluginLicense license)
        {
            for (DateTime maintenanceExpiryDate : license.getMaintenanceExpiryDate())
            {
                return maintenanceExpiryDate.isAfter(sevenDaysAgo) && maintenanceExpiryDate.isBefore(new DateTime());
            }
            return false; //this plugin does not maintenance expire - return false.
        }
    }

    public static Predicate<PluginLicense> isEvaluation()
    {
        return new Predicate<PluginLicense>()
        {
            @Override
            public boolean apply(PluginLicense license)
            {
                return license.isEvaluation();
            }
        };
    }

    /**
     * Returns the number of days since maintenance expiry, or none() if no maintenance expiry has expired
     *
     * @param pluginLicense the license
     * @return the number of days since maintenance expiry, or none() if no maintenance expiry has expired
     */
    public static Option<Days> getDaysSinceMaintenanceExpiry(PluginLicense pluginLicense)
    {
        for (DateTime maintenanceExpiryDate : pluginLicense.getMaintenanceExpiryDate())
        {
            if (!pluginLicense.isMaintenanceExpired())
            {
                return none(Days.class);
            }

            return some(Days.daysBetween(maintenanceExpiryDate, new DateTime()));
        }

        return none(Days.class);
    };

    /**
     * Returns true if the given plugin license can be bought directly from My Atlassian, false otherwise
     * @param pluginLicense the plugin license
     * @return true if the given plugin license can be bought directly from My Atlassian, false otherwise
     */
    public static boolean isPluginBuyable(Option<PluginLicense> pluginLicense)
    {
        for (PluginLicense registeredLicense : pluginLicense)
        {
            //can only buy if the current license is an eval license or of the wrong type (e.g. commercial, academic)
            return registeredLicense.isEvaluation() || isErrorEqual(registeredLicense.getError(), LicenseError.TYPE_MISMATCH);
        }

        //plugin is unlicensed - it can be bought.
        return true;
    }

    /**
     * Returns true if the given plugin license can be evaluated directly from My Atlassian, false otherwise
     * @param pluginLicense the plugin license
     * @return true if the given plugin license can be evaluated directly from My Atlassian, false otherwise
     */
    public static boolean isPluginTryable(Option<PluginLicense> pluginLicense)
    {
        //only unlicensed plugins are tryable
        return !pluginLicense.isDefined();
    }

    /**
     * Returns true if the given plugin license can be renewed directly from My Atlassian, false otherwise
     * @param pluginLicense the plugin license
     * @return true if the given plugin license can be renewed directly from My Atlassian, false otherwise
     */
    public static boolean isPluginRenewable(Option<PluginLicense> pluginLicense)
    {
        //plugins that are eligible to be upgraded cannot be renewed
        if (isPluginUpgradable(pluginLicense))
        {
            return false;
        }

        for (PluginLicense registeredLicense : pluginLicense)
        {
            boolean nearlyExpired = PluginLicenses.isNearlyExpired().apply(registeredLicense);
            boolean hasAppropriateError = registeredLicense.getError().isDefined() &&
                                          ImmutableSet.of(EXPIRED, VERSION_MISMATCH).contains(registeredLicense.getError().get());
            boolean hasAppropriateType = ImmutableSet.of(ACADEMIC, COMMERCIAL, STARTER).contains(registeredLicense.getLicenseType());
            boolean evaluation = registeredLicense.isEvaluation();

            //can only renew plugins which are expired, nearly expired, or are newer than the current license's maintenance expiry,
            //and only if the current license is of type Academic, Commercial, or Starter
            return (nearlyExpired || hasAppropriateError) && hasAppropriateType && !evaluation;
        }

        //plugin is unlicensed - it cannot be renewed.
        return false;
    }

    /**
     * Returns true if the given plugin license can be upgraded directly from My Atlassian, false otherwise
     * @param pluginLicense the plugin license
     * @return true if the given plugin license can be upgraded directly from My Atlassian, false otherwise
     */
    public static boolean isPluginUpgradable(Option<PluginLicense> pluginLicense)
    {
        //don't offer to Upgrade anything that is eligible to be bought
        if (isPluginBuyable(pluginLicense))
        {
            return false;
        }

        for (PluginLicense registeredLicense : pluginLicense)
        {
            //can only upgrade when a user mismatch exists
            return isErrorEqual(registeredLicense.getError(), LicenseError.USER_MISMATCH);
        }

        //plugin is unlicensed - it cannot be upgraded.
        return false;
    }

    /**
     * Executes a null-safe (and option-safe) equality check.
     */
    private static boolean isErrorEqual(Option<LicenseError> possibleError, LicenseError equalTo)
    {
        for (LicenseError error : possibleError)
        {
            return equalTo.equals(error);
        }

        return false;
    }
}
