package com.atlassian.jira.issue.transport;

/**
 * This contains String > Collection of Transport Objects
 */
public interface FieldTransportParams extends CollectionParams
{
    Object getFirstValueForNullKey();
    Object getFirstValueForKey(String key);

}
