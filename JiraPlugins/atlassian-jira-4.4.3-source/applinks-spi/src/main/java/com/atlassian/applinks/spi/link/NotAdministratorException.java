package com.atlassian.applinks.spi.link;

public class NotAdministratorException extends ReciprocalActionException {

    public NotAdministratorException() {
        super("The supplied credentials do not belong to an user with administrative privileges in the remote application.");
    }

}
