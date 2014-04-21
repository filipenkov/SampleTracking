package com.atlassian.crowd.embedded.admin.list;

import com.atlassian.crowd.embedded.admin.util.SimpleMessage;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationRoundInformation;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.sal.api.message.Message;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.atlassian.crowd.embedded.admin.list.DirectoriesController.Operation.DISABLE;
import static com.atlassian.crowd.embedded.admin.list.DirectoriesController.Operation.EDIT;
import static com.atlassian.crowd.embedded.admin.list.DirectoriesController.Operation.ENABLE;
import static com.atlassian.crowd.embedded.admin.list.DirectoriesController.Operation.REMOVE;
import static com.atlassian.crowd.embedded.admin.list.DirectoriesController.Operation.TROUBLESHOOT;

/**
 * Represents a directory in the UI.
 */
public final class DirectoryListItem
{
    private final long id;
    private final String name;
    private final Message type;
    private final boolean active;
    private final Map<Message, DirectoryListItemOperation> operations = new LinkedHashMap<Message, DirectoryListItemOperation>();
    private ListItemPosition position;
    private final DirectorySynchronisationInformation syncInfo;

    public DirectoryListItem(Directory directory, Message type, User currentUser, ListItemPosition position, DirectorySynchronisationInformation syncInfo, ApplicationType applicationType)
    {
        this.id = directory.getId();
        this.name = directory.getName();
        this.type = type;
        this.active = directory.isActive();
        if (directory.getType() == DirectoryType.INTERNAL)
        {
            if (!applicationType.equals(ApplicationType.CONFLUENCE))
            {
                addOperation(directory, EDIT);
            }
            if (canModifyDirectory(currentUser, directory))
            {
                addOperation(directory, directory.isActive() ? DISABLE : ENABLE);
            }
        }
        else
        {
            if (canModifyDirectory(currentUser, directory))
            {
                addOperation(directory, directory.isActive() ? DISABLE : ENABLE);
                addOperation(directory, EDIT);
                if (!directory.isActive())
                {
                    addOperation(directory, REMOVE);
                }
            }
        }
        if (directory.getType() != DirectoryType.INTERNAL && directory.getType() != DirectoryType.DELEGATING)
        {
            addOperation(directory, TROUBLESHOOT);
        }
        this.position = position;
        this.syncInfo = syncInfo;
    }

    private boolean canModifyDirectory(User currentUser, Directory directory)
    {
        return currentUser.getDirectoryId() != directory.getId();
    }

    public void addOperation(Directory directory, DirectoriesController.Operation operation)
    {
        operations.put(operation.getMessage(), new DirectoryListItemOperation(operation.getUrl(directory),operation.getMethodName()));
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Message getType()
    {
        return type;
    }

    public boolean isActive()
    {
        return active;
    }

    public Map<Message, DirectoryListItemOperation> getOperations()
    {
        return operations;
    }

    public boolean canMoveUp()
    {
        return position.canMoveUp();
    }

    public boolean canMoveDown()
    {
        return position.canMoveDown();
    }

    public boolean isSynchronisable()
    {
        // TODO: should be calling crowdDirectoryService.isSynchronisable()
        return syncInfo != null;
    }

    public String getLastSyncTime()
    {
        if (syncInfo.getLastRound() == null)
            return null;
        if (syncInfo.getLastRound().getStartTime() == 0)
            return null;
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return dateFormat.format(syncInfo.getLastRound().getStartTime() + syncInfo.getLastRound().getDurationMs());
    }

    public long getLastSyncDuration()
    {
        return syncInfo.getLastRound() == null ? 0 : syncInfo.getLastRound().getDurationMs() / 1000;
    }

    public boolean isSynchronising()
    {
        return syncInfo.isSynchronising();
    }

    public long getSecondsSinceSyncStarted()
    {
        if (syncInfo == null || !syncInfo.isSynchronising())
            return 0;
        return (System.currentTimeMillis() - syncInfo.getActiveRound().getStartTime()) / 1000;
    }

    public Message getSyncStatusMessage()
    {
        final DirectorySynchronisationRoundInformation syncRound =
                isSynchronising() ? syncInfo.getActiveRound() : syncInfo.getLastRound();
        if (syncRound == null)
        {
            return null;
        }
        final String statusKey = syncRound.getStatusKey();
        if (statusKey == null)
        {
            return null;
        }

        final Serializable[] params = syncRound.getStatusParameters().toArray(new Serializable[0]);
        return SimpleMessage.instance("embedded.crowd." + statusKey, params);
    }
}
