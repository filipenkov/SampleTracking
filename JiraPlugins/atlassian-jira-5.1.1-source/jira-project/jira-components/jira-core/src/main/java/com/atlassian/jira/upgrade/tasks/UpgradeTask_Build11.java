/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class UpgradeTask_Build11 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build11.class);
    private final ConstantsManager constantsManager;

    public UpgradeTask_Build11(ConstantsManager constantsManager)
    {
        super(false);
        this.constantsManager = constantsManager;
    }

    public String getBuildNumber()
    {
        return "11";
    }

    /**
     * This upgrade sets the default priorities up in the entity engine
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        log.debug("UpgradeTask_Build11 - setting up defaults for custom priorities, resolutions and types.");
        addPriorities();
        addResolutions();
        addIssueTypes();
        constantsManager.refresh();
    }

    private void addIssueTypes()
    {
        try
        {
            createNewEntity("IssueType", IssueFieldConstants.BUG_TYPE_ID, IssueFieldConstants.BUG_TYPE, "A problem which impairs or prevents the functions of the product.", "/images/icons/bug.gif");
            createNewEntity("IssueType", IssueFieldConstants.NEWFEATURE_TYPE_ID, IssueFieldConstants.NEWFEATURE_TYPE, "A new feature of the product, which has yet to be developed.", "/images/icons/newfeature.gif");
            createNewEntity("IssueType", IssueFieldConstants.TASK_TYPE_ID, IssueFieldConstants.TASK_TYPE, "A task that needs to be done.", "/images/icons/task.gif");
            createNewEntity("IssueType", IssueFieldConstants.IMPROVEMENT_TYPE_ID, IssueFieldConstants.IMPROVEMENT_TYPE, "An improvement or enhancement to an existing feature or task.", "/images/icons/improvement.gif");
        }
        catch (GenericEntityException e)
        {
            log.error("Error adding resolution: " + e, e);
            addError(getI18nBean().getText("admin.errors.error.adding.resolution"));
        }
    }

    private void addResolutions()
    {
        try
        {
            createNewEntity("Resolution", IssueFieldConstants.FIXED_RESOLUTION_ID, IssueFieldConstants.FIXED_RESOLUTION, "A fix for this issue is checked into the tree and tested.", null);
            createNewEntity("Resolution", IssueFieldConstants.WONTFIX_RESOLUTION_ID, IssueFieldConstants.WONTFIX_RESOLUTION, "The problem described is an issue which will never be fixed.", null);
            createNewEntity("Resolution", IssueFieldConstants.DUPLICATE_RESOLUTION_ID, IssueFieldConstants.DUPLICATE_RESOLUTION, "The problem is a duplicate of an existing issue.", null);
            createNewEntity("Resolution", IssueFieldConstants.INCOMPLETE_RESOLUTION_ID, IssueFieldConstants.INCOMPLETE_RESOLUTION, "The problem is not completely described.", null);
            createNewEntity("Resolution", IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION_ID, IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION, "All attempts at reproducing this issue failed, or not enough information was available to reproduce the issue. Reading the code produces no clues as to why this behavior would occur. If more information appears later, please reopen the issue.", null);
        }
        catch (GenericEntityException e)
        {
            log.error("Error adding resolution: " + e, e);
            addError(getI18nBean().getText("admin.errors.error.adding.resolution"));
        }
    }

    private void addPriorities()
    {
        try
        {
            createNewEntity("Priority", IssueFieldConstants.BLOCKER_PRIORITY_ID, IssueFieldConstants.BLOCKER_PRIORITY, "Blocks development and/or testing work, production could not run.", "/images/icons/priority_blocker.gif");
            createNewEntity("Priority", IssueFieldConstants.CRITICAL_PRIORITY_ID, IssueFieldConstants.CRITICAL_PRIORITY, "Crashes, loss of data, severe memory leak.", "/images/icons/priority_critical.gif");
            createNewEntity("Priority", IssueFieldConstants.MAJOR_PRIORITY_ID, IssueFieldConstants.MAJOR_PRIORITY, "Major loss of function.", "/images/icons/priority_major.gif");
            createNewEntity("Priority", IssueFieldConstants.MINOR_PRIORITY_ID, IssueFieldConstants.MINOR_PRIORITY, "Minor loss of function, or other problem where easy workaround is present.", "/images/icons/priority_minor.gif");
            createNewEntity("Priority", IssueFieldConstants.TRIVIAL_PRIORITY_ID, IssueFieldConstants.TRIVIAL_PRIORITY, "Cosmetic problem like misspelt words or misaligned text.", "/images/icons/priority_trivial.gif");
        }
        catch (GenericEntityException e)
        {
            log.error("Error adding priority: " + e, e);
            addError(getI18nBean().getText("admin.errors.error.adding.priority"));
        }
    }

    public static void createNewEntity(String entityName, int id, String name, String description, String iconurl) throws GenericEntityException
    {
        GenericValue gv = getOfBizDelegator().findByPrimaryKey(entityName, EasyMap.build("id", Long.toString(id)));

        if (gv == null)
        {
            Map fields = EasyMap.build("id", Long.toString(id), "sequence", new Long(id), "name", name, "iconurl", iconurl, "description", description);
            EntityUtils.createValue(entityName, fields);
        }
        else
        {
            log.warn("Could not doImport new " + entityName + " as one already exists with id: " + id);
        }
    }
}
