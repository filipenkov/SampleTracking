package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Category;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Handles error cases with SOAP fault responses for an application that is unable to handle normal requests.
 */
public class JohnsonSoapFilter extends AbstractJohnsonFilter
{
    public static final Category log = Category.getInstance(JohnsonSoapFilter.class);

    protected void handleError(JohnsonEventContainer appEventContainer, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        log.info("The application is unavailable, or there are errors.  Returning a SOAP fault with the event message.");
        servletResponse.setContentType(TEXT_XML_UTF8_CONTENT_TYPE);
        String message = getStringForEvents(appEventContainer.getEvents());
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        servletResponse.getWriter().write(buildSoapFault(message));
    }

    protected void handleNotSetup(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        log.info("The application is not setup.  Returning a SOAP fault with a 'not setup' message.");
        servletResponse.setContentType(TEXT_XML_UTF8_CONTENT_TYPE);
        String message = "The application has not yet been setup.";
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        servletResponse.getWriter().write(buildSoapFault(message));
    }

    String buildSoapFault(String errorMessage)
    {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <soapenv:Body>\n" +
                "        <soapenv:Fault>\n" +
                "            <faultcode>soapenv:Server</faultcode>\n" +
                "            <faultstring>" +
                errorMessage +
                "            </faultstring>\n" +
                "        </soapenv:Fault>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

}
