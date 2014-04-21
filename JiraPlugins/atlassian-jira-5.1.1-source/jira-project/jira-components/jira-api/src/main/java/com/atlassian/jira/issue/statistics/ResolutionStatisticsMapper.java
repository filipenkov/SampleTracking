package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;

public class ResolutionStatisticsMapper extends AbstractConstantStatisticsMapper
{
    public ResolutionStatisticsMapper(ConstantsManager constantsManager)
    {
        super(constantsManager);
    }

    protected String getConstantType()
    {
        return ConstantsManager.RESOLUTION_CONSTANT_TYPE;
    }

    protected String getIssueFieldConstant()
    {
        return IssueFieldConstants.RESOLUTION;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_RESOLUTION;
    }
}
