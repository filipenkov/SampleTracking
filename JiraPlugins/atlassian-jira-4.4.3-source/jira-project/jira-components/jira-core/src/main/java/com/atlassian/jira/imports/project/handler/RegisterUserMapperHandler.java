package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.user.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This handler records all users that exist in the backup file and register them with the user mapper.
 * The UserMapperHandler flags the required Users.
 *
 * @since v3.13
 * @see com.atlassian.jira.imports.project.handler.UserMapperHandler
 */
public class RegisterUserMapperHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(RegisterUserMapperHandler.class);

    private static final String OS_PROPERTY_ENTRY = "OSPropertyEntry";
    private static final String EXTERNAL_ENTITY = "ExternalEntity";
    private static final String ENTITY_NAME = "entityName";
    private static final String USER = "User";
    private static final String ID = "id";
    private static final String ENTITY_ID = "entityId";
    private static final String OS_PROPERTY_STRING = "OSPropertyString";
    private static final String VALUE = "value";
    private static final String USER_NAME = "userName";
    private static final String DISPLAY_NAME = "displayName";
    private static final String EMAIL = "emailAddress";
    private final UserMapper userMapper;
    private static final String CREDENTIAL = "credential";

    public RegisterUserMapperHandler(final UserMapper userMapper)
    {
        this.userMapper = userMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // Need to read the ExternalEntity table in order to map username -> ExternalEntity ID
        if (entityName.equals(EXTERNAL_ENTITY) && "com.atlassian.jira.user.OfbizExternalEntityStore".equals(attributes.get("type")))
        {
            // <ExternalEntity id="10000" name="admin" type="com.atlassian.jira.user.OfbizExternalEntityStore"/>
            // Get the ID
            final String id = attributes.get(ID);
            if (StringUtils.isEmpty(id))
            {
                throw new ParseException("Missing 'id' field for ExternalEntity.");
            }
            // Get the username
            final String username = attributes.get("name");
            if (StringUtils.isEmpty(username))
            {
                throw new ParseException("Missing name from ExternalEntity id = " + id);
            }
            externalEntityIdToUsernameMap.put(id, username);
        }
        // User properties are stored in OSProperty with entityName="ExternalEntity"
        else if (entityName.equals(OS_PROPERTY_ENTRY) && EXTERNAL_ENTITY.equals(attributes.get(ENTITY_NAME)))
        {
            // <OSPropertyEntry id="10070" entityName="ExternalEntity" entityId="10001" propertyKey="jira.meta.ice cream" type="5"/>

            // Get the OSPropertyEntry ID
            final String osPropertyID = attributes.get(ID);
            if (StringUtils.isEmpty(osPropertyID))
            {
                throw new ParseException("Missing 'id' field for OSPropertyEntry.");
            }
            // Get the ExternalEntityID
            final String externalEntityId = attributes.get(ENTITY_ID);
            if (StringUtils.isBlank(externalEntityId))
            {
                throw new ParseException("Missing entityId from OSPropertyEntry id = " + osPropertyID);
            }
            // Get the propertyKey
            final String propertyKey = attributes.get("propertyKey");
            if (StringUtils.isBlank(propertyKey))
            {
                throw new ParseException("Missing propertyKey from OSPropertyEntry id = " + osPropertyID);
            }

            // We need to store the username and propertyKey in a map indexed by OSPropertyEntry ID
            String username = externalEntityIdToUsernameMap.get(externalEntityId);
            if (username == null)
            {
                log.warn("Unable to associate a username with ExternalEntity ID '" + externalEntityId + "' when processing OSPropertyEntry " + osPropertyID);
            }
            else
            {
                // JRA-21453: If propertyKey is not one of email, fullname or jira.meta.*, then ignore it.
                if (UserPropertyKey.isRememberedPropertyKey(propertyKey))
                {
                    osPropertyEntryMap.put(osPropertyID, new UserPropertyKey(username, propertyKey));
                }
            }
        }
        else if (entityName.equals(OS_PROPERTY_STRING))
        {
            //<OSPropertyString id="10150" value="wsailor@example.com"/>
            //<OSPropertyString id="10151" value="Wendell Sailor"/>
            //<OSPropertyString id="10152" value="Ding Dong Dell"/>
            //<OSPropertyString id="10153" value="Purple"/>

            final String id = attributes.get(ID);
            // Retrieve the previously saved OSPropertyEntry entry.
            // Using Map.remove here as it may help free up memory a bit quicker
            final UserPropertyKey userPropertyKey = osPropertyEntryMap.remove(id);
            if (userPropertyKey != null)
            {
                // Now we can start to create the User object.
                ExternalUser externalUser = userMap.get(userPropertyKey.username);
                if (externalUser == null)
                {
                    // User not created yet - create it now...
                    externalUser = new ExternalUser();
                    // ... and store it in the map.
                    userMap.put(userPropertyKey.username, externalUser);
                }
                // Get the value from the OSPropertyString
                final String value = attributes.get(VALUE);
                // Check which property key we have just received a value for
                userPropertyKey.addPropertyValueToUser(externalUser, value);
            }
        }
        else if (entityName.equals(USER))
        {
            //<User id="10011" directoryId="10000" userName="wilma" lowerUserName="wilma" active="1" createdDate="2010-01-04 09:49:04.932591" updatedDate="2010-01-04 09:49:04.932591" firstName="" lowerFirstName="" lastName="" lowerLastName="" displayName="Wilma Flinstone" lowerDisplayName="wilma flinstone" emailAddress="wilma@example.com" lowerEmailAddress="wilma@example.com" credential="jEUdB0f3hSbXaKrvGQrOull9LQ74qN9hVSjNpi5cicihae2b8IIBATUtHNWwyEBCopdv9Uqm5hn+5rpGVJC0Gg=="/>
            final String userId = attributes.get(ID);
            if (StringUtils.isEmpty(userId))
            {
                throw new ParseException("Missing 'id' field for User entry.");
            }
            final String name = attributes.get(USER_NAME);
            if (StringUtils.isEmpty(name))
            {
                log.warn("Missing 'userName' field for User entry id = " + userId);
                return;
            }
            final String displayName = attributes.get(DISPLAY_NAME);
            if (StringUtils.isEmpty(displayName))
            {
                log.warn("Missing 'displayName' field for User entry id = " + userId);
            }
            final String email = attributes.get(EMAIL);
            if (StringUtils.isEmpty(email))
            {
                log.warn("Missing 'email' field for User entry id = " + userId);
            }
            final String credential = attributes.get(CREDENTIAL);

            // The user object may already be created because of OSProperty values.
            ExternalUser externalUser = userMap.get(name);
            if (externalUser == null)
            {
                //  User not created yet - create it now... Create a user that will have no full name or email address
                externalUser = new ExternalUser();
                userMap.put(name, externalUser);
            }
            externalUser.setId(userId);
            externalUser.setName(name);
            externalUser.setFullname(displayName);
            externalUser.setEmail(email);
            externalUser.setPasswordHash(credential);
            // We finally have a fully populated ExternalUser object, add it to the BackupOverviewBuilder.

            userMapper.registerOldValue(externalUser);
        }
    }

    public void startDocument()
    {
    // No-op
    }

    public void endDocument()
    {
    // No-op
    }

    Map<String, UserPropertyKey> osPropertyEntryMap = new HashMap<String, UserPropertyKey>();
    Map<String, ExternalUser> userMap = new HashMap<String, ExternalUser>();
    Map<String, String> externalEntityIdToUsernameMap = new HashMap<String, String>();

    static class UserPropertyKey
    {
        String username;
        String propertyKey;

        public UserPropertyKey(final String username, final String propertyKey)
        {
            this.username = username;
            this.propertyKey = propertyKey;
        }

        /**
         * Adds the given value for this propertyKey to the given User object.
         * @param externalUser ExternalUser to populate
         * @param value The value.
         */
        public void addPropertyValueToUser(final ExternalUser externalUser, final String value)
        {
            // <OSPropertyEntry id="10150" entityName="ExternalEntity" entityId="10060" propertyKey="jira.meta.colour" type="5"/>
            if (isRememberedPropertyKey(propertyKey))
            {
                final String simplePropertyKey = propertyKey.substring(UserUtil.META_PROPERTY_PREFIX.length());
                externalUser.setUserProperty(simplePropertyKey, value);
            }
        }

        public static boolean isRememberedPropertyKey(final String propertyKey)
        {
            return propertyKey.startsWith(UserUtil.META_PROPERTY_PREFIX);
        }
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final RegisterUserMapperHandler that = (RegisterUserMapperHandler) o;

        if (osPropertyEntryMap != null ? !osPropertyEntryMap.equals(that.osPropertyEntryMap) : that.osPropertyEntryMap != null)
        {
            return false;
        }
        if (userMap != null ? !userMap.equals(that.userMap) : that.userMap != null)
        {
            return false;
        }
        if (userMapper != null ? !userMapper.equals(that.userMapper) : that.userMapper != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (userMapper != null ? userMapper.hashCode() : 0);
        result = 31 * result + (osPropertyEntryMap != null ? osPropertyEntryMap.hashCode() : 0);
        result = 31 * result + (userMap != null ? userMap.hashCode() : 0);
        return result;
    }
}
