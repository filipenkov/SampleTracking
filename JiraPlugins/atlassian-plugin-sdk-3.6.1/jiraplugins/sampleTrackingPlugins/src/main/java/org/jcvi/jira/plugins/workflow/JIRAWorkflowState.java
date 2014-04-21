package org.jcvi.jira.plugins.workflow;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jcvi.jira.plugins.utils.DebugLogging;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class wraps the various forms of data passed to an OSWorkflow plugin
 * and provides methods to access the JIRA objects referenced.
 *
 * The calls to OSWorkflow plugins all take the same three parameters:
 * <TABLE>
 *     <TH><TD>Variable          </TD><TD>Contents                          </TD><TD>Scope</TD></TH>
 *     <TR><TD>transientVariables</TD><TD>Data associated with this call    </TD><TD>the call, although items in it may have longer life cycles e.g. Issue</TD></TR>
 *     <TR><TD>arguments         </TD><TD>function instance parameters. By
 *     default this only contains "class.name"                              </TD><TD>A particular reference to the plug-in within the workflow definition.</TD></TR>
 *     <TR><TD>propertySet      </TD><TD>Normally empty                     </TD><TD>A particular workflow instance</TD></TR>
 * </TABLE>
 *
 * The JIRA objects that can be accessed are:
 * <UL>
 *     <LI>Resource: A file or embedded block from the JIRA Plug-in definition</LI>
 * </UL>
 * todo: continue the list
 */
public class JIRAWorkflowState<WORKFLOW_MODULE_DESCRIPTOR extends AbstractWorkflowModuleDescriptor<?>> {
    private static final Logger log = Logger.getLogger(JIRAWorkflowState.class);
    private Class<WORKFLOW_MODULE_DESCRIPTOR> descriptorClass;
    private WORKFLOW_MODULE_DESCRIPTOR descriptor = null;
    private MutableIssue issue = null;
    private Map<TransientVariableTypes,Object> storedTransientVariables = null;
    private Map<String,String> arguments = null;

    /**
     * Create the DAO. See the class's description for information about the
     * parameters.
     * @param transientVariables    information about the current transition
     * @param args                  the configuration
     * @param propertySet           workflow instance data
     * @throws com.opensymphony.workflow.WorkflowException
     */
    public JIRAWorkflowState(Map transientVariables,
                             Map<Object,Object> args,
                             PropertySet propertySet,
                             Class<WORKFLOW_MODULE_DESCRIPTOR> descriptorType) throws WorkflowException {
        this.descriptorClass = descriptorType;

        initArguments(args); //only read methods are used and so the cast to Object is safe
        initTransientVariables(transientVariables);
        //propertySet currently isn't used  and so has no init method

        initIssue(transientVariables);
        initDescriptor(); //requires initArguments first
        initLogLevel(); //requires initDescriptor first

        if (log.isDebugEnabled()) {
            //find-out what is in the various maps
            log.debug(DebugLogging.getLogMessageFor("transientVariables", transientVariables));
            log.debug(DebugLogging.getLogMessageFor("arguments",          arguments));
            log.debug(DebugLogging.getLogMessageFor("propertySet",        propertySet));
            if (descriptor != null) {
                log.debug(descriptor.getDescription());
            } else {
                log.debug("No descriptor found");
            }
        }
    }

    /**
     * Read in a resource from the plug-in's definition in atlassian-plugin.xml
     * WARNING: There is no limit on the size of String object that could be
     * produced by this method. This could potentially be used to produce an
     * out-of-memory attack. To avoid this would require writing a version of
     * BufferedReader.readLine that took a max number of characters to read
     * and the risk doesn't seem large enough to justify the time.
     * @param type  a string matching the type parameter in the resource
     *              element
     * @param name  a string matching the name parameter in the resource
          *         element
     * @return The contents of the resource pointed to or
     *         The contents of the element if no location parameter exists or
     *         NULL if the resource is not found or
     *         NULL if an exception occurs
     * @throws java.io.IOException  If the resource exceeds the maximum size limit.
     *
     */
    public String readResource(String type, String name)
            throws IOException {
        //use the reader version of this method
        Reader rawReader = getResource(type,name); //may throw IOException

        String fileContent;

        if (rawReader == null) {
            log.debug("Resource not found");
            fileContent = null;
        } else {
            BufferedReader reader = new BufferedReader(rawReader);
            //copy the reader into a String
            fileContent = "";
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    fileContent += line + "\n";
                }
            } catch (IOException ioe) {
                log.debug("Failed while copying the reader into a string");
                IOUtil.shutdownReader(reader);
                throw ioe; //pass the exception back up the call chain
            }
        }
        return fileContent;
    }

    public Reader getResource(String type, String name)
            throws IOException {
        Reader reader = null;
        //try getting some resources
        ResourceDescriptor resourceDescriptor =
                getDescriptor().getResourceDescriptor(type, name);
        if (resourceDescriptor == null) {
            log.debug("Didn't find resource");
        } else {
            String location = resourceDescriptor.getLocation();
            if (!TextUtils.stringSet(location)) {
                //no location, see if the element has contents
                log.debug("Using resource content instead of location");
                reader = new StringReader(resourceDescriptor.getContent());
            } else {
                log.debug("Reading resource from location: "+location);
                URL url = ClassLoaderUtils.getResource(location, this.getClass());
                InputStream in = url.openStream();
                if (in == null) {
                    throw new FileNotFoundException(location);
                }
                reader = new InputStreamReader(in);
            }
        }
        return reader;
    }
    public WORKFLOW_MODULE_DESCRIPTOR getDescriptor() {
        return descriptor;
    }

    public MutableIssue getIssue() {
        return issue;
    }

    /**
     * Gets a String value from a named field of the Issue. The field
     * could be a JIRA field or a customField. CustomFields can be
     * reference by their id, name or customfield_(id).
     * @param name The identifier defining the field to use
     * @return A single Object, or null, of the type the field uses as a TRANSPORT OBJECT
     */
    public String getIssueFieldValue(String name) {
        CustomFieldManager customFieldManager
                = ComponentManager.getInstance().getCustomFieldManager();
        CustomField customField = customFieldManager.getCustomFieldObjectByName(name);
        if (customField == null) {
            customField = customFieldManager.getCustomFieldObject(name);
        }
        if (customField == null) {
            //in-case it is the id number
            try {
                customField = customFieldManager.getCustomFieldObject("customfield_" + name);
            } catch (NumberFormatException nfe) {
                //it wasn't an id then.
                //due to the generally low standard of programming in JIRA
                //it is assumed that any field starting with 'customfield_' can be
                //parsed as ending in a number.
            }
        }
        if (customField != null) {
            Object value = customField.getValue(getIssue());
            if (value != null) {
                return value.toString();
            }
            return null;
        }

        try {
            //no customfield found, try built-in fields
            //see IssueFieldConstants for the valid values
            String value = getIssue().getString(name);
            if (value != null) {
                return value;
            }
        } catch (IllegalArgumentException iae) {
            //if it isn't part of issue this is thrown
        }

        //not found
        return null;
    }

    public Object getTransientVariable(TransientVariableTypes varToGet) {
        return storedTransientVariables.get(varToGet);
    }

    public String getArgument(String name) {
        return arguments.get(name);
    }

    public boolean getBooleanArgument(String name) {
        String stringValue = getArgument(name);

        return !(stringValue == null) &&
            ("TRUE".equalsIgnoreCase(stringValue.trim()));
    }

    /**
     * This method retrieves the plugin key from the WorkflowFunction's arguments.
     * It should not be used with the DescriptorParams map which has a different
     * internal structure.
     * @return  The value of the key, if any is found.
     */
    public String getResourceKey() {
        return TypeMapperUtils.safeToString(
                getArgument(AddArgsWorkflowPluginFactory.PLUGIN_RESOURCE_KEY));
    }


    //--------------------------------------------------------------------------
    // util methods
    //--------------------------------------------------------------------------
    protected void initLogLevel() {
        WORKFLOW_MODULE_DESCRIPTOR descriptor = getDescriptor();
        //if the descriptor was null then initDescriptor would have
        //thrown an exception
        Object value = descriptor.getParams().get("debug");
        String strValue = TypeMapperUtils.safeToString(value);
        if( strValue != null &&
             ("YES".equalsIgnoreCase(strValue.trim()) ||
             "TRUE".equalsIgnoreCase(strValue.trim()))) {
            log.setLevel(Level.DEBUG);
        }


    }

    protected void initDescriptor() throws WorkflowException {
        if (descriptor == null) {
            //if the descriptor hasn't been found
            descriptor = AddArgsWorkflowPluginFactory.getDescriptorFromKey(
                            getResourceKey(), descriptorClass);
        }
        if (descriptor == null) {
            //try again using our class
            descriptor = AddArgsWorkflowPluginFactory.getDescriptorFromImplementationClass(this.getClass(),descriptorClass);
        }
        if (descriptor == null) {
            throw new WorkflowException("No descriptor found");
        }
    }

    protected void initIssue(Map transientVariables) throws WorkflowException {
        this.issue = getIssue(transientVariables);
        if (issue == null) {
            throw new WorkflowException("The issue could not be accessed");
        }
    }

    protected void initTransientVariables(Map transientVariables) {
        Map<TransientVariableTypes,Object> objectMap =
                new HashMap<TransientVariableTypes, Object>();
        for(Object key: transientVariables.keySet()) {
            String keyName = TypeMapperUtils.safeToString(key);
            TransientVariableTypes var = TransientVariableTypes.getByName(keyName);
            if (var != null) {
                objectMap.put(var, transientVariables.get(key));
            } else {
                log.warn("Unknown transient variable: "+keyName+
                        " ("+safeGetClassName(transientVariables.get(key))+")");
            }
        }
        storedTransientVariables = objectMap;
    }

    private String safeGetClassName(Object o) {
        if (o == null) {
            return "NULL";
        }
        return o.getClass().getName();
    }

    protected void initArguments(Map<Object,Object> args) {
        //all parameters should be String,String
        arguments = TypeMapperUtils.mapMap(
                                new TypeMapperUtils.StringMapper<Object>(),
                                new TypeMapperUtils.StringMapper<Object>(),
                                args);
    }

    /**
     * WARNING LIFTED FROM AbstractJiraFunctionProvider to be used with all
     * JIRA Workflow Plugins. Extended with some extra error handling.
     *
     * This method retrieves the (potentially modified) issue object that is
     * being transitioned through workflow.
     *
     * @param transientVars A String to Object map containing various meta-data
     *                      about the workflow instance
     * @return              The issue object representing the issue the
     *                      functions should modify
     * @throws com.atlassian.jira.exception.DataAccessException
     *                      If for some reason the issue doesn't exist, or
     *                      the workflow lacks the information required to
     *                      find it.
     *
     */
    protected MutableIssue getIssue(Map transientVars) throws DataAccessException {
        MutableIssue issue = (MutableIssue) transientVars.get(TransientVariableTypes.ISSUE_OBJECT.getName());
        if (issue == null) {
            WorkflowEntry entry = (WorkflowEntry) transientVars.get(TransientVariableTypes.ENTRY.getName());
            if (entry == null) {
                throw new DataAccessException("No '"+ TransientVariableTypes.ENTRY.getName()+"' associated with the workflow's meta-data. Cannot find associated Issue without it.");
            }
            try {
                issue = ComponentAccessor.getIssueManager().getIssueObjectByWorkflow(entry.getId());
            } catch (GenericEntityException e) {
                throw new DataAccessException("Problem looking up issue with workflow entry id "+entry.getId());
            }
            if (issue == null) {
                throw new DataAccessException("No issue found with workflow entry id "+entry.getId());
            }
        }
        return issue;
    }
}
