package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.admin.issuetypes.ExecutableAction;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * A bean that stores multiple {@link BulkEditBean}
 */
public interface MultiBulkMoveBean extends Serializable
{

    // -------------------------------------------------------------------------------------------------- Action Methods
    public void initOptionIds(Collection optionIds);

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
    public void initFromIssues(List issues, BulkEditBean parentBulkEditBean);

    /**
     * This method will remap the current {@link BulkEditBean} Map to be keyed by the <em>target</em>
     * {@link IssueContext} rather than the originating {@link IssueContext}.
     */
    public void remapBulkEditBeansByTargetContext();

    public void validate(ErrorCollection errors, BulkMoveOperation bulkMoveOperation, User user);

    // --------------------------------------------------------------------------------------------- View Helper Methods
    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public ListOrderedMap getIssuesInContext();

    public ListOrderedMap getBulkEditBeans();

    public ExecutableAction getExecutableAction();

    public void setExecutableAction(ExecutableAction executableAction);

    public String getFinalLocation();

    public void setFinalLocation(String finalLocation);

    public Collection getSelectedOptions();

    public List getRegularOptions();

    public List getSubTaskOptions();

    public int getSubTasksDiscarded();

    // -------------------------------------------------------------------------------------------------- Static Methods


    public int getNumberOfStatusChangeRequired(BulkMoveOperation bulkMoveOperation);

    public BulkEditBean getCurrentBulkEditBean();

    public void progressToNextBulkEditBean();

    public void progressToPreviousBulkEditBean();

    public boolean isLastBulkEditBean();

    public IssueContext getCurrentIssueContext();

    public int getCurrentBulkEditBeanIndex();

    public void setTargetProject(GenericValue targetProjectGV);
}
