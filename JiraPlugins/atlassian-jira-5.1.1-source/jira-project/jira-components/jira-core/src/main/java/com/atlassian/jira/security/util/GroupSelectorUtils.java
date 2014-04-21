package com.atlassian.jira.security.util;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.util.profiling.UtilTimerStack;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Hacky methods to support mapping a display name to a group name. A "display" name is a user-friendly equivalent of
 * a regular group name, eg. "JIRA Developers" instead of "jira-developers". In addition, a group name ("Admins")
 * may resolve to multiple groups ("jira-admins, qa-admins" etc).<p>
 * This code is used in groupnames.jsp,
 * {@link com.atlassian.jira.security.type.GroupCF} and {@link com.atlassian.jira.workflow.condition.InGroupCFCondition}
 */
// TODO: Make "Display" name an editable property of groups
// TODO: Create a "group picker" object which handles the "display" -> regular name mapping.
@EventComponent
public class GroupSelectorUtils
{
    private static final Logger log = Logger.getLogger(GroupSelectorUtils.class);

    private final GenericDelegator genericDelegator;
    private final FieldManager fieldManager;
    private final UserUtil userUtil;
    private final GroupManager groupManager;

    /**
     * PropertySet key for group's display name.
     */
    private static final String GROUP_DISPLAY_NAME = "jira.group.displayname";

    /**
     * Cache of name-> group mappings. eg. "Developers" -> ["jira-developers", "ABC-developers"].
     */
    private MultiMap displayNameCache;

    public GroupSelectorUtils(final GenericDelegator genericDelegator, final FieldManager fieldManager,
            final UserUtil userUtil, GroupManager groupManager)
    {
        this.genericDelegator = genericDelegator;
        this.fieldManager = fieldManager;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        if (displayNameCache != null)
        {
            displayNameCache.clear();
        }
    }

    /**
     * Get users from a group chosen by a Group Selector custom field, in a certain issue.
     *
     * @param issue
     * @param customFieldId Id of {@link GroupSelectorField}.
     * @return Set of {@link User}s.
     */
    public Set getUsers(Issue issue, String customFieldId)
    {
        UtilTimerStack.push("GroupCF.getUsers");
        if (issue == null)
        {
            return Collections.EMPTY_SET;
        }
        FieldManager fieldManager = ComponentManager.getInstance().getFieldManager();
        CustomField field = fieldManager.getCustomField(customFieldId);
        if (field == null)
        {
            throw new IllegalArgumentException("Group Selector permission configured with custom field " + customFieldId + ", but this field does not exist");
        }
        if (!(field.getCustomFieldType() instanceof GroupSelectorField))
        {
            throw new IllegalArgumentException("Group Selector permission configured with field " + customFieldId + ", but this is not a type that can select groups");
        }
        Object groupCFValue = field.getValue(issue);
        if (groupCFValue == null)
        {
            log.debug("Issue " + issue + " has no value for field " + field);
        }
        Set groups = getGroups(groupCFValue);
        if (log.isDebugEnabled())
        {
            if (groupCFValue == null)
            {
                log.debug("Issue " + issue + " does not have a value for field " + field);
            }
            else if (groups.size() == 0)
            {
                log.debug("No groups found for group selector value '" + groupCFValue + "' on issue " + issue + ". Perhaps no group is mapped to this name?");
            }
            else
            {
                log.debug("GroupCF returned users from groups " + printGroups(groups));
            }
        }
        Set users = userUtil.getAllUsersInGroups(groups);
        UtilTimerStack.pop("GroupCF.getUsers");
        return users;
    }

    public Set<com.atlassian.crowd.embedded.api.User> getUsers(Object groupCustomFieldRawValue)
    {
        if (groupCustomFieldRawValue == null)
        {
            return Collections.emptySet();
        }
        @SuppressWarnings ( { "unchecked" })
        Set<com.atlassian.crowd.embedded.api.Group> groups = (Set) getGroups(groupCustomFieldRawValue);
        return userUtil.getAllUsersInGroups(groups);
    }

    /**
     * Get all custom fields that could possibly be identifying a group. For example, select-lists, text fields.
     *
     * @return list of Field objects, never null
     */
    public List /* <Field> */ getCustomFieldsSpecifyingGroups()
    {
        List fields = new ArrayList();

        Set fieldSet;
        try
        {
            fieldSet = fieldManager.getAllAvailableNavigableFields();
        }
        catch (FieldException e)
        {
            return Collections.EMPTY_LIST;
        }

        for (Iterator i = fieldSet.iterator(); i.hasNext();)
        {
            Field field = (Field) i.next();
            if (fieldManager.isCustomField(field))
            {
                CustomField customField = (CustomField) field;
                // Exclude field types that obviously don't specify a group
                if (customField.getCustomFieldType() instanceof GroupSelectorField)
                {
                    fields.add(field);
                }
            }
        }

        return fields;

    }

    /**
     * Determines if a user is a member of a group specified by a custom field value.
     *
     * @param issue The current issue
     * @param field The custom field specifying the group(s). Eg. a select-list.
     * @param user  The user we wish to check for
     * @return If user is in one of the groups specified by the custom field.
     */
    public boolean isUserInCustomFieldGroup(Issue issue, CustomField field, User user)
    {
        Object cfValue = issue.getCustomFieldValue(field);
        Collection<Group> groups = getGroups(cfValue);
        for (Group group: groups)
        {
            if (groupManager.isUserInGroup(user, group))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Given an object (usually a custom field value) find the associated group.
     *
     * @param cfValue A {@link String} (eg. "JIRA Developers" or "jira-developers") {@link Group} or {@link Collection} of {@link String}s or {@link Group}s.
     * @return A Set of {@link Group}s.
     */
    public Set<Group> getGroups(Object cfValue)
    {
        if (cfValue == null)
        {
            return Collections.EMPTY_SET;
        }
        Set groups = null;
        if (cfValue instanceof Group)
        {
            groups = new HashSet(1);
            groups.add(cfValue);
        }
        else if (cfValue instanceof String)
        {
            groups = new HashSet(1);
            groups.addAll(getGroups((String) cfValue));
        }
        else if (cfValue instanceof Option)
        {
            groups = new HashSet(1);
            groups.addAll(getGroups(((Option) cfValue).getValue()));
        }
        else if (cfValue instanceof Collection)
        {
            Collection groupList = (Collection) cfValue;
            groups = new HashSet(groupList.size());
            Iterator iter = groupList.iterator();
            while (iter.hasNext())
            {
                Object groupValue = iter.next();
                if (groupValue instanceof String)
                {
                    groups.addAll(getGroups((String) groupValue));
                }
                else if (groupValue instanceof Option)
                {
                    groups.addAll(getGroups(((Option) groupValue).getValue()));
                }
                else if (groupValue instanceof Group)
                {
                    groups.add(groupValue);
                }
                else
                {
                    log.error("Object '" + groupValue + "' is of type " + cfValue.getClass().getName() + " which cannot be converted to a Group. Needs to be a Group object or a String representing group name.");
                }
            }
        }
        else
        {
            log.error("Object '" + cfValue + "' is of type " + cfValue.getClass().getName() + " which cannot be converted to a Group. Needs to be a Group object or a String representing group name.");
        }

        return groups;
    }

    /**
     * Given a string representing a group, return the Group.
     *
     * @param groupStr eg. "JIRA Developers" or "jira-developers".
     * @return A Set of {@link Group}s.
     */
    private Set getGroups(String groupStr)
    {
        Group group = groupManager.getGroup(groupStr);
        if (group != null)
        {
            Set groups = new HashSet(1);
            groups.add(group);
            return groups;
        }
        else
        {
            // Assume it's a group's "display" name, and map it to a group(s)
            try
            {
                return getGroupsFromDisplayName(groupStr);
            }
            catch (GenericEntityException e)
            {
                log.error("Error getting group from name '" + groupStr + "': " + e, e);
                return null;
            }
        }
    }

    /**
     * Print a comma-separated list of groups. Useful in debugging code.
     * @deprecated Create your own method if you really want a comma-separated list of groups. Since v4.4.
     */
    public String printGroups(Collection groups)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        Iterator groupIter = groups.iterator();
        while (groupIter.hasNext())
        {
            Group group = (Group) groupIter.next();
            buf.append(group.getName());
            if (groupIter.hasNext())
            {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }


    /**
     * Given a display name, return a group name if one is mapped.
     *
     * @param groupDisplayName Eg. "JIRA Developers"
     * @return A set of {@link Group}s, eg. [jira-developers], or null if none exists for the given display name.
     * @deprecated since v3.8 this is a very short term method as it will not work with external User/Group management.
     */
    public Set getGroupsFromDisplayName(String groupDisplayName) throws GenericEntityException
    {
        initDisplayNameCache();
        if (displayNameCache.get(groupDisplayName) != null)
        {
            return new HashSet((Collection) displayNameCache.get(groupDisplayName));
        }
        else
        {
            log.warn("No group with name '" + groupDisplayName + "' found");
            return Collections.EMPTY_SET;
        }
    }

    /**
     * @deprecated since v3.8 this is a very short term method as it will not work with external User/Group management.
     */
    private synchronized void initDisplayNameCache() throws GenericEntityException
    {
        if (displayNameCache == null)
        {
            log.debug("Populating display name cache. This should only happen once");
            Collection groups = groupManager.getAllGroups();
            displayNameCache = new MultiHashMap(groups.size());
            Iterator iter = groups.iterator();
            while (iter.hasNext())
            {
                Group group = (Group) iter.next();
                String displayName = getGroupDisplayName(group);
                if (displayName != null)
                {
                    displayNameCache.put(displayName, group);
                }
            }
        }
    }

    /**
     * Get a group's display name.
     *
     * @return The display name, or null if not set.
     * @deprecated since v3.8 this is a very short term method as it will not work with external User/Group management.
     */
    public final String getGroupDisplayName(final Group group) throws GenericEntityException
    {
        GenericValue groupGV = getGroupGV(group);
        PropertySet propSet = OFBizPropertyUtils.getPropertySet(groupGV);
        return propSet.getString("jira.group.displayname");
    }

    /**
     * Get a {@link GenericValue} for a group.
     *
     * @deprecated since v3.8 this is a very short term method as it will not work with external User/Group management.
     */
    private GenericValue getGroupGV(Group group) throws GenericEntityException
    {
        List groups = genericDelegator.findByAnd("Group", EasyMap.build("groupName", group.getName()));
        if (groups.isEmpty())
        {
            throw new IllegalArgumentException("The group '" + group.getName() + "' could not be found.");
        }

        return (GenericValue) groups.get(0);
    }

    /**
     * Update a group's display name.
     *
     * @param group  Group to update
     * @param oldVal The old value, if any (can be null)
     * @param newVal New display name
     * @return true if the display name was updated.
     * @deprecated since v3.8 this is a very short term method as it will not work with external User/Group management.
     */
    public boolean updateGroupName(final Group group, final String oldVal, final String newVal) throws GenericEntityException
    {
        if (newVal == null || newVal.equals(oldVal))
        {
            return false;
        }
        log.debug("Updating group name cache: " + newVal + " -> " + group);
        GenericValue groupGV = getGroupGV(group);
        PropertySet propSet = OFBizPropertyUtils.getPropertySet(groupGV);
        propSet.setAsActualType(GROUP_DISPLAY_NAME, newVal);
        if (displayNameCache != null)
        {
            displayNameCache.remove(oldVal, group);
            displayNameCache.put(newVal, group);
        }
        return true;
    }

}
