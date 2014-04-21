package com.atlassian.jira.event.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.OSUserConverter;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.issue.history.ChangeItemBean.STATIC_FIELD;

public class EventUtils
{
    private static final Logger log = Logger.getLogger(EventUtils.class);

    public static com.opensymphony.user.User getPreviousAssignee(IssueEvent event)
    {
        return OSUserConverter.convertToOSUser(getPreviousAssigneeUser(event));
    }

    public static User getPreviousAssigneeUser(IssueEvent event)
    {
        User previousAssignee = null;

        try
        {
            if (event.getChangeLog() != null)
            {
                Map fields = EasyMap.build("group", event.getChangeLog().getLong("id"), "fieldtype", STATIC_FIELD);
                List<GenericValue> changeItems = CoreFactory.getGenericDelegator().findByAnd("ChangeItem", fields);
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
        }
        catch (Exception e)
        {
            log.error(e, e);
        }

        return previousAssignee;
    }

}
