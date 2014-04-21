package com.atlassian.applinks.spi.link;

public class LinkCreationResponseException extends ReciprocalActionException {

    public LinkCreationResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
