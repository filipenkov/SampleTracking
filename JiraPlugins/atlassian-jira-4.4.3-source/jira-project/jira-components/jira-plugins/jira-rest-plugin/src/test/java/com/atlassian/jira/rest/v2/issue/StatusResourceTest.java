package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpStatus;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Unit test for StatusResource.
 *
 * @since v4.2
 */
public class StatusResourceTest extends TestCase
{
    static final String JIRA_BASE_URI = "http://localhost:8090/jira/";

    private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private VelocityRequestContextFactory velocityReqCtxFactory;
    private ResourceUriBuilder uriBuilder;

    public void testStatusFound() throws Exception
    {
        final String statusName = "superstatus";
        final String statusId = "statusId";

        Status status = createMock(Status.class);
        expect(status.getId()).andReturn(statusId).times(0, Integer.MAX_VALUE);
        expect(status.getName()).andReturn(statusName).times(0, Integer.MAX_VALUE);
        expect(status.getIconUrl()).andReturn("iconUrl").times(0, Integer.MAX_VALUE);
        expect(status.getNameTranslation()).andReturn("Super status").times(0, Integer.MAX_VALUE);
        expect(status.getDescTranslation()).andReturn("This is a formidable status").times(0, Integer.MAX_VALUE);

        Request request = createMock(Request.class);
        UriInfo uriInfo = createMock(UriInfo.class);

        User user = new MockUser("mockUser");
        VelocityRequestContext velocityRequestContext = createMock(VelocityRequestContext.class);
        expect(authContext.getLoggedInUser()).andReturn(user).times(0, Integer.MAX_VALUE);
        expect(velocityReqCtxFactory.getJiraVelocityRequestContext()).andReturn(velocityRequestContext);
        expect(velocityRequestContext.getCanonicalBaseUrl()).andReturn("http://localhost:8090/jira");
        expect(constantsService.getStatusById(user, statusName)).andReturn(ServiceOutcomeImpl.ok(status));
        expect(uriBuilder.build(uriInfo, StatusResource.class, statusId)).andReturn(new URI(JIRA_BASE_URI + "/icon.jpg"));

        replayMocks(status, request, uriInfo, velocityRequestContext);
        StatusResource statusResource = new StatusResource(authContext, constantsService, velocityReqCtxFactory, uriBuilder);
        Response resp = statusResource.getStatus(statusName, request, uriInfo);

        assertEquals(HttpStatus.SC_OK, resp.getStatus());
        assertEquals(statusName, ((StatusBean) resp.getEntity()).getName());
    }

    public void testStatusNotFound() throws Exception
    {
        final String statusName = "superstatus";

        Request request = createMock(Request.class);
        UriInfo uriInfo = createMock(UriInfo.class);

        SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addError("somefield", "somemessage");

        User user = new MockUser("mockUser");
        expect(authContext.getLoggedInUser()).andReturn(user).times(0, Integer.MAX_VALUE);
        expect(constantsService.getStatusById(user, statusName)).andReturn(new ServiceOutcomeImpl<Status>(errors, null));
        expect(uriBuilder.build(uriInfo, StatusResource.class, statusName)).andReturn(new URI(JIRA_BASE_URI + "/icon.jpg"));

        replayMocks(request, uriInfo);
        StatusResource statusResource = new StatusResource(authContext, constantsService, velocityReqCtxFactory, uriBuilder);
        try
        {
            statusResource.getStatus(statusName, request, uriInfo);
            fail("Expected NotFoundWebException");
        }
        catch (NotFoundWebException e)
        {
            // success
        }
    }
    
    @Override
    protected void setUp() throws Exception
    {
        authContext = createMock(JiraAuthenticationContext.class);
        constantsService = createMock(ConstantsService.class);
        velocityReqCtxFactory = createMock(VelocityRequestContextFactory.class);
        uriBuilder = createMock(ResourceUriBuilder.class);
    }

    protected void replayMocks(Object... mocks)
    {
        replay(mocks);
        replay(
                authContext,
                constantsService,
                velocityReqCtxFactory,
                uriBuilder
        );
    }
}
