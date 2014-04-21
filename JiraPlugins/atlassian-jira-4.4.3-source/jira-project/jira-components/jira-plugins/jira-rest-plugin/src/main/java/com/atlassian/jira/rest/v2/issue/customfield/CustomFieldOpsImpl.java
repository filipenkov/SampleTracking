package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.rest.api.field.FieldBean;
import com.atlassian.jira.rest.api.v2.customfield.CustomFieldMarshaller;
import com.atlassian.plugin.PluginAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * This class implements custom-field related functionality in the REST plugin.
 *
 * @since v4.2
 */
class CustomFieldOpsImpl implements CustomFieldOps
{
    /**
     * Logger for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(CustomFieldOpsImpl.class);

    /**
     * The CustomFieldManager dependency.
     */
    private final CustomFieldManager cfMgr;

    /**
     * The marshaller service.
     */
    private final MarshallerServiceImpl marshaller;

    /**
     * The PluginAccessor instance used to lookup other plugins.
     */
    private final PluginAccessor pluginAccessor;

    /**
     * Creates a new CustomFieldOps instance, passing all required dependencies.
     *
     * @param customFieldManager a CustomFieldManager
     * @param marshaller a MarshallerService
     * @param pluginAccessor a PluginAccessor
     */
    public CustomFieldOpsImpl(CustomFieldManager customFieldManager, MarshallerServiceImpl marshaller, PluginAccessor pluginAccessor)
    {
        this.cfMgr = customFieldManager;
        this.marshaller = marshaller;
        this.pluginAccessor = pluginAccessor;
    }


    @SuppressWarnings ("unchecked")
    public Map<String, FieldBean> getCustomFields(Issue issue)
    {
        logger.trace("Looking up installed marshallers");
        Map<String, CustomFieldMarshaller> installedMarshallers = lookupMarshallers();

        logger.trace("Starting to marshall custom fields for {}", issue);
        List<CustomField> cfs = cfMgr.getCustomFieldObjects(issue);
        Map<String, FieldBean> customFieldsByName = new HashMap<String, FieldBean>();
        for (CustomField customField : cfs)
        {
            customFieldsByName.put(customField.getId(), marshaller.marshall(customField, issue, installedMarshallers));
        }

        logger.trace("Finished marshalling custom fields for {}: {}", issue, customFieldsByName);
        return customFieldsByName;
    }

    /**
     * Returns the custom field marshallers that are currently enabled.
     *
     * @return a Map of custom field name to CustomFieldMarshaller
     */
    protected Map<String, CustomFieldMarshaller> lookupMarshallers()
    {
        Collection<MarshallerModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(MarshallerModuleDescriptor.class);

        Map<String, CustomFieldMarshaller> result = new HashMap<String, CustomFieldMarshaller>(descriptors.size());
        for (MarshallerModuleDescriptor descriptor : descriptors)
        {
            for (String customFieldKey : descriptor.getCustomFieldKey())
            {
                CustomFieldMarshaller previous = result.put(customFieldKey, descriptor.getModule());
                if (previous != null)
                {
                    logger.error(format("Custom field key '%s' already has a marshaller. Ignoring '%s'", descriptor.getKey(), descriptor.getModule()));
                }
            }
        }

        return result;
    }
}
