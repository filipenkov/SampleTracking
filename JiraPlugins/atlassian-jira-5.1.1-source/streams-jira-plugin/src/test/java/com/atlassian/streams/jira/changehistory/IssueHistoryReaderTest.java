package com.atlassian.streams.jira.changehistory;

import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith (MockitoJUnitRunner.class)
public class IssueHistoryReaderTest
{
    @Mock
    private ChangeHistoryManager changeHistorymanager;

    @Test(expected = NoSuchMethodException.class)
    public void reflectionInBulkIssueHistoryReaderShouldBeRemovedOnceStreamsGetsBuiltAgainstJira5_1() throws Exception
    {
        // if this test is failing, then you have probably updated Streams to compile against JIRA 5.1 or later instead
        // of 5.0. you should take this opportunity to remove the reflection that is happening in BulkIssueHistoryReader.
        // you can then delete this test altogether.
        new BulkIssueHistoryReader(changeHistorymanager);
    }
}
