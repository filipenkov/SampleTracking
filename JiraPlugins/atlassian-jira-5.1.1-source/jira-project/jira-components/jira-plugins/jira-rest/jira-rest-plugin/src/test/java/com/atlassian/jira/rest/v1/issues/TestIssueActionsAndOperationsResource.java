package com.atlassian.jira.rest.v1.issues;

import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import junit.framework.TestCase;

import javax.ws.rs.core.Response;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.classextension.EasyMock.createMock;

/**
 * Test case for {@link com.atlassian.jira.rest.v1.issues.IssueActionsAndOperationsResource},
 *
 * @since v4.2
 */
public class TestIssueActionsAndOperationsResource extends TestCase
{
    private IssueActionsAndOperationsResource tested;

    private JiraAuthenticationContext mockAuthContext;


    @Override
    protected void setUp() throws Exception
    {
        mockAuthContext = createNiceMock(JiraAuthenticationContext.class);
    }

    private IssueActionsAndOperationsResource createTestedResource()
    {
        return new IssueActionsAndOperationsResource(mockAuthContext, null, createMock(ContextI18n.class), null, null, null, null, null);
    }

    public void testUnauthenticatedRequest()
    {
        replay(mockAuthContext);
        tested = createTestedResource();
        Response response = tested.getActionsAndOperationsResponse(null);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

}
