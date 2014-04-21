package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.StringTokenizer;

/**
 * Matcher to compare User parts (username, Full Name and email) with a query string and return true any part matches.
 *
 * @since v5.0
 */
public class UserMatcherPredicate implements Predicate<User>
{
    private final String query;
    private final boolean canMatchAddresses;

    /**
     * @param query The query to compare. Query can not be null.  Empty string will return true for all, so don't pass one in.
     * @param canMatchAddresses Whether email should be searched
     */
    public UserMatcherPredicate(String query, boolean canMatchAddresses)
    {
        Assertions.notNull("query", query);
        this.query = query.toLowerCase();
        this.canMatchAddresses = canMatchAddresses;
    }

    @Override
    /**
     * @param user  The user to test. Cannot be null.
     * @return true if any part matches the query string
     */
    public boolean apply(User user)
    {
        // NOTE - we don't test against blank or null strings here. Do that once before the code that calls this method.

        // 1. Try the username
        String userPart = user.getName();
        if (StringUtils.isNotBlank(userPart) && userPart.toLowerCase().startsWith(query))
        {
            return true;
        }

        // 2. If allowed, try the User's email address
        if (canMatchAddresses)
        {
            userPart = user.getEmailAddress();
            if (StringUtils.isNotBlank(userPart) && userPart.toLowerCase().startsWith(query))
            {
                return true;
            }
        }

        // 3. If no match yet, try each word in the User's full name
        userPart = user.getDisplayName();
        if (StringUtils.isNotBlank(userPart))
        {
            // 3a. Go for a quick win with the start of the first name...
            String lowerCaseDisplayName = userPart.toLowerCase();
            if (lowerCaseDisplayName.startsWith(query))
            {
                return true;
            }

            // 3b. No? It was worth a try. Walk every word in the name
            final StringTokenizer tokenizer = new StringTokenizer(lowerCaseDisplayName);
            tokenizer.nextToken();    // skip the first word that we know didn't match
            while (tokenizer.hasMoreElements())
            {
                if (tokenizer.nextToken().startsWith(query))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
