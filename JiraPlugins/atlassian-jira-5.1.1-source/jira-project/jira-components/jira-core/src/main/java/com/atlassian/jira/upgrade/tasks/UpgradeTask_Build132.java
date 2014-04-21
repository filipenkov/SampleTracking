/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class UpgradeTask_Build132 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build132.class);
    private FieldScreenManager fieldScreenManager;

    public UpgradeTask_Build132(FieldScreenManager fieldScreenManager)
    {
        super(false);
        this.fieldScreenManager = fieldScreenManager;
    }

    public String getBuildNumber()
    {
        return "132";
    }

    public String getShortDescription()
    {
        return "Update Assign Issue Screen to something more apropriate - Workflow Screen";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        FieldScreen screen = fieldScreenManager.getFieldScreen(WorkflowTransitionUtil.VIEW_COMMENTASSIGN_ID);
        // The name of screen is not checked because it has been i18nised.
        if(screen != null && StringUtils.isNotEmpty(screen.getName()))
        {
            screen.setName(getI18nTextWithDefault("admin.field.screens.workflow.name", "Workflow Screen"));
            screen.setDescription(getI18nTextWithDefault("admin.field.screens.workflow.description", "This screen is used in the workflow and enables you to assign issues"));
            screen.store();
        }
        else
        {
            log.error("Upgrade error - unable to find screen with id: "+ WorkflowTransitionUtil.VIEW_COMMENTASSIGN_ID);
        }
     }

    private String getI18nTextWithDefault(String key, String defaultResult)
    {
        String result = getApplicationI18n().getText(key);
        if (result.equals(key))
        {
            return defaultResult;
        }
        else
        {
            return result;
        }
    }

    I18nHelper getApplicationI18n()
    {
        return new I18nBean();
    }
}