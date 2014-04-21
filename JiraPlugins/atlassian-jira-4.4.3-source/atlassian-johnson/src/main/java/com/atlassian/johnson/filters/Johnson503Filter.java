package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Category;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A handler that returns a no-content temporarily unavailable response suitable for refusing responses when an
 * application is unable to handle normal requests. This is especially useful for cases where the normal response
 * is of an unknown, or dynamic content-type and sending actual content may confuse clients.
 * <p>
 * Example uses include AJAX requests, generated images, pdf, excel and word docs.
 */
public class Johnson503Filter extends AbstractJohnsonFilter
{
    public static final Category log = Category.getInstance(Johnson503Filter.class);

    protected void handleError(JohnsonEventContainer appEventContainer, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        log.info("The application is unavailable, or there are errors.  Returing a temporarily unavailable status.");
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        // flushing the writer stops the app server from putting its html message into the otherwise empty response
        servletResponse.getWriter().flush();
    }

    protected void handleNotSetup(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        log.info("The application is not setup.  Returing a temporarily unavailable status.");
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        // flushing the writer stops the app server from putting its html message into the otherwise empty response
        servletResponse.getWriter().flush();
    }
}
