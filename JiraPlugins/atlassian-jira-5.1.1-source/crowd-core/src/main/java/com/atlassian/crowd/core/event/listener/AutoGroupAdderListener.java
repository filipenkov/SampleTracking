package com.atlassian.crowd.core.event.listener;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.event.user.UserAuthenticatedEvent;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.ReadOnlyGroupException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.EntityComparator;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.event.api.EventListener;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AutoGroupAdderListener
{
    protected static final String AUTO_GROUPS_ADDED = "autoGroupsAdded";

    private final Logger logger = Logger.getLogger(this.getClass());

    private DirectoryInstanceLoader directoryInstanceLoader;

    @EventListener
    public void handleEvent(final UserAuthenticatedEvent event)
    {
        final Directory directory = event.getDirectory();
        final String concatenatedGroupNames = directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS);

        if (StringUtils.isNotBlank(concatenatedGroupNames) && directory.getAllowedOperations().contains(OperationType.UPDATE_GROUP))
        {
            final String[] groups = StringUtils.split(concatenatedGroupNames, DirectoryImpl.AUTO_ADD_GROUPS_SEPARATOR);
            final User user = event.getUser();
            try
            {
                final RemoteDirectory remoteDirectory = directoryInstanceLoader.getDirectory(directory);

                // Do not proceed if the user has authenticated successfully before
                final UserWithAttributes userWithAttributes = remoteDirectory.findUserWithAttributesByName(user.getName());
                if (Boolean.parseBoolean(userWithAttributes.getValue(AUTO_GROUPS_ADDED)))
                {
                    return;
                }

                final Set<String> currentMemberships = new TreeSet<String>(EntityComparator.of(String.class));
                currentMemberships.addAll(searchDirectGroupMemberships(remoteDirectory, user));

                for (String groupName : groups)
                {
                    try
                    {
                        if (!currentMemberships.contains(groupName))
                        {
                            remoteDirectory.addUserToGroup(user.getName(), groupName);
                        }
                    }
                    catch (GroupNotFoundException e)
                    {
                        logger.error("Could not auto add user to group: " + e.getMessage(), e);
                    }
                    catch (OperationFailedException e)
                    {
                        logger.error("Could not access directory: " + e.getMessage(), e);
                    }
                    catch (UserNotFoundException e)
                    {
                        logger.error("Could not auto add user to group: " + e.getMessage(), e);
                    }
                    catch (ReadOnlyGroupException e)
                    {
                        logger.error("Could not auto add user to group: " + e.getMessage(), e);
                    }
                }

                remoteDirectory.storeUserAttributes(user.getName(),
                        ImmutableMap.of(AUTO_GROUPS_ADDED, Collections.singleton(Boolean.TRUE.toString())));
            }
            catch (DirectoryInstantiationException e)
            {
                logger.error("Could not instantiate directory: " + e.getMessage(), e);
            }
            catch (OperationFailedException e)
            {
                logger.error("Could not access directory: " + e.getMessage(), e);
            }
            catch (UserNotFoundException e)
            {
                logger.error("Could not access user: " + e.getMessage(), e);
            }
        }
        else
        {
            if (StringUtils.isNotBlank(concatenatedGroupNames))
            {
                logger.error("You have groups <" + concatenatedGroupNames + "> to be auto-added for the user <" + event.getUser().getName() + ">, but the directory does not have permission for Group updates.");
            }
        }
    }

    /**
     * Searches for user's direct group names.
     *
     * @param remoteDirectory remote directory to use for searching direct group memberships
     * @param user user to search the memberships for
     * @return set of user's direct group names
     * @throws OperationFailedException if the memberships could not be retrieved
     */
    private List<String> searchDirectGroupMemberships(RemoteDirectory remoteDirectory, User user) throws OperationFailedException
    {
        return remoteDirectory.searchGroupRelationships(QueryBuilder
                .queryFor(String.class, EntityDescriptor.group())
                .parentsOf(EntityDescriptor.user())
                .withName(user.getName())
                .returningAtMost(EntityQuery.ALL_RESULTS));
    }

    public void setDirectoryInstanceLoader(DirectoryInstanceLoader directoryInstanceLoader)
    {
        this.directoryInstanceLoader = directoryInstanceLoader;
    }
}
