package com.atlassian.applinks.core.auth;

import com.atlassian.sal.api.user.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Returns the username of the current user in plain text in the response body.
 * When the current session is not authenticated, an empty response body is
 * returned, implying "anonymous".
 * </p>
 * <p>
 * Bound under:
 * /plugins/servlet/applinks/whoami
 * </p>
 *
 * @since v3.0
 */
public class WhoAmIServlet extends HttpServlet
{
    private final UserManager userManager;

    public WhoAmIServlet(UserManager userManager)
    {
         this.userManager = userManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/plain");

        final String username = userManager.getRemoteUsername(request);
        if (username != null)
        {
            response.getWriter().print(username);
        }
    }
}
