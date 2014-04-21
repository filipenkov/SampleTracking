package com.atlassian.gadgets.directory.spi;

/**
 * Thrown when there is a problem while performing an operation on the external gadget spec URI persistent data store. 
 * 
 * @since 2.0
 */
public class ExternalGadgetSpecStoreException extends RuntimeException
{
    public ExternalGadgetSpecStoreException()
    {
        super();
    }

    public ExternalGadgetSpecStoreException(String message)
    {
        super(message);
    }

    public ExternalGadgetSpecStoreException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ExternalGadgetSpecStoreException(Throwable cause)
    {
        super(cause);
    }
}
