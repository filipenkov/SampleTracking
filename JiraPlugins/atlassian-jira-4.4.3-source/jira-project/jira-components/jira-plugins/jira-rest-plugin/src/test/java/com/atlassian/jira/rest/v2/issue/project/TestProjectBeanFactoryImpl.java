package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.rest.v2.issue.ProjectResource;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;

import static java.lang.String.format;
import static junit.framework.Assert.fail;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @since v4.4
 */
public class TestProjectBeanFactoryImpl
{
    @Test
    public void testShortProjectParams() throws Exception
    {
        IMocksControl control = createControl();
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        UserManager userManager = control.createMock(UserManager.class);
        UriInfo uriInfo = control.createMock(UriInfo.class);
        ResourceUriBuilder builder = control.createMock(ResourceUriBuilder.class);
        ProjectRoleService projectRoleService = control.createMock(ProjectRoleService.class);
        JiraAuthenticationContext authenticationContext = control.createMock(JiraAuthenticationContext.class);

        ProjectBeanFactoryImpl factory = new ProjectBeanFactoryImpl(versionBeanFactory, uriInfo, builder, projectRoleService, authenticationContext, userManager);
        String key = "key";
        String name = "name";
        URI selfUri = new URI("http://wwwww.com.com");

        control.replay();

        assertEquals(new ProjectBean(selfUri, key, name, null, null, 1, null, null, null, null, new HashMap<String, URI>()), factory.shortProject(key, name, selfUri));

        control.verify();
    }

    @Test
    public void testShortProject() throws Exception
    {
        IMocksControl control = createControl();
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        UserManager userManager = control.createMock(UserManager.class);
        UriInfo uriInfo = control.createMock(UriInfo.class);
        ResourceUriBuilder builder = control.createMock(ResourceUriBuilder.class);
        ProjectRoleService projectRoleService = control.createMock(ProjectRoleService.class);
        JiraAuthenticationContext authenticationContext = control.createMock(JiraAuthenticationContext.class);

        ProjectBeanFactoryImpl factory = new ProjectBeanFactoryImpl(versionBeanFactory, uriInfo, builder, projectRoleService, authenticationContext, userManager);
        String key = "key";
        String name = "name";
        URI selfUri = new URI("http://wwwww.com.com");

        expect(builder.build(uriInfo, ProjectResource.class, key)).andReturn(selfUri);

        MockProject project = new MockProject();
        project.setKey(key);
        project.setName(name);
        project.setDescription("This description should be ignored.");

        control.replay();

        assertEquals(new ProjectBean(selfUri, key, name, null, null, 1, null, null, null, null, new HashMap<String, URI>()), factory.shortProject(project));

        control.verify();
    }
    
    private static void assertEquals(ProjectBean o1, ProjectBean o2)
    {
        if (!EqualsBuilder.reflectionEquals(o1, o2))
        {
            fail(format("%s != %s", o1, o2));
        }
    }
}
