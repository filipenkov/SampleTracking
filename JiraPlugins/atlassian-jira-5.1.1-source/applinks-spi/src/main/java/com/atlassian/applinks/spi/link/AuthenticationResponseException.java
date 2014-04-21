package com.atlassian.applinks.spi.link;

public class AuthenticationResponseException extends ReciprocalActionException {

    public AuthenticationResponseException() {
        super("A valid response was not received to the request to authenticate with the remote application.");
    }

}
