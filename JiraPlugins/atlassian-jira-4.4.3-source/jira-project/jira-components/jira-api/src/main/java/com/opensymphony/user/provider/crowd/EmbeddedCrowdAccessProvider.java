package com.opensymphony.user.provider.crowd;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.search.EntityDescriptor;
import static com.atlassian.crowd.search.EntityDescriptor.group;
import static com.atlassian.crowd.search.EntityDescriptor.user;
import com.atlassian.crowd.search.builder.QueryBuilder;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import com.google.common.collect.Lists;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.provider.AccessProvider;

import java.util.Collections;
import java.util.List;

public class EmbeddedCrowdAccessProvider extends EmbeddedCrowdAbstractProvider implements AccessProvider
{
    public boolean addToGroup(final String userName, final String groupName)
    {
        CrowdService crowdService = getCrowdService();
        try
        {
            final User user = crowdService.getUser(userName);
            final Group group = crowdService.getGroup(groupName);

            if ((user != null) && (group != null))
            {
                if (!crowdService.isUserMemberOfGroup(user, group))
                {
                    crowdService.addUserToGroup(user, group);
                }
                return true;
            }
            else
            {
                logger.error("Could not add user:" + userName + " to group:" + groupName + " as user or group was not found");
                return false;
            }
        }
        catch (final Exception e)
        {
            logger.error("Could not add user:" + userName + " to group:" + groupName, e);
            return false;
        }
    }

    public boolean create(final String name)
    {
        try
        {
            Group group = new ImmutableGroup(name);
            getCrowdService().addGroup(group);

            return true;
        }
        catch (final Exception e)
        {
            logger.error("Could not create group: " + name, e);
            return false;
        }
    }

    public void flushCaches()
    {
        //do nothing (can't do anything)
    }

    public boolean handles(final String name)
    {
        if (name == null)
        {
            return false;
        }
        CrowdService crowdService = getCrowdService();
        // this is good because it uses caching if needed
        try
        {
            if (crowdService.getGroup(name) != null)
            {
                return true; // we handle this group
            }
            else if (crowdService.getUser(name) != null)
            {
                return true; // we handle this user
            }
            else
            {
                return false;
            }
        }
        catch (final Exception e)
        {
            logger.error("Could not determine if we handle: " + name, e);
            return false;
        }
    }

    public boolean inGroup(final String userName, final String groupName)
    {
        CrowdService crowdService = getCrowdService();
        try
        {
            final User user = crowdService.getUser(userName);
            final Group group = crowdService.getGroup(groupName);
            if ((user != null) && (group != null))
            {
                return crowdService.isUserMemberOfGroup(user, group);
            }
            else
            {
                return false;
            }
        }
        catch (final Exception e)
        {
            logger.error("Could not determine if user: " + userName + " is in group: " + groupName, e);
            return false;
        }
    }

    public List<String> list()
    {
        final Iterable<String> groupNames = getCrowdService().search(QueryBuilder.queryFor(String.class, group()).returningAtMost(ALL_RESULTS));
        return Collections.unmodifiableList(Lists.newArrayList(groupNames));
    }

    public List<String> listGroupsContainingUser(final String userName)
    {
        final Iterable<String> groupNames = getCrowdService().search(
                QueryBuilder.queryFor(String.class, group()).parentsOf(EntityDescriptor.user()).withName(userName).returningAtMost(ALL_RESULTS));
        return Collections.unmodifiableList(Lists.newArrayList(groupNames));
    }

    public List<String> listUsersInGroup(final String groupName)
    {
        final Iterable<String> userNames = getCrowdService().search(
                QueryBuilder.queryFor(String.class, user()).childrenOf(EntityDescriptor.group()).withName(groupName).returningAtMost(ALL_RESULTS));
        return Collections.unmodifiableList(Lists.newArrayList(userNames));
    }

    public boolean remove(final String name)
    {
        CrowdService crowdService = getCrowdService();
        try
        {
            final Group crowdGroup = crowdService.getGroup(name);
            if (crowdGroup != null)
            {
                crowdService.removeGroup(crowdGroup);
                return true;
            }
            else
            {
                // This returns true for backward compatibility with JiraOFBizAccessProvider,
                // which is of course inconsistent with OFBizCredentialsProvider
                return true;
            }
        }
        catch (final Exception e)
        {
            logger.warn("Could not remove group: " + name, e);
            return false;
        }
    }

    public boolean removeFromGroup(final String userName, final String groupName)
    {
        CrowdService crowdService = getCrowdService();
        try
        {
            final User user = crowdService.getUser(userName);
            final Group group = crowdService.getGroup(groupName);
            if ((user != null) && (group != null))
            {
                crowdService.removeUserFromGroup(user, group);
                return true;
            }
            else
            {
                logger.warn("Could not remove user: " + userName + " from group: " + groupName + " as either user or group was not found");
                return false;
            }
        }
        catch (final Exception e)
        {
            logger.warn("Could not remove user: " + userName + " from group: " + groupName, e);
            return false;
        }
    }
}
