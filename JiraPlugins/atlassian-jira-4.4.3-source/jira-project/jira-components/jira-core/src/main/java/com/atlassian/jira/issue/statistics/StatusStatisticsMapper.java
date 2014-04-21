package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;

public class StatusStatisticsMapper extends AbstractConstantStatisticsMapper
{
    public StatusStatisticsMapper(ConstantsManager constantsManager)
    {
        super(constantsManager);
    }

    protected String getConstantType()
    {
        return ConstantsManager.STATUS_CONSTANT_TYPE;
    }

    protected String getIssueFieldConstant()
    {
        return IssueFieldConstants.STATUS;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_STATUS;
    }
}
