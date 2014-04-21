package com.atlassian.upm.license.internal;

import com.atlassian.upm.license.internal.impl.DefaultLicenseDatePreferenceProvider;

import com.cenqua.fisheye.AppConfig;

import org.joda.time.DateTimeZone;

public class FecruLicenseDatePreferenceProvider extends DefaultLicenseDatePreferenceProvider
{
    @Override
    public DateTimeZone getUserTimeZone()
    {
        try
        {
            return DateTimeZone.forTimeZone(AppConfig.getsConfig().getTimezone());
        }
        catch (IllegalArgumentException e)
        {
            return DateTimeZone.getDefault();
        }
    }
}
