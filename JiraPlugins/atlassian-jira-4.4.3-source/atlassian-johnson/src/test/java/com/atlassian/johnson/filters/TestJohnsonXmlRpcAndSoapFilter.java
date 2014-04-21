package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import junit.framework.TestCase;
import org.easymock.MockControl;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

/**
 * Tests both the JohnsonSoapFilter and JohnsonXmlRpcFilter classes.
 */
public class TestJohnsonXmlRpcAndSoapFilter extends TestCase
{

    private static final String ERROR_MSG = "TEST ERROR MESSAGE";
    private static final int ERROR_CODE = 0;
    private static final String NOT_SETUP_ERROR_MESG = "The application has not yet been setup.";

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testHandleErrorSoapJohnsonFilter() throws IOException
    {
        JohnsonSoapFilter johnsonSoapFilter = new JohnsonSoapFilter()
        {
            protected String getStringForEvents(Collection events)
            {
                return null;
            }

            String buildSoapFault(String errorMessage)
            {
                return getSoapFaultMessage(ERROR_MSG);
            }
        };
        testHandleError(johnsonSoapFilter, getSoapFaultMessage(ERROR_MSG));
    }

    public void testHandleErrorXmlRpcJohnsonFilter() throws IOException
    {
        JohnsonXmlRpcFilter johnsonXmlRpcFilter = new JohnsonXmlRpcFilter()
        {

            protected String getStringForEvents(Collection events)
            {
                return null;
            }

            String buildXmlRpcErrorMessage(String error, int faultCode)
            {
                return getXmlRpcFaultMessage(ERROR_MSG);
            }
        };
        testHandleError(johnsonXmlRpcFilter, getXmlRpcFaultMessage(ERROR_MSG));
    }

    public void testHandleError(AbstractJohnsonFilter filter, String message) throws IOException
    {
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setContentType(AbstractJohnsonFilter.TEXT_XML_UTF8_CONTENT_TYPE);
        responseMockCtrl.setVoidCallable();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        PrintWriter writer = new PrintWriter(baos);
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();


        filter.handleError(new JohnsonEventContainer(){
            public Collection getEvents()
            {
                return Collections.EMPTY_LIST;
            }
        }, null, response);
        responseMockCtrl.verify();
        writer.flush();
        assertEquals(message, baos.toString());
    }

    public void testBuildXmlRpcErrorMessage()
    {
        JohnsonXmlRpcFilter johnsonXmlRpcFilter = new JohnsonXmlRpcFilter();
        assertEquals(getXmlRpcFaultMessage(ERROR_MSG), johnsonXmlRpcFilter.buildXmlRpcErrorMessage(ERROR_MSG, ERROR_CODE));
    }

    public void testHandleNotSetupXmlRpcJohnsonFilter() throws IOException
    {
        JohnsonXmlRpcFilter johnsonXmlRpcFilter = new JohnsonXmlRpcFilter();
        testHandleNotSetup(johnsonXmlRpcFilter, getXmlRpcFaultMessage(NOT_SETUP_ERROR_MESG));
    }

    public void testHandleNotSetupSoapJohnsonFilter() throws IOException
    {
        JohnsonSoapFilter filter = new JohnsonSoapFilter();
        testHandleNotSetup(filter, getSoapFaultMessage(NOT_SETUP_ERROR_MESG));
    }

    public void testHandleNotSetup(AbstractJohnsonFilter filter, String message) throws IOException
    {
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setContentType(AbstractJohnsonFilter.TEXT_XML_UTF8_CONTENT_TYPE);
        responseMockCtrl.setVoidCallable();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        PrintWriter writer = new PrintWriter(baos);
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        filter.handleNotSetup(null, response);
        responseMockCtrl.verify();
        writer.flush();
        assertEquals(message, baos.toString());

    }

    private String getXmlRpcFaultMessage(String errorMessage)
    {
        return "<?xml version=\"1.0\"?>\n" +

                "<methodResponse>\n" +
                "    <fault>\n" +
                "        <value>\n" +
                "            <struct>\n" +
                "                <member>\n" +
                "                    <name>faultString</name>\n" +
                "                    <value>" +
                errorMessage +
                "</value>\n" +
                "                </member>\n" +
                "                <member>\n" +
                "                    <name>faultCode</name>\n" +
                "                    <value>\n" +
                "                        <int>" +
                ERROR_CODE +
                "</int>\n" +
                "                    </value>\n" +
                "                </member>\n" +
                "            </struct>\n" +
                "        </value>\n" +
                "    </fault>\n" +
                "</methodResponse>";
    }

    private String getSoapFaultMessage(String notSetupErrorMesg)
    {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <soapenv:Body>\n" +
                "        <soapenv:Fault>\n" +
                "            <faultcode>soapenv:Server</faultcode>\n" +
                "            <faultstring>" +
                notSetupErrorMesg +
                "            </faultstring>\n" +
                "        </soapenv:Fault>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }
}
