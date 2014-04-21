package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.util.PasswordHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class InternalDirectoryUtilsImpl implements InternalDirectoryUtils
{
    private final PasswordHelper passwordHelper;

    public InternalDirectoryUtilsImpl(PasswordHelper passwordHelper)
    {
        this.passwordHelper = passwordHelper;
    }

    public void validateDirectoryForEntity(final DirectoryEntity entity, Long directoryId)
    {
        Validate.notNull(entity, "entity cannot be null");
        Validate.notNull(entity.getDirectoryId(), "directoryId of entity cannot be null");
        Validate.isTrue(entity.getDirectoryId() == directoryId, "directoryId does not match the directoryId of the InternalDirectory");
    }

    public void validateUsername(String username)
    {
        if (StringUtils.isBlank(username))
        {
            throw new IllegalArgumentException("A username must not be null or empty or blank");
        }
    }

    public void validateCredential(PasswordCredential credential, String regex) throws InvalidCredentialException
    {
        if (credential == null || StringUtils.isBlank(credential.getCredential()))
        {
            throw new InvalidCredentialException("You cannot have an empty password");
        }

        if (StringUtils.isNotBlank(regex) && !passwordHelper.validateRegex(regex, credential))
        {
            throw new InvalidCredentialException("Your new password does not meet the directory complexity requirements");
        }
    }

    public void validateGroupName(Group group, String groupName)
    {
        if (StringUtils.isBlank(groupName))
        {
            throw new IllegalArgumentException("A group name must not be null or empty or blank");
        }
    }
}