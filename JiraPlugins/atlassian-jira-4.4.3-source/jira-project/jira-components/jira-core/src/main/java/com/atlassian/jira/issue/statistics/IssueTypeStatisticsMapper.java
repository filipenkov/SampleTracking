package com.atlassian.jira.issue.statistics;

import com.atlassian.core.ofbiz.comparators.OFBizFieldComparator;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;

import java.util.Comparator;

public class IssueTypeStatisticsMapper extends AbstractConstantStatisticsMapper
{
    public IssueTypeStatisticsMapper(ConstantsManager constantsManager)
    {
        super(constantsManager);
    }

    protected String getConstantType()
    {
        return ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE;
    }

    protected String getIssueFieldConstant()
    {
        return IssueFieldConstants.ISSUE_TYPE;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_TYPE;
    }

    public Comparator getComparator()
    {
        return new OFBizFieldComparator("name");
    }
}
