package com.atlassian.jira.jql.resolver;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestWorkRatioIndexInfoResolver extends ListeningTestCase
{
    @Test
    public void testConvertToIndexValue() throws Exception
    {
        final WorkRatioIndexInfoResolver resolver = new WorkRatioIndexInfoResolver();
        assertEquals(Collections.singletonList("00050"), resolver.getIndexedValues(50L));
        assertEquals(Collections.singletonList("00040"), resolver.getIndexedValues("40"));
    }
}
