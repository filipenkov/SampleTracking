package com.atlassian.jira.bean;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.local.MockedComponentManagerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

public class TestSubTask extends MockedComponentManagerTestCase
{

    private SubTask subTask;

    IssueFactory newMockIssueFactory(GenericValue issueGv)
    {
        IssueFactory mock = createMock(IssueFactory.class);
        final MockIssue issueMock = new MockIssue();
        issueMock.setGenericValue(issueGv);
        expect(mock.getIssue(issueGv)).andReturn(issueMock);
        replay(mock);
        return mock;
    }

    @Test
    public void testGetters()
    {
        Long sequence = 0L;
        MockGenericValue subTaskIssue = new MockGenericValue("Issue", EasyMap.build("summary", "sub task test issue"));
        MockGenericValue parentIssue = new MockGenericValue("Issue", EasyMap.build("summary", "parent test issue"));
        addMock(IssueFactory.class, newMockIssueFactory(subTaskIssue));
        subTask = new SubTask(sequence, subTaskIssue, parentIssue);

        assertEquals(sequence, subTask.getSequence());
        assertEquals(subTaskIssue, subTask.getSubTaskIssueObject().getGenericValue());
        assertEquals(parentIssue, subTask.getParentIssue());
    }

}
