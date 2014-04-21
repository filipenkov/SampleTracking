package com.atlassian.plugins.rest.common.json;

public class JsonMarshallingException extends RuntimeException
{
    public JsonMarshallingException()
    {
    }

    public JsonMarshallingException(String message)
    {
        super(message);
    }

    public JsonMarshallingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JsonMarshallingException(Throwable cause)
    {
        super(cause);
    }
}
