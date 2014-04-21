package com.atlassian.plugins.rest.common.validation;

/**
 * Represents a validation error
 *
 * @since 2.0
 */
public class ValidationError
{
    private String message;
    private String path;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
