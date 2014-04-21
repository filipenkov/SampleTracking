package com.atlassian.security.auth.trustedapps;

/**
 * Responsible for getting Application details from a client
 */
public interface ApplicationRetriever
{
    /**
     * Reads an Application from the data supplied by the Reader.
     * 
     * @throws InvalidCertificateException
     *             if there are problems getting the cert
     */
    Application getApplication() throws RetrievalException;

    // --------------------------------------------------------------------------------------------------- inner classes
    
    /**
     * Used if the Application cannot be retrieved.
     */
    public static abstract class RetrievalException extends Exception
    {
        RetrievalException(String message)
        {
            super(message);
        }

        RetrievalException(Exception cause)
        {
            super(cause);
        }
    }

    /**
     * An application certificate was not found at a web site.
     */
    public static class ApplicationNotFoundException extends RetrievalException
    {
        ApplicationNotFoundException(String message)
        {
            super(message);
        }

        ApplicationNotFoundException(Exception cause)
        {
            super(cause);
        }
    }

    /**
     * An application certificate was found but is not valid.
     */
    public static class InvalidApplicationDetailsException extends RetrievalException
    {
        InvalidApplicationDetailsException(Exception cause)
        {
            super(cause);
        }
    }

    /**
     * Remote website counld not be contacted at the address provided.
     */
    public static class RemoteSystemNotFoundException extends RetrievalException
    {
        RemoteSystemNotFoundException(Exception cause)
        {
            super(cause);
        }
    }
}