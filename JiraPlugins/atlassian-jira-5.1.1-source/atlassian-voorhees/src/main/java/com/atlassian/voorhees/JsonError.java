package com.atlassian.voorhees;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Encapsulation of a JSON-RPC error message.
 */
public class JsonError
{
    private final int code;
    private final String message;
    private final Object data;

    public JsonError(ErrorCode code, String message)
    {
        this(code.intValue(), message);
    }

    public JsonError(ErrorCode code, String message, Object data)
    {
        this(code.intValue(), message, data);
    }

    public JsonError(int code, String message)
    {
        this(code, message,  null);
    }

    public JsonError(int numericCode, String message, Object data)
    {
        this.code = numericCode;
        this.message = message;

        if (data instanceof Exception)
        {
            this.data = convertExceptionToString((Exception) data);
        }
        else
        {
            this.data = data;
        }
    }

    public int getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }

    public Object getData()
    {
        return data;
    }

    private String convertExceptionToString(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        out.write(e.toString());
        out.write("\n");
        e.printStackTrace(out);
        out.flush();
        return sw.toString();
    }
}
