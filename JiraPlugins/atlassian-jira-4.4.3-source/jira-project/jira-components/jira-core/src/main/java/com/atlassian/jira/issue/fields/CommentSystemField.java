package com.atlassian.jira.issue.fields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.handlers.CommentSearchHandlerFactory;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.issue.bulkedit.BulkWorkflowTransition;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Defines a comment in Jira.
 */
public class CommentSystemField extends AbstractOrderableField implements RenderableField, UnscreenableField, ICommentSystemField
{
    public static final String CREATE_COMMENT = "comment.create.param";

    private static final Logger log = Logger.getLogger(CommentSystemField.class);

    private static final String COMMENT_NAME_KEY = "issue.field.comment";
    private static final String COMMENT_EDIT_TEMPLATE = "comment-edit.vm";

    /**
     * The parameter name of the user-chosen group-type "level" for restricting the comment visibility
     */
    public static final String PARAM_GROUP_LEVEL = "groupLevel";

    /**
     * The parameter name of the user-chosen group or role-type "level" for restricting the comment visibility
     */
    public static final String PARAM_COMMENT_LEVEL = "commentLevel";

    /**
     * The parameter name of the user-chosen role-type "level" for restricting the comment visibility
     */
    public static final String PARAM_ROLE_LEVEL = "roleLevel";

    private final RendererManager rendererManager;
    private final JiraAuthenticationContext authenticationContext;
    private final CommentService commentService;
    private ProjectRoleManager projectRoleManager;
    private ProjectFactory projectFactory;

    public CommentSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, RendererManager rendererManager, PermissionManager permissionManager, CommentService commentService, ProjectRoleManager projectRoleManager, ProjectFactory projectFactory, CommentSearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.COMMENT, COMMENT_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.rendererManager = rendererManager;
        this.authenticationContext = authenticationContext;
        this.commentService = commentService;
        this.projectRoleManager = projectRoleManager;
        this.projectFactory = projectFactory;
    }

    /**
     * Defines the object that will be passed through to the create method
     *
     * @param params is a representation of the request params that are available
     * @return an object that holds the params we need for this Field.
     */
    protected Object getRelevantParams(Map params)
    {
        Map commentParams = new HashMap();
        String[] value = (String[]) params.get(getId());
        if (value != null && value.length > 0)
        {
            commentParams.put(getId(), value[0]);
        }

        CommentVisibility commentVisibility = new CommentVisibility(params, PARAM_COMMENT_LEVEL);

        commentParams.put(PARAM_GROUP_LEVEL, commentVisibility.getGroupLevel());
        commentParams.put(PARAM_ROLE_LEVEL, commentVisibility.getRoleLevel());

        return commentParams;
    }



    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateVelocityParams(fieldLayoutItem, null, velocityParams);

        if (operationContext != null && operationContext.getFieldValuesHolder() != null &&
                operationContext.getFieldValuesHolder().containsKey(getId()))
        {
            Map commentParams = (Map) operationContext.getFieldValuesHolder().get(getId());
            if (commentParams != null)
            {
                velocityParams.put(getId(), commentParams.get(getId()));
                // put the selected value into the params if it exists so we can handle errors
                populateParamsWithSelectedValue(commentParams, velocityParams);
            }
        }

        return renderTemplate(COMMENT_EDIT_TEMPLATE, velocityParams);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateVelocityParams(fieldLayoutItem, (operationContext != null) ? operationContext.getFieldValuesHolder() : null, velocityParams);

        if (operationContext != null && operationContext.getFieldValuesHolder() != null &&
                operationContext.getFieldValuesHolder().containsKey(getId()))
        {
            Map commentParams = (Map) operationContext.getFieldValuesHolder().get(getId());
            if (commentParams != null)
            {
                velocityParams.put(getId(), commentParams.get(getId()));
                // put the selected value into the params if it exists so we can handle errors
                populateParamsWithSelectedValue(commentParams, velocityParams);
            }
        }

        return renderTemplate(COMMENT_EDIT_TEMPLATE, velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Comment system field does not know how to obtain a comment value given an Issue.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

        // get the rendered value without specifying an issue for context
        IssueRenderContext context;
        if (issue != null)
        {
            context = issue.getIssueRenderContext();
        }
        else
        {
            context = new IssueRenderContext(null);
        }
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;

        Map valueMap = (Map) value;

        velocityParams.put("value", rendererManager.getRenderedContent(rendererType, (String) valueMap.get(getId()), context));
        if (valueMap.containsKey(PARAM_GROUP_LEVEL))
        {
            velocityParams.put(PARAM_GROUP_LEVEL, valueMap.get(PARAM_GROUP_LEVEL));
        }
        if (valueMap.containsKey(PARAM_ROLE_LEVEL))
        {
            String roleId = (String) valueMap.get(PARAM_ROLE_LEVEL);
            // We need the display name of the role
            if (roleId != null)
            {
                ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(roleId));
                if (projectRole != null)
                {
                    velocityParams.put("selectedRoleName", projectRole.getName());
                }
            }
            velocityParams.put(PARAM_ROLE_LEVEL, roleId);
        }
        return renderTemplate("comment-view.vm", velocityParams);
    }

    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.COMMENT_ISSUE);
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        Map commentParams = new HashMap();
        commentParams.put(getId(), "");
        commentParams.put(PARAM_GROUP_LEVEL, null);
        fieldValuesHolder.put(getId(), commentParams);
    }

    /**
     * Extracts comment values from the fieldValuesHolder and places them in another map to be used by the WorkflowManager.
     * These additional inputs are required by the CreateCommentFunction to successfully create a comment.
     *
     * @param fieldValuesHolder a map containing comment values from a BulkEdit. Obtained from BulkEditBean.
     * @param additionalInputs  a map to be passed onto a WorkflowManager.
     */
    public void populateAdditionalInputs(Map fieldValuesHolder, Map additionalInputs)
    {
        final Map commentParams = (Map) fieldValuesHolder.get(getId());
        if (commentParams != null)
        {
            final String comment = (String) commentParams.get(getId());
            if (StringUtils.isNotBlank(comment))
            {
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT, commentParams.get(getId()));
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL, commentParams.get(PARAM_GROUP_LEVEL));
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT_ROLE_LEVEL, commentParams.get(PARAM_ROLE_LEVEL));
            }
        }
    }

    // since we don't edit the comment value and we can't resolve a single comment value from an issue,
    // just populate with defaults.
    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        populateDefaults(fieldValuesHolder, issue);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        Map commentParams = (Map) fieldValuesHolder.get(getId());
        String body = (String) commentParams.get(getId());

        String groupLevel = (String) commentParams.get(PARAM_GROUP_LEVEL);
        String roleLevel = (String) commentParams.get(PARAM_ROLE_LEVEL);
        User user = authenticationContext.getUser();

        // Validate user has the correct permissions IF we are actually adding a comment
        if (StringUtils.isNotBlank(body))
        {
            commentService.hasPermissionToCreate(user, issue, errorCollectionToAddTo);
        }

        // If we are on AddComment we need to validate the body
        if (fieldValuesHolder.get(CREATE_COMMENT) != null)
        {
            commentService.isValidCommentBody(body, errorCollectionToAddTo);
        }

        // Validate the group and role level settings
        commentService.isValidCommentData(user, issue, groupLevel, roleLevel, errorCollectionToAddTo);

    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void createValue(Issue issue, Object value)
    {
        throw new UnsupportedOperationException("CreateValue on the comment system field is unsupported.");
    }

    // all comment creations are seen as an update
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Map commentParams = (Map) modifiedValue.getNewValue();
        String body = (String) commentParams.get(getId());

        // allow the renderer for this field a change to transform the value
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
        body = (String) rendererManager.getRendererForType(rendererType).transformFromEdit(body);

        if (StringUtils.isNotBlank(body))
        {
            String groupLevel = (String) commentParams.get(PARAM_GROUP_LEVEL);

            Object rolelLevelId = commentParams.get(PARAM_ROLE_LEVEL);
            Long roleLevelId = rolelLevelId == null ? null : new Long((String) rolelLevelId);

            ErrorCollection errorCollection = new SimpleErrorCollection();

            final User user = authenticationContext.getUser();
            Comment comment = commentService.create(
                    user,
                    issue,
                    body,
                    groupLevel,
                    roleLevelId,
                    false,
                    errorCollection);

            if (errorCollection.hasAnyErrors())
            {
                log.error("There was an error creating a comment value: " + errorCollection.toString());
            }
            else
            {
                issueChangeHolder.setComment(comment);
            }
        }
    }

    /**
     * Sets the value as a modified external field in the issue so that this
     * field will be updated along with all the other modified issue values.
     */
    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            Map commentParams = (Map) fieldValueHolder.get(getId());
            if (StringUtils.isNotBlank((String) commentParams.get(getId())))
            {
                issue.setExternalFieldValue(getId(), fieldValueHolder.get(getId()));
            }
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        // Warn the users if we are bulk moving and the renderer types are different in one of the fieldLayoutItems
        if (originalIssues.size() > 1)
        {
            for (Iterator iterator = originalIssues.iterator(); iterator.hasNext();)
            {
                Issue originalIssue = (Issue) iterator.next();

                // Also if the field is renderable and the render types differ prompt with an edit
                FieldLayoutItem fieldLayoutItem = null;
                try
                {
                    fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(originalIssue.getProject(), originalIssue.getIssueTypeObject().getId()).getFieldLayoutItem(getId());
                }
                catch (DataAccessException e)
                {
                    log.warn(getName() + " field was unable to resolve the field layout item for issue " + originalIssue.getId(), e);
                }

                String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                String targetRendererType = (targetFieldLayoutItem != null) ? targetFieldLayoutItem.getRendererType() : null;
                if (!rendererTypesEqual(rendererType, targetRendererType))
                {
                    return new MessagedResult(false, getAuthenticationContext().getI18nHelper().getText("renderer.bulk.move.warning"), MessagedResult.WARNING);
                }
            }
        }

        return new MessagedResult(false);
    }

    // don't have the system field do anything for move at the moment
    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        throw new UnsupportedOperationException("Remove is not done through the system field for comment.");
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    // return false so that move does not get the wrong idea
    public boolean hasValue(Issue issue)
    {
        return false;
    }

    public Object getValueFromParams(Map params) throws FieldValidationException
    {
        if (params.containsKey(getId()))
        {
            return params.get(getId());
        }

        return null;
    }

    // no conversion is needed
    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
    }

    public String getValueFromIssue(Issue issue)
    {
        throw new UnsupportedOperationException("Comment system field does not know how to obtain a comment value given an Issue.");
    }

    public boolean isRenderable()
    {
        return true;
    }

    /**
     * Adds to the given velocity parameters using the given fieldValuesHolder and
     * fieldLayoutItem (to determine the renderer).
     *
     * @param fieldLayoutItem the FieldLayoutItem in play
     * @param fieldValuesHolder the fields values holder in play 
     * @param velocityParams    the velocity parameters to which values will be added
     */
    private void populateVelocityParams(FieldLayoutItem fieldLayoutItem, Map fieldValuesHolder, Map velocityParams)
    {
        if (fieldValuesHolder != null)
        {
            Map commentParams = (Map) fieldValuesHolder.get(getId());
            if (commentParams != null)
            {
                velocityParams.put(getId(), commentParams.get(getId()));
            }
        }
        velocityParams.put("rendererParams", new HashMap());
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
        velocityParams.put("rendererDescriptor", rendererManager.getRendererForType(rendererType).getDescriptor());
        velocityParams.put("groupLevels", getGroupLevels());

        Issue issue = (Issue) velocityParams.get("issue");
        if (issue != null)
        {
            velocityParams.put("roleLevels", getRoleLevels(issue));
        }
        else
        {
            // We are possibly in a bulk screen
            Object action = velocityParams.get("action");
            if (action != null && action instanceof BulkWorkflowTransition)
            {
                BulkWorkflowTransition bulkWorkflowTransition = (BulkWorkflowTransition) action;
                BulkEditBean bulkEditBean = bulkWorkflowTransition.getBulkEditBean();
                if (bulkEditBean != null)
                {
                    GenericValue project = bulkEditBean.getProject();
                    if (project != null)
                    {
                        velocityParams.put("roleLevels", getRoleLevels(project));
                    }
                }
            }
        }

    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        String rendererType = null;

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (Iterator iterator = bulkEditBean.getFieldLayouts().iterator(); iterator.hasNext();)
        {
            FieldLayout fieldLayout = (FieldLayout) iterator.next();
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }

            // Check for different renderer type
            if (StringUtils.isBlank(rendererType))
            {
                rendererType = fieldLayout.getRendererTypeForField(IssueFieldConstants.COMMENT);
            }
            else if (!rendererType.equals(fieldLayout.getRendererTypeForField(IssueFieldConstants.COMMENT)))
            {
                return "bulk.edit.unavailable.different.renderers";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Have to loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned to a role)
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            if (!isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    /**
     * Returns the list of group names that the current user is in.
     *
     * @return the possibly empty Collection of group names (Strings)
     */
    private Collection getGroupLevels()
    {
        Collection groups;
        if (authenticationContext.getUser() == null || !commentService.isGroupVisiblityEnabled())
        {
            groups = Collections.EMPTY_LIST;
        }
        else
        {
            List userGroups = new ArrayList(authenticationContext.getUser().getGroups());
            Collections.sort(userGroups);
            groups = userGroups;
        }
        return groups;
    }

    private Collection getRoleLevels(GenericValue project)
    {
        if (project == null) {
            throw new NullPointerException("project GenericValue was null");
        }
        Collection roles;
        if (commentService.isProjectRoleVisiblityEnabled())
        {
            User user = authenticationContext.getUser();
            roles = projectRoleManager.getProjectRoles(user, projectFactory.getProject(project));
        }
        else
        {
            roles = Collections.EMPTY_LIST;
        }
        return roles;
    }

    private Collection getRoleLevels(Issue issue)
    {
        Collection roles;
        if (commentService.isProjectRoleVisiblityEnabled())
        {
            User user = authenticationContext.getUser();
            roles = projectRoleManager.getProjectRoles(user, issue.getProjectObject());
        }
        else
        {
            roles = Collections.EMPTY_LIST;
        }
        return roles;
    }

    private void populateParamsWithSelectedValue(Map commentParams, Map velocityParams)
    {
        if (commentParams.get(PARAM_ROLE_LEVEL) != null)
        {
            velocityParams.put(PARAM_COMMENT_LEVEL, "role:" + commentParams.get(PARAM_ROLE_LEVEL));
        }
        else if (commentParams.get(PARAM_GROUP_LEVEL) != null)
        {
            velocityParams.put(PARAM_COMMENT_LEVEL, "group:" + commentParams.get(PARAM_GROUP_LEVEL));
        }
    }

}
