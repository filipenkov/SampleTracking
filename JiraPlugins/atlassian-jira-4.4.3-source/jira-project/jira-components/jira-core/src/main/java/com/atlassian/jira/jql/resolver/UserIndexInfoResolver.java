package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.CaseFolding;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for users.
 *
 * @since v4.0
 */
public class UserIndexInfoResolver implements IndexInfoResolver<User>
{
    private final NameResolver<User> userResolver;

    public UserIndexInfoResolver(final NameResolver<User> userResolver)
    {
        this.userResolver = userResolver;
    }

    public List<String> getIndexedValues(final String rawValue)
    {
        notNull("rawValue", rawValue);
        List<String> ids = userResolver.getIdsFromName(rawValue);
        if (ids.isEmpty())
        {
            return Collections.singletonList(CaseFolding.foldString(rawValue, Locale.ENGLISH));
        }
        else
        {
            return Lists.transform(ids, new Function<String, String>()
            {
                @Override
                public String apply(String from)
                {
                    return CaseFolding.foldString(from, Locale.ENGLISH);
                }
            });
        }
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        notNull("rawValue", rawValue);
        return getIndexedValues(rawValue.toString());
    }

    ///CLOVER:OFF
    public String getIndexedValue(final User user)
    {
        notNull("user", user);
        return CaseFolding.foldString(user.getName(), Locale.ENGLISH);
    }
    ///CLOVER:ON
}