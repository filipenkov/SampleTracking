package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This function returns a list of projects where the user has the requested permission.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used as
 * component lead.
 *
 * @since v4.2
 */
public class ProjectsWhereUserHasPermissionFunction extends AbstractUserCapabilityFunction
{
    public static final String FUNCTION_PROJECTS_WHERE_USER_HAS_PERMISSION = "projectsWhereUserHasPermission";
    private static final String JIRA_JQL_PROJECT_NO_SUCH_USER = "jira.jql.project.no.such.user";

    private final PermissionManager permissionManager;
    private final SchemePermissions schemePermissions;

    public ProjectsWhereUserHasPermissionFunction(final PermissionManager permissionManager, final SchemePermissions schemePermissions, final UserUtil userUtil)
    {
        super(userUtil);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.schemePermissions = notNull("schemePermissions", schemePermissions);
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.PROJECT;
    }

    protected MessageSet validateCapability(final String permissionName, final I18nHelper i18n)
    {
        MessageSet messageSet = new MessageSetImpl();
        // Check the permission requested exists
        if (getPermissionByName(permissionName) == null)
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.project.no.such.permission", getFunctionName(), permissionName));
        }
        return messageSet;
    }

    private Map.Entry<Integer, Permission> getPermissionByName(String name)
    {
        for (Map.Entry<Integer, Permission> permissionEntry : schemePermissions.getSchemePermissions().entrySet())
        {
            if (permissionEntry.getValue().getName().equalsIgnoreCase(name))
            {
                return permissionEntry;
            }
        }
        return null;
    }

    protected List<QueryLiteral> getFunctionValuesList(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final User user, final String permissionName)
    {
        Map.Entry<Integer, Permission> permissionEntry = getPermissionByName(permissionName);
        if (permissionEntry == null)
        {
            return Collections.emptyList();
        }

        List<QueryLiteral> values = new ArrayList<QueryLiteral>();

        Collection<Project> projects = permissionManager.getProjectObjects(permissionEntry.getKey(), user);
        for (Project project : projects)
        {
            if (!queryCreationContext.isSecurityOverriden())
            {
                if (queryCreationContext.isSecurityOverriden() || permissionManager.hasPermission(Permissions.BROWSE, project, queryCreationContext.getQueryUser()))
                {
                    values.add(new QueryLiteral(functionOperand, project.getId()));
                }
            }
        }

        return values;
    }

    protected String getUserNotFoundMessageKey()
    {
        return JIRA_JQL_PROJECT_NO_SUCH_USER;
    }
}
