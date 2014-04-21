package com.atlassian.core.user;

import com.atlassian.core.util.RandomGenerator;
import com.opensymphony.user.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * A utility class for operating on users.
 *
 * @see com.atlassian.core.user.GroupUtils
 * @deprecated Use {@link com.atlassian.jira.user.UserUtils} instead. Since v4.4.
 */
public class UserUtils
{
    private static final Logger log = Logger.getLogger(UserUtils.class);

    /**
     * Retrieves and returns the user by given username. If such user does not
     * exist throws {@link EntityNotFoundException}.
     *
     * @param username usename to lookup
     * @return user looked up by the given username
     * @throws EntityNotFoundException if user with given username not found
     */
    public static User getUser(String username) throws EntityNotFoundException
    {
        return username == null ? null : UserManager.getInstance().getUser(username);
    }

    /**
     * Checks if a user with given username exists. Returns true if such user
     * exists, false otherwise.
     *
     * @param username username to look up
     * @return true if user found, false otherwise
     */
    public static boolean existsUser(String username)
    {
        try
        {
            // we don't care what it returns
            // its contract is that it returns user if found,
            // throws exception otherwise, never returns null
            getUser(username);
            return true;
        }
        catch (EntityNotFoundException e)
        {
            return false;
        }
    }

    public static Collection getAllUsers()
    {
        return UserManager.getInstance().getUsers();
    }

    /**
     * Return the <b>first</b> user found that matches thie email address.  There may be many users with the
     * same email address, and this method should not be used to return a single user.
     * <p/>
     * The email address is matched case <b>insensitive</b>.  This loops through all users, and so is a
     * O(N) operation.
     *
     * @param email user email
     * @return The first user that matches the email address
     * @throws EntityNotFoundException if no user is found.
     */
    public static User getUserByEmail(String email) throws EntityNotFoundException
    {
        // Trim down the email address to remove any whitespace etc.
        String emailAddress = StringUtils.trimToNull(email);
        if (emailAddress != null)
        {
            for (Iterator iterator = getAllUsers().iterator(); iterator.hasNext();)
            {
                User user = (User) iterator.next();

                if (emailAddress.equalsIgnoreCase(user.getEmail()))
                {
                    return user;
                }
            }
        }

        throw new EntityNotFoundException("Could not find user with email: " + email);
    }

    /**
     * Finds the users by the given e-mail address. E-mail address look-up is
     * done case insensitively. Leading or trailing spaces in the given e-mail
     * address are trimmed before look up.
     *
     * @param email e-mail address
     * @return always returns a list of users found (even if empty)
     */
    public static List getUsersByEmail(String email)
    {
        List users = new ArrayList();
        String emailAddress = StringUtils.trimToNull(email);
        if (emailAddress != null)
        {
            for (Iterator iterator = getAllUsers().iterator(); iterator.hasNext();)
            {
                User user = (User) iterator.next();

                if (emailAddress.equalsIgnoreCase(user.getEmail()))
                {
                    users.add(user);
                }
            }
        }
        return users;
    }

    /**
     * Get a list of users in a set of groups (either Group objects or String group names)
     *
     * @param groups collection of groups (either Group objects or String group names)
     * @return a collection of {@link User} objects
     */
    public static Collection getUsers(Collection groups)
    {
        if (groups == null || groups.isEmpty())
        {
            return Collections.EMPTY_SET;
        }

        Set usernames = new HashSet();

        for (Iterator iterator = groups.iterator(); iterator.hasNext();)
        {
            Object o = iterator.next();
            if (o == null)
            {
                return UserUtils.getAllUsers();
            }
            else
            {
                Group group;
                if (o instanceof Group)
                {
                    group = (Group) o;
                }
                else
                {
                    group = GroupUtils.getGroup((String) o);
                }
                if (group != null)
                {
                    usernames.addAll(group.getUsers());
                }
            }
        }

        List users = new ArrayList();

        for (Iterator iterator = usernames.iterator(); iterator.hasNext();)
        {
            String username = (String) iterator.next();
            try
            {
                users.add(getUser(username));
            }
            catch (EntityNotFoundException e)
            {
                log.error("Could not find user " + username + " but user was returned as in groups " + groups);
            }
        }

        Collections.sort(users, new BestNameComparator());

        return users;
    }

    /**
     * This method is used when a user is created automatically, or by another user
     * (for example an administrator). Should not be used for users signing themselves
     * up.
     * <p/>
     * This method will also generate an automatic random password for the user.
     *
     * @param username username
     * @param email    user's e-mail address
     * @return newly created user
     * @throws DuplicateEntityException if user already exists
     * @throws ImmutableException       if setting the user's password fails
     */
    public static User createUser(String username, String email) throws DuplicateEntityException, ImmutableException
    {
        return createUser(username, RandomGenerator.randomPassword(), email, null);
    }

    /**
     * This method is used when a user is created automatically, or by another user
     * (for example an administrator). Should not be used for users signing themselves
     * up.
     * <p/>
     * This method will also generate an automatic random password for the user.
     *
     * @param username username
     * @param email    e-mail address
     * @param fullname user's full name
     * @return newly created user
     * @throws DuplicateEntityException if user already exists
     * @throws ImmutableException       if setting the user's password fails
     */
    public static User createUser(String username, String email, String fullname)
            throws DuplicateEntityException, ImmutableException
    {
        return createUser(username, RandomGenerator.randomPassword(), email, fullname);
    }

    /**
     * Generic method which actually creates users, and fires the given event.
     *
     * @param username username
     * @param password password
     * @param email    e-mail address
     * @param fullname user's full name
     * @return newly created user
     * @throws DuplicateEntityException if user already exists
     * @throws ImmutableException       if setting the user's password fails
     */
    public static User createUser(String username, String password, String email, String fullname)
            throws DuplicateEntityException, ImmutableException
    {
        return createUser(username, password, email, fullname, null);
    }

    /**
     * Creates a new user with given attributes and associates the user with
     * given collection of groups.
     *
     * @param username username
     * @param password passwod
     * @param email    e-mail address
     * @param fullname user's full name
     * @param groups   group names (String objects) the user will belong to
     * @return newly created user
     * @throws DuplicateEntityException if user already exists
     * @throws ImmutableException       if setting the user's password fails
     */
    public static User createUser(String username, String password, String email, String fullname, Collection groups)
            throws DuplicateEntityException, ImmutableException
    {
        log.debug("UserUtils.createUser");
        try
        {
            UserManager.getInstance().getUser(username.toLowerCase());
            throw new DuplicateEntityException("The user " + username + " already exists.");
        }
        catch (EntityNotFoundException ex)
        {
            User user = UserManager.getInstance().createUser(username.toLowerCase());
            user.setEmail(email);
            user.setPassword(password);

            if (fullname == null)
            {
                fullname = username;
            }

            user.setFullName(fullname);

            if (groups != null)
            {
                for (Iterator iterator = groups.iterator(); iterator.hasNext();)
                {
                    String groupname = (String) iterator.next();
                    user.addToGroup(GroupUtils.getGroup(groupname));
                }
            }
            return user;
        }
    }

    /**
     * Removes the given user.
     * A {@link ImmutableException} is thrown if given user cannot be removed.
     *
     * @param user user to remove
     * @throws Exception if user cannot be removed
     */
    public static void removeUser(User user) throws Exception
    {
        user.remove();
    }

    /**
     * For a user, create a new password, and dispatch an 'forgot password' event
     *
     * @param user The user whose password needs to be reset.
     * @return new password
     * @throws ImmutableException if password cannot be changed
     */
    public static String resetPassword(User user) throws ImmutableException
    {
        String newPassword = RandomGenerator.randomPassword();
        user.setPassword(newPassword);
        return newPassword;
    }

    /**
     * Changes the password for a given user.
     *
     * @param user     user to change password for
     * @param password new password
     * @throws ImmutableException if password cannot be changed
     */
    public static void changePassword(User user, String password) throws ImmutableException
    {
        user.setPassword(password);
    }

}
