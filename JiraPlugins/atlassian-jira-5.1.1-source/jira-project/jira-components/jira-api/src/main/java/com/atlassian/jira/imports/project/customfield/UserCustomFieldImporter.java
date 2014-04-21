package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Implementation of ProjectCustomFieldImporter for custom fields that store usernames.
 *
 * @since v3.13
 */
public class UserCustomFieldImporter implements ProjectCustomFieldImporter
{
    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        final String username = customFieldValue.getValue();
        // ignore empty username including null and empty String.
        if ((username != null) && (username.length() > 0))
        {
            // Flag the username as required
            projectImportMapper.getUserMapper().flagUserAsInUse(username);
            // We don't check the Mapper directly if the username can be mapped, because Users can sometimes be automatically imported
            // during the Project Import.
        }
        return null;
    }

    public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        // We don't actually map Users, we just use the same username.
        return new MappedCustomFieldValue(customFieldValue.getValue());
    }
}
