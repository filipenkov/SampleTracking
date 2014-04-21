package com.atlassian.plugins.helptips;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserPropertyManager;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Collection;

/**
 * Manages flags for help tips as relating to specific users
 *
 * @since v5.1
 */
public class HelpTipManager
{
    private final UserPropertyManager userPropertyManager;
    private static final String KEY_NAMESPACE = "jira.user.suppressedTips.";

    public HelpTipManager(UserPropertyManager userPropertyManager)
    {
        this.userPropertyManager = userPropertyManager;
    }

    public void dismissTip(final User user, final String id)
    {
        final String tipId = getTipId(id);
        PropertySet propertySet = userPropertyManager.getPropertySet(user);
        propertySet.setBoolean(tipId, true);
    }

    public void undismissTip(final User user, final String id)
    {
        final String tipId = getTipId(id);
        PropertySet propertySet = userPropertyManager.getPropertySet(user);
        if (propertySet.exists(tipId))
            propertySet.remove(tipId);
    }

    public Collection<String> getDismissedTips(final User user)
    {
        PropertySet propertySet = userPropertyManager.getPropertySet(user);
        Collection<String> results = propertySet.getKeys(KEY_NAMESPACE);
        return Collections2.transform(results, new Function<String, String>()
        {
            public String apply(String from)
            {
                return from.substring(KEY_NAMESPACE.length());
            }
        });
    }

    private String getTipId(final String id)
    {
        return KEY_NAMESPACE + id;
    }
}
