package com.atlassian.voorhees;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestLightModeRpcHandler extends AbstractRpcHandlerTest
{
    public void testInvalidJson() throws Exception
    {
        StringReader requestBody = new StringReader("{ \"method\" :");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        processJsonRpcCall("hello");

        verifyErrorResponse(response, responseBody.toString(), null, ErrorCode.PARSE_ERROR.intValue(), "voorhees.invalid.json.request");
    }

    public void testSuccessfulMethodCallNoArgs() throws Exception
    {
        StringReader requestBody = new StringReader("");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 0)).thenReturn(true);
        when(methodMapper.call("hello", new Class[0], new Object[0])).thenReturn("Hello!");

        processJsonRpcCall("hello");

        verifyJsonResponse(response);
        assertEquals("Hello!", new ObjectMapper().readValue(responseBody.toString(), String.class));
    }

    public void testSuccessfulMethodCallWithArgs() throws Exception
    {
        StringReader requestBody = new StringReader("[ \"cheese\" ]");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(Collections.singletonList(new Class[] { String.class }));
        when(methodMapper.call("hello", new Class[] { String.class }, new Object[] { "cheese" })).thenReturn("Hello!");

        processJsonRpcCall("hello");

        verifyJsonResponse(response);
        assertEquals("Hello!", new ObjectMapper().readValue(responseBody.toString(), String.class));
    }

    public void testSuccessfulMethodCallWithObjectArg() throws Exception
    {
        StringReader requestBody = new StringReader("{\"value\" : \"cheese\"}");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(Collections.singletonList(new Class[] { SimpleResponseObject.class }));
        when(methodMapper.call("hello", new Class[] { SimpleResponseObject.class }, new Object[] { new SimpleResponseObject("cheese") })).thenReturn("Hello!");

        processJsonRpcCall("hello");

        verifyJsonResponse(response);
        assertEquals("Hello!", new ObjectMapper().readValue(responseBody.toString(), String.class));
    }

    public void testMethodNotFound() throws Exception
    {
        StringReader requestBody = new StringReader("");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(false);

        processJsonRpcCall("hello");

        verifyErrorResponse(response, responseBody.toString(), null, ErrorCode.METHOD_NOT_FOUND.intValue(), "voorhees.method.not.found [hello]");
    }

    public void testMethodCallMismatchedArgs() throws Exception
    {
        StringReader requestBody = new StringReader("");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 0)).thenReturn(false);

        processJsonRpcCall("hello");

        verifyErrorResponse(response, responseBody.toString(), null, ErrorCode.METHOD_NOT_FOUND.intValue(), "voorhees.method.not.found.with.arity [hello, 0]");
    }

    public void testMethodCallMismatchedArgTypes() throws Exception
    {
        StringReader requestBody = new StringReader("[ \"cheese\" ]");
        StringWriter responseBody = new StringWriter();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(requestBody));
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(methodMapper.methodExists("hello")).thenReturn(true);
        when(methodMapper.methodExists("hello", 1)).thenReturn(true);
        when(methodMapper.getPossibleArgumentTypes("hello", 1)).thenReturn(Collections.singletonList(new Class[] { Integer.class }));

        processJsonRpcCall("hello");

        verifyErrorResponse(response, responseBody.toString(), null, ErrorCode.INVALID_METHOD_PARAMETERS.intValue(), "voorhees.method.argument.types.mismatch [hello, 1]");
    }

    public void testGoodRequestContentTypeWithParameters() throws Exception
    {
        testGoodRequestContentType("application/json");
        testGoodRequestContentType("application/json; parameter");
        testGoodRequestContentType("application/json; charset=UTF-8");
    }

    public void testBadRequestContentType() throws Exception
    {
        testBadRequestContentType("text/plain");
        testBadRequestContentType("multipart/form-data");
        testBadRequestContentType("application/x-www-form-encoded");
        testBadRequestContentType("application/x-www-form-encoded; charset=UTF-8");
    }

    private void testGoodRequestContentType(String mimeType) throws IOException
    {
        StringWriter responseBody = new StringWriter();

        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.getContentType()).thenReturn(mimeType);

        processJsonRpcCall("hello");

        verifyJsonResponse(response);
    }

    private void testBadRequestContentType(String mimeType) throws IOException
    {
        StringWriter responseBody = new StringWriter();

        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.getContentType()).thenReturn(mimeType);

        processJsonRpcCall("hello");

        verifyErrorResponse(response, responseBody.toString(), null, ErrorCode.INVALID_REQUEST.intValue(), "voorhees.invalid.mime.type");
    }

    private void processJsonRpcCall(String methodName) throws IOException
    {
        new JsonRpcHandler(methodMapper, i18nAdapter, errorMapper).process(methodName, request, response);
    }
}
