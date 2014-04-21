package com.atlassian.crowd.dao.user;

import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.util.persistence.hibernate.batch.BatchResultWithIdReferences;

import java.util.Collection;

/**
 * Manages persistence of {@link User}.
 */
public interface InternalUserDao extends UserDao
{
    public BatchResultWithIdReferences<User> addAll(final Collection<UserTemplateWithCredentialAndAttributes> users);

    void removeAll(long directoryId) throws DirectoryNotFoundException;

    /**
     * Bulk find of users using SQL disjunction.
     *
     * @param directoryID the directory to search for the users.
     * @param usernames   names of users to find
     * @throws DirectoryNotFoundException if the directory cannot be found
     * @return collection of found users.
     */
    Collection<InternalUser> findByNames(long directoryID, Collection<String> usernames) throws DirectoryNotFoundException;
}
