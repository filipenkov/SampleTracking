package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.ofbiz.db.DataAccessException;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.model.user.DelegatingUserWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.of;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;

/**
 * Implementation of the user DAO that works with OfBiz
 */
public class OfBizUserDao implements UserDao
{
    private final OfBizHelper ofBiz;
    private final DirectoryDao directoryDao;
    private final InternalMembershipDao membershipDao;
    private final ConcurrentMap<DirectoryEntityKey, OfBizUser> userCache = new ConcurrentHashMap<DirectoryEntityKey, OfBizUser>();
    /** Lazy cache of attributes. */
    private final ConcurrentMap<DirectoryEntityKey, Attributes> userAttributesCache = new ConcurrentHashMap<DirectoryEntityKey, Attributes>();

    public OfBizUserDao(final DelegatorInterface genericDelegator, final DirectoryDao directoryDao, final InternalMembershipDao membershipDao, final EventPublisher eventPublisher)
    {
        this.ofBiz = new OfBizHelper(genericDelegator);
        this.directoryDao = directoryDao;
        this.membershipDao = membershipDao;
        eventPublisher.register(this);
        buildCache();
    }

    /**
     * Listen for XMLRestoreFinishedEvents, which mean we need to clear any caches.
     * @param event XMLRestoreFinishedEvent
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onEvent(final XMLRestoreFinishedEvent event)
    {
        flushCache();
    }

    public OfBizUser findByName(final long directoryId, final String userName) throws UserNotFoundException
    {
        // Try with the case we have been given.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        OfBizUser user = userCache.get(DirectoryEntityKey.getKeyPreserveCase(directoryId, userName));
        if (user != null)
        {
            return user;
        }

             user = userCache.get(DirectoryEntityKey.getKey(directoryId, userName));
        if (user == null)
        {
            // Because the SPI says we should do this.
            throw new UserNotFoundException(userName);
        }
        return user;
    }

    public UserWithAttributes findByNameWithAttributes(final long directoryId, final String userName)
            throws UserNotFoundException
    {
        // Get the User from the cache
        OfBizUser user = findByName(directoryId, userName);
        // Get attributes from the cache if already loaded

        // Try with the case we have been given.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        Attributes attributes = userAttributesCache.get(DirectoryEntityKey.getKeyPreserveCase(directoryId, userName));
        if (attributes == null)
        {
            attributes = userAttributesCache.get(DirectoryEntityKey.getKey(directoryId, userName));
        }
        if (attributes == null)
        {
            // Get from the database and store in cache.
            List<GenericValue> attributesGenericValue = findAttributesGenericValues(directoryId, user.getId());
            attributes = OfBizAttributesBuilder.toAttributes(attributesGenericValue);
            userAttributesCache.put(DirectoryEntityKey.getKey(directoryId, userName), attributes);
        }
        return new DelegatingUserWithAttributes(user, attributes);
    }

    public PasswordCredential getCredential(final long directoryId, final String userName) throws UserNotFoundException
    {
        GenericValue userGenericValue = findUserGenericValue(directoryId, userName);
        if (userGenericValue == null)
        {
            return null;
        }

        String storedCredential = userGenericValue.getString(UserEntity.CREDENTIAL);
        if (storedCredential == null)
        {
            return null;
        }

        return new PasswordCredential(storedCredential, true);
    }

    public List<PasswordCredential> getCredentialHistory(final long directoryId, final String userName)
            throws UserNotFoundException
    {
        final GenericValue user = findUserGenericValue(directoryId, userName);

        final List<GenericValue> values = ofBiz.findByAnd(UserCredentialHistoryEntity.ENTITY, of(UserCredentialHistoryEntity.USER_ID,
                user.getLong(UserEntity.USER_ID)), singletonList(UserCredentialHistoryEntity.LIST_INDEX));
        return UserCredentialHistoryEntity.toCredentials(values);
    }

    private GenericValue findUserGenericValue(final long directoryId, final String userName)
            throws UserNotFoundException
    {
        final GenericValue userGenericValue = EntityUtil.getOnly(findUsers(of(UserEntity.DIRECTORY_ID, directoryId, UserEntity.LOWER_USER_NAME,
                toLowerCase(userName))));
        if (userGenericValue != null)
        {
            return userGenericValue;
        }
        else
        {
            throw new UserNotFoundException(userName);
        }
    }

    private List<GenericValue> findUsers(final Map<String, Object> filter)
    {
        return ofBiz.findByAnd(UserEntity.ENTITY, filter);
    }

    public BatchResult<User> addAll(Set<UserTemplateWithCredentialAndAttributes> users)
    {
        BatchResult<User> results = new BatchResult<User>(users.size());
        for (UserTemplateWithCredentialAndAttributes user : users) {
            try
            {
                final User addedUser = add(user, user.getCredential());
                results.addSuccess(addedUser);
            }
            catch (UserAlreadyExistsException e)
            {
                results.addFailure(user);
            }
            catch (IllegalArgumentException e)
            {
                results.addFailure(user);
            }
            catch (DataAccessException e)
            {
                // We want to try to catch as many failures as possible so that all the *other* users will
                // still get added
                results.addFailure(user);
            }
        }
        return results;
    }

    public synchronized User add(User user, PasswordCredential credential) throws UserAlreadyExistsException
    {
        if (credential != null)
        {
            Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
        }
        String userName = user.getName();

        // Check not a duplicate
        final GenericValue userGenericValue = EntityUtil.getOnly(findUsers(of(UserEntity.DIRECTORY_ID, user.getDirectoryId(), UserEntity.LOWER_USER_NAME,
                toLowerCase(user.getName()))));
        if (userGenericValue != null)
        {
            throw new UserAlreadyExistsException(user.getDirectoryId(), userName);
        }

        final Timestamp currentTimestamp = getCurrentTimestamp();
        final Map<String, Object> userData = UserEntity.getData(user, credential, currentTimestamp, currentTimestamp);
        ofBiz.createValue(UserEntity.ENTITY, userData);

        // We now retrieve the newly added user back out of the DB
        OfBizUser newUser;
        try
        {
            newUser = getUserFromDB(user.getDirectoryId(), userName);
        }
        catch (UserNotFoundException e)
        {
            // Strange ...
            throw new OperationFailedException("Created a new user '" + userName + "' - but was unable to retrieve them from the DB.", e);
        }
        putUserInCache(newUser);
        return newUser;
    }

    private Timestamp getCurrentTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }

    private OfBizUser getUserFromDB(Long directoryId, String userName) throws UserNotFoundException
    {
        return OfBizUser.from(findUserGenericValue(directoryId, userName));
    }

    public void storeAttributes(final User user, final Map<String, Set<String>> attributes) throws UserNotFoundException
    {
        for (final Map.Entry<String, Set<String>> attribute : checkNotNull(attributes).entrySet())
        {
            // remove attributes before adding new ones.
            // Duplicate key values are allowed, but we always add as a complete set under the key.
            removeAttribute(user, attribute.getKey());
            if ((attribute.getValue() != null) && !attribute.getValue().isEmpty())
            {
                storeAttributeValues(user, attribute.getKey(), attribute.getValue());
            }
        }
        // Clear the cache for this user
        userAttributesCache.remove(DirectoryEntityKey.getKey(user.getDirectoryId(), user.getName()));
    }

    private void storeAttributeValues(final User user, final String name, final Set<String> values)
            throws UserNotFoundException
    {
        // Need the users id, which is only in the database and not in the cache.
        final GenericValue userGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());
        Long userId = userGenericValue.getLong(UserEntity.USER_ID);
        for (final String value : values)
        {
            if (StringUtils.isNotEmpty(value))
            {
                storeAttributeValue(user.getDirectoryId(), userId, name, value);
            }
        }
    }

    public void removeAllUsers(long directoryId, Set<String> userNames)
    {
        for (String userName : userNames)
        {
            try
            {
                remove(findByName(directoryId,userName));
            }
            catch (UserNotFoundException e)
            {
                // do nothing
            }
        }
    }

    private void storeAttributeValue(final Long directoryId, final Long userId, final String name, final String value)
            throws UserNotFoundException
    {
        ofBiz.createValue(UserAttributeEntity.ENTITY, UserAttributeEntity.getData(directoryId,
                userId, name, value));
    }

    private List<GenericValue> findAttributesGenericValues(final Long directoryId, final Long userId)
    {
        return ofBiz.findByAnd(UserAttributeEntity.ENTITY, of(UserAttributeEntity.DIRECTORY_ID, directoryId,
                UserAttributeEntity.USER_ID, userId));
    }

    public synchronized User update(final User user) throws UserNotFoundException
    {
        final GenericValue userGenericValue = UserEntity.setData(user, findUserGenericValue(user.getDirectoryId(), user.getName()));

        userGenericValue.set(UserEntity.UPDATED_DATE, getCurrentTimestamp());

        OfBizUser newUser = OfBizUser.from(storeUser(userGenericValue));
        putUserInCache(newUser);
        return newUser;
    }

    public void updateCredential(final User user, final PasswordCredential credential, final int credentialHistory)
            throws UserNotFoundException
    {
        Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
        final GenericValue storeGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());
        storeGenericValue.set(UserEntity.CREDENTIAL, credential.getCredential());
        storeUser(storeGenericValue);
    }

    private GenericValue storeUser(final GenericValue userGenericValue)
    {
        ofBiz.store(userGenericValue);
        return userGenericValue;
    }

    public User rename(final User user, final String newName)
    {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(final User user, final String attributeName) throws UserNotFoundException
    {
        final GenericValue userGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());
        ofBiz.removeByAnd(UserAttributeEntity.ENTITY, of(UserAttributeEntity.USER_ID, userGenericValue.getLong(UserEntity.USER_ID),
                UserAttributeEntity.NAME, attributeName));
        userAttributesCache.remove(DirectoryEntityKey.getKey(user.getDirectoryId(), user.getName()));
    }

    public synchronized void remove(final User user) throws UserNotFoundException
    {
        final GenericValue userGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());

        // remove memberships
        membershipDao.removeAllUserMemberships(user);

        // Remove all attributes
        ofBiz.removeByAnd(UserAttributeEntity.ENTITY, of(UserAttributeEntity.USER_ID, userGenericValue.getLong(UserEntity.USER_ID)));
        // Remove User
        ofBiz.removeValue(userGenericValue);
        userCache.remove(DirectoryEntityKey.getKey(user));
        userAttributesCache.remove(DirectoryEntityKey.getKey(user.getDirectoryId(), user.getName()));
    }

    @SuppressWarnings ("unchecked")
    public <T> List<T> search(final long directoryId, final EntityQuery<T> query)
    {
        final UserQuery<T> userQuery = (UserQuery<T>) query;
        List<GenericValue> results;

        final EntityCondition baseCondition = new UserEntityConditionFactory(ofBiz).getEntityConditionFor(userQuery.getSearchRestriction());
        final EntityExpr directoryCondition = new EntityExpr(UserEntity.DIRECTORY_ID, EntityOperator.EQUALS, directoryId);
        final EntityCondition entityCondition;

        if (baseCondition == null)
        {
            return (getAllUsersFromCache(directoryId, query.getReturnType()));
        }
        else
        {
            final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>(2);
            entityConditions.add(baseCondition);
            entityConditions.add(directoryCondition);
            entityCondition = new EntityConditionList(entityConditions, EntityOperator.AND);
        }
        results = ofBiz.findByCondition(UserEntity.ENTITY, entityCondition, null, Collections.singletonList(UserEntity.LOWER_USER_NAME));

        ArrayList<T> typedResults = new ArrayList<T>(results.size());
        final Function<GenericValue, T> valueFunction = (Function<GenericValue, T>) (query.getReturnType().equals(String.class) ? TO_USERNAME_FUNCTION : TO_USER_FUNCTION);
        for (GenericValue result : results)
        {
            typedResults.add(valueFunction.apply(result));
        }

        return typedResults;
    }

    @SuppressWarnings ({ "unchecked" })
    private <T> List<T> getAllUsersFromCache(final long directoryId, final Class<T> returnType)
    {
        final List<OfBizUser> allUsers = new ArrayList<OfBizUser>();
        for (OfBizUser user : userCache.values())
        {
            if (user.getDirectoryId() == directoryId)
            {
                allUsers.add(user);
            }
        }
        if (returnType.isAssignableFrom(String.class))
        {
            // Transform from the lower case name to the case preserving name
            Function<OfBizUser, String> valueFunction = new Function<OfBizUser, String>()
            {
                public String apply(final OfBizUser from)
                {
                    return from.getName();
                }
            };
            ArrayList<T> typedResults = new ArrayList<T>(allUsers.size());
            for (OfBizUser user : allUsers)
            {
                typedResults.add((T) valueFunction.apply(user));
            }
            return typedResults;
        }
        // If the required return type is anything that OfBizUser can be cast to, then return OfBizUser Objects
        if (returnType.isAssignableFrom(OfBizUser.class))
        {
            return (List<T>) allUsers;
        }
        throw new IllegalArgumentException("Class type for return values ('" + returnType + "') is not 'String' or 'User'");
    }

    private static final Function<GenericValue, String> TO_USERNAME_FUNCTION = new Function<GenericValue, String>()
    {
        public String apply(final GenericValue gvUser)
        {
            return gvUser.getString(UserEntity.USER_NAME);
        }
    };

    private static final Function<GenericValue, OfBizUser> TO_USER_FUNCTION = new Function<GenericValue, OfBizUser>()
    {
        public OfBizUser apply(final GenericValue gvUser)
        {
            return OfBizUser.from(gvUser);
        }
    };

    public synchronized void flushCache()
    {
        userAttributesCache.clear();
        userCache.clear();
        buildCache();
    }

    private void buildCache()
    {
        for (Directory directory : directoryDao.findAll())
        {
            List<GenericValue> userGenericValues = findUsers(of(UserEntity.DIRECTORY_ID, directory.getId()));
            for (GenericValue userGenericValue : userGenericValues)
            {
                putUserInCache(TO_USER_FUNCTION.apply(userGenericValue));
            }
        }
    }

    private void putUserInCache(OfBizUser user)
    {
        DirectoryEntityKey key = DirectoryEntityKey.getKey(user.getDirectoryId(), user.getName());
        userCache.put(key, user);
    }

}
