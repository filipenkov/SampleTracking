package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.*;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A {@link RemoteDirectory} that allows integration with a remote Crowd server.
 * <p/>
 * This class performs all directory operations against a remote Crowd server.
 * The remote Crowd server sees this class as an application.
 */
public class RemoteCrowdDirectory implements RemoteDirectory
{
    private static final Logger logger = LoggerFactory.getLogger(RemoteCrowdDirectory.class);

    public static final String DESCRIPTIVE_NAME = "Remote Crowd Directory";

    /**
     * Directory attribute key for application name
     */
    public static final String APPLICATION_NAME = "application.name";

    /**
     * Directory attribute key for application password
     */
    public static final String APPLICATION_PASSWORD = "application.password";

    /**
     * Directory attribute key for remote Crowd server URL
     */
    public static final String CROWD_SERVER_URL = "crowd.server.url";

    /**
     * Directory attribute key for http timeout.
     */
    public static final String CROWD_HTTP_TIMEOUT = "crowd.server.http.timeout";

    /**
     * Directory attribute key for max connections.
     */
    public static final String CROWD_HTTP_MAX_CONNECTIONS = "crowd.server.http.max.connections";

    /**
     * Directory attribute key for http proxy host.
     */
    public static final String CROWD_HTTP_PROXY_HOST = "crowd.server.http.proxy.host";

    /**
     * Directory attribute key for http proxy port.
     */
    public static final String CROWD_HTTP_PROXY_PORT = "crowd.server.http.proxy.port";

    /**
     * Directory attribute key for http proxy username.
     */
    public static final String CROWD_HTTP_PROXY_USERNAME = "crowd.server.http.proxy.username";

    /**
     * Directory attribute key for http proxy password.
     */
    public static final String CROWD_HTTP_PROXY_PASSWORD = "crowd.server.http.proxy.password";

    private final CrowdClientFactory crowdClientFactory;
    // configuration parameters (initialisation)
    private long directoryId;
    protected AttributeValuesHolder attributes;

    private LazyReference<CrowdClient> crowdClientRef;

    /**
     * Creates a new RemoteCrowdDirectory using the given CrowdClientFactory.
     *
     * @param crowdClientFactory factory for creating a {@link CrowdClient}
     */
    public RemoteCrowdDirectory(final CrowdClientFactory crowdClientFactory)
    {
        this.crowdClientFactory = crowdClientFactory;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(final long directoryId)
    {
        this.directoryId = directoryId;
    }

    public User findUserByName(final String name) throws UserNotFoundException, OperationFailedException
    {
        try
        {
            return buildUserWithDirectoryId(getCrowdClient().getUser(name));
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public UserWithAttributes findUserWithAttributesByName(final String name)
            throws UserNotFoundException, OperationFailedException
    {
        try
        {
            return buildUserWithDirectoryId(getCrowdClient().getUserWithAttributes(name));
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public User authenticate(final String username, final PasswordCredential credential)
            throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException
    {
        try
        {
            return buildUserWithDirectoryId(getCrowdClient().authenticateUser(username, credential.getCredential()));
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public User addUser(final UserTemplate user, final PasswordCredential credential)
            throws InvalidUserException, InvalidCredentialException, OperationFailedException
    {
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(user.getName(), "user.name cannot be null");

        final UserTemplate userTemplate = new UserTemplate(user);
        // We unset the directory ID so that it is not checked further by the server.  We don't know the server's internal directory ids
        userTemplate.setDirectoryId(-1);
        try
        {
            getCrowdClient().addUser(userTemplate, credential);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
        try
        {
            return findUserByName(user.getName());
        } catch (UserNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public User updateUser(final UserTemplate user)
            throws InvalidUserException, UserNotFoundException, OperationFailedException
    {
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(user.getName(), "user.name cannot be null");

        final UserTemplate userTemplate = new UserTemplate(user);
        // We unset the directory ID so that it is not checked further by the server.  We don't know the server's internal directory ids
        userTemplate.setDirectoryId(-1);
        try
        {
            getCrowdClient().updateUser(userTemplate);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
        return findUserByName(user.getName());
    }

    public void updateUserCredential(final String username, final PasswordCredential credential)
            throws UserNotFoundException, InvalidCredentialException, OperationFailedException
    {
        try
        {
            getCrowdClient().updateUserCredential(username, credential.getCredential());
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public User renameUser(final String oldName, final String newName)
            throws UserNotFoundException, InvalidUserException
    {
        throw new UnsupportedOperationException("Renaming of users is not supported");
    }

    public void storeUserAttributes(final String username, final Map<String, Set<String>> attributes)
            throws UserNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().storeUserAttributes(username, attributes);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeUserAttributes(final String username, final String attributeName)
            throws UserNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().removeUserAttributes(username, attributeName);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeUser(final String username) throws UserNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().removeUser(username);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> searchUsers(final EntityQuery<T> query) throws OperationFailedException
    {
        if (query.getEntityDescriptor().getEntityType() != Entity.USER)
        {
            throw new IllegalArgumentException("Query is not a user query.");
        }

        try
        {
            Class<T> returnType = query.getReturnType();
            if (String.class.equals(returnType))
            {
                return (List<T>) getCrowdClient().searchUserNames(query.getSearchRestriction(), query.getStartIndex(), query.getMaxResults());
            } else if (com.atlassian.crowd.embedded.api.User.class.isAssignableFrom(returnType))
            {
                List<User> users = getCrowdClient().searchUsers(query.getSearchRestriction(), query.getStartIndex(), query.getMaxResults());
                return (List<T>) buildUserListWithDirectoryId(users);
            } else
            {
                throw new IllegalArgumentException("Unknown return type for query: " + returnType.getName());
            }
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public Group findGroupByName(final String name) throws GroupNotFoundException, OperationFailedException
    {
        try
        {
            return buildGroupWithDirectoryId(getCrowdClient().getGroup(name));
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public GroupWithAttributes findGroupWithAttributesByName(final String name)
            throws GroupNotFoundException, OperationFailedException
    {
        try
        {
            return buildGroupWithDirectoryId(getCrowdClient().getGroupWithAttributes(name));
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public Group addGroup(final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException
    {
        Validate.notNull(group, "group cannot be null");
        Validate.notNull(group.getName(), "group.name cannot be null");

        final GroupTemplate groupTemplate = new GroupTemplate(group);
        // We unset the directory ID so that it is not checked further by the server.  We don't know the server's internal directory ids
        groupTemplate.setDirectoryId(-1);
        try
        {
            getCrowdClient().addGroup(groupTemplate);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
        try
        {
            return findGroupByName(group.getName());
        } catch (GroupNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public Group updateGroup(final GroupTemplate group)
            throws InvalidGroupException, GroupNotFoundException, OperationFailedException
    {
        Validate.notNull(group, "group cannot be null");
        Validate.notNull(group.getName(), "group.name cannot be null");

        final GroupTemplate groupTemplate = new GroupTemplate(group);
        // We unset the directory ID so that it is not checked further by the server.  We don't know the server's internal directory ids
        groupTemplate.setDirectoryId(-1);
        try
        {
            getCrowdClient().updateGroup(groupTemplate);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
        return findGroupByName(group.getName());
    }

    public Group renameGroup(final String oldName, final String newName)
            throws GroupNotFoundException, InvalidGroupException, OperationFailedException
    {
        throw new OperationNotSupportedException("Renaming of groups is not supported");
    }

    public void storeGroupAttributes(final String groupName, final Map<String, Set<String>> attributes)
            throws GroupNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().storeGroupAttributes(groupName, attributes);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeGroupAttributes(final String groupName, final String attributeName)
            throws GroupNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().removeGroupAttributes(groupName, attributeName);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeGroup(final String groupname) throws GroupNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().removeGroup(groupname);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> searchGroups(final EntityQuery<T> query) throws OperationFailedException
    {
        if (query.getEntityDescriptor().getEntityType() != Entity.GROUP ||
                query.getEntityDescriptor().getGroupType() == GroupType.LEGACY_ROLE)
        {
            throw new IllegalArgumentException("Query is not a group query.");
        }

        try
        {
            Class<T> returnType = query.getReturnType();
            if (String.class.isAssignableFrom(returnType))
            {
                return (List<T>) getCrowdClient().searchGroupNames(query.getSearchRestriction(), query.getStartIndex(), query.getMaxResults());
            } else if (Group.class.isAssignableFrom(returnType) || com.atlassian.crowd.embedded.api.Group.class.isAssignableFrom(returnType))
            {
                List<Group> groups = getCrowdClient().searchGroups(query.getSearchRestriction(), query.getStartIndex(), query.getMaxResults());
                return (List<T>) buildGroupListWithDirectoryId(groups);
            } else
            {
                throw new IllegalArgumentException("Unknown return type for query: " + returnType.getName());
            }
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
            throws OperationFailedException
    {
        try
        {
            return getCrowdClient().isUserDirectGroupMember(username, groupName);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public boolean isGroupDirectGroupMember(final String childGroup, final String parentGroup)
            throws OperationFailedException
    {
        try
        {
            return getCrowdClient().isGroupDirectGroupMember(childGroup, parentGroup);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void addUserToGroup(final String username, final String groupName)
            throws GroupNotFoundException, UserNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().addUserToGroup(username, groupName);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, OperationFailedException
    {
        try
        {
            getCrowdClient().addGroupToGroup(childGroup, parentGroup);
        } catch (UserNotFoundException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeUserFromGroup(final String username, final String groupName)
            throws GroupNotFoundException, UserNotFoundException, MembershipNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().removeUserFromGroup(username, groupName);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, MembershipNotFoundException, OperationFailedException
    {
        try
        {
            getCrowdClient().removeGroupFromGroup(childGroup, parentGroup);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> searchGroupRelationships(final MembershipQuery<T> query) throws OperationFailedException
    {
        try
        {
            // Members
            if (query.isFindChildren())
            {
                if (query.getEntityToReturn().getEntityType() == Entity.USER)
                {
                    if (query.getReturnType() == String.class)
                    {
                        return (List<T>) getCrowdClient().getNamesOfUsersOfGroup(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                    } else
                    {
                        List<User> users = getCrowdClient().getUsersOfGroup(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                        return (List<T>) buildUserListWithDirectoryId(users);
                    }
                } else if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
                {
                    if (query.getReturnType() == String.class)
                    {
                        return (List<T>) getCrowdClient().getNamesOfChildGroupsOfGroup(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                    } else
                    {
                        List<Group> groups = getCrowdClient().getChildGroupsOfGroup(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                        return (List<T>) buildGroupListWithDirectoryId(groups);
                    }
                } else
                {
                    throw new IllegalArgumentException("Query is not a group or user membership query.");
                }
            }

            // Memberships
            else
            {
                if (query.getEntityToMatch().getEntityType() == Entity.USER)
                {
                    if (query.getReturnType() == String.class)
                    {
                        return (List<T>) getCrowdClient().getNamesOfGroupsForUser(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                    } else
                    {
                        List<Group> groups = getCrowdClient().getGroupsForUser(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                        return (List<T>) buildGroupListWithDirectoryId(groups);
                    }
                } else if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
                {
                    if (query.getReturnType() == String.class)
                    {
                        return (List<T>) getCrowdClient().getNamesOfParentGroupsForGroup(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                    } else
                    {
                        List<Group> groups = getCrowdClient().getParentGroupsForGroup(query.getEntityNameToMatch(), query.getStartIndex(), query.getMaxResults());
                        return (List<T>) buildGroupListWithDirectoryId(groups);
                    }
                } else
                {
                    throw new IllegalArgumentException("Query is not a group or user membership query.");
                }
            }
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (GroupNotFoundException e)
        {
            return Collections.emptyList();
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (UserNotFoundException e)
        {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a token that can be used for querying events that have happened
     * after the token was generated.
     * <p/>
     * If the event token has not changed since the last call to this method,
     * it is guaranteed that no new events have been received.
     * <p/>
     * The format of event token is implementation specific and can change
     * without a warning.
     *
     * @return token that can be used for querying events that have happened after the token was generated
     * @throws com.atlassian.crowd.exception.UnsupportedCrowdApiException
     *                                  if the remote server does not support this operation
     * @throws OperationFailedException if the operation has failed for any other reason, including invalid arguments
     * @throws IncrementalSynchronisationNotAvailableException
     *                                  if the application cannot provide incremental synchronisation
     */
    public String getCurrentEventToken() throws OperationFailedException, IncrementalSynchronisationNotAvailableException
    {
        try
        {
            return getCrowdClient().getCurrentEventToken();
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Returns an events object which contains a new eventToken and events that
     * happened after the given {@code eventToken} was generated.
     * <p/>
     * If for any reason event store is unable to retrieve events that happened
     * after the event token was generated, an
     * {@link EventTokenExpiredException} will be thrown. The caller is then
     * expected to call {@link #getCurrentEventToken()} again before asking for
     * new events.
     *
     * @param eventToken event token that was retrieved by a call to {@link #getCurrentEventToken()} or {@link #getNewEvents(String)}
     * @return events object which contains a new eventToken and events that happened after the given {@code eventToken} was generated
     * @throws EventTokenExpiredException if events that happened after the event token was generated can not be retrieved
     * @throws com.atlassian.crowd.exception.UnsupportedCrowdApiException
     *                                    if the remote server does not support this operation
     * @throws OperationFailedException   if the operation has failed for any other reason, including invalid arguments
     */
    public Events getNewEvents(String eventToken) throws EventTokenExpiredException, OperationFailedException
    {
        try
        {
            return getCrowdClient().getNewEvents(eventToken);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void testConnection() throws OperationFailedException
    {
        try
        {
            getCrowdClient().testConnection();
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (Exception e)
        {
            // All sorts of other unpredicted horrors may occur.
            throw new OperationFailedException(e.getMessage(), e);
        }
    }

    /**
     * Remote crowd directories always support inactive accounts.
     *
     * @return true
     */
    public boolean supportsInactiveAccounts()
    {
        return true;
    }

    public boolean supportsNestedGroups()
    {
        // by default nested groups is OFF unless the attribute is explicitly set ON
        return attributes.getAttributeAsBoolean(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS, false);
    }

    public boolean isRolesDisabled()
    {
        return true; // REST api does not support roles
    }

    public String getDescriptiveName()
    {
        return DESCRIPTIVE_NAME;
    }

    /**
     * Called by the {@link com.atlassian.crowd.directory.loader.DirectoryInstanceLoader} after
     * constructing an InternalDirectory.
     *
     * @param attributes attributes map.
     */
    public void setAttributes(final Map<String, String> attributes)
    {
        this.attributes = new AttributeValuesHolder(attributes);

        // ==Short Explanation==
        // The attributes contain information we need to build our connection so this is the time to
        // construct our Crowd client, lazily.
        //
        // ==Long Explanation==
        // The creation of CrowdClient requires parameters such as url, username, password. These are
        // not available at the creation of {@link RemoteCrowdDirectory} time, but passed in as
        // attributes through this {@link #setAttributes(Map)} method call.
        //
        // We therefore instead create CrowdClient here, when parameters are available. However, by doing so,
        // any exceptions bubbling from lower level don't seem so belonging to this setter method.
        // As such, we delay the actual creation until the first use of CrowdClient so that
        // any exception due to bad parameters can be addressed as {@link OperationFailedException}
        // which fits well with the abstraction for each rest call.
        //
        createCrowdClientLazily();
    }

    public Set<String> getValues(final String name)
    {
        return attributes.getValues(name);
    }

    public String getValue(final String name)
    {
        return attributes.getValue(name);
    }

    private String getValue(final String name, String defaultValue)
    {
        final String val = attributes.getValue(name);
        if (val == null)
        {
            return defaultValue;
        }

        return val;
    }


    public Set<String> getKeys()
    {
        return attributes.getKeys();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        return this;
    }

    /**
     * This creates the crowd client lazily since any exception during client creation will break the current abstraction.
     */
    private void createCrowdClientLazily()
    {
        final ClientProperties properties = getClientProperties();

        crowdClientRef = new LazyReference<CrowdClient>()
        {
            @Override
            protected CrowdClient create() throws Exception
            {
                return crowdClientFactory.newInstance(properties);
            }
        };
    }

    protected ClientProperties getClientProperties()
    {
        final Properties properties = new Properties();
        setProperty(properties, CROWD_SERVER_URL, getValue(CROWD_SERVER_URL));
        setProperty(properties, APPLICATION_NAME, getValue(APPLICATION_NAME));
        setProperty(properties, APPLICATION_PASSWORD, getValue(APPLICATION_PASSWORD));
        setProperty(properties, CROWD_HTTP_TIMEOUT, getValue(CROWD_HTTP_TIMEOUT));
        setProperty(properties, CROWD_HTTP_MAX_CONNECTIONS, getValue(CROWD_HTTP_MAX_CONNECTIONS));
        setProperty(properties, CROWD_HTTP_PROXY_HOST, getValue(CROWD_HTTP_PROXY_HOST));
        setProperty(properties, CROWD_HTTP_PROXY_PORT, getValue(CROWD_HTTP_PROXY_PORT));
        setProperty(properties, CROWD_HTTP_PROXY_USERNAME, getValue(CROWD_HTTP_PROXY_USERNAME));
        setProperty(properties, CROWD_HTTP_PROXY_PASSWORD, getValue(CROWD_HTTP_PROXY_PASSWORD));
        return ClientPropertiesImpl.newInstanceFromProperties(properties);
    }

    /**
     * Sets the property if the value is not null.
     *
     * @param properties Properties class
     * @param key        key of the property
     * @param value      value of the property
     */
    private static void setProperty(final Properties properties, final String key, final String value)
    {
        if (value != null)
        {
            properties.setProperty(key, value);
        }
    }

    private CrowdClient getCrowdClient() throws OperationFailedException
    {
        try
        {
            return crowdClientRef.get();
        } catch (LazyReference.InitializationException ie)
        {
            throw new OperationFailedException("Failed to create remote crowd client", ie.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends User> T buildUserWithDirectoryId(final T user)
    {
        final UserTemplateWithAttributes userTemplateWithAttributes;
        if (user instanceof UserWithAttributes)
        {
            userTemplateWithAttributes = new UserTemplateWithAttributes((UserWithAttributes) user);
        } else
        {
            userTemplateWithAttributes = UserTemplateWithAttributes.ofUserWithNoAttributes(user);
        }

        userTemplateWithAttributes.setDirectoryId(directoryId);

        return (T) userTemplateWithAttributes;
    }

    private <T extends User> List<T> buildUserListWithDirectoryId(final List<T> users)
    {
        List<T> newUsers = new ArrayList<T>();
        for (T user : users)
        {
            newUsers.add(buildUserWithDirectoryId(user));
        }
        return newUsers;
    }

    @SuppressWarnings("unchecked")
    private <T extends Group> T buildGroupWithDirectoryId(final T group)
    {
        final GroupTemplateWithAttributes groupTemplateWithAttributes;
        if (group instanceof GroupWithAttributes)
        {
            groupTemplateWithAttributes = new GroupTemplateWithAttributes((GroupWithAttributes) group);
        } else
        {
            groupTemplateWithAttributes = GroupTemplateWithAttributes.ofGroupWithNoAttributes(group);
        }

        groupTemplateWithAttributes.setDirectoryId(directoryId);

        return (T) groupTemplateWithAttributes;
    }

    private <T extends Group> List<T> buildGroupListWithDirectoryId(final List<T> groups)
    {
        List<T> newGroups = new ArrayList<T>();
        for (T group : groups)
        {
            newGroups.add(buildGroupWithDirectoryId(group));
        }
        return newGroups;
    }

    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        try
        {
            return crowdClientRef.get().getMemberships();
        } catch (UnsupportedCrowdApiException unsupported)
        {
            /* Fall back to looping over calls for each group */
            logger.info("Using separate requests to retrieve membership data. " + unsupported.getMessage());

            return new DirectoryMembershipsIterable(this);
        } catch (ApplicationPermissionException e)
        {
            throw new OperationFailedException(e);
        } catch (InvalidAuthenticationException e)
        {
            throw new OperationFailedException(e);
        }
    }
}
