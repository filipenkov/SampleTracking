package com.atlassian.streams.jira;

import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityObjectTypes.TypeFactory;

import static com.atlassian.streams.api.ActivityObjectTypes.ATLASSIAN_IRI_BASE;
import static com.atlassian.streams.api.ActivityObjectTypes.newTypeFactory;

public final class JiraActivityObjectTypes
{
    private static final TypeFactory jiraTypes = newTypeFactory(ATLASSIAN_IRI_BASE);
    
    public static ActivityObjectType issue()
    {
        return jiraTypes.newType("issue");
    }
}