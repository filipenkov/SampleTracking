package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;

/**
 * @since v4.0
 */
public class TestIdentityIndexInfoResolver extends ListeningTestCase
{
    private IdentityIndexInfoResolver identityIndexInfoResolver = new IdentityIndexInfoResolver();

    @Test
    public void testGetIndexedValuesString() throws Exception
    {
        String value = "testValue";
        assertEquals(EasyList.build(value), identityIndexInfoResolver.getIndexedValues(value));
    }

    @Test
    public void testGetIndexedValuesLong() throws Exception
    {
        Long value = new Long(30);
        assertEquals(EasyList.build(value.toString()), identityIndexInfoResolver.getIndexedValues(value));
    }

    @Test
    public void testGetIndexedValue() throws Exception
    {
        Long value = new Long(30);
        assertEquals(value.toString(), identityIndexInfoResolver.getIndexedValue(value));
    }
}
