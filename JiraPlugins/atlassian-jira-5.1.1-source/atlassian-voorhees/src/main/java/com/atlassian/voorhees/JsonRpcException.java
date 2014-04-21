package com.atlassian.voorhees;

/**
 * Internal data class
 */
class JsonRpcException extends Exception
{
    private final ErrorCode errorCode;
    private final String message;

    public JsonRpcException(ErrorCode errorCode, String message)
    {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public String getMessage()
    {
        return message;
    }

    public JsonError toJsonError()
    {
        return new JsonError(errorCode.intValue(), message);
    }
}
