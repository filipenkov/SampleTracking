package com.atlassian.jira.issue.customfields.impl;

public class FieldValidationException extends RuntimeException
{
    public FieldValidationException(String message)
    {
        super(message);
    }
}
