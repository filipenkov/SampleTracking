package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetPersister;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

public class UpgradeTask_Build101 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build101.class);

    private final FieldConfigSchemeManager configSchemeManager;
    private final JiraContextTreeManager contextTreeManager;
    private final FieldManager fieldManager;
    private final ApplicationProperties applicationProperties;
    private final OfBizDelegator delegator;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    public UpgradeTask_Build101(final FieldConfigSchemeManager configSchemeManager, final JiraContextTreeManager contextTreeManager, final FieldManager fieldManager, final ApplicationProperties applicationProperties, final OfBizDelegator delegator, final IssueTypeSchemeManager issueTypeSchemeManager)
    {
        this.configSchemeManager = configSchemeManager;
        this.contextTreeManager = contextTreeManager;
        this.fieldManager = fieldManager;
        this.applicationProperties = applicationProperties;
        this.delegator = delegator;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "101";
    }

    @Override
    public String getShortDescription()
    {
        return "Create default issue type schemes for the current issue types";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Set up the global context
        final List issueContextNodes = CustomFieldUtils.buildJiraIssueContexts(true, null, null, contextTreeManager);

        // Create the default scheme
        final ConfigurableField issueTypeSystemField = fieldManager.getConfigurableField(IssueFieldConstants.ISSUE_TYPE);
        final FieldConfigScheme fieldConfigScheme = configSchemeManager.createDefaultScheme(issueTypeSystemField, issueContextNodes);
        applicationProperties.setString(APKeys.DEFAULT_ISSUE_TYPE_SCHEME, fieldConfigScheme.getId().toString());

        // Do some loggin'
        final FieldConfigScheme defaultScheme = issueTypeSchemeManager.getDefaultIssueTypeScheme();
        final FieldConfig defaultConfig = defaultScheme.getOneAndOnlyConfig();
        final FieldConfigScheme.Builder builder = new FieldConfigScheme.Builder(defaultScheme);
        builder.setName("Default Issue Type Scheme");
        builder.setDescription("Default issue type scheme is the list of global issue types. All newly created issue types will automatically be added to this scheme.");
        final FieldConfigScheme defaultIssueTypeScheme = builder.toFieldConfigScheme();
        configSchemeManager.updateFieldConfigScheme(defaultIssueTypeScheme);
        log.info("Default scheme created '" + defaultIssueTypeScheme.getName() + "' (id: " + defaultIssueTypeScheme.getId() + ") and config id " + defaultConfig.getId());

        // Create the option sets on the dB level
        final List allIssueTypesGv = delegator.findAll(ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE, EasyList.build("style ASC", "sequence ASC"));
        int i = 0;
        String firstIssueTypeId = null;
        for (final Iterator iterator = allIssueTypesGv.iterator(); iterator.hasNext();)
        {
            final GenericValue issueType = (GenericValue) iterator.next();
            if (i == 0)
            {
                // Save the first issue type id for happiness of the world
                firstIssueTypeId = issueType.getString("id");
            }

            delegator.createValue(OptionSetPersister.ENTITY_TABLE_NAME, EasyMap.build(OptionSetPersister.ENTITY_FIELD_CONFIG, defaultConfig.getId(),
                OptionSetPersister.ENTITY_FIELD, defaultConfig.getFieldId(), OptionSetPersister.ENTITY_OPTION_ID, issueType.getString("id"),
                OptionSetPersister.ENTITY_SEQUENCE, new Long(i)));
            i++;
        }

        // Set the default issue type
        final String defaultIssueType = getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE);
        if (StringUtils.isNotBlank(defaultIssueType))
        {
            issueTypeSchemeManager.setDefaultValue(defaultConfig, defaultIssueType);
        }
        else
        {
            // Then default to the first issue type (to maintain backwards compatible behaviour)
            issueTypeSchemeManager.setDefaultValue(defaultConfig, firstIssueTypeId);
        }
    }

}
