package org.jcvi.jira.importer.jiramodel;

import javax.management.InvalidAttributeValueException;

/**
 * Used to indicate that the JIRA Sample doesn't have valid values
 * in the GLK
 */
public class InvalidGLKEntry extends InvalidAttributeValueException {
    private final String field;
    public InvalidGLKEntry(String error) {
        this.field = error;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return field;
    }
}
