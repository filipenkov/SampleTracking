package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.model.user.TimestampedUser;

import java.util.Date;

/**
 */
public class ImmutableTimestampedUser implements TimestampedUser
{
    private final String firstName;
    private final String lastName;
    private final long directoryId;
    private final boolean active;
    private final String emailAddress;
    private final String displayName;
    private final String name;
    private final Date createdDate;
    private final Date updatedDate;

    public ImmutableTimestampedUser(TimestampedUser user)
    {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        directoryId = user.getDirectoryId();
        active = user.isActive();
        emailAddress = user.getEmailAddress();
        displayName = user.getDisplayName();
        name = user.getName();
        createdDate = user.getCreatedDate();
        updatedDate = user.getUpdatedDate();
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int compareTo(User user)
    {
        return UserComparator.compareTo(this,user);
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public String getName()
    {
        return name;
    }
}
