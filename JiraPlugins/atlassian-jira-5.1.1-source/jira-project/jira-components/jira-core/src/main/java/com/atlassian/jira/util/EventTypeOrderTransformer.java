package com.atlassian.jira.util;

import com.atlassian.jira.event.type.EventType;
import org.apache.commons.collections.Transformer;

import java.util.HashMap;
import java.util.Map;

public class EventTypeOrderTransformer implements Transformer
{
    private Map mappings;


    public EventTypeOrderTransformer()
    {
        int i = 1;
        mappings = new HashMap();

        // The order they appear below is the order tehy appear on screen
        mappings.put(EventType.ISSUE_CREATED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_UPDATED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_ASSIGNED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_RESOLVED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_CLOSED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_COMMENTED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_COMMENT_EDITED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_REOPENED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_DELETED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_MOVED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_WORKLOGGED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_WORKSTARTED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_WORKSTOPPED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_WORKLOG_UPDATED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_WORKLOG_DELETED_ID, new Long(i++)) ;
        mappings.put(EventType.ISSUE_GENERICEVENT_ID, new Long(i++)) ;

    }


    public Object transform(Object object) {
        if (object instanceof EventType)
        {
            EventType eventType = (EventType)object;
            Long order;

            if(mappings.containsKey(eventType.getId()))
            {
                order = (Long)mappings.get(eventType.getId());
            }
            else
            {
                order = eventType.getId();
            }

            return order;

        }

        return object;
    }
}
