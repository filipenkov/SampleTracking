package com.atlassian.voorhees;

/**
 * Internal data class
 */
class JsonErrorResponse
{
    private final Object id;
    private final JsonError error;

    JsonErrorResponse(Object id, JsonError error)
    {
        this.id = id;
        this.error = error;
    }

    public Object getId()
    {
        return id;
    }

    public JsonError getError()
    {
        return error;
    }

    public String getJsonrpc()
    {
        return "2.0";
    }
}
