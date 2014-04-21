package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelIndex;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adding a unique constraint to the issue_key index - unfortunately this means deleting it and then
 * recreating it
 *
 * @since v5.0
 */
public class UpgradeTask_Build706 extends AbstractUpgradeTask
{


    public UpgradeTask_Build706()
    {
        super(false);
    }

    @Override
    public String getShortDescription()
    {
        return "Adding a unique constraint to the issue table for issue key";
    }

    @Override
    public String getBuildNumber()
    {
        return "706";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        GenericHelper helper = getDelegator().getEntityHelper("User");
        DatabaseUtil dbUtil = new DatabaseUtil(helper.getHelperName());
        ModelEntity issueEntity = getDelegator().getModelEntity("Issue");
        ModelIndex issueKeyIndex = new ModelIndex();
        dbUtil.deleteDeclaredIndex(issueEntity, issueEntity.getIndex("issue_key"));
        dbUtil.createDeclaredIndex(issueEntity, issueEntity.getIndex("issue_key"));
    }

}
