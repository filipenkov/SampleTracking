package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This function returns a list of components lead by a user.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used as
 * component lead.
 *
 * @since v4.2
 */
public class ComponentsLeadByUserFunction extends AbstractUserBasedFunction
{
    public static final String FUNCTION_COMPONENTS_LEAD_BY_USER = "componentsLeadByUser";
    private static final String JIRA_JQL_COMPONENT_NO_SUCH_USER = "jira.jql.component.no.such.user";

    private final PermissionManager permissionManager;
    private final ProjectComponentManager componentManager;
    private final ProjectManager projectManager;

    public ComponentsLeadByUserFunction(final PermissionManager permissionManager, final ProjectComponentManager componentManager, final ProjectManager projectManager, final UserUtil userUtil)
    {
        super(userUtil);
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.componentManager = notNull("componentManager", componentManager);
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.COMPONENT;
    }

    protected List<QueryLiteral> getFunctionValuesList(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final User user)
    {
        final Iterable<ProjectComponent> components = getLeadComponents(user);
        List<QueryLiteral> values = new ArrayList<QueryLiteral>();
        for (ProjectComponent component : components)
        {
            Project project = projectManager.getProjectObj(component.getProjectId());
            if (queryCreationContext.isSecurityOverriden() || permissionManager.hasPermission(Permissions.BROWSE, project, queryCreationContext.getQueryUser()))
            {
                values.add(new QueryLiteral(functionOperand, component.getId()));
            }
        }
        return values;
    }

    private Iterable<ProjectComponent> getLeadComponents(User user)
    {
        return componentManager.findComponentsByLead(user.getName());
    }

    protected String getUserNotFoundMessageKey()
    {
        return JIRA_JQL_COMPONENT_NO_SUCH_USER;
    }
}
