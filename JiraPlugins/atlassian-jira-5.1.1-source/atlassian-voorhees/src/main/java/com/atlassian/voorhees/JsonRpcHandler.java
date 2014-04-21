package com.atlassian.voorhees;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry-point for the JSON-RPC subsystem.
 */
public class JsonRpcHandler
{
    public static final String JSON_CONTENT_TYPE = "application/json";

    private static final Logger log = LoggerFactory.getLogger(JsonRpcHandler.class);

    private final RpcMethodMapper methodMapper;
    private final I18nAdapter i18nAdapter;
    private final ErrorMapper errorMapper;

    public JsonRpcHandler(RpcMethodMapper methodMapper, I18nAdapter i18nAdapter)
    {
        this(methodMapper, i18nAdapter, new DefaultErrorMapper(i18nAdapter));
    }

    public JsonRpcHandler(RpcMethodMapper methodMapper, I18nAdapter i18nAdapter, ErrorMapper errorMapper)
    {
        this.methodMapper = methodMapper;
        this.i18nAdapter = i18nAdapter;
        this.errorMapper = errorMapper;
    }

    /**
     * Process a "light mode" JSON-RPC request. In this request the method name has already been determined (possibly
     * from the URL) and the request payload is solely the method arguments.
     *
     * In the event of a successful response, the response will be _solely_ the result object from the request with no
     * JSON-RPC envelope. In the event of an error, a full JSON-RPC error envelope/payload will be returned.
     *
     * @param methodName the name of the method to execute
     * @param httpRequest the request, the payload of which must be a JSON map or array containing the method arguments
     * @param httpResponse the response to fill with any results
     * @throws IOException if (and only if) the handler was unable to send a valid JSON-RPC error message due to some
     *         server failure.
     */
    public void process(String methodName, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        String strippedContentType = httpRequest.getContentType();
        if(strippedContentType.contains(";"))
            strippedContentType = strippedContentType.substring(0,httpRequest.getContentType().indexOf(";"));

        if (!JSON_CONTENT_TYPE.equals(strippedContentType))
        {
            writeResponse(httpResponse, error(new JsonError(ErrorCode.INVALID_REQUEST, i18nAdapter.getText("voorhees.invalid.mime.type"))));
            return;
        }

        Object jsonResponse;

        JsonNode parameters;

        try
        {
            try
            {
                parameters = readObject(httpRequest);
            }
            catch (EOFException e)
            {
                parameters = null;
            }

            try
            {
                jsonResponse = executeMethod(null, methodName, parameters);
            }
            catch (Exception e)
            {
                jsonResponse = error(new JsonError(ErrorCode.INTERNAL_RPC_ERROR, i18nAdapter.getText("voorhees.internal.server.error", e.getMessage()), e));
            }
        }
        catch (IOException e)
        {
            jsonResponse = error(new JsonError(ErrorCode.PARSE_ERROR, i18nAdapter.getText("voorhees.invalid.json.request")));
        }

        if (jsonResponse == null)
            writeEmptyResponse(httpResponse);
        else
            writeResponse(httpResponse, jsonResponse);
    }

    /**
     * Process something that might be a JSON-RPC request. All errors will be reported as JSON-RPC error
     * messages through the provided response object.
     *
     * @param httpRequest the request that may or may not contain valid JSON-RPC call
     * @param httpResponse the response through which a valid JSON-RPC response will be sent to the client
     * @throws IOException if (and only if) the handler was unable to send a valid JSON-RPC error message due to some
     *         server failure.
     */
    public void process(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        if (!JSON_CONTENT_TYPE.equals(httpRequest.getContentType()))
        {
            writeResponse(httpResponse, error(new JsonError(ErrorCode.INVALID_REQUEST, i18nAdapter.getText("voorhees.invalid.mime.type"))));
            return;
        }

        Object jsonResponse;

        try
        {
            JsonNode jsonRequest = readObject(httpRequest);

            if (jsonRequest.isArray())
            {
                List<Object> responses = new ArrayList<Object>(jsonRequest.size());

                for (JsonNode jsonNode : jsonRequest)
                {
                    Object retVal = processRequest(jsonNode);
                    if (retVal != null)
                        responses.add(retVal);
                }

                jsonResponse = responses;

            }
            else
            {
                jsonResponse = processRequest(jsonRequest);
            }
        }
        catch (EOFException e)
        {
            jsonResponse = error(new JsonError(ErrorCode.PARSE_ERROR, i18nAdapter.getText("voorhees.empty.json.request")));

        }
        catch (JsonParseException e)
        {
            jsonResponse = error(new JsonError(ErrorCode.PARSE_ERROR, i18nAdapter.getText("voorhees.invalid.json.request"), e));
        }
        catch (IOException e)
        {
            jsonResponse = error(new JsonError(ErrorCode.INTERNAL_RPC_ERROR, i18nAdapter.getText("voorhees.internal.server.error", e.getMessage()), e));
        }

        if (jsonResponse != null)
        {
            writeResponse(httpResponse, jsonResponse);
        }
        else
        {
            writeEmptyResponse(httpResponse);
        }
    }

    private Object processRequest(JsonNode rpcRequest)
    {
        JsonNode methodNode = rpcRequest.get("method");
        JsonNode jsonVersion = rpcRequest.get("jsonrpc");
        JsonNode idNode = rpcRequest.get("id");
        boolean idSet = rpcRequest.has("id");

        if (!isValidId(idNode))
            return error(new JsonError(ErrorCode.INVALID_REQUEST, i18nAdapter.getText("voorhees.illegal.id.type")));

        Object id = extractId(rpcRequest.get("id"));

        if (methodNode == null || methodNode.getTextValue().equals(""))
            return error(id, new JsonError(ErrorCode.INVALID_REQUEST, i18nAdapter.getText("voorhees.incomplete.request")));

        if (jsonVersion != null && !jsonVersion.getValueAsText().equals("2.0"))
            return error(id, new JsonError(ErrorCode.INVALID_REQUEST, i18nAdapter.getText("voorhees.unsupported.jsonrpc.version")));

        String methodName = methodNode.getTextValue();

        Object retVal = executeMethod(id, methodName, rpcRequest.get("params"));

        if (!idSet || (id == null && jsonVersion == null))
            return null;
        else if (retVal instanceof JsonErrorResponse)
            return retVal;
        else
            return success(id, retVal);
    }

    private Object executeMethod(Object requestId, String methodName, JsonNode params)
    {
        MethodData methodData = new MethodData(methodName, params);

        if (!methodMapper.methodExists(methodData.getMethodName()))
            return error(requestId, new JsonError(ErrorCode.METHOD_NOT_FOUND, i18nAdapter.getText("voorhees.method.not.found", methodName)));

        if (!methodMapper.methodExists(methodData.getMethodName(), methodData.getArity()))
            return error(requestId, new JsonError(ErrorCode.METHOD_NOT_FOUND, i18nAdapter.getText("voorhees.method.not.found.with.arity", methodData.getMethodName(), methodData.getArity())));

        List<Class[]> possibleArgumentTypes = methodMapper.getPossibleArgumentTypes(methodData.getMethodName(), methodData.getArity());

        Object[] args = new Object[0];
        Class[] argumentTypes = new Class[0];
        for (Class[] candidateArgumentTypes : possibleArgumentTypes)
        {
            argumentTypes = candidateArgumentTypes;
            try
            {
                args = methodData.getArguments(new ObjectMapper(), argumentTypes);
                break;
            }
            catch (JsonMappingException e)
            {
                // method match not found.
            }
            catch (IOException e)
            {
                // Something else went wrong???
                return error(requestId, new JsonError(ErrorCode.PARSE_ERROR, i18nAdapter.getText("unable.to.parse.method.arguments")));
            }
        }

        if (args.length != argumentTypes.length)
            return error(requestId, new JsonError(ErrorCode.INVALID_METHOD_PARAMETERS, i18nAdapter.getText("voorhees.method.argument.types.mismatch", methodData.getMethodName(), methodData.getArity())));

        try
        {
            return methodMapper.call(methodData.getMethodName(), argumentTypes, args);
        }
        catch (ApplicationException e)
        {
            return error(requestId, errorMapper.mapError(methodName, e.getCause()));
        }
        catch (Exception e)
        {
            return error(requestId, new JsonError(ErrorCode.INTERNAL_RPC_ERROR, i18nAdapter.getText("voorhees.internal.server.error", e.getMessage()), e));
        }

    }

    private void writeEmptyResponse(HttpServletResponse response)
    {
        this.setEmptyResponseHeaders(response);
    }

    private void writeResponse(HttpServletResponse response, Object responseObject) throws IOException
    {
        this.setResponseHeaders(response);
        new ObjectMapper().writeValue(response.getWriter(), responseObject);
    }

    private JsonNode readObject(HttpServletRequest request) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(request.getReader());
    }

    private void setEmptyResponseHeaders(HttpServletResponse response)
    {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setResponseHeaders(HttpServletResponse response)
    {
        response.setContentType(JSON_CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Object success(Object id, Object retVal)
    {
        return new JsonSuccessResponse(id, retVal);
    }

    private Object error(JsonError error)
    {
        return error(null, error);
    }

    private Object error(Object requestId, JsonError error)
    {
        return new JsonErrorResponse(requestId, error);
    }

    private boolean isValidId(JsonNode idNode)
    {
        return (idNode == null || idNode.isNull() ||idNode.isNumber() || idNode.isTextual());
    }

    private Object extractId(JsonNode idNode)
    {
        if (idNode == null)
            return null;

        if (idNode.isNumber())
            return idNode.getNumberValue();

        if (idNode.isTextual())
            return idNode.getTextValue();

        if (idNode.isNull())
            return null;

        throw new IllegalArgumentException("Not a valid id type: " + idNode);
    }
}
