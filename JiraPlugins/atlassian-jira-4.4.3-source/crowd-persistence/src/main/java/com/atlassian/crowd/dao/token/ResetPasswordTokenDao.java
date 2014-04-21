package com.atlassian.crowd.dao.token;

import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.model.token.ResetPasswordToken;

/**
 * Implements the ResetPasswordToken DAO.
 */
public interface ResetPasswordTokenDao
{
    /**
     * Adds the reset token to the DAO.
     *
     * @param token reset token to add.
     * @return reset token just added.
     */
    ResetPasswordToken addToken(ResetPasswordToken token);

    /**
     * Removes the reset token.
     *
     * @param username username used to remove reset token from the DAO.
     */
    void removeTokenByUsername(String username);

    /**
     * Finds the reset token by username.
     *
     * @param username username used to search for the reset token
     * @return reset password token
     * @throws ObjectNotFoundException if no reset tokens for <code>username</code> could be found
     */
    ResetPasswordToken findTokenByUsername(String username) throws ObjectNotFoundException;
}
