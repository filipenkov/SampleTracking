package com.atlassian.crowd.exception;

/**
 * Failure because this API call is only supported by a later version of Crowd.
 */
public class UnsupportedCrowdApiException extends OperationFailedException
{
    public UnsupportedCrowdApiException(String requiredVersion, String functionality)
    {
        super("Crowd REST API version " + requiredVersion
                + " or greater is required on the server " + functionality + ".");
    }
}
