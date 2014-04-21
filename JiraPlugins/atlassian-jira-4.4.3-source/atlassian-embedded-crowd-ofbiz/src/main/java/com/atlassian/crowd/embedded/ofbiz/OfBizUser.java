package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.model.user.TimestampedUser;
import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Date;

import static com.atlassian.crowd.embedded.ofbiz.UserEntity.*;
import static com.google.common.base.Preconditions.checkNotNull;

class OfBizUser implements TimestampedUser, IdName
{
    static OfBizUser from(final GenericValue userGenericValue)
    {
        return new OfBizUser(userGenericValue);
    }

    private final long id;
    private final long directoryId;
    private final String name;
    private final boolean active;
    private final Date createdDate;
    private final Date updatedDate;
    private final String emailAddress;
    private final String firstName;
    private final String lastName;
    private final String displayName;

    private OfBizUser(final GenericValue userGenericValue)
    {
        checkNotNull(userGenericValue);
        id = userGenericValue.getLong(USER_ID);
        directoryId = userGenericValue.getLong(DIRECTORY_ID);
        name = userGenericValue.getString(USER_NAME);
        active = BooleanUtils.toBoolean(userGenericValue.getInteger(ACTIVE));
        createdDate = userGenericValue.getTimestamp(CREATED_DATE);
        updatedDate = userGenericValue.getTimestamp(UPDATED_DATE);
        emailAddress = userGenericValue.getString(EMAIL_ADDRESS);
        firstName = userGenericValue.getString(FIRST_NAME);
        lastName = userGenericValue.getString(LAST_NAME);
        displayName = userGenericValue.getString(DISPLAY_NAME);
    }

    public long getId()
    {
        return id;
    }

    public boolean isActive()
    {
        return active;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof User) && UserComparator.equal(this, (User) o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(final User other)
    {
        return UserComparator.compareTo(this, other);
    }
}
