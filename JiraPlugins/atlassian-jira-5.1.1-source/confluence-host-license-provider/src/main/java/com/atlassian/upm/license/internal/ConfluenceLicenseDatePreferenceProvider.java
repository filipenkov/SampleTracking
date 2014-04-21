package com.atlassian.upm.license.internal;

import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUserPreferences;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.upm.license.internal.impl.DefaultLicenseDatePreferenceProvider;
import com.atlassian.user.User;

import org.joda.time.DateTimeZone;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfluenceLicenseDatePreferenceProvider extends DefaultLicenseDatePreferenceProvider
{
    private final FormatSettingsManager formatSettingsManager;
    private final UserAccessor userAccessor;

    public ConfluenceLicenseDatePreferenceProvider(FormatSettingsManager formatSettingsManager,
                                                   UserAccessor userAccessor)
    {
        this.formatSettingsManager = checkNotNull(formatSettingsManager, "formatSettingsManager");
        this.userAccessor = checkNotNull(userAccessor, "userAccessor");
    }

    @Override
    public String getDateTimeFormat()
    {
        return formatSettingsManager.getDateFormat();
    }

    @Override
    public DateTimeZone getUserTimeZone()
    {
        User user = AuthenticatedUserThreadLocal.getUser();
        final ConfluenceUserPreferences userPreferences = userAccessor.getConfluenceUserPreferences(user);

        try
        {
            return DateTimeZone.forTimeZone(userPreferences.getTimeZone().getWrappedTimeZone());
        }
        catch (IllegalArgumentException e)
        {
            return DateTimeZone.getDefault();
        }
    }
}
