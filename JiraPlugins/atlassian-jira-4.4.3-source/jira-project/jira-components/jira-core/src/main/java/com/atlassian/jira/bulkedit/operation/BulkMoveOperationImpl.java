package com.atlassian.jira.bulkedit.operation;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionRuntimeException;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Operatin for moving <strong>parent</strong> and their sub-takks issues from one or many contexts to a single target
 * context.
 */
public class BulkMoveOperationImpl extends AbstractBulkOperation implements BulkMoveOperation
{
    protected static final Logger log = Logger.getLogger(BulkMoveOperationImpl.class);

    private static final String DESCRIPTION_KEY = "bulk.move.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.move.cannotperform";

    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;
    private final FieldManager fieldManager;
    private final IssueFactory issueFactory;
    private final IssueManager issueManager;
    private final AttachmentPathManager attachmentPathManager;

    public BulkMoveOperationImpl(WorkflowManager workflowManager, ProjectManager projectManager, FieldManager fieldManager,
            IssueFactory issueFactory, IssueManager issueManager, AttachmentPathManager attachmentPathManager)
    {
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.fieldManager = fieldManager;
        this.issueFactory = issueFactory;
        this.issueManager = issueManager;
        this.attachmentPathManager = attachmentPathManager;
    }

    ///// ------------------------ Operation Methods ------------------------ /////

    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        //  Check whether the user has the move permission on all original selected issues
        List selectedIssues = bulkEditBean.getSelectedIssues();
        PermissionManager permissionManager = ManagerFactory.getPermissionManager();
        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext(); )
        {
            Issue issueObject = (Issue) iterator.next();

            if (!permissionManager.hasPermission(Permissions.MOVE_ISSUE, issueObject.getGenericValue(), remoteUser))
            {
                return false;
            }

            // Check Sub-Tasks
            Collection subTasks = issueObject.getSubTasks();
            for (Iterator iterator1 = subTasks.iterator(); iterator1.hasNext(); )
            {
                GenericValue subTask = (GenericValue) iterator1.next();
                if (!permissionManager.hasPermission(Permissions.MOVE_ISSUE, subTask, remoteUser))
                {
                    return false;
                }
            }

        }
        return true;
    }

    public boolean canPerform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser)
    {
        return canPerform(bulkEditBean, (User) remoteUser);
    }

    public void perform(final BulkEditBean bulkEditBean, final User remoteUser) throws Exception
    {
        moveIssuesAndIndex(bulkEditBean, remoteUser);
        if (bulkEditBean.getSubTaskBulkEditBean() != null)
        {
            moveIssuesAndIndex(bulkEditBean.getSubTaskBulkEditBean(), remoteUser);
        }
    }

    // Move the selected issues (and subtasks)
    public void perform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser) throws Exception
    {
        perform(bulkEditBean, (User) remoteUser);
    }

    public void moveIssuesAndIndex(BulkEditBean bulkEditBean, User remoteUser) throws Exception
    {
        Collection selectedIssues = bulkEditBean.getSelectedIssues();

        Exception firstException = null;
        for (Object selectedIssue : selectedIssues)
        {
            MutableIssue issue = (MutableIssue) selectedIssue;
            try
            {
                Transaction txn = Txn.begin();
                try
                {
                    moveIssueInsideTxn(txn, bulkEditBean, remoteUser, issue);
                }
                catch (Exception e)
                {
                    log.error(String.format("An exception occured while moving '%s' : %s. Rolling back the operation", issue.getKey(), e.getMessage()));
                    firstException = e;
                }
                finally
                {
                    txn.finallyRollbackIfNotCommitted();
                }
            }
            catch (TransactionRuntimeException tre)
            {
                log.error(String.format("Unable to obtain database transaction for '%s' : %s", issue.getKey(), tre.getMessage()));
                firstException = tre;
            }

            // the original semantics of this code was that the first exception stops the whole operation
            // I would argue against that but I have preserved that behaviour but inside a transaction boundary
            if (firstException != null)
            {
                throw firstException;
            }
        }
    }

    private void moveIssueInsideTxn(final Transaction txn, final BulkEditBean bulkEditBean, final User remoteUser, final MutableIssue issue)
            throws Exception
    {
        MutableIssue newIssue = (MutableIssue) bulkEditBean.getTargetIssueObjects().get(issue);

        // See if the Issue is moving project
        boolean issueProjectMoved = !issue.getProjectObject().getId().equals(bulkEditBean.getTargetPid());
        final String newKey;
        if (issueProjectMoved)
        {
            newKey = getNewKey(bulkEditBean);  // changed project - get a new key
        }
        else
        {
            newKey = null;
        }

        JiraWorkflow originalWorkflow = workflowManager.getWorkflow(issue.getGenericValue());

        // Move attachments if we are moving to a new project. We want to do the move before anything starts writing
        // to the Issue table in the DB including the Workflow migration.
        if (issueProjectMoved)
        {
            moveAttachments(issue, newKey, bulkEditBean);
        }

        // Only migrate the issue if the target workflow is different from the current workflow
        if (!originalWorkflow.equals(bulkEditBean.getTargetWorkflow()))
        {
            GenericValue newStatus = bulkEditBean.getTargetStatus(issue);
            if (newStatus == null)
            {
                newStatus = issue.getStatus();
            }

            // Note that this migrateIssueToWorkflow() operation will actually store the given Issue Generic Value to the DB.
            // This state is incomplete, and therefore invalid until we save other values (eg it has the new ProjectID, but the old issue key).
            workflowManager.migrateIssueToWorkflow(newIssue.getGenericValue(), bulkEditBean.getTargetWorkflow(), newStatus);
        }

        // TODO: Why are we doing a store here with only half the changes? - we need to do another store later anyway. This data is invalid and subtasks aren't moved.
        // Note that the workflowManager.migrateIssueToWorkflow() operation above also does an Issue.store().
        newIssue.store();

        // Log and set new details for target
        newIssue.setUpdated(new Timestamp(System.currentTimeMillis()));
        IssueChangeHolder issueChangeHolder =
                moveIssueDetails(issue, newIssue, newKey, bulkEditBean, originalWorkflow);

        //create and store the changelog for this whole process
        GenericValue updateLog = ChangeLogUtils.createChangeGroup(remoteUser, issue, newIssue,
                issueChangeHolder.getChangeItems(), false);

        //  and do it all inside a transaction these days
        txn.commit();
        finishUpdate(newIssue, updateLog, issueChangeHolder, bulkEditBean, remoteUser, issueProjectMoved);
    }

    private void rollback(Transaction txn)
    {
        txn.rollback();
    }

    public void chooseContextNoValidate(BulkEditBean bulkEditBean, User remoteUser)
    {
        // Populate
        fieldManager.getProjectField().populateFromParams(bulkEditBean.getFieldValuesHolder(), ActionContext.getParameters());
        fieldManager.getIssueTypeField().populateFromParams(bulkEditBean.getFieldValuesHolder(), ActionContext.getParameters());

        finishChooseContext(bulkEditBean, remoteUser);
    }

    public void chooseContext(BulkEditBean bulkEditBean, User remoteUser, I18nHelper i18nHelper, ErrorCollection errors)
    {
        // Pre-parses the action params for just this bulkEditBean (to cope with multiple fields on the same page)
        Map actionParams = extractBulkEditBeanParams(bulkEditBean);

        // Validate as if we are creating a new issue
        MutableIssue issueObject = getIssueObject(null);

        // Set the parent id of subtask object in order to validate issue type selection
        if (bulkEditBean.isSubTaskCollection())
        {
            // This is actually some BS hack that happens to work... Basically trick the issue object into thinking it's a subTask
            issueObject.setParentId(new Long(0));
        }

        // Validate Project
        ErrorCollection newErrors = new SimpleErrorCollection();

        final ProjectField projectField = fieldManager.getProjectField();
        projectField.populateFromParams(bulkEditBean.getFieldValuesHolder(), actionParams);
        projectField.validateParams(bulkEditBean, newErrors, i18nHelper, issueObject, null);

        // Validate Issue Type
        GenericValue targetProject = bulkEditBean.getTargetProjectGV();
        issueObject.setProject(targetProject);
        final IssueTypeField issueTypeField = fieldManager.getIssueTypeField();
        issueTypeField.populateFromParams(bulkEditBean.getFieldValuesHolder(), actionParams);
        issueTypeField.validateParams(bulkEditBean, newErrors, i18nHelper, issueObject, null);

        // Checks if there are sub-tasks in the target project if the issues have sub tasks
        Collection subTaskOptionsForTargetProject = issueTypeField.getOptionsForIssue(issueObject, true);
        if (subTaskOptionsForTargetProject.isEmpty() && !bulkEditBean.getSubTaskOfSelectedIssues().isEmpty())
        {
            newErrors.addError(projectField.getId(), i18nHelper.getText("bulk.move.no.subtask", String.valueOf(bulkEditBean.getSubTaskOfSelectedIssues().size())));
        }

        if (newErrors.hasAnyErrors())
        {
            // Fix up the error messages & add to original error collection
            Map errorsForFields = newErrors.getErrors();
            Set entries = errorsForFields.entrySet();
            for (Iterator iterator = entries.iterator(); iterator.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                String field = (String) entry.getKey();
                String message = (String) entry.getValue();

                errors.addError(bulkEditBean.getKey() + field, message);
            }

            errors.getErrorMessages().addAll(newErrors.getErrorMessages());
        }
    }

    public void finishChooseContext(MultiBulkMoveBean multiBulkMoveBean, User remoteUser)
    {
        for (Iterator iterator = multiBulkMoveBean.getBulkEditBeans().values().iterator(); iterator.hasNext(); )
        {
            BulkEditBean bulkEditBean = (BulkEditBean) iterator.next();
            finishChooseContext(bulkEditBean, remoteUser);
        }
    }

    public void finishChooseContext(BulkEditBean bulkEditBean, User remoteUser)
    {
        // Set the target field layout (aka "Field Configuration") according to target Project and Issue Type.
        bulkEditBean.setTargetFieldLayout();
        setMoveFieldLayoutItems(bulkEditBean, remoteUser);
        setRemovedFields(bulkEditBean, remoteUser);

        GenericValue targetProject = bulkEditBean.getTargetProjectGV();
        // Works out which sub-tasks will need to be moved, if any
        List subTasksRequiringMove = new ArrayList();
        for (Iterator iterator = bulkEditBean.getSubTaskOfSelectedIssues().iterator(); iterator.hasNext(); )
        {
            Issue subTask = (Issue) iterator.next();
            if (!targetProject.equals(subTask.getProject()))
            {
                subTasksRequiringMove.add(subTask);
            }
        }

        // Sets the sub task ball rolling if sub-tasks need to be moved by settting a "SubTaskBulkEditBean" within this
        // BulkEditBean.
        if (!subTasksRequiringMove.isEmpty())
        {
            BulkEditBean subTaskBulkEditBean = new BulkEditBeanImpl(issueManager);
            subTaskBulkEditBean.setOperationName(bulkEditBean.getOperationName());
            subTaskBulkEditBean.initSelectedIssues(subTasksRequiringMove);
            subTaskBulkEditBean.setTargetProject(targetProject);

            bulkEditBean.setSubTaskBulkEditBean(subTaskBulkEditBean);
        }
    }

    /**
     * This is a nasty hack of method that allows multiple project and issue types per page. It also "knows" about the
     * parameter sameAsBulkEditBean
     *
     * @return Map of String Arrays pretending to be the action params
     */
    private Map extractBulkEditBeanParams(BulkEditBean bulkEditBean)
    {
        String bulkEditKey = bulkEditBean.getKey();

        Map actionParams = ActionContext.getParameters();
        Map modifiedMap = new HashedMap(actionParams);

        Object bulkeditPid = actionParams.get(bulkEditKey + "pid");
        if (bulkeditPid != null)
        {
            modifiedMap.put("pid", bulkeditPid);
        }

        Object bulkEditIssueTypeId = actionParams.get(bulkEditKey + fieldManager.getIssueTypeField().getId());
        if (bulkEditIssueTypeId != null)
        {
            modifiedMap.put(fieldManager.getIssueTypeField().getId(), bulkEditIssueTypeId);
        }

        // Do the funky hack with sameAsBulkEditBean
        String sameAsBulkEditBean = ParameterUtils.getStringParam(modifiedMap, "sameAsBulkEditBean");
        if (!bulkEditBean.isSubTaskCollection() && StringUtils.isNotBlank(sameAsBulkEditBean))
        {
            String[] pid = (String[]) modifiedMap.get(sameAsBulkEditBean + "pid");
            String[] issueTypeId = (String[]) modifiedMap.get(sameAsBulkEditBean + fieldManager.getIssueTypeField().getId());

            modifiedMap.put(bulkEditKey + "pid", pid);
            modifiedMap.put(bulkEditKey + fieldManager.getIssueTypeField().getId(), issueTypeId);

            modifiedMap.put("pid", pid);
            modifiedMap.put(fieldManager.getIssueTypeField().getId(), issueTypeId);
        }

        ActionContext.setParameters(modifiedMap);
        return modifiedMap;
    }

    public boolean isStatusValid(BulkEditBean bulkEditBean)
    {
        try
        {
            return bulkEditBean.getInvalidStatuses().isEmpty();
        }
        catch (WorkflowException e)
        {
            log.warn(e, e);
            throw new NestableRuntimeException(e);
        }
    }

    public void setStatusFields(BulkEditBean bulkEditBean) throws WorkflowException
    {
        bulkEditBean.populateStatusHolder();
//        bulkEditBean.populateSubTaskStatusHolder();
    }

    public void validatePopulateFields(final BulkEditBean bulkEditBean, ErrorCollection errors, I18nHelper i18nHelper)
    {
        // Populate the retain values collection
        Map params = ActionContext.getParameters();
        if (params != null && !params.isEmpty())
        {
            Set keys = params.keySet();
            for (Iterator iterator1 = keys.iterator(); iterator1.hasNext(); )
            {
                String key = (String) iterator1.next();
                if (key.startsWith("retain"))
                {
                    String fieldId = StringUtils.substringAfter(key, "retain_");
                    bulkEditBean.addRetainValue(fieldId);
                }
            }
        }

        Collection selectedIssues = bulkEditBean.getSelectedIssues();
        final Set errorCollection = new HashSet();

        // pre-calculate mappings and substitutions for all fields
        calculateFieldSubstitutions(bulkEditBean);

        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext(); )
        {
            MutableIssue selectedIssue = (MutableIssue) iterator.next();
            validateFieldLayoutItems(selectedIssue, (MutableIssue) bulkEditBean.getTargetIssueObjects().get(selectedIssue), bulkEditBean.getMoveFieldLayoutItems(), bulkEditBean, errorCollection, errors, i18nHelper);
        }
    }

    private void calculateFieldSubstitutions(final BulkEditBean bulkEditBean)
    {
        final Collection<?> fieldLayoutItems = bulkEditBean.getMoveFieldLayoutItems();
        for (Object o : fieldLayoutItems)
        {
            final FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) o;
            final OrderableField orderableField = fieldLayoutItem.getOrderableField();
            final String fieldId = orderableField.getId();

            // mappings would only be present if the field is shown in the target issue
            if (orderableField.isShown(bulkEditBean.getFirstTargetIssueObject()))
            {
                if (doesFieldHaveMappings(fieldId, ActionContext.getParameters()))
                {
                    // calculate the mappings specified by the user
                    final Map<Long, Long> substitutions = createSubstitutionMap(fieldId, ActionContext.getParameters());

                    // store them in the BulkEditBean for future use
                    bulkEditBean.getFieldSubstitutionMap().put(orderableField.getId(), substitutions);

                    // update the session bean with the latest mapped value to keep track for users using multiple beans
                    final Map<String, Map<Long, Long>> sessionFieldSubstitutionMap = BulkEditBean.getFromSession().getFieldSubstitutionMap();
                    if (sessionFieldSubstitutionMap.containsKey(orderableField.getId()))
                    {
                        // merge the existing map for this field with the new one
                        // the new one gets precedence so it is the first map in the Composite.
                        final Map<Long, Long> compositeMap = CompositeMap.of(substitutions, sessionFieldSubstitutionMap.get(orderableField.getId()));
                        sessionFieldSubstitutionMap.put(orderableField.getId(), compositeMap);
                    }
                    else
                    {
                        sessionFieldSubstitutionMap.put(orderableField.getId(), substitutions);
                    }
                }
            }
        }
    }

    private void validateFieldLayoutItems(MutableIssue selectedIssue, MutableIssue targetIssue, Collection fieldLayoutItems, final BulkEditBean bulkEditBean, final Collection errorCollection, ErrorCollection errors, I18nHelper i18nHelper)
    {
        FieldLayout targetFieldLayout = bulkEditBean.getTargetFieldLayoutForType(bulkEditBean.getTargetIssueTypeGV().getString("id"));

        // Loop over all the fields that need to be edited for the move
        for (final Object item : fieldLayoutItems)
        {
            final FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) item;
            final OrderableField orderableField = fieldLayoutItem.getOrderableField();
            final String fieldId = orderableField.getId();

            if (orderableField.isShown(targetIssue))
            {
                // only try update values if the field needs moving or we don't want to retain the value of this field
                final boolean fieldNeedsMoveForThisIssue = fieldNeedsMove(Collections.singletonList(selectedIssue), targetIssue, bulkEditBean, targetFieldLayout.getFieldLayoutItem(orderableField)).getResult();
                if (fieldNeedsMoveForThisIssue || !bulkEditBean.isRetainChecked(fieldId))
                {
                    // As the issue has been shown then initialise it from the action's parameters
                    orderableField.populateFromParams(bulkEditBean.getFieldValuesHolder(), ActionContext.getParameters());

                    // If there was mapping going on then we need to set the field values holder in a more complex way
                    if (doesFieldHaveMappings(fieldId, ActionContext.getParameters()))
                    {
                        // get the calculated mapping for this bean
                        final Map<Long, Long> substitutions = bulkEditBean.getFieldSubstitutionMap().get(fieldId);

                        // obtain the new value to set on the issue by substitution
                        final Object newValue = createSubstitutedValues(selectedIssue, orderableField, substitutions);

                        // set the new values against this field in our FieldValuesHolder, ready for validation.
                        bulkEditBean.getFieldValuesHolder().put(fieldId, newValue);
                    }

                    // Validate the value of the field
                    orderableField.validateParams(bulkEditBean, errors, i18nHelper, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));

                    // Need to log the error message only once - otherwise, the error is shown for each issue within the collection to be moved.
                    if (errors.hasAnyErrors())
                    {
                        for (Iterator iterator2 = errors.getErrorMessages().iterator(); iterator2.hasNext(); )
                        {
                            String message = (String) iterator2.next();
                            if (errorCollection.isEmpty() || !errorCollection.contains(message))
                            {
                                errorCollection.add(message);
                            }
                        }
                        errors.setErrorMessages(new ArrayList<String>());
                    }
                    else
                    {
                        orderableField.updateIssue(fieldLayoutItem, targetIssue, bulkEditBean.getFieldValuesHolder());
                    }
                }
            }
            else
            {
                // If the field has not been shown (but it needs to be moved) populate with the default value
                orderableField.populateDefaults(bulkEditBean.getFieldValuesHolder(), targetIssue);

                // Validate the parameter. In theory as the field places a default value itself the value should be valid, however, a check for
                // 'requireability' still has to be made.
                ErrorCollection errorCollection2 = new SimpleErrorCollection();
                orderableField.validateParams(bulkEditBean, errorCollection2, i18nHelper, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));
                if (errorCollection2.getErrors() != null && !errorCollection2.getErrors().isEmpty())
                {
                    // The field has reported errors but is not rendered on the screen - report errors as error messages
                    for (Iterator iterator2 = errorCollection2.getErrors().values().iterator(); iterator2.hasNext(); )
                    {
                        errors.addErrorMessage(getFieldName(orderableField, i18nHelper) + ": " + iterator2.next());
                    }
                }
                else
                {
                    orderableField.updateIssue(fieldLayoutItem, targetIssue, bulkEditBean.getFieldValuesHolder());
                }
            }
        }
        // This is added for fields that do not have values but are required in the target project
        errors.addErrorMessages(errorCollection);
    }

    private Object createSubstitutedValues(final Issue selectedIssue, final OrderableField orderableField, final Map<Long, Long> substitutions)
    {
        if (orderableField instanceof CustomField)
        {
            return createSubstitutedValuesForCustomFields(selectedIssue, (CustomField) orderableField, substitutions);
        }
        else
        {
            return createSubstitutedValuesForSystemFields(selectedIssue, orderableField, substitutions);
        }
    }

    /**
     * Extracts the values of the specified system field from the Issue and then substitutes any present values for new
     * ones according to the substitution map. The result is the new values for the field for this issue.
     * <p/>
     * Note that if there are values set which are not present in the substitution map, they will be removed from the
     * issue.
     * <p/>
     * Note that if there are values set which are not present in the substitution map, they will be removed from the
     * issue.
     *
     * @param selectedIssue the issue to read values from
     * @param orderableField the field whose values we should read from the issue
     * @param substitutions the substitutions to apply
     * @return a new collection of ids which represents the substituted values for the issue; never null.
     */
    private Collection<Long> createSubstitutedValuesForSystemFields(final Issue selectedIssue, final OrderableField orderableField, final Map<Long, Long> substitutions)
    {
        final String fieldId = orderableField.getId();
        // obtain the ids of the values of this field for this issue
        final Map<String, Object> workingMap = new LinkedHashMap<String, Object>();
        orderableField.populateFromIssue(workingMap, selectedIssue);
        final Collection<Long> existingIds = (Collection<Long>) workingMap.get(fieldId);
        final Collection<Long> newValues = new ArrayList<Long>();

        // for each existing value, if it exists in the substitution map then collect its substitute, otherwise just drop it
        if (existingIds != null)
        {
            // if we previously had no values and we have mapped the blank value (-1) to something, then we must apply that mapping
            if (existingIds.isEmpty() && substitutions.containsKey(-1L))
            {
                newValues.add(substitutions.get(-1L));
            }
            else
            {
                for (Long existingId : existingIds)
                {
                    if (existingId != null)
                    {
                        final Long newValue = substitutions.get(existingId);
                        if (newValue != null)
                        {
                            newValues.add(newValue);
                        }
                    }
                }
            }
        }

        return newValues;
    }

    /**
     * Extracts the values of the specified CustomField from the Issue and then substitutes any present values for new
     * ones according to the substitution map. The result is the new values for the custom field for this issue.
     * <p/>
     * Note that if there are values set which are not present in the substitution map, they will be removed from the
     * issue.
     *
     * @param selectedIssue the issue to read values from
     * @param customField the custom field whose values we should read from the issue
     * @param substitutions the substitutions to apply
     * @return a new CustomFieldParams object which represents the substituted values for the issue; never null.
     */
    private CustomFieldParams createSubstitutedValuesForCustomFields(final Issue selectedIssue, final CustomField customField, final Map<Long, Long> substitutions)
    {
        final String fieldId = customField.getId();
        // obtain the ids of the values of this field for this issue
        final Map<String, Object> workingMap = new LinkedHashMap<String, Object>();
        customField.populateFromIssue(workingMap, selectedIssue);
        final CustomFieldParams existingValues = (CustomFieldParams) workingMap.get(fieldId);

        Collection<String> newValues = new ArrayList<String>();

        if (existingValues != null)
        {
            final Collection<String> existingIdsAsStrings = existingValues.getAllValues();

            // if we previously had no values and we have mapped the blank value (-1) to something, then we must apply that mapping
            if (existingIdsAsStrings.isEmpty() && substitutions.containsKey(-1L))
            {
                newValues.add(substitutions.get(-1L).toString());
            }
            else
            {
                // for each existing value, if it exists in the substitution map then collect its substitute, otherwise just drop it
                for (String existingId : existingIdsAsStrings)
                {
                    if (existingId != null)
                    {
                        Long key = parseLong(existingId);
                        final Long newValue = substitutions.get(key);
                        if (newValue != null)
                        {
                            newValues.add(newValue.toString());
                        }
                    }
                }
            }
        }

        return new CustomFieldParamsImpl(customField, newValues);
    }

    /**
     * Reads in the action parameters and creates a map of substitutions for the values of the specified field. The
     * parameters should be present in the form: <code>fieldId_OLDID -> NEWID</code>, where <code>OLDID</code> and
     * <code>NEWID</code> are both numbers.
     *
     * @param fieldId the id of the field contained in the action parameters that we wish to substitute values for
     * @param actionParameters the parameter map from the submitted action
     * @return a mapping of old ids to substitute with new ids; never null.
     */
    private Map<Long, Long> createSubstitutionMap(final String fieldId, final Map<String, Object> actionParameters)
    {
        final Pattern pattern = Pattern.compile(fieldId + "_(.+)");
        final Map<Long, Long> result = new TreeMap<Long, Long>();
        for (String paramName : actionParameters.keySet())
        {
            final Matcher matcher = pattern.matcher(paramName);
            if (matcher.matches())
            {
                Long oldId = parseLong(matcher.group(1));
                if (oldId == null)
                {
                    continue;
                }

                final String value = ParameterUtils.getStringParam(actionParameters, paramName);
                if (value != null)
                {
                    Long newId = parseLong(value);
                    if (newId == null)
                    {
                        continue;
                    }

                    // if the new Id is -1, this means that we essentially delete the existing value.
                    // Use null to denote this in the substitution map.
                    if (newId.equals(-1L))
                    {
                        newId = null;
                    }
                    result.put(oldId, newId);
                }
            }
        }
        return result;
    }

    private Long parseLong(final String value)
    {
        try
        {
            return Long.valueOf(value);
        }
        catch (NumberFormatException e)
        {
            // wasn't a number - return null
            return null;
        }
    }

    /**
     * Determines if the field has mappings by looking for the field id as a prefix in the action parameters map. Note
     * that with the advent of composite fields (e.g. Time Tracking, Log Work) that utilise a "field id plus underscore"
     * prefixing strategy, we now can't guarantee that just because an underscore is present in the action parameters
     * that it means that we want to apply mappings for this field. Thus we also check the suffix to see if it is a
     * parsable Long; only fields who store their values as Longs are mappable (i.e. Components, Versions).
     * <p/>
     * If in future we want to apply mappings to more fields than just these, then this strategy will need to change!
     *
     * @param fieldId the field id e.g. <code>components</code>, <code>timetracking</code>
     * @param parameters the action parameters
     * @return true if this field has mappings specified in the action parameters.
     */
    private boolean doesFieldHaveMappings(final String fieldId, final Map parameters)
    {
        final String prefix = fieldId + "_";
        for (Object o : parameters.keySet())
        {
            String key = (String) o;
            if (key.startsWith(prefix))
            {
                String suffix = key.substring(prefix.length());
                try
                {
                    Long.parseLong(suffix);
                }
                catch (NumberFormatException e)
                {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String getFieldName(Field field, I18nHelper i18nHelper)
    {
        if (field instanceof CustomField)
        {
            return field.getName();
        }
        else
        {
            return i18nHelper.getText(field.getNameKey());
        }
    }


    private void setMoveFieldLayoutItems(BulkEditBean bulkEditBean, User remoteUser)
    {
        Collection moveFieldLayoutItems = new HashSet();
        Map messagedFieldLayoutItems = new HashMap();

        // Loop over all the visible fields and see which ones require to be moved
        FieldLayout targetFieldLayout = bulkEditBean.getTargetFieldLayout();
        for (Iterator iterator = targetFieldLayout.getVisibleLayoutItems(remoteUser, bulkEditBean.getTargetProject(), EasyList.build(bulkEditBean.getTargetIssueTypeId())).iterator(); iterator.hasNext(); )
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            OrderableField orderableField = fieldLayoutItem.getOrderableField();
            // Issue type is shown on the first stage of the move so no need to work with it here
            if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
            {
                // Check if we need to change the value of this field.
                MessagedResult messagedResult = fieldNeedsMove(fieldLayoutItem, bulkEditBean);
                if (messagedResult.getResult())
                {
                    /*if (orderableField.isShown(getBulkEditBean().getTargetIssue()))
                    {
                        // Populate fields with intersection of all values - e.g. if all versions are set to 3.2 and 3.2
                        // is valid in target - display with 3.2 selected.
                        orderableField.populateForMove(getBulkEditBean().getCustomFieldValuesHolder(), targetIssue, targetIssue);
                    }*/
                    // Record that the field needs to be edited for the move
                    moveFieldLayoutItems.add(fieldLayoutItem);
                }
                else if (messagedResult.getMessage() != null)
                {
                    messagedFieldLayoutItems.put(fieldLayoutItem.getOrderableField().getName(), messagedResult);
                }
            }

//            @todo wmc Not too sure what this is meant to do, but unlikely to need
//            if (!moveFieldLayoutItems.contains(fieldManager.getOrderableField(IssueFieldConstants.ASSIGNEE)))
//            {
//                // Check assignee in relation to sub-tasks
//                if (IssueFieldConstants.ASSIGNEE.equals(orderableField.getId()) && isSubTaskAssigneeChangeRequired(fieldLayoutItem, bulkEditBean, remoteUser))
//                    moveFieldLayoutItems.add(fieldLayoutItem);
//            }
        }

        bulkEditBean.setMessagedFieldLayoutItems(messagedFieldLayoutItems);
        bulkEditBean.setMoveFieldLayoutItems(moveFieldLayoutItems);
    }

    /**
     * Checks if the given field needs to "move" (change its value) in order that we can move the given list of Issues.
     * <p> This method mostly relies on {@link com.atlassian.jira.issue.fields.OrderableField#needsMove} (hence the
     * name), but it also adds logic so that subtasks that take a parent's field value return true when the parent
     * returned true. (This is just Security Levels at present). </p>
     *
     * @param fieldLayoutItem The FieldLayout configuration for our field.
     * @param bulkEditBean The BulkEditBean.
     * @return A <code>true</code> MessagedResult if the given field needs to be moved.
     */
    private MessagedResult fieldNeedsMove(FieldLayoutItem fieldLayoutItem, BulkEditBean bulkEditBean)
    {
        return fieldNeedsMove(bulkEditBean.getSelectedIssues(), bulkEditBean.getFirstTargetIssueObject(), bulkEditBean, fieldLayoutItem);
    }

    /**
     * Checks if the given field needs to "move" (change its value) in order that we can move the given list of Issues.
     * <p> This method mostly relies on {@link com.atlassian.jira.issue.fields.OrderableField#needsMove} (hence the
     * name), but it also adds logic so that subtasks that take a parent's field value return true when the parent
     * returned true. (This is just Security Levels at present). </p>
     *
     * @param originalIssues the issues being moved
     * @param firstTargetIssueObject the first target issue object
     * @param bulkEditBean The BulkEditBean.
     * @param fieldLayoutItem The FieldLayout configuration for our field.
     * @return A <code>true</code> MessagedResult if the given field needs to be moved.
     */
    private MessagedResult fieldNeedsMove(final Collection originalIssues, final Issue firstTargetIssueObject, BulkEditBean bulkEditBean, FieldLayoutItem fieldLayoutItem)
    {
        OrderableField orderableField = fieldLayoutItem.getOrderableField();
        // Check if this BulkEditBean holds subtasks
        if (bulkEditBean.getParentBulkEditBean() != null)
        {
            // Check if we just take our value from parent. For now this is just for Security Level. (see JRA-14350)
            if (orderableField.getId().equals(IssueFieldConstants.SECURITY))
            {
                // OK - we take our value from parent, now is our parent changing its value?
                if (bulkEditBeanIsChangingField(bulkEditBean.getParentBulkEditBean(), IssueFieldConstants.SECURITY))
                {
                    return new MessagedResult(true);
                }
            }
        }

        // ask the individual field if it "needsMove".
        return orderableField.needsMove(originalIssues, firstTargetIssueObject, fieldLayoutItem);
    }

    /**
     * Returns <code>true</code> if the given BulkEditBean is changing the value of the given field.
     *
     * @param bulkEditBean The BulkEditBean.
     * @param fieldID ID of the field we are interested in.
     * @return <code>true</code> if the given BulkEditBean is changing the value of the given field.
     */
    private boolean bulkEditBeanIsChangingField(final BulkEditBean bulkEditBean, final String fieldID)
    {
        for (Iterator iterator = bulkEditBean.getMoveFieldLayoutItems().iterator(); iterator.hasNext(); )
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            if (fieldLayoutItem.getOrderableField() != null && fieldID.equals(fieldLayoutItem.getOrderableField().getId()))
            {
                return true;
            }
        }
        return false;
    }

    private void setRemovedFields(BulkEditBean bulkEditBean, User remoteUser)
    {
        HashSet removedFields = new HashSet();
        Collection selectedIssues = bulkEditBean.getSelectedIssues();
        Collection targetHiddenFields = getTargetHiddenFields(bulkEditBean, remoteUser);
        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext(); )
        {
            Issue issue = (MutableIssue) iterator.next();

            // Hidden fields include custom fields that are not in scope
            for (Iterator iterator1 = targetHiddenFields.iterator(); iterator1.hasNext(); )
            {
                Field field = (Field) iterator1.next();
                if (fieldManager.isOrderableField(field))
                {
                    OrderableField orderableField = (OrderableField) field;

                    // Remove values of all the fields that have a value but are hidden in the target project
                    if (orderableField.hasValue(issue))
                    {
                        // JRA-13479 We need to remove the value from the TARGET issue, not the source
                        MutableIssue targetIssue = (MutableIssue) bulkEditBean.getTargetIssueObjects().get(issue);
                        if (targetIssue == null)
                        {
                            throw new IllegalStateException("Could not find target issue for issue " + issue.getKey() +
                                    " in BulkMoveOperation.");
                        }
                        if (orderableField.canRemoveValueFromIssueObject(targetIssue))
                        {
                            orderableField.removeValueFromIssueObject(targetIssue);
                            removedFields.add(orderableField);
                        }
                    }
                }
            }
        }
        bulkEditBean.setRemovedFields(removedFields);
    }

    protected Collection getTargetHiddenFields(BulkEditBean bulkEditBean, User remoteUser)
    {
        return bulkEditBean.getTargetFieldLayout().getHiddenFields(remoteUser, bulkEditBean.getTargetProjectGV(), EasyList.build(bulkEditBean.getTargetIssueTypeId()));
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }


    ///// ------------------------ Workflow / Status Methods ------------------------ ///// ------------------------
    // Check if the workflow is the same for the current and target issue types
    public JiraWorkflow getWorkflowForType(Long projectId, String issueTypeId) throws WorkflowException
    {
        return workflowManager.getWorkflow(projectId, issueTypeId);
    }


    ///// ------------------------ Move Methods ------------------------ ///// ------------------------

    /**
     * Create a new directory for this issue, and move all the attachments from the old directory to the new directory.
     * <p/>
     * NB - this will fail if the old directory and new directory are on different filesystems as {@link
     * java.io.File#renameTo} fails across filesystems.
     *
     * @param issue The original issue.
     */
    private void moveAttachments(Issue issue, String newIssueKey, BulkEditBean bulkEditBean) throws Exception
    {
        final Collection<Attachment> attachments = issue.getAttachments();
        for (final Attachment attachment : attachments)
        {
            final File originalAttachmentFile = AttachmentUtils.getAttachmentFile(attachment);
            final String fileName = originalAttachmentFile.getName();
            String originalFilePath = getAttachmentPath(issue.getProjectObject().getKey(), issue.getKey()) + "/" + fileName;
            log.debug("Attachment: " + originalFilePath);

            String targetDirectoryPath = getAttachmentPath(bulkEditBean.getTargetProjectGV().getString("key"), newIssueKey);
            log.debug("Attachment: " + targetDirectoryPath + "/" + fileName);

            File attachmentFile = new File(originalFilePath);
            if (!attachmentFile.exists())
            {
                // The issue was maybe moved by someone else in the middle of our move. See JRA-15475
                Issue latestIssue = issueManager.getIssueObject(issue.getId());
                if (latestIssue.getKey().equals(issue.getKey()))
                {
                    log.warn("The attachment '" + attachment.getFilename() + "' for issue '" + issue.getKey() + "' was not found in expected path '" + originalFilePath + "'. Attachment cannot be moved.");
                    attachmentFile = null;
                }
                else
                {
                    // The Issue has changed key - it must have been moved.
                    // try to get the new attachment path
                    originalFilePath = getAttachmentPath(latestIssue.getProjectObject().getKey(), latestIssue.getKey()) + "/" + fileName;
                    attachmentFile = new File(originalFilePath);
                    if (attachmentFile.exists())
                    {
                        log.warn("The attachment '" + attachment.getFilename() + "' for issue '" + issue.getKey() + "' was not found in expected path '" +
                                originalFilePath + "'. This issue has been moved to '" + latestIssue.getKey() + "', we will move the attachment from '" + originalFilePath + "'.");
                    }
                    else
                    {
                        log.warn("The attachment '" + attachment.getFilename() + "' for issue '" + issue.getKey() + "' was not found in expected path '" + originalFilePath +
                                "'. This issue has been moved to '" + latestIssue.getKey() + "', but the attachment is not in the new path '" + originalFilePath + "' either.");
                        attachmentFile = null;
                    }
                }
            }

            if (attachmentFile != null)
            {
                File destDirectory = new File(targetDirectoryPath);
                destDirectory.mkdirs();
                attachmentFile.renameTo(new File(destDirectory, fileName));
            }
        }
    }

    private String getAttachmentPath(final String projectKey, final String issueKey)
    {
        return attachmentPathManager.getAttachmentPath() + "/" + projectKey + "/" + issueKey;
    }

    // Return next available issue key in target project
    private String getNewKey(BulkEditBean bulkEditBean)
    {
        GenericValue targetProjectGV = bulkEditBean.getTargetProjectGV();
        if (targetProjectGV != null)
        {
            long incCount = projectManager.getNextId(targetProjectGV);
            return targetProjectGV.getString("key") + "-" + incCount;
        }

        log.error("Unable to determine new key for issue.");
        return null;
    }

    // Create change log items for all new details - set details of "moved" issue also
    private IssueChangeHolder moveIssueDetails(MutableIssue oldIssue, MutableIssue newIssue, String newKey,
            BulkEditBean bulkEditBean, JiraWorkflow originalWorkflow)
            throws WorkflowException
    {
        DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();

        GenericValue currentProjectGV = oldIssue.getProject();
        GenericValue targetProjectGV = bulkEditBean.getTargetProjectGV();
        String oldKey = oldIssue.getString("key");
        String newWorkflowId = newIssue.getGenericValue().getLong("workflowId").toString();

        // Set new project and issue key - issue key only changes if issue is moving to new project
        if (!(currentProjectGV.equals(targetProjectGV)))
        {
            // The project is already set on the target issue so we do not need to set it here.
            issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Project", currentProjectGV.getLong("id").toString(), currentProjectGV.getString("name"), targetProjectGV.getLong("id").toString(), targetProjectGV.getString("name")));

            if (newKey != null)
            {
                issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Key", null, oldKey, null, newKey));
                newIssue.setKey(newKey);
            }
        }

        // issue type is already set in the target issue ?? does a change item get generated for this?

        // Only log a workflow/status change if the target workflow is different from the current workflow
        if (!originalWorkflow.equals(bulkEditBean.getTargetWorkflow()))
        {
            issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow", oldIssue.getWorkflowId().toString(), originalWorkflow.getName(), newWorkflowId, bulkEditBean.getTargetWorkflow().getName()));

            GenericValue originalStatus = oldIssue.getStatus();
            GenericValue newStatus = newIssue.getStatus();
            if (originalStatus != null && !originalStatus.equals(newStatus))
            {
                issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "status", originalStatus.getString("id"), originalStatus.getString("name"), newStatus.getString("id"), newStatus.getString("name")));
            }
        }

        Map<String, ModifiedValue> modifiedFields = newIssue.getModifiedFields();
        for (final String fieldId : modifiedFields.keySet())
        {
            if (fieldManager.isOrderableField(fieldId))
            {
                OrderableField field = fieldManager.getOrderableField(fieldId);

                // Validate and retain original values where possible
                if (bulkEditBean.getRetainValues() != null && bulkEditBean.getRetainValues().contains(fieldId))
                {
                    FieldLayout targetFieldLayout = bulkEditBean.getTargetFieldLayoutForType(bulkEditBean.getTargetIssueTypeGV().getString("id"));
                    // Change the value if the field needs to be 'moved'
                    if (field.needsMove(EasyList.build(oldIssue), newIssue, targetFieldLayout.getFieldLayoutItem(field)).getResult())
                    {
                        field.updateValue(targetFieldLayout.getFieldLayoutItem(field), newIssue, modifiedFields.get(fieldId), issueChangeHolder);
                    }
                }
                else
                {
                    FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(newIssue.getProject(), newIssue.getIssueTypeObject().getId()).getFieldLayoutItem(field);
                    field.updateValue(fieldLayoutItem, newIssue, (ModifiedValue) modifiedFields.get(fieldId), issueChangeHolder);// subtask security is changed now
                }
            }
        }

        // Store the issue
        newIssue.store();

        // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
        // method of the issue, so that the fiels removes itself from the modified list as soon as it is persisted.
        newIssue.resetModifiedFields();

        return issueChangeHolder;
    }


    // Flush caches and generate relevant event
    private void finishUpdate(Issue newIssue, GenericValue updateLog, IssueChangeHolder issueChangeHolder,
            BulkEditBean bulkEditBean, User remoteUser, boolean issueProjectMoved)
    {
        if (updateLog != null && !issueChangeHolder.getChangeItems().isEmpty())
        {
            // Determine if mail notification should be sent or not
            boolean sendMail = bulkEditBean.isSendBulkNotification();

            Long eventTypeId;
            if (issueProjectMoved)
            {
                eventTypeId = EventType.ISSUE_MOVED_ID;
            }
            else
            {
                eventTypeId = EventType.ISSUE_UPDATED_ID;
            }
            IssueEventDispatcher.dispatchEvent(eventTypeId, newIssue, remoteUser, updateLog, sendMail,
                    issueChangeHolder.isSubtasksUpdated());
        }
    }

    private MutableIssue getIssueObject(GenericValue issue)
    {
        return issueFactory.getIssue(issue);
    }
}
