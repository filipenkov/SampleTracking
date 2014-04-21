package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.manager.DefaultOptionsManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.OfBizCustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class UpgradeTask_Build85 extends AbstractFieldScreenUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build85.class);

    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager configSchemeManager;
    private final GenericConfigManager genericConfigManager;
    private final OfBizDelegator delegator;
    private final JiraContextTreeManager treeManager;

    public UpgradeTask_Build85(ProjectManager projectManager, ConstantsManager constantsManager, CustomFieldManager customFieldManager, FieldConfigSchemeManager configSchemeManager, GenericConfigManager genericConfigManager, OfBizDelegator delegator, JiraContextTreeManager treeManager)
    {
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.configSchemeManager = configSchemeManager;
        this.genericConfigManager = genericConfigManager;
        this.delegator = delegator;
        this.treeManager = treeManager;
    }

    public String getBuildNumber()
    {
        return "85";
    }

    public String getShortDescription()
    {
        return "Upgrade custom fields to use new configuration";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Create default config for each custom field
        // associate all options to the right config
        log.info("Upgrading custom fields");
        List customFields = delegator.findAll(CustomFieldImpl.ENTITY_TABLE_NAME);
        for (Iterator iterator = customFields.iterator(); iterator.hasNext();)
        {
            GenericValue customFieldGv = (GenericValue) iterator.next();
            final CustomField customField = customFieldManager.getCustomFieldInstance(customFieldGv);
            log.info("Upgrading field: " + customFieldGv);
            if (customField.getCustomFieldType() != null)
            {
                Long projectId = customFieldGv.getLong("project");
                String issueTypeId = customFieldGv.getString("issuetype");
                List context = new ArrayList();
                List issueTypes = new ArrayList();
                // global
                if (projectId == null && issueTypeId == null)
                {
                    issueTypes.add(null);
                    context.add(treeManager.getRootNode());
                }
                else if (projectId != null)
                {
                    issueTypes.add(null);
                    context.add(new ProjectContext(projectManager.getProject(projectId),treeManager));
                }
                else if (issueTypeId != null)
                {
                    issueTypes.add(constantsManager.getIssueType(issueTypeId));
                    context.add(treeManager.getRootNode());
                }

                final FieldConfigScheme defaultScheme = configSchemeManager.createDefaultScheme(customField,context, issueTypes);
                FieldConfig config = (FieldConfig) defaultScheme.getConfigsByConfig().keySet().iterator().next();

                // Update options
                List options = customFieldGv.getRelated("ChildCustomFieldOption");
                if (options != null && !options.isEmpty())
                {
                    log.info("Importing options");
                    for (Iterator iterator1 = options.iterator(); iterator1.hasNext();)
                    {
                        GenericValue optionGv = (GenericValue) iterator1.next();
                        optionGv.set(DefaultOptionsManager.ENTITY_CONFIG_ID, config.getId());
                        optionGv.store();
                    }
                }

                // Deal with Defaults
                try
                {
                    log.info("Importing default values");
                    Map defaultFields = EasyMap.build("valuetype","DEFAULT",
                                                      "customfield", customFieldGv.getLong("id"));
                    List defaultValues = delegator.findByAnd("CustomFieldValue", defaultFields);
                    if (defaultValues != null && !defaultValues.isEmpty())
                    {
                        if (customField.getCustomFieldType() instanceof AbstractSingleFieldType)
                        {
                            // single fields
                            GenericValue defaultGv = (GenericValue) defaultValues.iterator().next();
                            Object defValue = getValue(defaultGv, customField);

                            genericConfigManager.create(CustomFieldType.DEFAULT_VALUE_TYPE, config.getId().toString(), defValue);
                        }
                        else if (customField.getCustomFieldType() instanceof MultiSelectCFType)
                        {
                            CollectionUtils.transform(defaultValues, new Transformer(){
                                public Object transform(Object input)
                                {
                                    return getValue((GenericValue) input, customField);
                                }
                            });

                            genericConfigManager.create(CustomFieldType.DEFAULT_VALUE_TYPE, config.getId().toString(), defaultValues);
                        }
                        else if (customField.getCustomFieldType() instanceof CascadingSelectCFType)
                        {
                            Map map = new MultiHashMap();
                            for (Iterator iterator1 = defaultValues.iterator(); iterator1.hasNext();)
                            {
                                GenericValue value = (GenericValue) iterator1.next();
                                final String string = value.getString("parentkey");
                                if (string != null)
                                {
                                    map.put("1", value.getString("stringvalue"));
                                }
                                else
                                {
                                    map.put(null, value.getString("stringvalue"));
                                }
                            }

                            CustomFieldParams cfp = new CustomFieldParamsImpl(null, map);

                            genericConfigManager.create(CustomFieldType.DEFAULT_VALUE_TYPE, config.getId().toString(), cfp);
                        }

                    }
                }
                catch (Exception e)
                {
                    log.info("Default values for " + customField + " failed to import. ", e);
                }

            }
            else
            {
                log.info("Custom field type for " + customFieldGv + " is not valid. " +
                         "CustomFieldType: " + customField.getCustomFieldType());
            }
        }

        customFieldManager.refresh();
    }

    private Object getValue(GenericValue gv, CustomField customField)
    {
        Object value;

        if (StringUtils.isNotEmpty(gv.getString(OfBizCustomFieldValuePersister.FIELD_TYPE_STRING)))
        {
            value = gv.getString(OfBizCustomFieldValuePersister.FIELD_TYPE_STRING);
        }
        else if (StringUtils.isNotEmpty(gv.getString(OfBizCustomFieldValuePersister.FIELD_TYPE_TEXT)))
        {
            value = gv.getString(OfBizCustomFieldValuePersister.FIELD_TYPE_TEXT);
        }
        else if (gv.get(OfBizCustomFieldValuePersister.FIELD_TYPE_DATE) != null)
        {
            value = gv.get(OfBizCustomFieldValuePersister.FIELD_TYPE_DATE);
        }
        else if (gv.getDouble(OfBizCustomFieldValuePersister.FIELD_TYPE_NUMBER) != null)
        {
            value = gv.getDouble(OfBizCustomFieldValuePersister.FIELD_TYPE_NUMBER);
        }
        else
        {
            log.info("Default value is blank");
            value = null;
        }

        return value;
    }
}
