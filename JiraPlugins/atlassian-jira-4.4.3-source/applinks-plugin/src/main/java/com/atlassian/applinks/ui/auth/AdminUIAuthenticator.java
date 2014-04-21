package com.atlassian.applinks.ui.auth;

import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.security.Principal;


/**
 * Component for use in servlet filters and interceptors to limit AppLinks end-point access to administrators.
 */
public class AdminUIAuthenticator
{
    /**
     * Username request parameter
     */
    public static final String ADMIN_USERNAME = "al_username";
    /**
     * Password request parameter
     */
    public static final String ADMIN_PASSWORD = "al_password";

    private static final String ADMIN_SESSION_KEY = "al_auth";

    private final UserManager userManager;
    private final AuthenticationListener authenticationListener;
    private final InternalHostApplication internalHostApplication;

    public AdminUIAuthenticator(final UserManager userManager, AuthenticationListener authenticationListener, InternalHostApplication internalHostApplication)
    {
        this.userManager = userManager;
        this.authenticationListener = authenticationListener;
        this.internalHostApplication = internalHostApplication;
    }

    /**
     * Check the supplied username and password, and set a session flag if valid.
     *
     * @param username       the username of the admin user to authenticate as
     * @param password       the password of the admin user to authenticate as
     * @param sessionHandler a {@link SessionHandler} for mutating the user's current session
     * @return <ul><li>true if the supplied username & password <strong>are not</strong> null and they match an admin user; or</li>
     *         <li>true if the supplied username & password <strong>are</strong> null, and the currently logged in user is an admin; or</li>
     *         <li>false otherwise.</li></ul>
     * @see #canAccessAdminUI(String, String)
     */
    public boolean canAccessAdminUI(final String username, final String password, final SessionHandler sessionHandler)
    {
        if (sessionHandler.get(ADMIN_SESSION_KEY) != null)
        {
            return true;
        }

        if (canAccessAdminUI(username, password))
        {
            sessionHandler.set(ADMIN_SESSION_KEY, true);
            return true;
        }

        return false;
    }

    /**
     * @param username the username of the admin user to authenticate as, can be null
     * @param password the password of the admin user to authenticate as, can be null
     * @return <ul><li>true if the supplied username & password <strong>are not</strong> null and they match an admin user; or</li>
     *         <li>true if the supplied username & password <strong>are</strong> null, and the currently logged in user is an admin; or</li>
     *         <li>false otherwise.</li></ul>
     */
    public boolean canAccessAdminUI(final String username, final String password)
    {
        boolean isAdmin = false;

        if (username != null)
        {
            if (userManager.authenticate(username, password) && hasAppLinksAdminRights(username))
            {
                isAdmin = true;
            }
        }
        else
        {
            final String currentUser = userManager.getRemoteUsername();
            if (currentUser != null && hasAppLinksAdminRights(currentUser))
            {
                isAdmin = true;
            }
        }

        return isAdmin;
    }

    public boolean canAccessAdminUI(final HttpServletRequest request)
    {
        return request.getSession().getAttribute(ADMIN_SESSION_KEY) != null ||
                hasAppLinksAdminRights(userManager.getRemoteUsername());
    }

    public boolean canCurrentUserAccessAdminUI()
    {
        return canAccessAdminUI(null, null);
    }

    public Result logInAsAdmin(final String username, final String password,
                                HttpServletRequest request, HttpServletResponse response)
    {
        // Verify the user
        if (canAccessAdminUI(username, password, new ServletSessionHandler(request)))
        {
            final Principal user = userManager.resolve(username);

            // HACK: the only way we could actually log into the container (as of SAL 2.4.0)
            Message message = new Message()
            {
                public String getKey()
                {
                    return "Successfully authenticated";
                }

                public Serializable[] getArguments()
                {
                    return null;
                }
            };
            Authenticator.Result result = new Authenticator.Result.Success(message, user);

            // This is the only SAL-provided method of logging into the instance.
            authenticationListener.authenticationSuccess(result, request, response);
            return SUCCESS;
        }
        else
        {
            /**
             * User is not able to administrate UAL, let's now work out why.
             */
            if (!userManager.authenticate(username, password))
            {
                return new Result(false, new Message()
                {
                    public String getKey()
                    {
                        return "applinks.admin.login.auth.failed";
                    }

                    public Serializable[] getArguments()
                    {
                        return new Serializable[0];
                    }
                });
            }
            if (!hasAppLinksAdminRights(username))
            {
                return new Result(false, new Message()
                {
                    public String getKey()
                    {
                        return "applinks.admin.login.auth.authorization.failed";
                    }

                    public Serializable[] getArguments()
                    {
                        return new Serializable[]{ username, internalHostApplication.getName() };
                    }
                });
            }

            /**
             * This should never happen.
             * Either we were unable to login the user or
             * the user does not have sufficient permissions to administrate UAL.
             */
            return new Result(false, new Message()
            {
                public String getKey()
                {
                    return "applinks.admin.login.failed";
                }

                public Serializable[] getArguments()
                {
                    return new Serializable[0];
                }
            });
        }
    }

    private static final Result SUCCESS = new Result(true);

    static class Result
    {
        private final boolean success;
        private final Message message;

        public Result(final boolean success)
        {
            this(success, null);
        }

        Result(final boolean success, final Message message)
        {
            this.success = success;
            this.message = message;
        }

        public boolean success()
        {
            return success;
        }

        public Message getMessage()
        {
            return message;
        }
    }

    private boolean hasAppLinksAdminRights(final String username)
    {
        return username != null && userManager.isSystemAdmin(username);
    }

    public static interface SessionHandler
    {
        void set(String key, Object value);

        Object get(String key);
    }
}
