/**
 *
 */
package com.sysbliss.jira.plugins.workflow.auth;

import com.atlassian.crowd.embedded.api.User;

/**
 * @author jdoklovic
 */
public interface UserTokenManager {

    public User getUserFromToken(String token);

    public String findToken(final User user);

    public String createToken(final User user) throws Exception;
}
