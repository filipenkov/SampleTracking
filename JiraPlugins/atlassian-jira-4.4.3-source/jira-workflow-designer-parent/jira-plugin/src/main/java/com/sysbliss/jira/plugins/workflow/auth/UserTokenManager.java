/**
 *
 */
package com.sysbliss.jira.plugins.workflow.auth;

import com.opensymphony.user.User;
import com.sysbliss.jira.plugins.workflow.exception.FlexLoginException;

/**
 * @author jdoklovic
 */
public interface UserTokenManager {

    public String login(String username, String password) throws FlexLoginException;

    public User getUserFromToken(String token);

    public String findToken(final User user);

    public String createToken(final User user) throws Exception;
}
