package com.atlassian.crowd.exception;

/**
 * An exception that is thrown if we have failed to encrypt a password
 * with a given PasswordEncoder
 */
public class PasswordEncoderException extends RuntimeException
{
    public PasswordEncoderException(String s)
    {
        super(s);
    }

    public PasswordEncoderException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
