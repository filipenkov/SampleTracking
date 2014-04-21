package com.atlassian.jira.bc.group.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupComparator;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;


/**
 * {@link UserManager} based implementation of {@link GroupPickerSearchService}
 *
 * @since v4.4
 */
public class GroupPickerSearchServiceImpl implements GroupPickerSearchService
{
    private UserManager userManager;

    public GroupPickerSearchServiceImpl(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * @see GroupPickerSearchService#findGroups(String)
     */
    public List<Group> findGroups(final String query)
    {
        final Collection<Group> matchingGroups = new TreeSet<Group>(GroupComparator.GROUP_COMPARATOR);
        final Collection<Group> groups = userManager.getGroups();

        if(StringUtils.isBlank(query))
        {
            matchingGroups.addAll(groups);
            return new ArrayList<Group>(matchingGroups);
        }

        for (final Group group : groups)
        {
            if(group.getName().contains(query))
            {
                matchingGroups.add(group);
            }
        }
        return new ArrayList<Group>(matchingGroups);
    }

}
