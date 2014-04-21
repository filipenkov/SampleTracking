package com.atlassian.crowd.exception;

/**
 * This exception is thrown if no {@see PasswordEncoder} is found when a lookup
 * is done on the {@see PasswordEncoderFactory}
 */
public class PasswordEncoderNotFoundException extends RuntimeException
{
    public PasswordEncoderNotFoundException(String msg)
    {
        super(msg);
    }

    public PasswordEncoderNotFoundException(String msg, Throwable ex)
    {
        super(msg, ex);
    }
}
