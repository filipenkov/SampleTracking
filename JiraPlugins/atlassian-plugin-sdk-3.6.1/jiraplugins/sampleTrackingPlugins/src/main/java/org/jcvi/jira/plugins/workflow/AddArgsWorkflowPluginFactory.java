package org.jcvi.jira.plugins.workflow;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.workflow.*;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.apache.log4j.Logger;
import org.jcvi.jira.plugins.utils.DebugLogging;
import org.jcvi.jira.plugins.utils.JIRAFieldUtils;

import java.util.*;

/**
 * Note that despite its name WorkflowPluginFactory is not used to create
 * WorkflowPlugin objects. It is used when the post-functions of a transition
 * are created, viewed, or edited. It controls the data passed to the
 * velocity templates and the conversion of that data into the XML definition
 * that is added to the workflow XML. It is not involved in the execution of a
 * workflow.
 *
 * This class, when used with templates/org/jcvi/jira/plugins/workflow/edit.vm
 * extracts the plugin key from the atlassian-plugin.xml definition and
 * stores it as a property of the 'post-functions.function' element. The
 * value can be used later by the plugin to get its descriptor.
 *
 * All form parameters are copied from the workflow descriptor into the
 * velocity environment before edit or view.
 *
 * All form parameters are copied from the forms submission into the workflow
 * XML descriptor after input or edit.
 *
 * It also has logging to check the life-cycle of the object
 */
public class AddArgsWorkflowPluginFactory extends    AbstractWorkflowPluginFactory
                                          implements WorkflowPluginFunctionFactory,
                                                     WorkflowPluginConditionFactory {
    public static final String PLUGIN_RESOURCE_KEY = "plugin-resource-key";
    private static final Class  thisClass = AddArgsWorkflowPluginFactory.class;
    private static final Logger log = Logger.getLogger(thisClass);

    private static final Set<String> parametersToIgnore = new HashSet<String>();
    static {
        parametersToIgnore.add("alt_token");
        parametersToIgnore.add("workflowMode");

    }

    static {
        log.debug("loaded");
    }

    //a new instance is created when the plugin is loaded and
    //the first time that it is used editing a workflow
    public AddArgsWorkflowPluginFactory() {
        log.debug("created");
    }

    //--------------------------------------------------------------------------
    // WorkflowPluginFunctionFactory methods
    //--------------------------------------------------------------------------

    /**
     * Data for Create/Edit pages that doesn't depend of any existing
     * config values
     * KEY_FIELD    Resource identifier used later by the real plug-in to
     *              access data/config from plugins.xml
     * allFields    CustomFields + normalFields to allow creating a drop down
     *              list to select field.
     * @param velocityParams    Used as an output the contents of this map are
     *                          available inside the velocity template.
     */
    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        log.debug(DebugLogging.getLogMessageFor("VelocityParamsForInput",velocityParams));
        velocityParams.put("KEY_FIELD", PLUGIN_RESOURCE_KEY);
//        velocityParams.put("customFields", getCustomFields());
        velocityParams.put("allFields", JIRAFieldUtils.getFields());
    }

    /**
     * A combination of the 'Input' page data and the 'View' page data
     */
    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        log.debug(DebugLogging.getLogMessageFor("VelocityParamsForEdit", velocityParams));
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams,descriptor);
    }

    /**
     * The existing configuration information, used both for editing the values
     * and for viewing them.
     * @param velocityParams    Used as an output the contents of this map are
     *                          available inside the velocity template.
     * @param descriptor        The stored configuration information
     */
    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        log.debug(DebugLogging.getLogMessageFor("VelocityParamsForView", velocityParams));
        if (descriptor == null) {
            throw new IllegalArgumentException("Descriptor must be set.");
        }
        if (log.isDebugEnabled()) {
            log.debug(descriptor.asXML());
        }
        if (descriptor instanceof FunctionDescriptor) {
            FunctionDescriptor functionDescriptor = (FunctionDescriptor)descriptor;
            //not much that can be done about this, the api returns an untyped map
            //noinspection unchecked
            velocityParams.putAll((Map<String,Object>)functionDescriptor.getArgs());
        } else if (descriptor instanceof ConditionDescriptor) {
            //annoyingly repeated code as the two classes don't have a shared
            //interface with the getArgs method
            ConditionDescriptor conditionDescriptor = (ConditionDescriptor)descriptor;
            //noinspection unchecked
            velocityParams.putAll((Map<String, Object>) conditionDescriptor.getArgs());
        } else if (descriptor instanceof ValidatorDescriptor) {
            ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor)descriptor;
            //noinspection unchecked
            velocityParams.putAll((Map<String, Object>) validatorDescriptor.getArgs());
        } else {
            throw new IllegalArgumentException("Descriptor must be one of FunctionDescriptor, ConditionDescriptor or ValidatorDescriptor.");
        }
        velocityParams.put("allFields", JIRAFieldUtils.getFields());
    }


    /**
     * This method is passed the contents of the returned form and is expected
     * to pull out the information relevant to this plug-in's configuration.
     *
     * The basic implementation copies all of the form parameters as single
     * value string parameters.
     * If the value of the parameter is only white space then it isn't added
     * to the output map.
     * @param formParams    The map of form input names to their values
     * @return  A Map&lt;String, String&gt; of name value pairs
     */
    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        log.debug(DebugLogging.getLogMessageFor("DescriptorParams",formParams));
        Map<String,String> params = new HashMap<String, String>();

        for (String key : formParams.keySet()) {
            String value = extractSingleParam(formParams,key).trim();
            //if (value.length() > 0 &&
//                    !parametersToIgnore.contains(value)) {
                params.put(key,value);
//            }
        }

        //check that the descriptor-key is there
        if (!params.containsKey(PLUGIN_RESOURCE_KEY)) {
            throw new IllegalArgumentException("A value for '"+PLUGIN_RESOURCE_KEY+"' must be passed from the form.");
        }

        return params;
    }

    //--------------------------------------------------------------------------
    // public descriptor retrieval methods
    //--------------------------------------------------------------------------
    /**
     * This method gets the ModuleDescriptor with ImplementationClass = this.class
     * This is an option if the key for the descriptor hasn't been added to the
     * arguments.
     * Notes:
     * <p>Not thread safe</p>
     * <p>As the class is used to identify the descriptor only one descriptor
     * per class is supported.</p>
     * @return The WorkflowFunctionModuleDescriptor that matches or NULL if
     *         no match is found
     */
    public static <WORKFLOW_MODULE_DESCRIPTOR extends AbstractWorkflowModuleDescriptor<?>>
        WORKFLOW_MODULE_DESCRIPTOR getDescriptorFromImplementationClass(Class type, Class<WORKFLOW_MODULE_DESCRIPTOR> descriptorClass) {
        if (type == null) {
            return null;
        }
        if (!(type.isInstance(FunctionProvider.class))) {
            log.error("The parameter type must be a valid Workflow-Function.function-class");
        }
        //find our ModuleDescriptor(s)
        List<WORKFLOW_MODULE_DESCRIPTOR> matchingDescriptor
                = new ArrayList<WORKFLOW_MODULE_DESCRIPTOR>();
        {
            PluginAccessor pluginAccessor = ComponentManager.getInstance().getPluginAccessor();
            List<WORKFLOW_MODULE_DESCRIPTOR> allWorkflowFunctionPlugins =
              pluginAccessor.getEnabledModuleDescriptorsByClass(descriptorClass);
            for(WORKFLOW_MODULE_DESCRIPTOR currentDesc : allWorkflowFunctionPlugins) {
                //check if this is the correct descriptor
                if (type.equals(currentDesc.getImplementationClass())) {
                    matchingDescriptor.add(currentDesc);
                }
            }
        }

        //Generate Warning messages
        {
            //missing descriptor, I'm not sure how this can happen though
            if (matchingDescriptor.size() == 0) {
                log.warn("No descriptor found for class: "+type);
            //duplicate descriptors
            } else if (matchingDescriptor.size() > 1) {
                String message = "Only one descriptor per class is supported. " +
                                 "The first descriptor will be used\n";
                int descriptorNumber = 0;
                for(ModuleDescriptor currentdesc: matchingDescriptor) {
                    String name;
                    if (descriptorNumber == 0) {
                        name = "Active descriptor: ";
                    } else {
                        name = "Duplicate descriptor "+descriptorNumber;
                    }
                    message += getSummaryOfDescriptor(name,currentdesc);
                    descriptorNumber++;
                }
                log.warn(message);
            }
        }

        //return the 1st descriptor
        if (!matchingDescriptor.isEmpty()) {
            return matchingDescriptor.get(0);
        }
        return null;
    }

    /**
     * This method gets the ModuleDescriptor using the Plugin 'key' from the
     * 'atlassian-plugin.xml' file.
     * The plugin key is a combination of the 'key' parameter from the
     * 'atlassian-plugin' element and the 'key' parameter from the
     * 'workflow-function' element.
     * the element 'workflow-function' in 'atlassian-plugin.xml'.
     *
     * Example:
     * &lt; atlassian-plugin key="org.jcvi.jira.plugins.SampleTrackingPlugins"
     * ...
     *     &lt;workflow-function key="NOPWorkflowFunction"
     *                      name="A template workflow function"
     *                      class="org.jcvi.jira.plugins.workflow.AddArgsWorkflowPluginFactory"
     *                      &gt;
     * For the above element the key is
     * 'org.jcvi.jira.plugins.SampleTrackingPlugins:NOPWorkflowFunction'
     *
     * <h4>Getting and Storing the key</h4>
     * The value should be stored as an argument in the definition of the
     * post function in the workflow's XML. The argument
     * AddArgsWorkflowPluginFactory.PLUGIN_RESOURCE_KEY ("plugin-resource-key")
     * should be used.
     *
     * To set this initially the key should be transferred by the edit velocity
     * template into the definition of the post-function in the workflow XML.
     * This is done by adding the following to the velocity template used for
     * 'input-parameters' and possibly 'edit-parameters', if the
     * AddArgsWorkflowPluginFactory is used.
     * <code><input type="hidden" name="${KEY_FIELD}" value="${descriptor.completeKey}"></code>
     *
     * If a customWorkflowPluginFactory is used then 'getVelocityParamsForEdit'
     * function will need to add 'KEY_FIELD' to the environment and
     * 'getDescriptorParams' will need to copy the value from the input Map
     * to the output Map.
     *
     * <p>Not thread safe</p>
     * <p>If more than one definition has the same key but different</p>
     * @return The WorkflowFunctionModuleDescriptor that matches or NULL if
     *         no match is found
     */
    public static <WORKFLOW_MODULE_DESCRIPTOR extends AbstractWorkflowModuleDescriptor<?>>
        WORKFLOW_MODULE_DESCRIPTOR getDescriptorFromKey(String key, Class<WORKFLOW_MODULE_DESCRIPTOR> clazz) {
        if (key == null) {
            return null;
        }
        //find our ModuleDescriptor(s)
        PluginAccessor pluginAccessor = ComponentManager.getInstance().getPluginAccessor();
        List<WORKFLOW_MODULE_DESCRIPTOR> allWorkflowFunctionPlugins =
                pluginAccessor.getEnabledModuleDescriptorsByClass(clazz);
        for(WORKFLOW_MODULE_DESCRIPTOR currentDesc : allWorkflowFunctionPlugins) {
            //check if this is the correct descriptor
            if (key.equals(currentDesc.getCompleteKey())) {
                return currentDesc;
            }
        }
        return null;
    }



    //as we know nothing about our context we have to return all the customFields
    //and then filter them in velocity
//    private static List<CustomField> getCustomFields() {
//        CustomFieldManager customFieldManager
//                = ComponentManager.getInstance().getCustomFieldManager();
//        return customFieldManager.getCustomFieldObjects();
//    }


    //--------------------------------------------------------------------------
    // private debug logging methods
    //--------------------------------------------------------------------------

    private static String getSummaryOfDescriptor(String name, ModuleDescriptor descriptor) {
        return DebugLogging.wrapLine(name,"CompleteKey    = '"+ descriptor.getCompleteKey())+
               DebugLogging.wrapLine(name,"Description    = '"+ descriptor.getDescription());
    }
}
