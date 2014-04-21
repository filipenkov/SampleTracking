package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.rest.api.v2.customfield.CustomFieldMarshaller;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Module descriptor for the &lt;customfield-marshaller&gt; plugin module type that the JIRA REST plugin introduces.
 *
 * @since v4.2
 */
class MarshallerModuleDescriptor extends AbstractModuleDescriptor<CustomFieldMarshaller>
{
    /**
     * The name of the customfield element.
     */
    private static final String CUSTOMFIELD_ELEM = "customfield";

    /**
     * Logger for this class.
     */
    private final Logger log = LoggerFactory.getLogger(MarshallerModuleDescriptor.class);

    /**
     * The complete keys of the custom field type.
     */
    private List<String> customFieldTypes = new ArrayList<String>();

    public MarshallerModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);

        // add all the custom fields to the list
        @SuppressWarnings("unchecked")
        List<Element> elements = element.elements(CUSTOMFIELD_ELEM);
        if (elements.isEmpty())
        {
            log.warn("<customfield-marshaller> does not contain any <{}> child elements: {}", CUSTOMFIELD_ELEM, element);
            return;
        }

        for (Element customField : elements)
        {
            String pkg = customField.attributeValue("package");
            String key = customField.attributeValue("key");
            if (key == null) {
                log.warn("<{}> element is missing required 'key' attribute: {}", CUSTOMFIELD_ELEM, customField);
            }

            // use the plugin package if not specified
            customFieldTypes.add((pkg != null ? pkg : plugin.getKey()) + ":" + key);
        }
    }

    /**
     * Ensures that the module class is an instance of CustomFieldMarshaller.
     */
    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(CustomFieldMarshaller.class);
    }

    /**
     * Returns a new instance of the module class.
     *
     * @return a new instance of the module class
     */
    @Override
    public CustomFieldMarshaller getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }

    /**
     * Returns the complete key of the custom field that this module refers to.
     *
     * @return a String containing the complete key of a custom field
     */
    public List<String> getCustomFieldKey()
    {
        return customFieldTypes;
    }
}
