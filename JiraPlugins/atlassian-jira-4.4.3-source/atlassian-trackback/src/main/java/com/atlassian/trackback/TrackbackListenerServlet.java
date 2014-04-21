package com.atlassian.trackback;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A servlet that listens for track back pings.
 */
public class TrackbackListenerServlet implements Servlet
{
    private ServletConfig servletConfig;
    private TrackbackStore store;
    private static Log log = LogFactory.getLog(TrackbackListenerServlet.class);

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
    {
        String title = servletRequest.getParameter("title");
        String excerpt = servletRequest.getParameter("excerpt");
        String url = servletRequest.getParameter("url");
        String blogName = servletRequest.getParameter("blog_name");

        if (url == null)
        {
            sendErrorMessage("No url given.", servletResponse);
            return;
        }

        if (title == null)
        {
            title = url; // if title is blank, set title to URL (as per http://www.movabletype.org/docs/mttrackback.html#sending%20a%20trackback%20ping)
        }

        Trackback tb = new Trackback();
        tb.setTitle(title);
        tb.setUrl(url);
        tb.setExcerpt(excerpt);
        tb.setBlogName(blogName);

        try
        {
            if (store == null) throw new TrackbackException("Store not configured. Please check the trackbackStore param is set in web.xml, and the specified class exists.");
            store.storeTrackback(tb, ((HttpServletRequest) servletRequest));
            sendSuccess(servletResponse);
        }
        catch (TrackbackException e)
        {
            log.error(e);
            sendErrorMessage("Internal error - could not store ping - " +e.getMessage(), servletResponse);
        }
    }

    private void sendErrorMessage(String errorMessage, ServletResponse servletResponse) throws IOException
    {
        servletResponse.setContentType("text/xml");
        servletResponse.getWriter().write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
                "<response>\n" +
                "<error>1</error>\n" +
                "<message>" + errorMessage + "</message>\n" +
                "</response>");
    }

    private void sendSuccess(ServletResponse servletResponse) throws IOException
    {
        servletResponse.setContentType("text/xml");
        servletResponse.getWriter().write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
                "<response>\n" +
                "<error>0</error>\n" +
                "</response>");
    }

    public void init(ServletConfig servletConfig) throws ServletException
    {
        this.servletConfig = servletConfig;

        String storageClass = servletConfig.getInitParameter("trackbackStore");
        try
        {
	    store = (TrackbackStore) this.getClass().forName(storageClass).newInstance();
        }
        catch (Exception e)
        {
            log.error("Could not find or create class: " + storageClass + " from init-param named trackbackStore", e);
        }
    }

    public void destroy()
    {
    }

    public String getServletInfo()
    {
        return "A servlet which listens for trackback pings and stores them via a TrackbackStore.";
    }

    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }
}
