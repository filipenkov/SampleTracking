package com.atlassian.voorhees;

import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public abstract class AbstractRpcHandlerTest extends TestCase
{
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    I18nAdapter i18nAdapter = new MockI18nAdapter();
    @Mock RpcMethodMapper methodMapper;
    @Mock ErrorMapper errorMapper;

    @Override
    protected void setUp() throws Exception
    {
        initMocks(this);
    }

    protected void verifyErrorResponse(HttpServletResponse response, String responseBody, Object id, int errorCode, String errorMessage) throws IOException
    {
        verifyJsonResponse(response);
        Map rpcResponse = new ObjectMapper().readValue(responseBody, Map.class);
        verifyErrorResponse(rpcResponse, id, errorCode, errorMessage);
    }

    protected void verifyErrorResponse(Map rpcResponse, Object id, int errorCode, String errorMessage)
    {
        assertEquals("2.0", rpcResponse.get("jsonrpc"));
        assertFalse(rpcResponse.containsKey("result"));
        assertTrue(rpcResponse.containsKey("id"));
        assertEquals(id, rpcResponse.get("id"));

        Map errorResponse = (Map) rpcResponse.get("error");
        assertNotNull(errorResponse);
        assertEquals(errorCode, errorResponse.get("code"));
        assertEquals("(i18n) " + errorMessage, errorResponse.get("message"));
    }

    protected void verifyJsonResponse(HttpServletResponse response)
    {
        verify(response, atLeastOnce()).setContentType("application/json");
        verify(response, atLeastOnce()).setStatus(HttpServletResponse.SC_OK);
    }
}
