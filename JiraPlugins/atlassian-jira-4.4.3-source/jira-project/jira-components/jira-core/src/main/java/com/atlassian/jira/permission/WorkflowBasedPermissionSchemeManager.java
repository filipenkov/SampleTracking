package com.atlassian.jira.permission;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Custom permission scheme manager that allows the list of assignable users to be restricted by workflow state.
 * In jira-workflow.xml, every step can have a meta attribute stating which groups are assignable:
 * <p/>
 * &lt;step id="23" name="Pending Biz User Approval">
 * ....
 * &lt;meta name="jira.permission.assignable.group">${pkey}-bizusers&lt;/meta>
 * <p/>
 * When {@link #getUsers(Long, PermissionContext)} is called to discover assignable users,
 * this permission scheme manager first does the regular "Assignable" check, and then filters returned users for membership
 * of the 'jira.permission.assignable.group' (in this example).  If jira.permission.assignable.* isn't specified for a step, all permission-derived users
 * are returned.
 * @see com.atlassian.jira.security.WorkflowBasedPermissionManager
 */
public class WorkflowBasedPermissionSchemeManager extends DefaultPermissionSchemeManager implements Startable
{
    private static final Logger log = Logger.getLogger(WorkflowBasedPermissionSchemeManager.class);
    private SubTaskManager subTaskManager;
    private WorkflowPermissionFactory workflowPermissionFactory;
    private PermissionContextFactory permissionContextFactory;
    private final EventPublisher eventPublisher;

    public WorkflowBasedPermissionSchemeManager(ProjectManager projectManager, PermissionTypeManager permissionTypeManager,
            WorkflowPermissionFactory workflowPermissionFactory, PermissionContextFactory permissionContextFactory,
            OfBizDelegator ofBizDelegator, SchemeFactory schemeFactory, final EventPublisher eventPublisher, final AssociationManager associationManager, final GroupManager groupManager)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, ofBizDelegator, schemeFactory, associationManager, groupManager);
        this.workflowPermissionFactory = workflowPermissionFactory;
        this.permissionContextFactory = permissionContextFactory;
        this.eventPublisher = eventPublisher;
        subTaskManager = ComponentManager.getInstance().getSubTaskManager();
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @com.atlassian.event.api.EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
    }

    public Collection getUsers(Long permissionId, final PermissionContext ctx)
    {
        Collection users = super.getUsers(permissionId, ctx);

        // Get workflow permission overrides from this issue's workflow step and its parent's
        if (ctx.hasIssuePermissions())
        {
            List workflowPerms = workflowPermissionFactory.getWorkflowPermissions(ctx, permissionId.intValue(), false);
            if (subTaskManager.isSubTasksEnabled() && ctx.getIssue().getParentObject() != null)
            {
                PermissionContext parentCtx = permissionContextFactory.getPermissionContext(ctx.getIssue().getParentObject());
                workflowPerms.addAll(workflowPermissionFactory.getWorkflowPermissions(parentCtx, permissionId.intValue(), true));
            }

            if (workflowPerms.size() > 0)
            {
                // We have 1 or more workflow permission overrides; evaluate them to get the list of allowed users
                Set allowedUsers = new HashSet(users.size());
                Iterator iter = workflowPerms.iterator();
                while (iter.hasNext())
                {
                    WorkflowPermission perm = (WorkflowPermission) iter.next();
                    try
                    {
                        Collection newUsers = perm.getUsers(ctx);
                        log.info(perm+" added users: "+listUsers(newUsers));
                        allowedUsers.addAll(newUsers);
                    }
                    catch (IllegalArgumentException e)
                    {
                        log.error("Error with workflow permission" + perm +": "+e.getMessage(), e);
                        throw new RuntimeException("Error with workflow permission "+perm+": "+e.getMessage());
                    }
                }
                log.info("Retaining " + listUsers(allowedUsers) + " of " + listUsers(users));
                // Return the intersection of permission scheme users and workflow permission users
                users.retainAll(allowedUsers);
            }
        }

        return users;
    }

    /** Print a user list for debugging. */
    private String listUsers(final Collection newUsers)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (Iterator iterator = newUsers.iterator(); iterator.hasNext();)
        {
            User user = (User) iterator.next();
            buf.append(user.getName());
            if (iterator.hasNext()) buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }
}
