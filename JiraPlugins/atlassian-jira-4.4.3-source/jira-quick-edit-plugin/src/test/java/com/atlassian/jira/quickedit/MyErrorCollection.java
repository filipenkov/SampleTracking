package com.atlassian.jira.quickedit;

import java.util.Collection;
import java.util.Map;

/**
* need this because the errorcollection part of JIRA REST does not provide getErrors().
*/
public class MyErrorCollection
{
    private Collection<String> errorMessages;
    private Map<String, String> errors;

    public Collection<String> getErrorMessages()
    {
        return errorMessages;
    }

    public Map<String, String> getErrors()
    {
        return errors;
    }
}
