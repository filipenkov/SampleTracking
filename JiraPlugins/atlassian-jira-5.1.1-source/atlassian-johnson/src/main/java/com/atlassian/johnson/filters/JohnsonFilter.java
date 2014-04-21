/**
 * Atlassian Source Code Template.
 * User: Bobby
 * Date: Apr 8, 2003
 * Time: 9:07:18 AM
 * CVS Revision: $Revision: 1.5 $
 * Last CVS Commit: $Date: 2006/10/09 01:01:38 $
 * Author of last CVS Commit: $Author: bkuo $
 */

package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Category;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter that handles cases where the application is unable to handle a normal request and redirects to the
 * configured error path so that a nice error page can be provided.
 */
public class JohnsonFilter extends AbstractJohnsonFilter
{
    public static final Category log = Category.getInstance(JohnsonFilter.class);

    public static final String ALREADY_FILTERED = JohnsonFilter.class.getName() + "_already_filtered";
    public static final String URL_SETUP = "/secure/Setup!default.jspa";

    protected void handleError(JohnsonEventContainer appEventContainer, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        String servletPath = getServletPath(servletRequest);
        String contextPath = servletRequest.getContextPath();
        log.info("The application is still starting up, or there are errors.  Redirecting request from '" + servletPath + "' to '" + config.getErrorPath() + "'");
        servletResponse.sendRedirect(contextPath + config.getErrorPath());
    }

    protected void handleNotSetup(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        String servletPath = getServletPath(servletRequest);
        String contextPath = servletRequest.getContextPath();
        log.info("The application is not yet setup.  Redirecting request from '" + servletPath + "' to '" + config.getSetupPath() + "'");
        servletResponse.sendRedirect(contextPath + config.getSetupPath());
    }
}
