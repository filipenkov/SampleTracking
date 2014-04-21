package com.atlassian.voorhees;

/**
 * Internal data class
 */
class JsonSuccessResponse
{
    private Object id;
    private Object result;

    public JsonSuccessResponse(Object id, Object result)
    {
        this.id = id;
        this.result = result;
    }

    public Object getId()
    {
        return id;
    }

    public Object getResult()
    {
        return result;
    }

    public String getJsonrpc()
    {
        return "2.0";
    }
}
