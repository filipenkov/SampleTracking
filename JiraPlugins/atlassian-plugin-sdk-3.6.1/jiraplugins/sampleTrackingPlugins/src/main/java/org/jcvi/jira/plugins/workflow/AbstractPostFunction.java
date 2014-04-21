package org.jcvi.jira.plugins.workflow;

import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

/**
 * This extends JIRAs PostFunction implementation by changing the called method
 * to take a JIRAWorkflowState object that provides a much clearer interface
 * to the workflow than the original three untyped maps interface did.
 *
 */
public abstract class AbstractPostFunction extends AbstractJiraFunctionProvider {
    //--------------------------------------------------------------------------
    // FunctionProvider interface
    //--------------------------------------------------------------------------

    /**
     * This is the only function that needs to be implemented
     * The three inputs each have different life-cycles and scopes.
     * <table>
     *     <tr><td>transientVariables</td><td>transition/Issue</td></tr>
     *     <tr><td>arguments</td><td>function instance parameters.
     *     By default this only contains "class.name" a String containing
     *     the name of the class.</td></tr>
     *     <tr><td>propertySet</td><td>workflow instance. Normally empty</td></tr>
     * </table>
     * <b>transientVariables</b> can be used to gain access to the Issue.
     * The issue object has its own life-cycle.
     * <b>propertySet</b> This could possibly be used to pass information
     * between workflow plugins without adding fields to the Issue.
     * @see TransientVariableTypes
     * @param transientVariables    information about the current transition
     * @param args                  the configuration?
     * @param propertySet           workflow instance data
     * @throws WorkflowException
     */
    @Override
    public void execute(Map transientVariables, Map args, PropertySet propertySet) throws WorkflowException {
        //suppresses the cast of Map to Map<Object,Object> warning
        //noinspection unchecked
        execute(new JIRAWorkflowState<WorkflowFunctionModuleDescriptor>(
                                transientVariables,
                                args,
                                propertySet,
                                WorkflowFunctionModuleDescriptor.class));
    }

    /**
     * <p>This is a template method that should be used by extending classes
     * instead of the execute(Map,Map,PropertySet) method.
     * Before this is called the JIRAWorkflowState object is created to
     * simplify accessing workflow and JIRA data.</p>
     * <p>Implementations should assume that this object may be in use for
     * multiple instances of the plug-in.</p>
     * <p>Implementations should be thread-safe</p>
     * @param state A DAO that provides access the information from JIRA and the workflow
     * @throws WorkflowException    If the transition needs to be blocked?
     */
    protected abstract void execute(JIRAWorkflowState<WorkflowFunctionModuleDescriptor> state) throws WorkflowException;
}
