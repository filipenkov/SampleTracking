package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.*;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.*;
import com.atlassian.crowd.plugin.rest.entity.*;
import com.atlassian.plugins.rest.common.*;
import org.apache.commons.lang.*;

/**
 * Utility class for UserEntity.
 */
public class UserEntityUtil
{
    private UserEntityUtil()
    {
        // prevent instantiation
    }

    /**
     * Expands a UserEntity from its minimal form to the expanded version. Attributes are expanded if <tt>expandAttributes</tt> is <tt>true</tt>, otherwise, UserEntity is
     * returned with no attributes expanded.
     * <p/>
     * N.B. This is not the same as expanding a user entity, which expands a UserEntity from its minimal form, to having
     * all the first name, last name, email, etc filled in. Expanding a UserEntity is automatically performed in
     * {@link com.atlassian.crowd.plugin.rest.entity.UserEntityExpander}.
     * 
     * @param applicationService ApplicationService to find a user
     * @param application name of the application requesting the user
     * @param minimalUserEntity Minimal representation of a UserEntity. Must include at least a username and a link.
     * @param expandAttributes set to true if the expanded UserEntity should expand attributes.
     * @return UserEntity expanded UserEntity
     * @throws IllegalArgumentException if the minimal UserEntity does not include at least a username and a link.
     * @throws UserNotFoundException if a user with the specified username could not be found.
     */
    public static UserEntity expandUser(final ApplicationService applicationService, final Application application, final UserEntity minimalUserEntity, final boolean expandAttributes)
            throws UserNotFoundException
    {
        Validate.notNull(applicationService);
        Validate.notNull(application);
        Validate.notNull(minimalUserEntity);
        Validate.notNull(minimalUserEntity.getName(), "Minimal user entity must include a username");
        Validate.notNull(minimalUserEntity.getLink(), "Minimal user entity must include a link");

        final String username = minimalUserEntity.getName();
        final Link userLink = minimalUserEntity.getLink();

        UserEntity expandedUser;
        if (expandAttributes)
        {
            UserWithAttributes user = applicationService.findUserWithAttributesByName(application, username);
            Link updatedLink = LinkUriHelper.updateUserLink(userLink, user.getName()); // use the canonical username in the link
            expandedUser = EntityTranslator.toUserEntity(user, user, updatedLink);
        }
        else
        {
            User user = applicationService.findUserByName(application, username);
            Link updatedLink = LinkUriHelper.updateUserLink(userLink, user.getName()); // use the canonical username in the link
            expandedUser = EntityTranslator.toUserEntity(user, updatedLink);
        }
        return expandedUser;
    }
}
