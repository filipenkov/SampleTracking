package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.rest.api.field.FieldBean;
import com.atlassian.jira.user.MockUser;
import org.easymock.classextension.EasyMock;

import java.net.URI;
import java.util.Collections;

import static com.atlassian.jira.rest.v2.issue.ListOfIssueLinkBeanMatcher.hasKeys;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Unit tests for issue links.
 *
 * @since v4.2
 */
public class IssueResourceIssueLinksTest extends IssueResourceTest
{
    private static final String USER_NAME = "aUser";
    private static final String ISSUE_BASE_URI = "http://localhost:8090/jira/rest/api/2.0/issue/";

    /**
     * Verifies that any found links are added to the IssueBean.
     *
     * @throws Exception if anything goes wrong
     */
    public void testIssueLinkAdded() throws Exception
    {
        User user = new MockUser(USER_NAME);
        expect(authContext.getLoggedInUser()).andReturn(user);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).andReturn(true);

        String linkTypeName = "duplicates";
        final String linkedIssueKey = "LNK-2";

        Issue linkedIssue = createMock(Issue.class);
        expect(linkedIssue.getKey()).andReturn(linkedIssueKey).anyTimes();
        replay(linkedIssue);

        IssueLinkType linkType = createMock(IssueLinkType.class);
        expect(linkType.getName()).andReturn(linkTypeName).anyTimes();
        expect(linkType.getInward()).andReturn("my inward").anyTimes();
        expect(linkType.getOutward()).andReturn("my outward").anyTimes();
        replay(linkType);

        LinkCollection linkCollection = createMock(LinkCollection.class);
        expect(linkCollection.getLinkTypes()).andReturn(singleton(linkType)).times(0, MAX_VALUE);
        expect(linkCollection.getOutwardIssues(linkTypeName)).andReturn(singletonList(linkedIssue));
        expect(linkCollection.getInwardIssues(linkTypeName)).andReturn(Collections.<Issue>emptyList());
        replay(linkCollection);

        expect(issueLinkManager.getLinkCollection(issue, user)).andReturn(linkCollection);

        bean.addField(EasyMock.eq("links"), hasKeys(linkedIssueKey));
        expectLastCall();

        expect(uriBuilder.build(contextUriInfo, IssueResource.class, linkedIssueKey)).andReturn(new URI(ISSUE_BASE_URI + linkedIssueKey));

        RendererManager rendererManager = null;

        replayMocks();
        IssueResource issueResource = createIssueResource();
        issueResource.addIssueLinks(issue, bean);

        // make sure the issue link was added
        verify(bean);
    }

    public void testNoLinkTypes() throws Exception
    {
        User user = new MockUser(USER_NAME);
        expect(authContext.getLoggedInUser()).andReturn(user);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).andReturn(true);

        String linkTypeName = "duplicates";
        final String linkedIssueKey = "LNK-2";

        Issue linkedIssue = createMock(Issue.class);
        expect(linkedIssue.getKey()).andReturn(linkedIssueKey).times(0, MAX_VALUE);
        replay(linkedIssue);

        IssueLinkType linkType = createMock(IssueLinkType.class);
        expect(linkType.getName()).andReturn(linkTypeName);
        replay(linkType);

        LinkCollection linkCollection = createMock(LinkCollection.class);
        expect(linkCollection.getLinkTypes()).andReturn(null).times(0, MAX_VALUE);
        replay(linkCollection);

        expect(issueLinkManager.getLinkCollection(issue, user)).andReturn(linkCollection);

        expect(uriBuilder.build(uriInfo, IssueResource.class, linkedIssueKey)).andReturn(new URI(ISSUE_BASE_URI + linkedIssueKey));

        replayMocks();
        IssueResource issueResource = createIssueResource();
        issueResource.addIssueLinks(issue,  bean);

        // make sure no issue link was added
        verify(bean);
    }

    public void testNoOutwardLinksOfType() throws Exception
    {
        User user = new MockUser(USER_NAME);
        expect(authContext.getLoggedInUser()).andReturn(user);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).andReturn(true);

        String linkTypeName = "duplicates";
        final String linkedIssueKey = "LNK-2";

        Issue linkedIssue = createMock(Issue.class);
        expect(linkedIssue.getKey()).andReturn(linkedIssueKey).times(0, MAX_VALUE);
        replay(linkedIssue);

        IssueLinkType linkType = createMock(IssueLinkType.class);
        expect(linkType.getName()).andReturn(linkTypeName).anyTimes();
        replay(linkType);

        LinkCollection linkCollection = createMock(LinkCollection.class);
        expect(linkCollection.getLinkTypes()).andReturn(singleton(linkType)).times(0, MAX_VALUE);
        expect(linkCollection.getOutwardIssues(linkTypeName)).andReturn(Collections.<Issue>emptyList());
        expect(linkCollection.getInwardIssues(linkTypeName)).andReturn(Collections.<Issue>emptyList());
        replay(linkCollection);

        expect(issueLinkManager.getLinkCollection(issue, user)).andReturn(linkCollection);

        // expect an empty list
        bean.addField("links", FieldBean.create("links", JiraDataTypes.getType(IssueFieldConstants.ISSUE_LINKS), Collections.<IssueLinkBean>emptyList()));
        expectLastCall();

        expect(uriBuilder.build(uriInfo, IssueResource.class, linkedIssueKey)).andReturn(new URI(ISSUE_BASE_URI + linkedIssueKey));
        
        replayMocks();
        IssueResource issueResource = createIssueResource();
        issueResource.addIssueLinks(issue, bean);

        // make sure the issue link list was set to empty
        verify(bean);
    }

}
