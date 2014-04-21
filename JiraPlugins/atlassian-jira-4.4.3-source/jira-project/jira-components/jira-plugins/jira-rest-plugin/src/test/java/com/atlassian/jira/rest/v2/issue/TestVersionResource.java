package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Date;

import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.BAD_NAME;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.FORBIDDEN;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertStatus;
import static java.util.EnumSet.of;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class TestVersionResource
{
    @Test
    public void testGetVersionBadId() throws Exception
    {
        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        ProjectService projectService = control.createMock(ProjectService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ContextI18n i18n = control.createMock(ContextI18n.class);

        User user = new MockUser("bbain");
        String translation = "translation";
        String badId = "badId";

        expect(context.getLoggedInUser()).andReturn(user);
        expect(i18n.getText("admin.errors.version.not.exist.with.id", badId)).andReturn(translation);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, i18n, versionBeanFactory, null, null);

        try
        {
            resource.getVersion(badId, null);
            fail("Expected exception.");
        }
        catch (NotFoundWebException e)
        {
            Response re = e.getResponse();
            assertResponseCacheNever(re);
            assertResponseBody(com.atlassian.jira.rest.api.util.ErrorCollection.of(translation), re);
        }

        control.verify();
    }

    @Test
    public void testGetVersionVersionDoesNotExist() throws Exception
    {
        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        ProjectService projectMgr = control.createMock(ProjectService.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ContextI18n i18n = control.createMock(ContextI18n.class);

        User user = new MockUser("bbain");
        String error = "error";
        long id = 1;

        expect(context.getLoggedInUser()).andReturn(user);
        expect(versionService.getVersionById(user, id)).andReturn(new VersionService.VersionResult(errors(error)));

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectMgr, context, i18n, versionBeanFactory, null, null);

        try
        {
            resource.getVersion(String.valueOf(id), null);
            fail("Expected exception.");
        }
        catch (NotFoundWebException e)
        {
            Response re = e.getResponse();
            assertResponseCacheNever(re);
            assertResponseBody(com.atlassian.jira.rest.api.util.ErrorCollection.of(error), re);
        }

        control.verify();
    }

    @Test
    public void testGetVersionGoodVersion() throws Exception
    {
        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);
        ContextI18n i18n = control.createMock(ContextI18n.class);

        User user = new MockUser("bbain");
        MockVersion version = new MockVersion(1718L, "Version1");
        long id = 1;

        VersionBean versionBean = new VersionBean();

        expect(context.getLoggedInUser()).andReturn(user);
        expect(versionService.getVersionById(user, id)).andReturn(new VersionService.VersionResult(ok(), version));
        expect(versionBeanFactory.createVersionBean(version, false)).andReturn(versionBean);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, i18n, versionBeanFactory, null, null);

        Response response = resource.getVersion(String.valueOf(id), null);
        assertResponseCacheNever(response);
        assertResponseBody(versionBean, response);

        control.verify();
    }

    @Test
    public void testCreateVersionWithoutProject() throws Exception
    {
        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);

        Response response = resource.createVersion(new VersionBean.Builder().build());
        assertResponseCacheNever(response);
        assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.create.no.project")), response);

        control.verify();
    }

    @Test
    public void testCreateVersionWithoutTwoReleaseDates() throws Exception
    {
        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);

        VersionBean.Builder builder = new VersionBean.Builder().setProject("brenden")
                .setReleaseDate(new Date()).setUserReleaseDate("10/12/2001");
        Response response = resource.createVersion(builder.build());
        assertResponseCacheNever(response);
        assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.create.two.dates")), response);

        control.verify();
    }
    
    @Test
    public void testCreateVersionBadProject() throws Exception
    {
        User user = new MockUser("bbain");
        String key = "BJB";

        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        expect(context.getLoggedInUser()).andReturn(user);
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(errors("Error"), null));

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);

        VersionBean.Builder builder = new VersionBean.Builder().setProject(key);
        try
        {
            resource.createVersion(builder.build());
            fail("Expected an exception");
        }
        catch (NotFoundWebException e)
        {
            assertResponseCacheNever(e.getResponse());
            assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.no.create.permission", key)), e.getResponse());
        }

        control.verify();
    }

    @Test
    public void testCreateVersionOkWithDate() throws Exception
    {
        User user = new MockUser("bbain");
        String key = "BJB";
        MockProject project = new MockProject(2829L);
        String name = "name";
        String desc = "description";
        Date releaseDate = new Date();
        VersionBean versionBean = new VersionBean();

        MockVersion version = new MockVersion(171718L, "Test Version");
        VersionService.CreateVersionValidationResult createResult =
                new VersionService.CreateVersionValidationResult(ok(), project, name, releaseDate, desc, null);

        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        expect(context.getLoggedInUser()).andReturn(user);
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(ok(), project));
        expect(versionService.validateCreateVersion(user, project, name, releaseDate, desc, null))
            .andReturn(createResult);
        expect(versionService.createVersion(user, createResult))
            .andReturn(version);
        expect(versionBeanFactory.createVersionBean(version, false)).andReturn(versionBean);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);
        VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setName(name).setDescription(desc)
                .setReleaseDate(releaseDate);

        Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(versionBean, actualResponse);

        control.verify();
    }

    @Test
    public void testCreateVersionOkWithUserDate() throws Exception
    {
        User user = new MockUser("bbain");
        String key = "BJB";
        MockProject project = new MockProject(2829L);
        String name = "name";
        String desc = "description";
        String releaseDate = "28829292";
        VersionBean versionBean = new VersionBean();

        MockVersion version = new MockVersion(171718L, "Test Version");
        VersionService.CreateVersionValidationResult createResult =
                new VersionService.CreateVersionValidationResult(ok(), project, name, new Date(), desc, null);

        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        expect(context.getLoggedInUser()).andReturn(user);
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(ok(), project));
        expect(versionService.validateCreateVersion(user, project, name, releaseDate, desc, null))
            .andReturn(createResult);
        expect(versionService.createVersion(user, createResult))
            .andReturn(version);
        expect(versionBeanFactory.createVersionBean(version, false)).andReturn(versionBean);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);
        VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setName(name).setDescription(desc)
                .setUserReleaseDate(releaseDate);

        Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(versionBean, actualResponse);

        control.verify();
    }

    @Test
    public void testCreateVersionValidationAuthErrors() throws Exception
    {
        User user = new MockUser("bbain");
        String errorMessage = "errorMessage";
        String key = "BJB";
        String name = "name";
        MockProject project = new MockProject(2829L);

        VersionService.CreateVersionValidationResult createResult =
                new VersionService.CreateVersionValidationResult(errors(errorMessage), of(FORBIDDEN));

        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        expect(context.getLoggedInUser()).andReturn(user);
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(ok(), project));
        expect(versionService.validateCreateVersion(user, project, name, (String) null, null, null))
            .andReturn(createResult);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);
        VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setName(name);

        Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.no.create.permission", key)), actualResponse);
        assertStatus(Response.Status.NOT_FOUND, actualResponse);

        control.verify();
    }

    @Test
    public void testCreateVersionValidationOtherErrors() throws Exception
    {
        User user = new MockUser("bbain");
        String errorMessage = "errorMessage";
        String key = "BJB";
        String name = "name";
        MockProject project = new MockProject(2829L);

        VersionService.CreateVersionValidationResult createResult =
                new VersionService.CreateVersionValidationResult(errors(errorMessage), of(BAD_NAME));

        IMocksControl control = createControl();
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);

        expect(context.getLoggedInUser()).andReturn(user);
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(ok(), project));
        expect(versionService.validateCreateVersion(user, project, name, (String) null, null, null))
            .andReturn(createResult);

        control.replay();

        VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), versionBeanFactory, null, null);
        VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setName(name);

        Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(restErrors(errorMessage), actualResponse);
        assertStatus(Response.Status.BAD_REQUEST, actualResponse);

        control.verify();
    }

    private static com.atlassian.jira.rest.api.util.ErrorCollection restErrors(String... errors)
    {
        return com.atlassian.jira.rest.api.util.ErrorCollection.of(errors);
    }

    private static ErrorCollection errors(String... errors)
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessages(Arrays.asList(errors));
        return collection;
    }

    private static ErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }
}
