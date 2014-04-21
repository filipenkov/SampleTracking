package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Category;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Handles error cases with XmlRpc fault responses for an application that is unable to handle normal requests.
 */
public class JohnsonXmlRpcFilter extends AbstractJohnsonFilter
{
    public static final Category log = Category.getInstance(JohnsonXmlRpcFilter.class);
    // NOTE: there is no clear definition of what this fault code should be, http://www.xmlrpc.com/spec
    // it seems that Axis is using 0 so we will also :)
    private static final int FAULT_CODE = 0;

    protected void handleError(JohnsonEventContainer appEventContainer, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        log.info("The application is unavailable, or there are errors.  Returning a SOAP fault with the event message.");
        servletResponse.setContentType(TEXT_XML_UTF8_CONTENT_TYPE);
        String message = getStringForEvents(appEventContainer.getEvents());
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        servletResponse.getWriter().write(buildXmlRpcErrorMessage(message, FAULT_CODE));
    }

    protected void handleNotSetup(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        log.info("The application is not setup.  Returning a SOAP fault with a 'not setup' message.");
        servletResponse.setContentType(TEXT_XML_UTF8_CONTENT_TYPE);
        String message = "The application has not yet been setup.";
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        servletResponse.getWriter().write(buildXmlRpcErrorMessage(message, FAULT_CODE));
    }

    String buildXmlRpcErrorMessage(String error, int faultCode)
    {
        return "<?xml version=\"1.0\"?>\n" +
                "<methodResponse>\n" +
                "    <fault>\n" +
                "        <value>\n" +
                "            <struct>\n" +
                "                <member>\n" +
                "                    <name>faultString</name>\n" +
                "                    <value>" +
                error +
                "</value>\n" +
                "                </member>\n" +
                "                <member>\n" +
                "                    <name>faultCode</name>\n" +
                "                    <value>\n" +
                "                        <int>" +
                faultCode +
                "</int>\n" +
                "                    </value>\n" +
                "                </member>\n" +
                "            </struct>\n" +
                "        </value>\n" +
                "    </fault>\n" +
                "</methodResponse>";
    }
}
