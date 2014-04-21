package com.atlassian.jira.jql.resolver;

import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves User objects and their names.
 *
 * @since v4.0
 */
public class UserResolverImpl implements UserResolver
{
    private static final Logger log = Logger.getLogger(UserResolverImpl.class);

    private final UserPickerSearchService userPickerSearchService;

    public UserResolverImpl(UserPickerSearchService userPickerSearchService)
    {
        this.userPickerSearchService = notNull("userPickerSearchService", userPickerSearchService);
    }

    public List<String> getIdsFromName(final String name)
    {
        notNull("name", name);
        String user = getUserNameFromUserName(name);
        if (user == null)
        {
            return getUserNameFromFullNameOrEmail(name);
        }
        else
        {
            return Collections.singletonList(user);
        }
    }

    public boolean nameExists(final String name)
    {
        notNull("name", name);
        if (getUserNameFromUserName(name) != null)
        {
            return true;
        }
        if (log.isDebugEnabled())
        {
            log.debug("Username '" + name + "' not found - searching as email or full name.");
        }
        return !getUserNameFromFullNameOrEmail(name).isEmpty();
    }

    public boolean idExists(final Long id)
    {
        notNull("name", id);
        return getUserNameFromUserName(id.toString()) != null || !getUserNameFromFullNameOrEmail(id.toString()).isEmpty();
    }

    /**
     * picks between the matched full name and email matches. Iff one is empty, the other is returned, otherwise if the
     * name looks like an email the email matches are returned, and if it doesnt look like an emailthe full name matches
     * are returned.
     *
     * @param name the name to find matches for
     * @param fullNameMatches the names of users whoms full name matched the name
     * @param emailMatches the names of users whoms email matched the name
     * @return a list of user names that best match the inputed name.
     */
    List<String> pickEmailOrFullNameMatches(final String name, final List<String> fullNameMatches, final List<String> emailMatches)
    {
        if (!fullNameMatches.isEmpty() && emailMatches.isEmpty())
        {
            return fullNameMatches;
        }
        else if (!emailMatches.isEmpty() && fullNameMatches.isEmpty())
        {
            return emailMatches;
        }
        else if (isEmail(name))
        {
            return emailMatches;
        }
        else
        {
            return fullNameMatches;
        }
    }

    // Users and UserUtil can't be mocked any way so these aren't testable
    ///CLOVER:OFF
    String getUserNameFromUserName(String name)
    {
        try
        {
            return UserUtils.getUser(name.toLowerCase()).getName();
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
    }

    List<String> getUserNameFromFullNameOrEmail(String name)
    {
        List<String> fullNameMatches = new ArrayList<String>();
        List<String> emailMatches = new ArrayList<String>();
        if (userPickerSearchService.isAjaxSearchEnabled())
        {
            Collection<User> users = UserUtils.getAllUsers();
            for (User user : users)
            {
                if (user.getDisplayName() != null && user.getDisplayName().equalsIgnoreCase(name))
                {
                    fullNameMatches.add(user.getName());
                }
                if (user.getEmailAddress() != null && user.getEmailAddress().equalsIgnoreCase(name))
                {
                    emailMatches.add(user.getName());
                }
            }
        }

        return pickEmailOrFullNameMatches(name, fullNameMatches, emailMatches);
    }

    private boolean isEmail(final String name)
    {
        return TextUtils.verifyEmail(name);
    }

    public User get(final Long id)
    {
        return getUser(id.toString());
    }

    public Collection<User> getAll()
    {
        return UserUtils.getAllUsers();
    }

    User getUser(String name)
    {
        try
        {
            return UserUtils.getUser(name.toLowerCase());    
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
    }
    ///CLOVER:ON
}
