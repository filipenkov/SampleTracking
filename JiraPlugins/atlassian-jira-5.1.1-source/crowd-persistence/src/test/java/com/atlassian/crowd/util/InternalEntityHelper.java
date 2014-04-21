package com.atlassian.crowd.util;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.UserTemplate;

public class InternalEntityHelper
{
    public static DirectoryImpl createDirectory(long id, String name)
    {
        InternalEntityTemplate template = new InternalEntityTemplate();
        template.setId(id);
        template.setName(name);
        return new DirectoryImpl(template);
    }

    public static InternalUser createUser(long directoryId, String name)
    {
        return createUser(directoryId, name, true);
    }

    public static InternalUser createUser(long directoryId, String name, boolean active)
    {
        DirectoryImpl directory = createDirectory(directoryId, "");
        PasswordCredential credential = new PasswordCredential("password", true);
        UserTemplate template = new UserTemplate(name, directoryId);
        template.setActive(active);
        template.setDisplayName("a");
        template.setFirstName("a");
        template.setLastName("a");
        template.setEmailAddress("a");
        return new InternalUser(template, directory, credential);
    }
}
