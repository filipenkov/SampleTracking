package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.user.UserUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static com.atlassian.jira.issue.history.ChangeItemBean.STATIC_FIELD;

public class EventUtils
{
    public static User getPreviousAssignee(IssueEvent event)
    {
        return getPreviousAssigneeUser(event);
    }

    public static User getPreviousAssigneeUser(IssueEvent event)
    {
        User previousAssignee = null;

        if (event.getChangeLog() != null)
        {
            FieldMap fields = FieldMap.build("group", event.getChangeLog().getLong("id")).add("fieldtype", STATIC_FIELD);
            List<GenericValue> changeItems = ComponentAccessor.getOfBizDelegator().findByAnd("ChangeItem", fields);
            for (GenericValue changeItem : changeItems)
            {
                if (changeItem.getString("field").equals("assignee"))
                {
                    if (changeItem.getString("oldvalue") != null)
                    {
                        previousAssignee = UserUtils.getUser(changeItem.getString("oldvalue"));
                    }
                    break;
                }
            }
        }

        return previousAssignee;
    }

}
