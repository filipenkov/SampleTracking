package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.WorkflowContext;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Useful methods for JIRA OSWorkflow conditions and functions. Use the subclasses for real implementations.
 */
public class WorkflowFunctionUtils
{
    public static final String ORIGINAL_ISSUE_KEY = "originalissueobject";
    /** @deprecated - typo in name, please use ORIGINAL_ISSUE_KEY. Deprecated since v4.0 */
    public static final String ORIGNAL_ISSUE_KEY = "originalissueobject";
    private static final Logger log = Logger.getLogger(WorkflowFunctionUtils.class);

    /**
     * Get the name of the user executing this condition.
     * @return Username, or null if run anonymously.
     */
    protected String getCallerName(Map transientVars, Map args)
    {
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        String username = (String) args.get("username");
        if (!TextUtils.stringSet(username))
        {
            username = context.getCaller();
        }
        return username;
    }

    /**
     * Get the {@link User} executing this condition.
     * @return The User, or null if run anonymously.
     */
    protected User getCaller(Map transientVars, Map args)
    {
        String username = getCallerName(transientVars, args);
        if (username == null)
            return null;

        return UserUtils.getUser(username);
    }
}
