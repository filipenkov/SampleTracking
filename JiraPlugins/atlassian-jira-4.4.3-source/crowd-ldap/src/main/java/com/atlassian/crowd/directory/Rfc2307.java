package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.StringUtils;

import javax.naming.directory.Attributes;


/**
 * This class provides read-only support for the POSIX LDAP Schema (RFC2307)
 */
public class Rfc2307 extends RFC2307Directory
{
    private final PasswordEncoderFactory passwordEncoderFactory;

    public Rfc2307(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, final PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
        this.passwordEncoderFactory = passwordEncoderFactory;
    }

    public static String getStaticDirectoryType()
    {
        return "Generic Posix/RFC2307 Directory (Read-Only)";
    }

    /**
     * Translates a clear-text password into an encrypted one, based on the directory settings.
     *
     * @param unencodedPassword password
     * @return encoded password
     */
    protected String encodePassword(String unencodedPassword)
    {
        if (unencodedPassword == null)
        {
            return null;
        }

        String encryptionAlgorithm = ldapPropertiesMapper.getUserEncryptionMethod();
        if (!StringUtils.isBlank(encryptionAlgorithm))
        {
            PasswordEncoder passwordEncoder = passwordEncoderFactory.getLdapEncoder(encryptionAlgorithm);
            return passwordEncoder.encodePassword(unencodedPassword, null);
        }
        else
        {
            return unencodedPassword;
        }

    }

    @Override
    protected void getNewUserDirectorySpecificAttributes(final User user, final Attributes attributes)
    {
        addDefaultSnToUserAttributes(attributes, user.getName());
    }

    public String getDescriptiveName()
    {
        return Rfc2307.getStaticDirectoryType();
    }

    public void addUserToGroup(final String username, final String groupName) throws UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup) throws GroupNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    public void removeUserFromGroup(final String username, final String groupName) throws UserNotFoundException, GroupNotFoundException, MembershipNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup) throws GroupNotFoundException, MembershipNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public LDAPUserWithAttributes addUser(final UserTemplate user, final PasswordCredential credential) throws InvalidUserException, InvalidCredentialException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public Group addGroup(final GroupTemplate group) throws InvalidGroupException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public Group renameGroup(final String oldName, final String newName) throws GroupNotFoundException, InvalidGroupException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public User renameUser(final String oldName, final String newName) throws UserNotFoundException, InvalidUserException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public Group updateGroup(final GroupTemplate group) throws GroupNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public User updateUser(final UserTemplate user) throws UserNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public void removeUser(final String name) throws UserNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }

    @Override
    public void removeGroup(final String name) throws GroupNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("POSIX support is currently read-only");
    }
}