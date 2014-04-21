package com.atlassian.applinks.spi.link;

import java.util.List;

public class RemoteErrorListException extends ReciprocalActionException {

    private final List<String> errors;

    public RemoteErrorListException(final List<String> errors) {
        super("The remote application reported one or more errors.");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
