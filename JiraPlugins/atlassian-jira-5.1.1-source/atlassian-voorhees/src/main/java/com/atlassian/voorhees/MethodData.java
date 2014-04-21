package com.atlassian.voorhees;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Internal data class
 */
class MethodData
{
    private final String methodName;
    private final JsonNode params;
    private int arity;

    public MethodData(String methodName, JsonNode params)
    {
        this.methodName = methodName;
        this.params = params;

        if (params == null)
            arity = 0;
        else if (params.isObject())
            arity = 1;
        else if (params.isArray())
            arity = params.size();
    }

    public String getMethodName()
    {
        return methodName;
    }

    public int getArity()
    {
        return arity;
    }

    public Object[] getArguments(ObjectMapper objectMapper, Class[] argumentTypes) throws IOException
    {
        if (arity == 0)
            return new Object[0];

        if (params.isObject())
            return new Object[] { objectMapper.readValue(params, argumentTypes[0]) };

        Object[] arguments = new Object[argumentTypes.length];

        for (int i = 0; i < argumentTypes.length; i++)
            arguments[i] = objectMapper.readValue(params.get(i), argumentTypes[i]);

        return arguments;
    }
}
