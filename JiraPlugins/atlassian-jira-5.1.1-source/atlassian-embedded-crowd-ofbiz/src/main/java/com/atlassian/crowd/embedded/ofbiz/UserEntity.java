package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.model.user.User;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Map;

class UserEntity
{
    static final String ENTITY = "User";
    static final String USER_ID = "id";
    static final String USER_NAME = "userName";
    static final String DIRECTORY_ID = "directoryId";
    static final String LOWER_USER_NAME = "lowerUserName";
    static final String ACTIVE = "active";
    static final String CREDENTIAL = "credential";
    static final String FIRST_NAME = "firstName";
    static final String LOWER_FIRST_NAME = "lowerFirstName";
    static final String LAST_NAME = "lastName";
    static final String LOWER_LAST_NAME = "lowerLastName";
    static final String DISPLAY_NAME = "displayName";
    static final String LOWER_DISPLAY_NAME = "lowerDisplayName";
    static final String EMAIL_ADDRESS = "emailAddress";
    static final String LOWER_EMAIL_ADDRESS = "lowerEmailAddress";
    static final String CREATED_DATE = "createdDate";
    static final String UPDATED_DATE = "updatedDate";

    private UserEntity()
    {}

    static Map<String, Object> getData(final User user, final PasswordCredential credential)
    {
        PrimitiveMap.Builder data = getUserDetails(user);
        if (credential != null)
            data.put(CREDENTIAL, credential.getCredential());
        return data.build();
    }

    static Map<String, Object> getData(final User user)
    {
        return getUserDetails(user).build();
    }

    static Map<String, Object> getData(final User user, final PasswordCredential credential, final Timestamp updatedDate, final Timestamp createdDate)
    {
        PrimitiveMap.Builder data = getUserDetails(user);
        if (credential != null)
        {
            data.put(CREDENTIAL, credential.getCredential());
        }
        if (updatedDate != null)
        {
            data.put(UPDATED_DATE, updatedDate);
        }
        if (createdDate != null)
        {
            data.put(CREATED_DATE, createdDate);
        }
        return data.build();
    }

    private static PrimitiveMap.Builder getUserDetails(User user)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(USER_NAME, user.getName());
        data.put(DIRECTORY_ID, user.getDirectoryId());
        data.putCaseInsensitive(LOWER_USER_NAME, user.getName());
        data.put(ACTIVE, user.isActive());
        data.put(FIRST_NAME, user.getFirstName());
        data.putCaseInsensitive(LOWER_FIRST_NAME, user.getFirstName());
        data.put(LAST_NAME, user.getLastName());
        data.putCaseInsensitive(LOWER_LAST_NAME, user.getLastName());
        data.put(DISPLAY_NAME, user.getDisplayName());
        data.putCaseInsensitive(LOWER_DISPLAY_NAME, user.getDisplayName());
        data.put(EMAIL_ADDRESS, user.getEmailAddress());
        data.putCaseInsensitive(LOWER_EMAIL_ADDRESS, user.getEmailAddress());
        return data;
    }

    static GenericValue setData(final User user, final GenericValue userGenericValue)
    {
        userGenericValue.setFields(getData(user));
        return userGenericValue;
    }
}
