package com.atlassian.crowd.embedded.admin.list;

import com.atlassian.crowd.embedded.admin.jirajdbc.JiraJdbcDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.util.SimpleMessage;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.sal.api.message.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Items in the drop-down list of directory types which the user can choose when adding a new directory.
 * Doesn't really correspond with anything else in the system -- it is just used for the UI.
 */
public enum NewDirectoryType
{
    ACTIVE_DIRECTORY("embedded.crowd.directory.type.microsoft.active.directory", "/configure/activedirectory/"),
    LDAP("embedded.crowd.directory.type.ldap", "/configure/ldap/"),
    DELEGATING_LDAP("embedded.crowd.directory.type.delegating.ldap", "/configure/delegatingldap/"),
    CROWD("embedded.crowd.directory.type.crowd", "/configure/crowd/"),
    JIRA("embedded.crowd.directory.type.jira", "/configure/jira/"),
    JIRAJDBC("embedded.crowd.directory.type.jirajdbc", "/configure/jirajdbc");

    private static List<NewDirectoryType> validNewDirectoryTypes;

    private final Message label;
    private final String formUrl;

    NewDirectoryType(String labelKey, String formUrl)
    {
        this.label = SimpleMessage.instance(labelKey);
        this.formUrl = formUrl;
    }

    public Message getLabel()
    {
        return label;
    }

    public String getFormUrl()
    {
        return formUrl;
    }

    public static List<NewDirectoryType> getValidNewDirectoryTypes(ApplicationType applicationType)
    {
        if (validNewDirectoryTypes == null)
        {
            List<NewDirectoryType> values = new ArrayList<NewDirectoryType>(Arrays.asList(NewDirectoryType.values()));
            if (!applicationType.equals(ApplicationType.CONFLUENCE))
                values.remove(JIRAJDBC);

            validNewDirectoryTypes = Collections.unmodifiableList(values);
        }

        return validNewDirectoryTypes;
    }
}
