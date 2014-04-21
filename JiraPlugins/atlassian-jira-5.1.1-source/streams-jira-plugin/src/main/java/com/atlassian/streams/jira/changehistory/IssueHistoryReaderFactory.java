package com.atlassian.streams.jira.changehistory;

import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring factory bean that instantiates the best ChangeHistoryReader implementation depending on JIRA capabilities.
 *
 * @since v5.1.2
 */
@SuppressWarnings ("UnusedDeclaration")
public class IssueHistoryReaderFactory implements FactoryBean
{
    private final ChangeHistoryManager changeHistoryManager;

    public IssueHistoryReaderFactory(ChangeHistoryManager changeHistoryManager)
    {
        this.changeHistoryManager = changeHistoryManager;
    }

    @Override
    public Class getObjectType()
    {
        return IssueHistoryReader.class;
    }

    @Override
    public boolean isSingleton()
    {
        return false;
    }

    @Override
    public IssueHistoryReader getObject()
    {
        try
        {
            return new BulkIssueHistoryReader(changeHistoryManager);
        }
        catch (NoSuchMethodException e)
        {
            // fall back to single-issue read
            return new SingleIssueHistoryReader(changeHistoryManager);
        }
    }
}
