package com.atlassian.crowd.model.user;

import com.atlassian.crowd.model.DirectoryEntity;

import java.security.Principal;

public interface User extends Principal, DirectoryEntity, com.atlassian.crowd.embedded.api.User
{
    String getFirstName();

    String getLastName();
}
