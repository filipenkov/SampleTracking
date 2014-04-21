package com.atlassian.jira.workflow;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WorkflowUtil
{
    private static final Logger log = Logger.getLogger(WorkflowUtil.class);

    /**
     * This method adds to an existing list stored in the transient args map.
     * <p/>
     * If the existing list does not exist, the new list is just added -
     * otherwise the new list is added to the old list, and the result readded
     * to the transientArgs map.
     */
    public static void addToExistingTransientArgs(final Map transientArgs, final String key, final List list)
    {
        final List existingList = (List) transientArgs.get(key);

        if (existingList == null)
        {
            transientArgs.put(key, list);
        }
        else
        {
            existingList.addAll(list);
            transientArgs.put(key, existingList);
        }
    }

    /**
     * Get the next usable ID value for a given list of descriptors.
     */
    public static int getNextId(final List descriptors)
    {
        return getNextId(descriptors, 1);
    }

    /**
     * Get the next usable ID value for a given list of descriptors and a start point.
     */
    public static int getNextId(final List descriptors, final int start)
    {
        int maxId = start;
        for (final Iterator iterator = descriptors.iterator(); iterator.hasNext();)
        {
            final AbstractDescriptor descriptor = (AbstractDescriptor) iterator.next();
            if (descriptor.getId() >= maxId)
            {
                maxId = descriptor.getId() + 1;
            }
        }

        return maxId;
    }

    /**
     * Variable interpolation. Eg. given a project TestProject and groupName '${pkey}-users', will return 'TP-users', or null if groupName is null
     *
     * @deprecated Use {@link #replaceProjectKey(com.atlassian.jira.project.Project, String)} instead. Since v5.0.
     */
    public static String interpolateProjectKey(final GenericValue project, String groupName)
    {
        if ((groupName != null) && (groupName.indexOf("${") != -1) && (groupName.indexOf("}") != -1))
        {
            groupName = groupName.substring(0, groupName.indexOf("${")) + project.getString("key") + groupName.substring(groupName.indexOf("}") + 1);
        }
        return groupName;
    }

    /**
     * Replaces ${pkey} in the given groupName with the given Project's key.
     *
     * Eg. given a project TestProject(key="TP") and groupName '${pkey}-users', will return 'TP-users', or null if groupName is null
     *
     * TODO: this seems like a hangover from before Project Roles - can we remove this?
     */
    public static String replaceProjectKey(final Project project, String groupName)
    {
        if (groupName == null)
        {
            return null;
        }

        int index = groupName.indexOf("${pkey}");
        if (index == -1)
        {
            return groupName;
        }
        return groupName.substring(0, index) + project.getKey() + groupName.substring(index + "${pkey}".length());
    }

    /**
     * Return a meta attribute applying to a whole workflow (ie. right under the <workflow> start tag).
     */
    public static String getGlobalMetaAttributeForIssue(final GenericValue issue, final String metaKey)
    {
        JiraWorkflow issueWorkflow = null;
        try
        {
            issueWorkflow = getWorkflowManager().getWorkflow(issue);
        }
        catch (final WorkflowException e)
        {
            throw new RuntimeException("Could not get workflow for issue " + issue);
        }
        final String metaValue = (String) issueWorkflow.getDescriptor().getMetaAttributes().get(metaKey);
        return interpolate(metaValue, issue);
    }

    /**
     * Return a workflow meta attribute for the current state of an issue.
     */
    public static String getMetaAttributeForIssue(final GenericValue issue, final String metaKey)
    {
        final String metaValue = (String) getMetaAttributesForIssue(issue).get(metaKey);
        return interpolate(metaValue, issue);
    }

    /**
     * Return all meta attribute values whose key starts with a certain prefix. For example, given:
     * <meta name="jira.status.id">3</meta>
     * <meta name="jira.permission.subtasks.comment.group">jira-qa</meta>
     * <meta name="jira.permission.subtasks.comment.group.1">jira-administrators</meta>
     * <p/>
     * Prefix 'jira.permission.subtasks.comment.group' would return {'jira-qa', 'jira-administrators'}.
     * Unfortunately OSWorkflow does not allow multiple meta attributes with the same name.
     */
    public static List getMetaAttributesForIssue(final GenericValue issue, final String metaKeyPrefix)
    {
        final Map metaAttributes = getMetaAttributesForIssue(issue);
        final List results = new ArrayList(metaAttributes.size());
        final Iterator iter = metaAttributes.keySet().iterator();
        while (iter.hasNext())
        {
            final String key = (String) iter.next();
            if (key.startsWith(metaKeyPrefix))
            {
                results.add(interpolate((String) metaAttributes.get(key), issue));
            }
        }
        return results;
    }

    /**
     * Get all meta attributes for an issue's current state.
     */
    public static Map getMetaAttributesForIssue(final GenericValue issue)
    {
        StepDescriptor stepDesc = null;
        try
        {
            stepDesc = WorkflowUtil.getStepDescriptorForIssue(issue);
        }
        catch (final WorkflowException e)
        {
            throw new RuntimeException("Could not get workflow for issue " + issue);
        }
        final Map metaAttributes = stepDesc.getMetaAttributes();
        if (metaAttributes == null)
        {
            throw new RuntimeException("Null meta attributes");
        }
        return metaAttributes;
    }

    /**
     * Converts a {@link com.opensymphony.workflow.loader.WorkflowDescriptor} to XML.
     *
     * @param descriptor The {@link com.opensymphony.workflow.loader.WorkflowDescriptor} to convert
     * @return An XML representation of the workflowdescritpor passed in.
     */
    public static String convertDescriptorToXML(final WorkflowDescriptor descriptor)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        writer.println(WorkflowDescriptor.XML_HEADER);
        writer.println(WorkflowDescriptor.DOCTYPE_DECL);
        descriptor.writeXML(writer, 0);
        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    private static String interpolate(final String metaValue, final GenericValue issue)
    {
        if ((metaValue != null) && (metaValue.indexOf("${") != -1))
        {
            GenericValue project = null;
            project = ComponentAccessor.getProjectManager().getProject(issue);
            return WorkflowUtil.interpolateProjectKey(project, metaValue);
        }
        else
        {
            return metaValue;
        }
    }

    public static boolean isAcceptableName(final String workflowName)
    {
        if (workflowName == null)
        {
            return false;
        }

        return StringUtils.isStringAllASCII(workflowName);
    }

    /**
     * Retrieves a descriptor from the workflow definition for this issue's current state.
     */
    private static StepDescriptor getStepDescriptorForIssue(final GenericValue issue) throws WorkflowException
    {
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("Cannot get step descriptor for non-issue (" + issue + ")");
        }
        final JiraWorkflow issueWorkflow = getWorkflowManager().getWorkflow(issue);
        return issueWorkflow.getLinkedStep(ComponentAccessor.getConstantsManager().getStatus(issue.getString("status")));
    }

    /**
     * JRA-4429 (prevent invalid characters)
     */
    public static void checkInvalidCharacters(final String fieldValue, final String fieldName, final ErrorCollection errorCollection)
    {
        if (fieldValue.indexOf('<') != -1)
        {
            errorCollection.addError(fieldName, getI18nBean().getText("admin.errors.invalid.character", "'<'"));
        }

        if (fieldValue.indexOf('&') != -1)
        {
            errorCollection.addError(fieldName, getI18nBean().getText("admin.errors.invalid.character", "'&'"));
        }

        // JRA-5733 - '"' is also invalid
        if (fieldValue.indexOf('"') != -1)
        {
            errorCollection.addError(fieldName, getI18nBean().getText("admin.errors.invalid.character", "'\"'"));
        }
    }

    /**
     * Converts a string representation of a workflow XML into the {@link com.opensymphony.workflow.loader.WorkflowDescriptor}
     * object representation.
     *
     * @param workflowDescriptorXML the XML representation of an OSWorkflow
     * @return the {@link com.opensymphony.workflow.loader.WorkflowDescriptor} that represents the workflow.
     * @throws FactoryException thrown if the XML is malformed or can not be converted to the object representation.
     */
    public static WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String workflowDescriptorXML) throws FactoryException
    {
        if (org.apache.commons.lang.StringUtils.isEmpty(workflowDescriptorXML))
        {
            throw new FactoryException("Error: workflow descriptor XML can not be null.");
        }

        InputStream is = null;
        try
        {
            is = new ByteArrayInputStream(workflowDescriptorXML.getBytes("UTF-8"));
            // The descriptor XML has encoding hard-coded to UTF-8, so convert the descriptor to UTF-8 bytes
            return WorkflowLoader.load(is, true);
        }
        catch (final Exception e)
        {
            throw new FactoryException("Error converting XML to workflow descriptor.", e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (final IOException e)
                {
                    log.warn("Error closing stream, while converting XML to workflow descriptor.", e);
                }
            }
        }
    }

    /**
     * Appends "(Draft)" to the end of the workflow name for an draft workflow.
     *
     * @param workflow The workflow to create the display name for.
     * @return A String with the workflow name plus an optional (Draft).
     */
    public static String getWorkflowDisplayName(final JiraWorkflow workflow)
    {
        if (workflow == null)
        {
            return null;
        }

        if (workflow.isDraftWorkflow())
        {
            return workflow.getName() + " (" + getI18nBean().getText("common.words.draft") + ")";
        }
        return workflow.getName();
    }

    /**
     * Creates a name to be used for a copy of a given workflow.
     *
     * @param currentName The name of the current workflow.
     * @return A name for the copy of the current workflow.
     *
     * @deprecated Since 5.1. Use {@link com.atlassian.jira.workflow.names.WorkflowCopyNameFactory} instead.
     */
    @Deprecated
    public static String cloneWorkflowName(final String currentName)
    {
        return getWorkflowCopyNameFactory().createFrom(currentName, getAuthenticationContext().getLocale());
    }

    private static JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    private static WorkflowCopyNameFactory getWorkflowCopyNameFactory()
    {
        return ComponentAccessor.getComponent(WorkflowCopyNameFactory.class);
    }

    public static WorkflowManager getWorkflowManager()
    {
        return ComponentAccessor.getComponentOfType(WorkflowManager.class);
    }

    private static I18nHelper getI18nBean()
    {
        return getAuthenticationContext().getI18nHelper();
    }

    /**
     * Get the translated display name of a workflow transition.
     *
     * @param descriptor The action descriptor to get the name of
     * @return The name of the transition.
     */
    public static String getWorkflowTransitionDisplayName(final ActionDescriptor descriptor)
    {
        if(descriptor == null)
        {
            return getI18nBean().getText("common.words.unknown");
        }
        final Map<String, Object> metadata = descriptor.getMetaAttributes();
        if (metadata.containsKey(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N))
        {
            final String key = (String) metadata.get(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N);
            final String value = getI18nBean().getText(key);
            if ((value != null) && !"".equals(value.trim()) && !value.trim().equals(key.trim()))
            {
                return value;
            }
        }
        return descriptor.getName();
    }

    /**
     * Get the translated description of the workflow transition.
     *
     * @param descriptor The action descriptor to get the description of
     * @return the translated description of the workflow transition.
     */
    public static String getWorkflowTransitionDescription(final ActionDescriptor descriptor)
    {
        return (String) descriptor.getMetaAttributes().get("jira.description");
    }

    /**
     * Given a map of transientVars from a Workflow Function, returns the username of the caller.
     *
     * @param transientVars the "transientVars" from the workflow FunctionProvider
     * @return the username of the caller (can be null for anonymous).
     *
     * @since 4.4
     *
     * @see com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)
     * @see com.opensymphony.workflow.WorkflowContext#getCaller()
     * @see #getCaller(java.util.Map)
     */
    public static String getCallerName(Map transientVars)
    {
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        return context.getCaller();
    }

    /**
     * Given a map of transientVars from a Workflow Function, returns the User object of the caller.
     *
     * @param transientVars the "transientVars" from the workflow FunctionProvider
     * @return the username of the caller (can be null for anonymous).
     *
     * @since 4.4
     *
     * @see com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)
     * @see com.opensymphony.workflow.WorkflowContext#getCaller()
     * @see #getCallerName(java.util.Map)
     */
    public static User getCaller(Map transientVars)
    {
        String username = getCallerName(transientVars);
        if (username == null)
        {
            return null;
        }
        else
        {
            return ComponentAccessor.getUserManager().getUserObject(username);
        }
    }
}