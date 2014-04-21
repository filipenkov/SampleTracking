package com.atlassian.voorhees;

import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class TestJsonRpcHandler extends AbstractRpcHandlerTest
{

    public void testInvalidRequestMimeType() throws Exception
    {
        testRequestProducesError("text/plain", "{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", null, "voorhees.invalid.mime.type", -32600);
        testRequestProducesError("multipart/form-data", "{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", null, "voorhees.invalid.mime.type", -32600);
        testRequestProducesError("application/x-www-form-encoded", "{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", null, "voorhees.invalid.mime.type", -32600);
    }

    public void testEmptyRequest() throws Exception
    {
        testRequestProducesError("", "voorhees.empty.json.request", -32700);
    }

    public void testRequestNotJson() throws Exception
    {
        testRequestProducesError("Monkeybutter", "voorhees.invalid.json.request", -32700);
    }

    public void testRequestInvalidJson() throws Exception
    {
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"sum\", \"params\": [}", "voorhees.invalid.json.request", -32700);
    }

    public void testOmitMethodName() throws Exception
    {
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"id\": 1}", 1, "voorhees.incomplete.request", -32600);
    }

    public void testNoArgRpcCall() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", "Hello!");
    }

    public void testNoArgRpcCallNullResponse() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", null);
    }

    public void testNoArgRpcCallObjectResponse() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", new SimpleResponseObject("Hello!"));
    }

    public void testOmitJsonVersion() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"method\": \"hello\", \"id\": 1}", "Hello!");
    }

    public void testOmitId() throws Exception
    {
        testSuccessfulNoArgNoResponseRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\"}");
    }

    public void testStringId() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": \"cheese\"}", "Hello!", "cheese");
    }

    public void testFloatId() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 3.5}", "Hello!", 3.5);
    }

    public void testInvalidId() throws Exception
    {
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": true}", "voorhees.illegal.id.type", -32600);
    }

    public void testNullIdRpc20() throws Exception
    {
        testSuccessfulNoArgRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": null}", "Hello!", null);
    }

    public void testNullIdRpc10() throws Exception
    {
        testSuccessfulNoArgNoResponseRpcCall("{\"method\": \"hello\", \"id\": null}");
    }

    public void testUnrecognisedJsonVersion() throws Exception
    {
        testRequestProducesError("{\"jsonrpc\": \"2.1\", \"method\": \"hello\", \"id\": 1}", 1, "voorhees.unsupported.jsonrpc.version", -32600);
    }

    public void testCallWithPrimitiveArgs() throws Exception
    {
        testSuccessfulRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"params\": [42, \"banana\"], \"id\": 1}",
                42, 1, new Class[] { Integer.class, String.class }, new Object[] { 42, "banana"});
    }

    public void testCallWithObjectArgs() throws Exception
    {
        SimpleResponseObject first = new SimpleResponseObject("first");
        NotSoSimpleResponseObject second = new NotSoSimpleResponseObject(new SimpleResponseObject("third"), "fourth");

        testSuccessfulRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"params\": [{\"value\" : \"first\"} , " +
                "{\"internal\" : { \"value\" : \"third\" }, \"value\" : \"fourth\" }], \"id\": 1}",
                42, 1, new Class[] { SimpleResponseObject.class, NotSoSimpleResponseObject.class }, new Object[] { first, second});
    }

    // "Named parameter"-style arguments are mapped to a single object with those properties
    public void testCallWithNamedParameterArgs() throws Exception
    {
        testSuccessfulRpcCall("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"params\": {\"value\" : \"first\"}, \"id\": 1}",
                42, 1, new Class[] { SimpleResponseObject.class }, new Object[] { new SimpleResponseObject("first")});
    }

    public void testNonexistantMethodName() throws Exception
    {
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", 1, "voorhees.method.not.found [hello]", -32601);
    }

    public void testWrongMethodArgsCountNoParam() throws Exception
    {
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 0)).thenReturn(false);
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"id\": 1}", 1, "voorhees.method.not.found.with.arity [hello, 0]", -32601);
    }

    public void testWrongMethodArgsCountArrayParam() throws Exception
    {
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 2)).thenReturn(false);
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", \"params\" : [ 12, 13 ], \"id\": 1}", 1, "voorhees.method.not.found.with.arity [hello, 2]", -32601);
    }

    public void testWrongMethodArgsCountObjectParam() throws Exception
    {
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(false);
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", " +
                "\"params\" : { \"value\" : \"first\" }, \"id\": 1}", 1, "voorhees.method.not.found.with.arity [hello, 1]", -32601);
    }

    public void testWrongMethodArgsTypeMismatch() throws Exception
    {
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(Collections.singletonList(new Class[] { Integer.class }));
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", " +
                "\"params\" : [ \"cheese\" ], \"id\": 1}}", 1, "voorhees.method.argument.types.mismatch [hello, 1]", -32602);
    }

    public void testInternalServerError() throws Exception
    {
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 0)).thenReturn(true);
        when(methodMapper.call("hello", new Class[0], new Object[0])).thenThrow(new RuntimeException("Bugger this"));
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", " +
                "\"id\": 1}}", 1, "voorhees.internal.server.error [Bugger this]", -32603);
    }

    public void testApplicationError() throws Exception
    {
        RuntimeException actualError = new RuntimeException("Bugger this");

        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 0)).thenReturn(true);
        when(methodMapper.call("hello", new Class[0], new Object[0])).thenThrow(new ApplicationException(actualError));
        // Error mapper i18ns its own messages
        when(errorMapper.mapError("hello", actualError)).thenReturn(new JsonError(12345, "(i18n) It's broken", "BORKEN"));
        testRequestProducesError("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", " +
                "\"id\": 1}}", 1, "It's broken", 12345);
    }

    public void testNastyUnexpectedIOException() throws Exception
    {
        IOException problem = new IOException("OMGWTFBBQ!");
        StringWriter responseBody = new StringWriter();
        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenThrow(problem);
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        processJsonRpcCall();

        verifyErrorResponse(response, responseBody.toString(), null, -32603, "voorhees.internal.server.error [OMGWTFBBQ!]");
    }

    public void testGuessOverloadedMethod() throws Exception
    {
        List<Class[]> potentialArgumentTypes = new ArrayList<Class[]>();
        potentialArgumentTypes.add(new Class[] { Integer.class });
        potentialArgumentTypes.add(new Class[] { String.class });

        StringReader requestBody = new StringReader("{\"jsonrpc\": \"2.0\", \"method\": \"hello\", " +
                "\"params\" : [ \"cheese\" ], \"id\": 1}}");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(potentialArgumentTypes);

        processJsonRpcCall();
        verify(methodMapper).call("hello", new Class[]{String.class}, new Object[]{"cheese"});
    }

    public void testSuccessfulBatchRequest() throws Exception
    {
        StringReader requestBody = new StringReader(
                "[{\"jsonrpc\" : \"2.0\", \"method\" : \"hello\", \"params\" : [\"stilton\"], \"id\" : 1234},\n" +
                "{\"jsonrpc\" : \"2.0\", \"method\" : \"hello\", \"params\" : [\"cheddar\"], \"id\" :223}]");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(Collections.singletonList(new Class[] { String.class }));
        when(methodMapper.call("hello", new Class[]{String.class}, new Object[]{"stilton"})).thenReturn("I like stilton!");
        when(methodMapper.call("hello", new Class[] { String.class }, new Object[] { "cheddar"})).thenReturn("I like cheddar!");

        processJsonRpcCall();

        List result = new ObjectMapper().readValue(responseBody.toString(), List.class);
        assertEquals(2, result.size());

        verifySuccessfulResult((Map)result.get(0), 1234, "I like stilton!");
        verifySuccessfulResult((Map)result.get(1), 223, "I like cheddar!");
    }

    public void testMixedBatchRequest() throws Exception
    {
        StringReader requestBody = new StringReader(
                "[{\"jsonrpc\" : \"2.0\", \"method\" : \"hello\", \"params\" : [\"stilton\"], \"id\" : 1234},\n" +
                "{\"jsonrpc\" : \"2.0\", \"method\" : \"hello\", \"params\" : [\"cheddar\"], \"id\" :223},\n" +
                "{\"jsonrpc\" : \"2.0\", \"method\" : \"hello\", \"params\" : [\"brie\"]},\n" +
                "{\"jsonrpc\" : \"2.0\", \"params\" : [\"cheddar\"], \"id\" :334}]");

        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(Collections.singletonList(new Class[] { String.class }));
        when(methodMapper.call("hello", new Class[]{String.class}, new Object[]{"stilton"})).thenReturn("I like stilton!");
        when(methodMapper.call("hello", new Class[] { String.class }, new Object[] { "cheddar"})).thenReturn("I like cheddar!");
        when(methodMapper.call("hello", new Class[] { String.class }, new Object[] { "brie"})).thenReturn("I like brie!");

        processJsonRpcCall();

        List result = new ObjectMapper().readValue(responseBody.toString(), List.class);
        assertEquals(3, result.size());

        verifySuccessfulResult((Map)result.get(0), 1234, "I like stilton!");
        verifySuccessfulResult((Map)result.get(1), 223, "I like cheddar!");
        verifyErrorResponse((Map)result.get(2), 334, ErrorCode.INVALID_REQUEST.intValue(), "voorhees.incomplete.request");
    }

    private void processJsonRpcCall() throws IOException
    {
        new JsonRpcHandler(methodMapper, i18nAdapter, errorMapper).process(request, response);
    }

    private void testSuccessfulNoArgRpcCall(String rawRpcCall, Object methodResult) throws Exception
    {
        testSuccessfulNoArgRpcCall(rawRpcCall, methodResult, 1);
    }

    private void testSuccessfulNoArgNoResponseRpcCall(String rawRpcCall) throws Exception
    {
        StringReader requestBody = new StringReader(rawRpcCall);
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 0)).thenReturn(true);

        processJsonRpcCall();

        verify(methodMapper).call("hello", new Class[]{}, new Object[]{});
        verify(response, never()).setContentType(anyString());

        assertEquals(0, responseBody.toString().length());
    }

    private void testSuccessfulNoArgRpcCall(String rawRpcCall, Object methodResult, Object requestId) throws Exception
    {
        testSuccessfulRpcCall(rawRpcCall, methodResult, requestId, new Class[]{}, new Object[]{});
    }

    private void testSuccessfulRpcCall(String rawRpcCall, Object methodResult, Object requestId, Class[] argumentTypes, Object[] arguments) throws Exception
    {
        StringReader requestBody = new StringReader(rawRpcCall);
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", argumentTypes.length)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", argumentTypes.length)).thenReturn(Collections.singletonList(argumentTypes));
        when(methodMapper.call("hello", argumentTypes, arguments)).thenReturn(methodResult);

        processJsonRpcCall();

        verifyJsonResponse(response);

        Map rpcResponse = new ObjectMapper().readValue(responseBody.toString(), Map.class);
        verifySuccessfulResult(rpcResponse, requestId, methodResult);
    }

    private void verifySuccessfulResult(Map rpcResponse, Object requestId, Object methodResult)
    {
        assertEquals("2.0", rpcResponse.get("jsonrpc"));
        assertEquals(requestId, rpcResponse.get("id"));
        assertFalse(rpcResponse.containsKey("error"));
        assertTrue(rpcResponse.containsKey("result"));
        verifyResult(rpcResponse.get("result"), methodResult);
    }

    private void testRequestProducesError(String requestString, String errorMessage, int errorCode) throws IOException
    {
        testRequestProducesError(requestString, null, errorMessage, errorCode);
    }

    private void testRequestProducesError(String requestString, Object requestId, String errorMessage, int errorCode) throws IOException
    {
        testRequestProducesError("application/json", requestString, requestId, errorMessage, errorCode);
    }

    private void testRequestProducesError(String requestContentType, String requestString, Object requestId, String errorMessage, int errorCode) throws IOException
    {
        when(request.getContentType()).thenReturn(requestContentType);

        StringReader requestBody = new StringReader(requestString);
        StringWriter responseBody = new StringWriter();

        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        processJsonRpcCall();

        verifyErrorResponse(response, responseBody.toString(), requestId, errorCode, errorMessage);
    }

    private void verifyResult(Object actual, Object expected)
    {
        if (expected == null)
            assertNull(actual);
        else if (expected instanceof SimpleResponseObject)
            assertEquals("Hello!", ((Map) actual).get("value"));
        else
            assertEquals(expected, actual);
    }

}
