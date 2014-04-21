package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.*;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.encoder.UpgradeablePasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.UserUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class holds methods that are common to both {@link InternalDirectory} and {@link CachingDirectory}.
 */
public abstract class AbstractInternalDirectory implements InternalRemoteDirectory
{
    public static final String DESCRIPTIVE_NAME = "Crowd Internal Directory";
    public static final String ATTRIBUTE_PASSWORD_REGEX = "password_regex";
    public static final String ATTRIBUTE_PASSWORD_MAX_ATTEMPTS = "password_max_attempts";
    public static final String ATTRIBUTE_PASSWORD_HISTORY_COUNT = "password_history_count";
    public static final String ATTRIBUTE_USER_ENCRYPTION_METHOD = "user_encryption_method";
    public static final String ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME = "password_max_change_time";
    private static final int MILLIS_IN_DAY = 60000 * 60 * 24;
    private static final Logger logger = LoggerFactory.getLogger(InternalDirectory.class);// configuration parameters
    protected long directoryId;
    protected AttributeValuesHolder attributes;// injected dependencies
    protected final PasswordEncoderFactory passwordEncoderFactory;
    protected final DirectoryDao directoryDao;
    protected final UserDao userDao;
    protected final GroupDao groupDao;
    protected final MembershipDao membershipDao;
    protected final InternalDirectoryUtils internalDirectoryUtils;

    public AbstractInternalDirectory(InternalDirectoryUtils internalDirectoryUtils, PasswordEncoderFactory passwordEncoderFactory, DirectoryDao directoryDao, UserDao userDao, GroupDao groupDao, MembershipDao membershipDao)
    {
        this.internalDirectoryUtils = internalDirectoryUtils;
        this.directoryDao = directoryDao;
        this.passwordEncoderFactory = passwordEncoderFactory;
        this.membershipDao = membershipDao;
        this.groupDao = groupDao;
        this.userDao = userDao;
    }

    public long getDirectoryId()
    {
        return this.directoryId;
    }

    /**
     * Called by the {@link com.atlassian.crowd.directory.loader.DirectoryInstanceLoader} after
     * constructing an InternalDirectory.
     *
     * @param id The unique <code>id</code> of the Directory stored in the database.
     */
    public void setDirectoryId(final long id)
    {
        this.directoryId = id;
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
    }

    public Set<String> getValues(final String name)
    {
        return attributes.getValues(name);
    }

    public String getValue(final String name)
    {
        return attributes.getValue(name);
    }

    public Set<String> getKeys()
    {
        return attributes.getKeys();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public String getDescriptiveName()
    {
        return DESCRIPTIVE_NAME;
    }

    public TimestampedUser findUserByName(final String name) throws UserNotFoundException
    {
        Validate.notNull(name, "name argument cannot be null");

        return userDao.findByName(this.getDirectoryId(), name);
    }

    public UserWithAttributes findUserWithAttributesByName(final String name) throws UserNotFoundException
    {
        Validate.notNull(name, "name argument cannot be null");

        return userDao.findByNameWithAttributes(this.getDirectoryId(), name);
    }

    /**
     * @param name       The name of the user (username).
     * @param credential The supplied credentials (password).
     * @return user entity.
     * @throws com.atlassian.crowd.exception.InactiveAccountException
     *          The supplied user is inactive.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException
     *          Authentication with the provided credentials failed OR the user has exceeded the maximum number of failed authentication attempts.
     * @throws com.atlassian.crowd.exception.UserNotFoundException
     *          The user wth the supplied name does not exist.
     * @throws com.atlassian.crowd.exception.ExpiredCredentialException
     *          The user's credentials have expired. The user must change their credentials in order to successfully authenticate.
     */
    public User authenticate(final String name, final PasswordCredential credential)
            throws InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException
    {
        UserWithAttributes user = userDao.findByNameWithAttributes(getDirectoryId(), name);

        // check if the user is active
        if (user.isActive())
        {
            // authenticate the user
            processAuthentication(user, credential);

            return user;
        } else
        {
            throw new InactiveAccountException(user.getName());
        }
    }

    /**
     * @param user       user with attributes.
     * @param credential password credential to authenticate with.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException
     *          authentication failed or if the user has exceeded the number of failed authentication attempts.
     * @throws com.atlassian.crowd.exception.UserNotFoundException
     *          if the user has been deleted by another thread during the processing.
     * @throws com.atlassian.crowd.exception.ExpiredCredentialException
     *          The user's credentials have expired. The user must change their credentials in order to successfully authenticate.
     */
    private void processAuthentication(UserWithAttributes user, PasswordCredential credential)
            throws InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException
    {
        // Has the user exceeded their maximum password attempts?
        // If yes an InvalidAuthenticationException will be thrown
        long currentInvalidAttempts = processPasswordAttempts(user);

        Map<String, Set<String>> attributesToUpdate = new HashMap<String, Set<String>>();

        // authenticate the principal
        try
        {

            final PasswordCredential currentCredential = userDao.getCredential(directoryId, user.getName());

            authenticate(user, credential, currentCredential, getValue(ATTRIBUTE_USER_ENCRYPTION_METHOD));

            // check if the password change attribute needs to be reset
            boolean requiresPasswordChange = requiresPasswordChange(user);

            // set if the password needs to be changed
            attributesToUpdate.put(com.atlassian.crowd.model.user.UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.toString(requiresPasswordChange)));

            // authentication worked fine, set the invalid attempts to 0
            attributesToUpdate.put(com.atlassian.crowd.model.user.UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(Long.toString(0)));

            // update the last password authentication
            attributesToUpdate.put(com.atlassian.crowd.model.user.UserConstants.LAST_AUTHENTICATED, Collections.singleton(Long.toString(System.currentTimeMillis())));

            userDao.storeAttributes(user, attributesToUpdate);

            // prevent the auth process from getting any further if password has expired
            if (requiresPasswordChange)
            {
                logger.info(user.getName() + ": Attempting to log in with expired passsword.");
                throw new ExpiredCredentialException("Attempting to log in with expired passsword.");
            }
        } catch (InvalidAuthenticationException e)
        {
            // The user has entered incorrect password details
            // increment the invalid password attempts
            currentInvalidAttempts++;

            // set this on the principal object
            attributesToUpdate.put(com.atlassian.crowd.model.user.UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(Long.toString(currentInvalidAttempts)));

            userDao.storeAttributes(user, attributesToUpdate);

            throw e;
        }
    }

    /**
     * @param user user with attributes.
     * @return current number of invalid password attempts attribute from the user.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException
     *          if the maximum allowed invalid password attempts has been reached.
     * @throws com.atlassian.crowd.exception.UserNotFoundException
     *          if the user was deleted by another thread and hence cannot be found during the update.
     */
    private long processPasswordAttempts(UserWithAttributes user)
            throws InvalidAuthenticationException, UserNotFoundException
    {
        long currentInvalidAttempts = currentPrincipalInvalidPasswordAttempts(user);
        long maxInvalidAttempts;

        String maxAttemptValue = getValue(ATTRIBUTE_PASSWORD_MAX_ATTEMPTS);
        if (maxAttemptValue != null)
        {
            maxInvalidAttempts = Long.parseLong(maxAttemptValue);

            // if enforcing invalid attempts, enforce
            if (maxInvalidAttempts > 0 && currentInvalidAttempts >= maxInvalidAttempts)
            {
                // principal needs to have their password changed, this password is locked!
                Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();
                attributes.put(com.atlassian.crowd.model.user.UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.TRUE.toString()));
                userDao.storeAttributes(user, attributes);

                logger.info(user.getName() + ": Maximum allowed invalid password attempts has been reached.");
                throw new InvalidAuthenticationException("Maximum allowed invalid password attempts has been reached");
            }
        }
        return currentInvalidAttempts;
    }

    /**
     * @param user user with attributes.
     * @return long value of the invalid password attempts attribute on the user.
     */
    protected long currentPrincipalInvalidPasswordAttempts(UserWithAttributes user)
    {
        String attemptsAsString = user.getValue(com.atlassian.crowd.model.user.UserConstants.INVALID_PASSWORD_ATTEMPTS);

        long longAttempts = 0;

        if (attemptsAsString != null)
        {
            try
            {
                longAttempts = Long.parseLong(attemptsAsString);
            } catch (NumberFormatException e)
            {
                // can't do much
            }
        }

        return longAttempts;
    }

    /**
     * @param user user with attributes.
     * @return <code>true</code> if the requires password change attribute on the user is set to true, or if
     *         the password last changed attribute on the user exceeds the password max change time attribute on the
     *         directory (ie. password timeout).
     */
    protected boolean requiresPasswordChange(UserWithAttributes user)
    {
        // if the requires password change is already set to true, we should use that.
        boolean requiresPasswordChange = Boolean.parseBoolean(user.getValue(com.atlassian.crowd.model.user.UserConstants.REQUIRES_PASSWORD_CHANGE));

        if (requiresPasswordChange)
        {
            return true;
        }

        String maxChangeValue = getValue(ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME);

        // get the max change time from the Internal Directory
        if (maxChangeValue != null)
        {

            long maxUnchangedDays = Long.parseLong(maxChangeValue);

            if (maxUnchangedDays > 0L)
            {
                Date lastChanged;

                String time = user.getValue(com.atlassian.crowd.model.user.UserConstants.PASSWORD_LASTCHANGED);
                if (time != null)
                {
                    // stored as a long in the db
                    try
                    {
                        lastChanged = new Date(Long.parseLong(time));

                    } catch (NumberFormatException e)
                    {
                        // invalid, reset to now
                        lastChanged = new Date();
                    }
                } else
                {
                    lastChanged = new Date();
                }

                Date now = new Date();

                long maxUnchangedMilli = maxUnchangedDays * MILLIS_IN_DAY;

                if ((now.getTime() - lastChanged.getTime()) > maxUnchangedMilli)
                {
                    requiresPasswordChange = true;
                }
            }
        }
        return requiresPasswordChange;
    }

    private void authenticate(User user, PasswordCredential providedCredential, PasswordCredential storedCredential, String encoderAlgorithm)
            throws InvalidAuthenticationException, UserNotFoundException
    {
        // Get the required encryption algorithm
        PasswordEncoder encoder = passwordEncoderFactory.getInternalEncoder(encoderAlgorithm);

        // Now iterate over the credentials to authenticcate the user
        if (!encoder.isPasswordValid(storedCredential.getCredential(), providedCredential.getCredential(), null))
        {
            throw new InvalidAuthenticationException("Failed to authenticate principal, password was invalid");
        }

        upgradePasswordIfRequired(user, encoder, storedCredential.getCredential(), providedCredential.getCredential());
    }

    private void upgradePasswordIfRequired(User user, PasswordEncoder encoder, String encPass, String rawPass)
            throws UserNotFoundException
    {
        // When using UpgradeablePasswordEncoder, we might be asked to re-encode the password.
        if (encoder instanceof UpgradeablePasswordEncoder)
        {
            final UpgradeablePasswordEncoder upgradeableEncoder = (UpgradeablePasswordEncoder) encoder;
            if (upgradeableEncoder.isUpgradeRequired(encPass))
            {
                final String encPassword = upgradeableEncoder.encodePassword(rawPass, null);
                final int maxHistoryCount = NumberUtils.toInt(getValue(ATTRIBUTE_PASSWORD_HISTORY_COUNT), 0);
                userDao.updateCredential(user, new PasswordCredential(encPassword, true), maxHistoryCount);
            }
        }
    }

    public abstract User addUser(UserTemplate user, PasswordCredential credential)
            throws InvalidCredentialException, InvalidUserException, UserAlreadyExistsException, OperationFailedException;

    protected void encryptCredential(PasswordCredential passwordCredential)
    {
        // Check to see if the credential needs encrypting
        if (passwordCredential != null && !passwordCredential.isEncryptedCredential())
        {
            String encryptedPassword = getEncoder().encodePassword(passwordCredential.getCredential(), null);
            passwordCredential.setCredential(encryptedPassword);
            passwordCredential.setEncryptedCredential(true);
        }
    }

    protected PasswordEncoder getEncoder()
    {
        String userEncoder = getValue(ATTRIBUTE_USER_ENCRYPTION_METHOD);
        return passwordEncoderFactory.getInternalEncoder(userEncoder);
    }

    public User updateUser(final UserTemplate user) throws InvalidUserException, UserNotFoundException
    {
        internalDirectoryUtils.validateDirectoryForEntity(user, directoryId);

        // prepoulate names (displayname, firstname, lastname)
        User prepopulatedUser = UserUtils.populateNames(user);

        try
        {
            return userDao.update(prepopulatedUser);
        } catch (IllegalArgumentException e)
        {
            throw new InvalidUserException(user, e.getMessage(), e);
        }
    }

    static String historyMatchDescription(int historyCount)
    {
        switch (historyCount)
        {
            case 1:
                return "the current password";
            case 2:
                return "either the current password or the previous password";
            default:
                return "either the current password or one of the previous " + (historyCount - 1) + " passwords";
        }
    }

    public void updateUserCredential(final String name, final PasswordCredential newCredential)
            throws InvalidCredentialException, UserNotFoundException
    {
        User user = userDao.findByName(this.getDirectoryId(), name);

        // validate the password meets expectations
        internalDirectoryUtils.validateCredential(newCredential, getValue(ATTRIBUTE_PASSWORD_REGEX));

        // check password history
        int historyCount = 0;
        String historyCountString = getValue(ATTRIBUTE_PASSWORD_HISTORY_COUNT);
        if (NumberUtils.isNumber(historyCountString))
        {
            historyCount = Integer.parseInt(historyCountString);

            // check if the password was already used
            final PasswordCredential currentCredential = userDao.getCredential(directoryId, name);

            final List<PasswordCredential> credentialHistory = userDao.getCredentialHistory(directoryId, name);

            if (historyCount != 0 && !isUniquePassword(newCredential, currentCredential, credentialHistory, historyCount))
            {
                // The user has tried to use a password that already exists for them
                // since we are tracking password history, throw an exception
                throw new InvalidCredentialException("Unable to update password since this password matches "
                        + historyMatchDescription(historyCount) + ".");
            }
        }

        // encrypt the new password
        encryptCredential(newCredential);

        // update password and credential history list
        try
        {
            userDao.updateCredential(user, newCredential, historyCount);
        } catch (IllegalArgumentException e)
        {
            throw new InvalidCredentialException(e);
        }

        // update attributes
        Map<String, Set<String>> userAttributes = ImmutableMap.of(
                com.atlassian.crowd.model.user.UserConstants.PASSWORD_LASTCHANGED, Collections.singleton(Long.toString(System.currentTimeMillis())),
                com.atlassian.crowd.model.user.UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.FALSE.toString()),
                com.atlassian.crowd.model.user.UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(Long.toString(0))
        );

        userDao.storeAttributes(user, userAttributes);
    }

    /**
     * Checks if the new encrypted credential has already been used by the the user
     * in their credential history.
     *
     * @param newCredential        The new encrypted credential.
     * @param oldCredential        The old encrypted credential.
     * @param credentialHistory    Previous credentials used by the User
     * @param lastPasswordsToCheck number of passwords to check.
     * @return boolean <code>true</code> if the password does not match the existing password or any from the user's credential history.
     */
    private boolean isUniquePassword(PasswordCredential newCredential, PasswordCredential oldCredential, List<PasswordCredential> credentialHistory, int lastPasswordsToCheck)
    {
        Validate.isTrue(false == newCredential.isEncryptedCredential(),
                "The credentials should not be encrypted for the unique password check");

        String newPassword = newCredential.getCredential();

        PasswordEncoder encoder = getEncoder();

        // check current credential
        if (oldCredential != null && encoder.isPasswordValid(oldCredential.getCredential(), newPassword, null))
        {
            return false;
        }

        // check up to lastPasswordsToCheck in credential history
        if (lastPasswordsToCheck > credentialHistory.size())
        {
            lastPasswordsToCheck = credentialHistory.size();
        }

        for (int i = credentialHistory.size() - lastPasswordsToCheck; i < credentialHistory.size(); i++)
        {
            PasswordCredential historicalCredential = credentialHistory.get(i);
            if (encoder.isPasswordValid(historicalCredential.getCredential(), newPassword, null))
            {
                return false;
            }
        }

        return true;
    }

    public User renameUser(final String oldName, final String newName)
            throws InvalidUserException, UserNotFoundException, UserAlreadyExistsException
    {
        Validate.notEmpty(oldName, "oldName cannot be null or empty");
        Validate.notEmpty(newName, "newName cannot be null or empty");

        User user = findUserByName(oldName);

        internalDirectoryUtils.validateUsername(newName);

        try
        {
            findUserByName(newName);
            throw new UserAlreadyExistsException(getDirectoryId(), newName);
        } catch (UserNotFoundException e)
        {
            // sweet, no name clash
            return userDao.rename(user, newName);
        }
    }

    public void storeUserAttributes(final String username, final Map<String, Set<String>> attributes)
            throws UserNotFoundException
    {
        Validate.notNull(attributes, "attributes cannot be null");
        User user = findUserByName(username);
        userDao.storeAttributes(user, attributes);
    }

    public void removeUserAttributes(final String username, final String attributeName) throws UserNotFoundException
    {
        Validate.notEmpty(username, "username cannot be null or empty");
        Validate.notNull(attributeName, "attributeName cannot be null");

        User user = findUserByName(username);
        userDao.removeAttribute(user, attributeName);
    }

    public void removeUser(final String name) throws UserNotFoundException
    {
        User user = findUserByName(name);
        userDao.remove(user);
    }

    public void removeAllUsers(Set<String> userNames)
    {
        userDao.removeAllUsers(this.getDirectoryId(), userNames);
    }

    public void removeAllGroups(Set<String> groupNames)
    {
        groupDao.removeAllGroups(this.getDirectoryId(), groupNames);
    }

    public <T> List<T> searchUsers(final EntityQuery<T> query)
    {
        Validate.notNull(query, "query cannot be null");

        return userDao.search(this.getDirectoryId(), query);
    }

    public InternalDirectoryGroup findGroupByName(String name) throws GroupNotFoundException
    {
        Validate.notNull(name, "name argument cannot be null");

        return groupDao.findByName(this.getDirectoryId(), name);
    }

    public GroupWithAttributes findGroupWithAttributesByName(final String name) throws GroupNotFoundException
    {
        Validate.notNull(name, "name argument cannot be null");

        return groupDao.findByNameWithAttributes(this.getDirectoryId(), name);
    }

    public Group addGroup(final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException
    {
        internalDirectoryUtils.validateDirectoryForEntity(group, directoryId);

        internalDirectoryUtils.validateGroupName(group, group.getName());

        try
        {
            return group.isLocal() ? groupDao.addLocal(group) : groupDao.add(group);
        } catch (IllegalArgumentException e)
        {
            throw new InvalidGroupException(group, e.getMessage(), e);
        } catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public abstract Group addLocalGroup(final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException;

    public Group updateGroup(final GroupTemplate group) throws InvalidGroupException, GroupNotFoundException
    {
        internalDirectoryUtils.validateDirectoryForEntity(group, directoryId);

        try
        {
            return groupDao.update(group);
        } catch (IllegalArgumentException e)
        {
            throw new InvalidGroupException(group, e.getMessage(), e);
        }
    }

    public Group renameGroup(final String oldName, final String newName) throws InvalidGroupException, GroupNotFoundException
    {
        Validate.notEmpty(oldName, "oldName cannot be null or empty");
        Validate.notEmpty(newName, "newName cannot be null or empty");

        Group group = findGroupByName(oldName);

        internalDirectoryUtils.validateGroupName(group, newName);

        try
        {
            findGroupByName(newName);
            throw new InvalidGroupException(group, "Cannot rename group as group with new name already exists: " + newName);
        } catch (GroupNotFoundException e)
        {
            // sweet, no name clash
            return groupDao.rename(group, newName);
        }
    }

    public void storeGroupAttributes(final String groupName, final Map<String, Set<String>> attributes) throws GroupNotFoundException
    {
        Validate.notEmpty(groupName, "groupName cannot be null or empty");
        Validate.notNull(attributes, "attributes cannot be null");

        Group group = findGroupByName(groupName);
        groupDao.storeAttributes(group, attributes);
    }

    public void removeGroupAttributes(final String groupName, final String attributeName) throws GroupNotFoundException
    {
        Validate.notEmpty(groupName, "groupName cannot be null or empty");
        Validate.notNull(attributeName, "attributeName cannot be null");

        Group group = findGroupByName(groupName);
        groupDao.removeAttribute(group, attributeName);
    }

    public void removeGroup(final String name) throws GroupNotFoundException
    {
        Group group = findGroupByName(name);

        groupDao.remove(group);
    }

    public <T> List<T> searchGroups(final EntityQuery<T> query)
    {
        Validate.notNull(query, "query cannot be null");

        return groupDao.search(getDirectoryId(), query);
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
    {
        Validate.notEmpty(username, "username cannot be null or empty");
        Validate.notEmpty(groupName, "groupName cannot be null or empty");

        return membershipDao.isUserDirectMember(getDirectoryId(), username, groupName);
    }

    public boolean isGroupDirectGroupMember(final String childGroup, final String parentGroup)
    {
        Validate.notEmpty(childGroup, "childGroup cannot be null or empty");
        Validate.notEmpty(parentGroup, "parentGroup cannot be null or empty");

        return membershipDao.isGroupDirectMember(getDirectoryId(), childGroup, parentGroup);
    }

    public void addUserToGroup(final String username, final String groupName)
            throws UserNotFoundException, GroupNotFoundException
    {
        Validate.notEmpty(username, "username cannot be null or empty");
        Validate.notEmpty(groupName, "groupName cannot be null or empty");

        membershipDao.addUserToGroup(getDirectoryId(), username, groupName);
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup)
            throws InvalidMembershipException, GroupNotFoundException
    {
        Validate.notEmpty(childGroup, "childGroup cannot be null or empty");
        Validate.notEmpty(parentGroup, "parentGroup cannot be null or empty");

        Group child = findGroupByName(childGroup);
        Group parent = findGroupByName(parentGroup);

        if (child.getType().equals(parent.getType()))
        {
            membershipDao.addGroupToGroup(getDirectoryId(), childGroup, parentGroup);
        } else
        {
            throw new InvalidMembershipException("Cannot add group of type " + child.getType().name() + " to group of type " + parent.getType().name());
        }
    }

    public void removeUserFromGroup(final String username, final String groupName)
            throws MembershipNotFoundException, GroupNotFoundException, UserNotFoundException
    {
        Validate.notEmpty(username, "username cannot be null or empty");
        Validate.notEmpty(groupName, "groupName cannot be null or empty");

        // eager ONFE
        findUserByName(username);
        findGroupByName(groupName);

        if (!isUserDirectGroupMember(username, groupName))
        {
            throw new MembershipNotFoundException(username, groupName);
        }

        membershipDao.removeUserFromGroup(getDirectoryId(), username, groupName);
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup)
            throws InvalidMembershipException, MembershipNotFoundException, GroupNotFoundException
    {
        Validate.notEmpty(childGroup, "childGroup cannot be null or empty");
        Validate.notEmpty(parentGroup, "parentGroup cannot be null or empty");

        Group child = findGroupByName(childGroup);
        Group parent = findGroupByName(parentGroup);

        if (!isGroupDirectGroupMember(childGroup, parentGroup))
        {
            throw new MembershipNotFoundException(childGroup, parentGroup);
        }

        if (child.getType().equals(parent.getType()))
        {
            membershipDao.removeGroupFromGroup(getDirectoryId(), childGroup, parentGroup);
        } else
        {
            throw new InvalidMembershipException("Cannot remove group of type " + child.getType().name() + " from group of type " + parent.getType().name());
        }
    }

    public <T> List<T> searchGroupRelationships(final MembershipQuery<T> query)
    {
        Validate.notNull(query, "query cannot be null");

        return membershipDao.search(getDirectoryId(), query);
    }

    /**
     * Does nothing, connection is determined by the ability to communicate with the database. Crowd
     * wouldn't have started if the database connection failed.
     */
    public void testConnection() throws OperationFailedException
    {
        // do nothing
    }

    /**
     * Internal directories always support inactive accounts.
     *
     * @return true
     */
    public boolean supportsInactiveAccounts()
    {
        return true;
    }

    /**
     * @return <code>true</code> because Internal Directories support nested groups as of Crowd 2.0.
     */
    public boolean supportsNestedGroups()
    {
        // by default nested groups is OFF unless the attribute is explicitly set ON
        return attributes.getAttributeAsBoolean(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS, false);
    }

    public boolean isRolesDisabled()
    {
        return false;
    }
    @Override
    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        return new DirectoryMembershipsIterable(this);
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        return this;
    }
}
