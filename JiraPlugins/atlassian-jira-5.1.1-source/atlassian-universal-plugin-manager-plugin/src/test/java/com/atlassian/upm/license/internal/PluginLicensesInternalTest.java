package com.atlassian.upm.license.internal;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.PluginLicenseBaseTest;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginLicensesInternalTest extends PluginLicenseBaseTest
{
    @Mock PluginLicenseRepository licenseRepository;
    @Mock PluginAccessorAndController pluginAccessorAndController;

    @Before
    public void setUp()
    {
        super.setUp();
        
        when(licenseRepository.getPluginLicenses()).thenReturn(
            ImmutableList.of(oldExpiredEvaluationLicense, newlyExpiredEvaluationLicense,
                             soonToExpireEvaluationLicense, userMismatchEvaluationLicense,
                             oldExpiredNonEvaluationLicense, newlyExpiredNonEvaluationLicense,
                             soonToExpireNonEvaluationLicense, userMismatchNonEvaluationLicense,
                             inTheFarFutureExpireEvaluationLicense, inTheFarFutureExpireNonEvaluationLicense));
        when(licenseRepository.getPluginLicenses(LicenseError.USER_MISMATCH)).thenReturn(
            ImmutableList.of(userMismatchEvaluationLicense, userMismatchNonEvaluationLicense));
        when(licenseRepository.getPluginLicenses(LicenseError.EXPIRED)).thenReturn(
            ImmutableList.of(oldExpiredNonEvaluationLicense, newlyExpiredNonEvaluationLicense,
                             oldExpiredEvaluationLicense, newlyExpiredEvaluationLicense));
    }

    @Test
    public void getRecentlyExpiredEvaluationLicensesIsCorrect()
    {
        //don't return active licenses, non-eval licenses, or licenses which expired long ago
        assertThat(PluginLicensesInternal.getRecentlyExpiredEvaluationPluginLicenses(licenseRepository,
                                                                                     pluginAccessorAndController),
                   contains(newlyExpiredEvaluationLicense));
    }

    @Test
    public void getNearlyExpiredEvaluationLicensesIsCorrect()
    {
        //don't return expired licenses, licenses with expiration dates in the far future, or non-eval licenses
        assertThat(PluginLicensesInternal.getNearlyExpiredEvaluationPluginLicenses(licenseRepository,
                                                                                   pluginAccessorAndController), contains(soonToExpireEvaluationLicense));
    }

    @Test
    public void getRecentlyMaintenanceExpiredLicensesIsCorrect()
    {
        assertThat(PluginLicensesInternal.getMaintenanceRecentlyExpiredPluginLicenses(licenseRepository,
                                                                                      pluginAccessorAndController), contains(newlyExpiredNonEvaluationLicense));
    }

    @Test
    public void getNearlyMaintenanceExpiredEvaluationLicensesIsCorrect()
    {
        assertThat(PluginLicensesInternal.getMaintenanceNearlyExpiredPluginLicenses(licenseRepository,
                                                                                    pluginAccessorAndController), contains(soonToExpireNonEvaluationLicense));
    }

    @Test
    public void uninstalledLegacyLicensePluginIsFilteredOut()
    {
        when(pluginAccessorAndController.isLegacyLicensePlugin(PLUGIN_KEY)).thenReturn(true);
        when(pluginAccessorAndController.isPluginInstalled(PLUGIN_KEY)).thenReturn(false);
        assertThat(PluginLicensesInternal.getMaintenanceNearlyExpiredPluginLicenses(licenseRepository,
                                                                                    pluginAccessorAndController), is(Matchers.<PluginLicense>emptyIterable()));
    }

    @Test
    public void installedLegacyLicensePluginIsNotFilteredOut()
    {
        when(pluginAccessorAndController.isLegacyLicensePlugin(PLUGIN_KEY)).thenReturn(true);
        when(pluginAccessorAndController.isPluginInstalled(PLUGIN_KEY)).thenReturn(true);
        assertThat(PluginLicensesInternal.getMaintenanceNearlyExpiredPluginLicenses(licenseRepository,
                                                                                    pluginAccessorAndController), contains(soonToExpireNonEvaluationLicense));
    }

    @Test
    public void getUserMismatchLicensesIsCorrect()
    {
        //only return user mismatch licenses (both eval and non-eval)
        assertThat(PluginLicensesInternal.getUserMismatchPluginLicenses(licenseRepository), contains(userMismatchEvaluationLicense, userMismatchNonEvaluationLicense));
    }
}
