package com.atlassian.jira.web.bean;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.admin.issuetypes.ExecutableAction;
import com.opensymphony.user.User;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * A bean that stores multiple {@link BulkEditBean}
 */
public abstract class MultiBulkMoveBean implements Serializable
{

    // -------------------------------------------------------------------------------------------------- Action Methods
    public abstract void initOptionIds(Collection optionIds);

    /**
     * Initialises this MultiBulkMoveBean given a list of issues.
     * <p>
     * If this MultiBulkMoveBean links a BulkEditBean with parent issues to BulkEditBeans with subtasks, then include
     * the parent BulkEditBean in the parentBulkEditBean parameter. Otherwise you can pass null.
     * </p>
     * @param issues Issues for this MultiBulkMoveBean.
     * @param parentBulkEditBean If this MultiBulkMoveBean represents subtasks, then this is the BulkEditBean that
     *                              contains the parents of the subtasks, otherwise null.
     */
    public abstract void initFromIssues(List issues, BulkEditBean parentBulkEditBean);

    /**
     * This method will remap the current {@link BulkEditBean} Map to be keyed by the <em>target</em>
     * {@link IssueContext} rather than the originating {@link IssueContext}.
     */
    public abstract void remapBulkEditBeansByTargetContext();

    public abstract void validate(ErrorCollection errors, BulkMoveOperation bulkMoveOperation, User user);

    // --------------------------------------------------------------------------------------------- View Helper Methods
    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public abstract ListOrderedMap getIssuesInContext();

    public abstract ListOrderedMap getBulkEditBeans();

    public abstract ExecutableAction getExecutableAction();

    public abstract void setExecutableAction(ExecutableAction executableAction);

    public abstract String getFinalLocation();

    public abstract void setFinalLocation(String finalLocation);

    public abstract Collection getSelectedOptions();

    public abstract List getRegularOptions();

    public abstract List getSubTaskOptions();

    public abstract int getSubTasksDiscarded();

    // -------------------------------------------------------------------------------------------------- Static Methods


    public abstract int getNumberOfStatusChangeRequired(BulkMoveOperation bulkMoveOperation);

    public abstract BulkEditBean getCurrentBulkEditBean();

    public abstract void progressToNextBulkEditBean();

    public abstract void progressToPreviousBulkEditBean();

    public abstract boolean isLastBulkEditBean();

    public abstract IssueContext getCurrentIssueContext();

    public abstract int getCurrentBulkEditBeanIndex();

    public abstract void setTargetProject(GenericValue targetProjectGV);
}
