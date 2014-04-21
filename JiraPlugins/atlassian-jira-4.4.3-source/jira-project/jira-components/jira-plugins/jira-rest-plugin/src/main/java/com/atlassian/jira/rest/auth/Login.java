package com.atlassian.jira.rest.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.bc.security.login.DeniedReason;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.CookieUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.filter.PasswordBasedLoginFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.atlassian.jira.rest.api.http.CacheControl.never;


/**
 * Implement a REST resource for acquiring a session cookie.
 * @since v4.2
 */
// Implement REST authentication per https://extranet.atlassian.com/display/DEV/Rest+Authentication+Specification+Proposal
@Path ("session")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class Login
{
    private final LoginService loginService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public Login(final LoginService loginService, final JiraAuthenticationContext jiraAuthenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.loginService = loginService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * Get information about the current user. If the current user is anonymous they will get a permission denied error
     * trying to access this resource.
     * The response contains information about the current user. It will contain their username, login information,
     * and a link to the User Resource for the user.
     * @return JSON containing information about the current user
     *
     * @response.representation.200.example
     *     {@link CurrentUser#DOC_EXAMPLE}
     *
     * @throws java.net.URISyntaxException if the self URI is invalid somehow
     */
    @GET
    public Response currentUser() throws URISyntaxException
    {
        final User user = jiraAuthenticationContext.getUser();

        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        final String fullPath = baseUrl + "/rest/api/latest/user?username=" + user.getName();
        final URI selfUri = new URI(fullPath);

        final CurrentUser currentUser = new CurrentUser()
                .userName(user.getName())
                .self(selfUri)
                .loginInfo(new LoginInfo(loginService.getLoginInfo(user.getName())));
        return Response.ok(currentUser).cacheControl(never()).build();
    }

    /**
     * Login a user to JIRA.
     * @param session the username and password to authenticate
     * @param request injected by Jersey
     * @param response injected by Jersey
     *
     * @return an AuthSuccess entity
     *
     * @request.representation.doc
     *      The POST should contain a username and password of the user being authenticated.
     *
     * @request.representation.example
     *      {@link AuthParams#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      The response contains an Atlassian-wide "session" portion containing the session ID that can
     *      used for further authenticated-requests.
     *
     *      It also contains a JIRA-specific "loginInfo" section containing information about the current user's
     *      login details.
     *
     * @response.representation.200.example
     *      {@link AuthSuccess#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the login is denied due to a CAPTCHA requirement, throtting, or any other reason. It's possible
     *      that the supplied credentials are valid, in this case.
     *
     * @response.representation.401.doc
     *      Returned if the login fails due to an invalid credentials.
     *
     * @throws com.atlassian.seraph.auth.AuthenticatorException if the DefaultAuthenticator explodes
     */
    @POST
    @AnonymousAllowed
    public Response login(final AuthParams session, @Context HttpServletRequest request, @Context HttpServletResponse response)
    {
        // JRADEV-3987: reuse the login code from the Seraph filter
        String loginOutcome = new LoginResourceFilter(session.username, session.password).login(request, response);
        if (BaseLoginFilter.LOGIN_SUCCESS.equals(loginOutcome))
        {
            final SessionInfo sessionInfo = new SessionInfo(CookieUtils.JSESSIONID, request.getSession().getId());
            final LoginInfo loginInfo = new LoginInfo(loginService.getLoginInfo(session.username));
            final AuthSuccess authSuccess = new AuthSuccess(sessionInfo, loginInfo);

            return Response.ok(authSuccess).build();
        }

        LoginResult loginResult = (LoginResult) request.getAttribute(LoginService.LOGIN_RESULT);
        if (loginResult != null && loginResult.getReason() == LoginReason.AUTHENTICATION_DENIED)
        {
            stampDeniedReasonsOnResponse(response, loginResult.getDeniedReasons());
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        response.setHeader("WWW-Authenticate", "JIRA REST POST");
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /**
     * Log the current user out of JIRA.
     * @param request injected by Jersey
     * @param response injected by Jersey
     * @return 401 if the user is not authenticated. NO_CONTENT if the successful.
     *
     * @response.representation.204.doc
     *      Returned if the user was successfully logged out.
     *
     * @response.representation.401.doc
     *      Returned if the user is not authenticated.
     */
    @DELETE
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response)
    {
        loginService.logout(request, response);
        // JRADEV-2292 This isn't really necessary for REST, as far as I can tell, but it is needed to make the ActionCleanupDelayFilter happy.
        // It freaks out because it thinks someone isn't cleaning up after themselves.
        ActionContextKit.resetContext();
        return Response.noContent().build();
    }

    /**
     * Stamps the '{@value com.atlassian.jira.bc.security.login.DeniedReason#X_DENIED_HEADER}' header on the response
     * object.
     *
     * @param response a HttpServletResponse
     * @param deniedReasons a Set of DeniedReason
     */
    protected void stampDeniedReasonsOnResponse(HttpServletResponse response, Set<DeniedReason> deniedReasons)
    {
        // JRADEV-2132: set the X_DENIED_HEADER values
        for (DeniedReason reason : deniedReasons)
        {
            response.setHeader(DeniedReason.X_DENIED_HEADER, reason.asString());
        }
    }

    /**
     * Extends the {@link PasswordBasedLoginFilter} from Seraph in order to reuse its login code.
     */
    private class LoginResourceFilter extends PasswordBasedLoginFilter
    {
        private final String username;
        private final String password;

        private LoginResourceFilter(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        @Override
        protected UserPasswordPair extractUserPasswordPair(HttpServletRequest request)
        {
            return new UserPasswordPair(username, password, false);
        }

        @Override
        protected SecurityConfig getSecurityConfig()
        {
            return SecurityConfigFactory.getInstance();
        }
    }
}
