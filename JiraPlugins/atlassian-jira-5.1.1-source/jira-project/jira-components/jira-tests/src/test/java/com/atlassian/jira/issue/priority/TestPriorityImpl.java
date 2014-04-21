package com.atlassian.jira.issue.priority;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Map;

/**
 * @since v4.1.1
 */
public class TestPriorityImpl extends ListeningTestCase
{
    @Test
    public void testStatusColor() throws Exception
    {
        final Map<String,String> propsMap = MapBuilder.<String, String>newBuilder().add("statusColor", "GREEN").toMutableMap();
        final MockGenericValue priorityGv = new MockGenericValue("priority", propsMap);
        final PriorityImpl priority = new PriorityImpl(priorityGv, null, null);

        assertEquals("GREEN", priority.getStatusColor());

        priority.setStatusColor("RED");

        assertEquals("RED", priority.getStatusColor());
    }
}
