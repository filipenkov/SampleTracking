package com.atlassian.jira.bc;

public class EntityNotFoundException extends Exception
{
    public EntityNotFoundException()
    {
    }

    public EntityNotFoundException(String message)
    {
        super(message);
    }

}
