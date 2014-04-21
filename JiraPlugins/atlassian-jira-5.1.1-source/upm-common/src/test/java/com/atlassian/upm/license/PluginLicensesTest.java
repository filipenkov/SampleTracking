package com.atlassian.upm.license;

import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.LicenseType;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginLicensesTest extends PluginLicenseBaseTest
{
    @Test
    public void oldExpiredPluginLicenseIsNotRecentlyExpired()
    {
        assertFalse(PluginLicenses.isRecentlyExpired().apply(oldExpiredEvaluationLicense));
    }

    @Test
    public void newlyExpiredPluginLicenseIsRecentlyExpired()
    {
        assertTrue(PluginLicenses.isRecentlyExpired().apply(newlyExpiredEvaluationLicense));
    }

    @Test
    public void soonToExpirePluginLicenseIsNotRecentlyExpired()
    {
        assertFalse(PluginLicenses.isRecentlyExpired().apply(soonToExpireEvaluationLicense));
    }

    @Test
    public void inTheFarFutureExpirePluginLicenseIsNotRecentlyExpired()
    {
        assertFalse(PluginLicenses.isRecentlyExpired().apply(inTheFarFutureExpireEvaluationLicense));
    }

    @Test
    public void oldExpiredPluginLicenseIsNotNearlyExpired()
    {
        assertFalse(PluginLicenses.isNearlyExpired().apply(oldExpiredEvaluationLicense));
    }

    @Test
    public void newlyExpiredPluginLicenseIsNotNearlyExpired()
    {
        assertFalse(PluginLicenses.isNearlyExpired().apply(newlyExpiredEvaluationLicense));
    }

    @Test
    public void soonToExpirePluginLicenseIsNearlyExpired()
    {
        assertTrue(PluginLicenses.isNearlyExpired().apply(soonToExpireEvaluationLicense));
    }

    @Test
    public void inTheFarFutureExpirePluginLicenseIsNotNearlyExpired()
    {
        assertFalse(PluginLicenses.isNearlyExpired().apply(inTheFarFutureExpireEvaluationLicense));
    }

    @Test
    public void evaluationLicenseIsEvaluation()
    {
        assertTrue(PluginLicenses.isEvaluation().apply(newlyExpiredEvaluationLicense));
    }

    @Test
    public void nonEvaluationLicenseIsNotEvaluation()
    {
        assertFalse(PluginLicenses.isEvaluation().apply(newlyExpiredNonEvaluationLicense));
    }

    @Test
    public void nonexistantLicenseIsTryable()
    {
        assertTrue(PluginLicenses.isPluginTryable(Option.none(PluginLicense.class)));
    }

    @Test
    public void existantLicenseIsNotTryable()
    {
        assertFalse(PluginLicenses.isPluginTryable(Option.some(soonToExpireEvaluationLicense)));
    }

    @Test
    public void nonexistantLicenseIsBuyable()
    {
        assertTrue(PluginLicenses.isPluginBuyable(Option.none(PluginLicense.class)));
    }

    @Test
    public void evaluationLicenseIsBuyable()
    {
        assertTrue(PluginLicenses.isPluginBuyable(Option.some(inTheFarFutureExpireEvaluationLicense)));
    }

    @Test
    public void nonevaluationLicenseIsNotBuyable()
    {
        assertFalse(PluginLicenses.isPluginBuyable(Option.some(inTheFarFutureExpireNonEvaluationLicense)));
    }

    @Test
    public void typeMismatchLicenseIsBuyable()
    {
        when(inTheFarFutureExpireNonEvaluationLicense.getError()).thenReturn(Option.some(LicenseError.TYPE_MISMATCH));
        assertTrue(PluginLicenses.isPluginBuyable(Option.some(inTheFarFutureExpireNonEvaluationLicense)));
    }
    
    @Test
    public void nonexistantLicenseIsNotUpgradable()
    {
        assertFalse(PluginLicenses.isPluginUpgradable(Option.none(PluginLicense.class)));
    }

    @Test
    public void evaluationLicenseIsNotUpgradable()
    {
        assertFalse(PluginLicenses.isPluginUpgradable(Option.some(userMismatchEvaluationLicense)));
    }

    @Test
    public void nonevaluationLicenseWithoutUserMismatchIsNotUpgradable()
    {
        assertFalse(PluginLicenses.isPluginUpgradable(Option.some(inTheFarFutureExpireNonEvaluationLicense)));
    }

    @Test
    public void nonevaluationLicenseWithUserMismatchIsUpgradable()
    {
        assertTrue(PluginLicenses.isPluginUpgradable(Option.some(userMismatchNonEvaluationLicense)));
    }

    @Test
    public void typeMismatchLicenseIsNotUpgradable()
    {
        when(inTheFarFutureExpireNonEvaluationLicense.getError()).thenReturn(Option.some(LicenseError.TYPE_MISMATCH));
        assertFalse(PluginLicenses.isPluginUpgradable(Option.some(inTheFarFutureExpireNonEvaluationLicense)));
    }

    @Test
    public void nonexistantLicenseIsNotRenewable()
    {
        assertFalse(PluginLicenses.isPluginRenewable(Option.none(PluginLicense.class)));
    }

    @Test
    public void evaluationLicenseIsNotRenewable()
    {
        assertFalse(PluginLicenses.isPluginRenewable(Option.some(userMismatchEvaluationLicense)));
    }

    @Test
    public void nonevaluationLicenseIsNotRenewable()
    {
        assertFalse(PluginLicenses.isPluginRenewable(Option.some(inTheFarFutureExpireNonEvaluationLicense)));
    }

    @Test
    public void nonevaluationLicenseWithUserMismatchIsNotRenewable()
    {
        assertFalse(PluginLicenses.isPluginRenewable(Option.some(userMismatchNonEvaluationLicense)));
    }

    @Test
    public void typeMismatchLicenseIsNotRenewable()
    {
        when(inTheFarFutureExpireNonEvaluationLicense.getError()).thenReturn(Option.some(LicenseError.TYPE_MISMATCH));
        assertFalse(PluginLicenses.isPluginRenewable(Option.some(inTheFarFutureExpireNonEvaluationLicense)));
    }

    @Test
    public void newlyExpiredNonevaluationLicenseIsRenewable()
    {
        assertTrue(PluginLicenses.isPluginRenewable(Option.some(newlyExpiredNonEvaluationLicense)));
    }

    @Test
    public void expiredEvaluationLicenseIsNotRenewable()
    {
        assertFalse(PluginLicenses.isPluginRenewable(Option.some(newlyExpiredEvaluationLicense)));
    }

    @Test
    public void longAgoExpiredNonevaluationLicenseIsRenewable()
    {
        assertTrue(PluginLicenses.isPluginRenewable(Option.some(oldExpiredNonEvaluationLicense)));
    }

    @Test
    public void newlyExpiredDeveloperNonevaluationLicenseIsNotRenewable()
    {
        when(newlyExpiredNonEvaluationLicense.getLicenseType()).thenReturn(LicenseType.DEVELOPER);
        assertFalse(PluginLicenses.isPluginRenewable(Option.some(newlyExpiredNonEvaluationLicense)));
    }
}
