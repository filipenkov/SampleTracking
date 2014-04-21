package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.rest.api.field.FieldBean;
import org.easymock.EasyMock;

import java.net.URI;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Unit tests for the REST API's parent/subtask linking feature.
 *
 * @since v4.2
 */
public class IssueResourceSubtaskTest extends IssueResourceTest
{
    private static final String ISSUE_BASE_URI = "http://localhost:8090/jira/rest/api/2.0/issue/";

    public void testParentLinkAdded() throws Exception
    {
        final String parentIssueKey = "PAR-1";

        Issue parent = createMock(Issue.class);
        expect(parent.getKey()).andReturn(parentIssueKey).times(0, Integer.MAX_VALUE);
        
        expect(issue.getParentObject()).andReturn(parent);
        expect(issue.getSubTaskObjects()).andReturn(Collections.<Issue>emptyList());
        bean.addField(EasyMock.eq("parent"), IssueLinkBeanMatcher.hasKey(parentIssueKey));
        bean.addField("sub-tasks", FieldBean.create("sub-tasks", JiraDataTypes.getType(IssueFieldConstants.ISSUE_LINKS), Collections.<IssueLinkBean>emptyList()));

        expect(uriBuilder.build(contextUriInfo, IssueResource.class, parentIssueKey)).andReturn(new URI(ISSUE_BASE_URI + parentIssueKey));

        replayMocks(parent);
        IssueResource issueResource = createIssueResource();
        issueResource.addParentSubtaskLinks(issue, bean);

        verify(bean);
    }

    public void testSubtaskLinkAdded() throws Exception
    {
        final String subtaskIssueKey = "SUB-1";

        Issue subtask = createMock(Issue.class);
        expect(subtask.getKey()).andReturn(subtaskIssueKey).times(0, Integer.MAX_VALUE);

        expect(issue.getParentObject()).andReturn(null);
        expect(issue.getSubTaskObjects()).andReturn(singletonList(subtask));

        bean.addField(EasyMock.eq("sub-tasks"), ListOfIssueLinkBeanMatcher.hasKeys(subtaskIssueKey));
        expect(uriBuilder.build(contextUriInfo, IssueResource.class, subtaskIssueKey)).andReturn(new URI(ISSUE_BASE_URI + subtaskIssueKey));

        replayMocks(subtask);
        IssueResource issueResource = createIssueResource();
        issueResource.addParentSubtaskLinks(issue, bean);

        verify(bean);
    }

    protected void replayMocks(Object... mocks)
    {
        super.replayMocks();
        replay(mocks);
    }
}
