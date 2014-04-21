package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.plugin.rest.entity.GroupEntity;
import com.atlassian.plugins.rest.common.Link;
import org.apache.commons.lang.Validate;

/**
 * Utility class for GroupEntity.
 */
public class GroupEntityUtil
{
    private GroupEntityUtil()
    {
        // prevent instantiation
    }

    /**
     * Expands a GroupEntity from its minimal form to the expanded version.
     * Attributes are expanded if <tt>expandAttributes</tt> is <tt>true</tt>,
     * otherwise, GroupEntity is returned with no attributes expanded.
     * <p/>
     * N.B. This is not the same as expanding a group entity, which expands a
     * GroupEntity from its minimal form, to having all the description, type,
     * etc filled in. Expanding a GroupEntity is automatically performed in
     * {@link com.atlassian.crowd.plugin.rest.entity.GroupEntityExpander}.
     * 
     * @param applicationService ApplicationService to find a group
     * @param application name of the application requesting the group
     * @param minimalGroupEntity Minimal representation of a GroupEntity. Must
     * include at least a group name and a link.
     * @param expandAttributes set to true if the expanded GroupEntity should
     * expand attributes.
     * @return GroupEntity expanded GroupEntity
     * @throws IllegalArgumentException if the minimal GroupEntity does not
     * include at least a group name and a link.
     */
    public static GroupEntity expandGroup(final ApplicationService applicationService, final Application application, final GroupEntity minimalGroupEntity, final boolean expandAttributes)
            throws GroupNotFoundException
    {
        Validate.notNull(applicationService);
        Validate.notNull(application);
        Validate.notNull(minimalGroupEntity);
        Validate.notNull(minimalGroupEntity.getName(), "Minimal group entity must include a group name");
        Validate.notNull(minimalGroupEntity.getLink(), "Minimal group entity must include a link");

        final String groupName = minimalGroupEntity.getName();
        final Link groupLink = minimalGroupEntity.getLink();

        GroupEntity expandedGroup;
        if (expandAttributes)
        {
            GroupWithAttributes group = applicationService.findGroupWithAttributesByName(application, groupName);
            Link updatedLink = LinkUriHelper.updateGroupLink(groupLink, group.getName()); // use the canonical group name in the link
            expandedGroup = EntityTranslator.toGroupEntity(group, group, updatedLink);
        }
        else
        {
            Group group = applicationService.findGroupByName(application, groupName);
            Link updatedLink = LinkUriHelper.updateGroupLink(groupLink, group.getName()); // use the canonical group name in the link
            expandedGroup = EntityTranslator.toGroupEntity(group, updatedLink);
        }
        return expandedGroup;
    }
}
