/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.history;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.opensymphony.user.EntityNotFoundException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for managing ChangeGroup entities on issues, also known
 * as the Change Log or Change History.
 * <p>
 * TODO: Migrate methods to ChangeHistoryManager and deprecate
 */
public class ChangeLogUtils
{
    private static final Logger log = Logger.getLogger(ChangeLogUtils.class);

    /**
     * Deletes all the change groups and change items associated with the provided issue.
     * @param issue represents the issue that is being deleted
     *
     * @deprecated use {@link com.atlassian.jira.issue.changehistory.ChangeHistoryManager#removeAllChangeItems(com.atlassian.jira.issue.Issue)}
     * instead.
     */
    @Deprecated
    public static void deleteChangesForIssue(GenericValue issue)
    {
        OfBizDelegator delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        // get all changeGroups for the issue
        Map params = EasyMap.build("issue", issue.getLong("id"));
        List changeGroups = delegator.findByAnd("ChangeGroup", params);
        for (Iterator iterator = changeGroups.iterator(); iterator.hasNext();)
        {
            GenericValue changeGroup = (GenericValue) iterator.next();
            // remove all changeItems associated with the changeGroup
            delegator.removeByAnd("ChangeItem", EasyMap.build("group", changeGroup.getLong("id")));
        }

        // remove all changeGroups at once
        delegator.removeByAnd("ChangeGroup", params);
    }

    /**
     * Writes the given field changes to the db and optionally a changelog.
     *
     * @param  before The issue before the update.
     * @param  after This issue after the update.
     * @param incomingChangeItems Some {@link ChangeItemBean}.
     * @param generateChangeItems if true, a changelog is actually generated.
     * @param changeAuthor the User making the change.
     * @return the ChangeGroup GenericValue
     */
    public static GenericValue createChangeGroup(User changeAuthor, GenericValue before, GenericValue after, Collection incomingChangeItems, boolean generateChangeItems)
    {
        OfBizDelegator delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        if (generateChangeItems && EntityUtils.identical(before, after) && (incomingChangeItems == null || incomingChangeItems.size() == 0))
            return null;

        GenericValue changeGroup = null;

        ArrayList changeItems = new ArrayList();

        if (generateChangeItems && !EntityUtils.identical(before, after))
            changeItems.addAll(generateChangeItems(before, after));

        if (incomingChangeItems != null)
            changeItems.addAll(incomingChangeItems);

        if (!changeItems.isEmpty())
        {
            changeGroup = delegator.createValue("ChangeGroup", EasyMap.build("issue", before.getLong("id"), "author", (changeAuthor != null ? changeAuthor.getName() : null), "created", UtilDateTime.nowTimestamp()));

            for (Iterator iterator = changeItems.iterator(); iterator.hasNext();)
            {
                ChangeItemBean cib = (ChangeItemBean) iterator.next();
                Map fields = EasyMap.build("group", changeGroup.getLong("id"));
                fields.put("fieldtype", cib.getFieldType());
                fields.put("field", cib.getField());
                fields.put("oldvalue", cib.getFrom());
                fields.put("oldstring", cib.getFromString());
                fields.put("newvalue", cib.getTo());
                fields.put("newstring", cib.getToString());
                delegator.createValue("ChangeItem", fields);
            }
        }

        return changeGroup;
    }

    public static GenericValue createChangeGroup(User changeAuthor, Issue before, Issue after, Collection incomingChangeItems, boolean generateChangeItems)
    {
        return createChangeGroup(changeAuthor, before.getGenericValue(), after.getGenericValue(), incomingChangeItems, generateChangeItems);
    }

    /**
     * Returns a List of ChangeItemBean objects for each of the relevant fields
     * that differ between the two issues.
     *
     * @param before A GenericValue for the issue before the change.
     * @param after A GenericValue for the issue after the change.
     * @return the list of ChangeItemBeans.
     */
    public static List generateChangeItems(GenericValue before, GenericValue after)
    {
        List changeItems = new ArrayList();

        for (Iterator iterator = before.getModelEntity().getAllFieldNames().iterator(); iterator.hasNext();)
        {
            String fieldname = (String) iterator.next();

            if (fieldname.equals("id") || fieldname.equals("created") || fieldname.equals("updated") || fieldname.equals("workflowId") || fieldname.equals("key") || fieldname.equals("project") || fieldname.equals("fixfor") || fieldname.equals("component") || fieldname.equals("votes"))
                continue;

            ChangeItemBean changeItem = generateChangeItem(before, after, fieldname);

            if (changeItem != null)
            {
                changeItems.add(changeItem);
            }
        }

        return changeItems;
    }

    public static ChangeItemBean generateChangeItem(GenericValue before, GenericValue after, String fieldname)
    {
        if (before.get(fieldname) == null && after.get(fieldname) == null)
            return null;

        if (before.get(fieldname) != null && after.get(fieldname) != null && before.get(fieldname).equals(after.get(fieldname)))
            return null;

        String from = null;
        String to = null;
        String fromString = null;
        String toString = null;

        if (fieldname.equals("assignee") || fieldname.equals("reporter"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
            {
                try
                {
                    fromString = UserUtils.getUser(from).getFullName();
                }
                catch (EntityNotFoundException e)
                {
                    log.warn("User: " + from + " not found - change item will be missing full name.");
                }
            }

            if (to != null)
            {
                try
                {
                    toString = UserUtils.getUser(to).getFullName();
                }
                catch (EntityNotFoundException e)
                {
                    log.warn("User: " + to + " not found - change item will be missing full name.");
                }
            }
        }
        else if (fieldname.equals("type"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
                fromString = ManagerFactory.getConstantsManager().getIssueType(from).getString("name");

            if (to != null)
                toString = ManagerFactory.getConstantsManager().getIssueType(to).getString("name");
        }
        else if (fieldname.equals("resolution"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
                fromString = ManagerFactory.getConstantsManager().getResolution(from).getString("name");

            if (to != null)
                toString = ManagerFactory.getConstantsManager().getResolution(to).getString("name");
        }
        else if (fieldname.equals("priority"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
                fromString = ManagerFactory.getConstantsManager().getPriority(from).getString("name");

            if (to != null)
                toString = ManagerFactory.getConstantsManager().getPriority(to).getString("name");
        }
        else if (fieldname.equals("timeestimate") || fieldname.equals("timespent"))
        {
            Long fromValue = before.getLong(fieldname);
            Long toValue = after.getLong(fieldname);

            if (fromValue != null)
            {
                from = fromValue.toString();
                // DO NOT store formatted strings in the database, as they cannot be i18n'ed when they are retrieved
                // store the raw value and i18n it when displaying
                fromString = from = fromValue.toString();
            }

            if (toValue != null)
            {
                to = toValue.toString();
                // DO NOT store formatted strings in the database, as they cannot be i18n'ed when they are retrieved
                // store the raw value and i18n it when displaying
                toString = toValue.toString();
            }
        }
        else if (fieldname.equals("status"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
            {
                GenericValue fromStatus = ManagerFactory.getConstantsManager().getStatus(from);

                if (fromStatus != null)
                    fromString = fromStatus.getString("name");
            }

            if (to != null)
            {
                GenericValue toStatus = ManagerFactory.getConstantsManager().getStatus(to);

                if (toStatus != null)
                    toString = toStatus.getString("name");
            }
        }
        else if (fieldname.equals("security"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            return generateSecurityChangeItem(fieldname, from, to);
        }
        else
        {
            fromString = before.getString(fieldname);
            toString = after.getString(fieldname);
        }

        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, fieldname, from, fromString, to, toString);
    }

    public static ChangeItemBean generateSecurityChangeItem(String fieldname, String from, String to)
    {
        String fromString = null;
        String toString = null;
        if (from != null)
        {
            try
            {
                GenericValue fromLevel = ManagerFactory.getIssueSecurityLevelManager().getIssueSecurityLevel(new Long(from));

                if (fromLevel != null)
                    fromString = fromLevel.getString("name");
            }
            catch (GenericEntityException e)
            {
                log.error(e, e);
            }
            catch (NumberFormatException e)
            {
                log.error(e, e);
            }
        }

        if (to != null)
        {
            try
            {
                GenericValue toLevel = ManagerFactory.getIssueSecurityLevelManager().getIssueSecurityLevel(new Long(to));

                if (toLevel != null)
                    toString = toLevel.getString("name");
            }
            catch (GenericEntityException e)
            {
                log.error(e, e);
            }
            catch (NumberFormatException e)
            {
                log.error(e, e);
            }
        }
        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, fieldname, from, fromString, to, toString);
    }
}