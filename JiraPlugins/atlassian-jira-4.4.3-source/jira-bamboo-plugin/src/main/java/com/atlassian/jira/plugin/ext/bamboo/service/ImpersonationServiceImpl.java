package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.seraph.auth.AuthenticationContext;
import com.atlassian.seraph.auth.AuthenticationContextImpl;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.Principal;

public class ImpersonationServiceImpl implements ImpersonationService
{
    private static final Logger log = Logger.getLogger(ImpersonationServiceImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final UserManager userManager;
    private final com.atlassian.sal.api.user.UserManager salUserManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public ImpersonationServiceImpl(final UserManager userManager, final com.atlassian.sal.api.user.UserManager salUserManager)
    {
        this.userManager = userManager;
        this.salUserManager = salUserManager;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods

    @NotNull
    public Runnable runAsUser(@NotNull final String username, @NotNull final Runnable delegate)
    {
        return new Runnable()
        {
            public void run()
            {
                AuthenticationContext authenticationContext = new AuthenticationContextImpl();
                Principal currentPrincipal = authenticationContext.getUser();
                try
                {
                    final User user = userManager.getUserObject(username);
                    if (user == null)
                    {
                        throw new IllegalStateException("username '" + username + "' does not exist. Cannot impersonate this user.");
                    }

                    authenticationContext.setUser(user);

                    if (!username.equals(salUserManager.getRemoteUsername()))
                    {
                        throw new IllegalStateException("Could not impersonate user '" + username + "'. Call to '" + salUserManager.getClass() + ".getRemoteUsername()' returns '" + salUserManager.getRemoteUsername() + "'");
                    }

                    delegate.run();
                }
                finally
                {
                    authenticationContext.setUser(currentPrincipal);
                }
            }
        };
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
