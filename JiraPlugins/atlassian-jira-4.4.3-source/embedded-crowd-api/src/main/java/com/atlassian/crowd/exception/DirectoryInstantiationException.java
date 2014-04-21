package com.atlassian.crowd.exception;

/**
 * Exception when a {@link com.atlassian.crowd.directory.RemoteDirectory} implementation can not be loaded
 * by a {@link Directory}.
 */
public class DirectoryInstantiationException extends OperationFailedException
{
    /**
     * Default constructor.
     */
    public DirectoryInstantiationException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public DirectoryInstantiationException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     * @param throwable the {@link Exception Exception}.
     */
    public DirectoryInstantiationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public DirectoryInstantiationException(Throwable throwable)
    {
        super(throwable);
    }
}