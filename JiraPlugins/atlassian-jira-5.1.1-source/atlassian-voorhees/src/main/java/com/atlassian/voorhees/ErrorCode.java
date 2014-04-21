package com.atlassian.voorhees;

/**
 * Error codes defined by the JSON-RPC Spec
 */
public enum ErrorCode
{
    // Official error codes from the JSON-RPC 2.0 spec
    PARSE_ERROR(-32700),
    INVALID_REQUEST(-32600),
    METHOD_NOT_FOUND(-32601),
    INVALID_METHOD_PARAMETERS(-32602),
    INTERNAL_RPC_ERROR(-32603);
    
    // -32099 to -32000 are reserved for server errors

    private final int code;

    ErrorCode(int code)
    {
        this.code = code;
    }

    public int intValue()
    {
        return code;
    }
}
