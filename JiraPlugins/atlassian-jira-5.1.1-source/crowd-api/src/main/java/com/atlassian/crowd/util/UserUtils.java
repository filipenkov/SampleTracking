package com.atlassian.crowd.util;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import org.apache.commons.lang.StringUtils;

/**
 * General utility class for email related methods.
 */
public class UserUtils
{
    /**
     * Returns true if an email address is valid.
     *
     * @param email potential email address.
     * @return true if an email address is valid.
     */
    public static boolean isValidEmail(String email)
    {
        /* We have no standard parser for RFC-2822 addresses, so we've decided
        to only check the value is present and there is @
         */
        return StringUtils.isNotBlank(email) && email.contains("@");
    }

    /**
     * Splits the first name and last name out from a full name string.
     * <p/>
     * The first name is formed by the characters prior to the first space
     * in the string, the last name is formed by characters after the space.
     * <p/>
     * This method will always return an array of size 2, with each element
     * being non-null.
     * <p/>
     * The last name will be trimmed to remove trailing or leading spaces.
     * Leading spaces in the {@code fullname} will be ignored.
     * <p/>
     * Examples:
     * <ul>
     * <li>getFirstNameLastName("Bob Smith") = { "Bob", "Smith"}
     * <li>getFirstNameLastName("Bob &nbsp;&nbsp; Smith") = { "Bob", "Smith" }
     * <li>getFirstNameLastName("BobSmith") = { "", "BobSmith" }
     * <li>getFirstNameLastName("  Bob Smith ") = { "Bob", "Smith"}
     * <li>getFirstNameLastName("") = { "", "" }
     * <li>getFirstNameLastName(null) = { "", "" }
     * </ul>
     *
     * @param fullname the fullname to parse.
     * @return String[0] = firstname, String[1] = lastname. If the fullname contains no spaces, then the
     *         firstname will be empty and the lastname will be the fullname.
     */
    /* default */ static String[] getFirstNameLastName(String fullname)
    {
        // Split the fullname on the first space and add this as the <first name> <space> <last name>
        String[] strings = StringUtils.split(fullname, " ", 2);
        String firstName;
        String lastName;

        if (strings == null || strings.length == 0)
        {
            firstName = "";
            lastName = "";
        }
        else if (strings.length > 1)
        {
            firstName = strings[0].trim();
            lastName = strings[1].trim();
        }
        else
        {
            // priority given to populate last name
            firstName = "";
            lastName = strings[0].trim();
        }

        return new String[] { firstName, lastName };
    }

    /**
     * Returns the appropriate display name based on the {@code displayName},
     * {@code firstName} and {@code lastName} of the user.
     * <p/>
     * If the {@code displayName} is blank, it will attempt to construct the
     * full name from the first and last names.
     * <p/>
     * If either the {@code firstName} or {@code lastName} is blank, the method
     * will return the non-blank name.
     * <p/>
     * If both {@code firstName} and {@code lastName} are blank, it will return
     * the {@code username}.
     *
     * @param displayName display name of the user. Can be blank or null.
     * @param firstName   first name of the user. Can be blank or null.
     * @param lastName    last name of the user. Can be blank or null.
     * @param username    username of the user. Must not be blank.
     * @return appropriate displayName for the user. Guaranteed not to be blank.
     * @throws IllegalArgumentException if the username is blank.
     */
    /* default */ static String getDisplayName(String displayName, String firstName, String lastName, String username)
    {
        Assert.notBlank(username);

        if (StringUtils.isNotBlank(displayName))
        {
            return displayName;
        }
        else if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName))
        {
            return new StringBuilder(firstName).append(" ").append(lastName).toString();
        }
        else if (StringUtils.isNotBlank(firstName))
        {
            return firstName;
        }
        else if (StringUtils.isNotBlank(lastName))
        {
            return lastName;
        }
        else
        {
            return username;
        }
    }

    /**
     * Returns a first name based on the {@code firstName} and {@code displayName} of the user or an empty string.
     *
     * If the {@code firstName} is blank, the {@code displayName} will be parsed to extract the appropriate first name;
     * otherwise the {@code firstName} parameter is returned.
     *
     * @param firstName   the first name of the user. Can be blank or null.
     * @param displayName the display name of the user. Can be blank or null or a calculated value.
     * @return non-null first name of the user. This may be blank if the {@code displayName} does not contain a space, starts
     * with a space or is null.
     */
    /* default */ static String getFirstName(String firstName, String displayName)
    {
        if (StringUtils.isNotBlank(firstName))
        {
            return firstName;
        }
        else
        {
            String[] firstLast = getFirstNameLastName(displayName);
            return firstLast[0];
        }
    }

    /**
     * Returns a last name based on the {@code lastName} and {@code displayName} of the user or an empty string.
     *
     * If the {@code lastName} is blank, the {@code displayName} will be parsed to extract the appropriate last name;
     * otherwise the {@code lastName} parameter is returned.
     *
     * If {@code displayName} is also blank, the {@code username} parameter will be returned (worst-case).
     *
     * @param lastName    the last name of the user. Can be blank or null.
     * @param displayName the display name of the user. Must not be blank; can be a calculated value.
     * @return appropriate last name for the user. Guaranteed not to be blank.
     */
    /* default */ static String getLastName(String lastName, String displayName)
    {
        Assert.notBlank(displayName);

        if (StringUtils.isNotBlank(lastName))
        {
            return lastName;
        }
        else
        {
            String[] firstLast = getFirstNameLastName(displayName);
            if (StringUtils.isNotBlank(firstLast[1]))
            {
                return firstLast[1];
            }
            else
            {
                return displayName;
            }
        }
    }

    /**
     * Ensures that the first name, last name and displayName of
     * the user object is fully populated.
     * <p/>
     * Examples:
     * <table>
     * <tr><th>Original data</th><th>Populated data</th></tr>
     * <tr><td>("jsmith", "John", "Smith", "John Smith")</td><td>("jsmith", "John", "Smith", "John Smith")</td></tr>
     * <tr><td>("jsmith", "", "", "John Smith")</td><td>("jsmith", "John", "Smith", "John Smith")</td></tr>
     * <tr><td>("jsmith", "John", "Smith", "")</td><td>("jsmith", "John", "Smith", "John Smith")</td></tr>
     * <tr><td>("jsmith", "", "", "")</td><td>("jsmith", "", "jsmith", "jsmith")</td></tr>
     * <tr><td>("jsmith", null, null, null)</td><td>("jsmith", "", "jsmith", "jsmith")</td></tr>
     * </table>
     * <p/>
     * For more complicated cases, see the documentation for the other methods in this class.
     *
     * @param user potentially partially populated user. The username
     * of the user cannot be blank.
     * @return populated user with non-blank last name and displayName
     * and non-null first name.
     * @see #getDisplayName(String, String, String, String)
     * @see #getFirstName(String, String)
     * @see #getLastName(String, String)
     */
    public static User populateNames(User user)
    {
        UserTemplate populatedUser = new UserTemplate(user);

        String calculatedDisplayName = getDisplayName(user.getDisplayName(), user.getFirstName(), user.getLastName(), user.getName());
        populatedUser.setDisplayName(calculatedDisplayName);

        /* If the displayName isn't a calculated one, or we don't have a last name, generate the
         *  first and last name as necessary. */
        if (StringUtils.isNotBlank(user.getDisplayName()) || StringUtils.isBlank(user.getLastName()))
        {
            populatedUser.setFirstName(getFirstName(user.getFirstName(), calculatedDisplayName));
            populatedUser.setLastName(getLastName(user.getLastName(), calculatedDisplayName));
        }
        else
        {
            /* Ensure the first name is never null */
            populatedUser.setFirstName(StringUtils.defaultString(populatedUser.getFirstName(), ""));
        }

        return populatedUser;
    }
}
