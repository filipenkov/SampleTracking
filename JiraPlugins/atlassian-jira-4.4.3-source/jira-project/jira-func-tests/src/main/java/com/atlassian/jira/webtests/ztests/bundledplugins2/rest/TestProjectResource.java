package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Component;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Errors;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueType;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.ProjectClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.User;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Version;
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Func test for ProjectResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestProjectResource extends RestFuncTest
{
    private ProjectClient projectClient;

    public void testViewProject() throws Exception
    {
        //Check the HSP project.
        checkProject(createProjectMky(), true, false);
        checkProject(createProjectAtl(), true, true);
        checkProject(createProjectHid(), false, false);
        checkProject(createProjectFred(), true, false);
        checkProject(createProjectDodo(), false, false);
    }

    public void testViewProjectNoPermission() throws Exception
    {
        assertCantSeeProject("fred", "HID");
        assertCantSeeProject(null, "MKY");
        assertCantSeeProject(null, "HID");
        assertCantSeeProject(null, "FRED");
    }

    public void testViewProjects() throws Exception
    {
        Project projectAtl = makeSimple(createProjectAtl());
        Project projectFred = makeSimple(createProjectFred());
        Project projectHsp = makeSimple(createProjectHsp());
        Project projectHid = makeSimple(createProjectHid());
        Project projectMky = makeSimple(createProjectMky());
        Project projectDodo = makeSimple(createProjectDodo());

        //System admin should see all projects.
        assertEquals(list(projectAtl, projectDodo, projectFred, projectHid, projectHsp, projectMky), projectClient.getProjects());

        //Fred should see projects he can see or admin.
        assertEquals(list(projectAtl, projectFred, projectHsp, projectMky), projectClient.loginAs("fred").getProjects());

        //Anonymous should only see one project.
        assertEquals(list(projectAtl), projectClient.anonymous().getProjects());
    }

    public void testViewProjectDoesNotExist() throws Exception
    {
        Response respXXX = projectClient.getResponse("XXX");
        assertEquals(404, respXXX.statusCode);
        assertEquals(1, respXXX.entity.errorMessages.size());
        assertTrue(respXXX.entity.errorMessages.contains("No project could be found with key 'XXX'."));
    }

    public void testViewProjectVersions() throws Exception
    {
        //Make sure no versions works.
        assertTrue(projectClient.getVersions("MKY").isEmpty());

        //Make sure it works for a particular project.
        assertEquals(createVersionsAtl(), projectClient.getVersions("ATL"));
    }

    public void testViewProjectVersionsAnonymous() throws Exception
    {
        assertEquals(createVersionsAtl(), projectClient.anonymous().getVersions("ATL"));

        Response response = projectClient.getVersionsResponse("MKY");
        assertEquals(404, response.statusCode);
        assertEquals(new Errors().addError("You cannot view this project."),
                response.entity);
    }

    public void testViewProjectComponents() throws Exception
    {
        //Make sure no components works.
        assertTrue(projectClient.getComponents("MKY").isEmpty());

        //Make sure it works for a particular project.
        assertEquals(createComponentsHsp(), projectClient.getComponents("HSP"));
    }

    public void testViewProjectComponentsAnonymous() throws Exception
    {
        assertEquals(createComponentsAtlFull(), projectClient.anonymous().getComponents("ATL"));

        Response response = projectClient.getVersionsResponse("MKY");
        assertEquals(404, response.statusCode);
        assertEquals(new Errors().addError("You cannot view this project."),
                response.entity);
    }

    private Project makeSimple(Project project)
    {
        return project.components(null).assigneeType(null).description(null).lead(null).versions(null).issueTypes(null).roles(new HashMap<String, String>());
    }

    private void assertCantSeeProject(String username, String hid)
    {
        if (username == null)
        {
            projectClient.anonymous();
        }
        else
        {
            projectClient.loginAs(username);
        }
        Response response = projectClient.getResponse(hid);
        assertEquals(404, response.statusCode);
        assertEquals(new Errors().addError("You cannot view this project."), response.entity);
    }

    private void checkProject(Project expectedProject, boolean fred, boolean anonymous)
    {
        Project actualProject = projectClient.loginAs("admin").get(expectedProject.key);
        assertEquals(expectedProject, actualProject);

        if (fred)
        {
            actualProject = projectClient.loginAs("fred").get(expectedProject.key);
            assertEquals(expectedProject, actualProject);
        }

        if (anonymous)
        {
            actualProject = projectClient.anonymous().get(expectedProject.key);
            assertEquals(expectedProject, actualProject);
        }
    }

    private List<IssueType> createStandardIssueTypes()
    {
        return CollectionBuilder.newBuilder(
            new IssueType().self(getRestApiUrl("issueType/1")).name("Bug"),
            new IssueType().self(getRestApiUrl("issueType/2")).name("New Feature"),
            new IssueType().self(getRestApiUrl("issueType/3")).name("Task"),
            new IssueType().self(getRestApiUrl("issueType/4")).name("Improvement")
        ).asList();
    }

    private Map<String, String> createStandardRoles(String projectKey)
    {
        return MapBuilder.<String, String>newBuilder()
                .add("Users", getRestApiUri("project", projectKey, "role", "10000").toString())
                .add("Developers", getRestApiUri("project", projectKey, "role", "10001").toString())
                .add("Administrators", getRestApiUri("project", projectKey, "role", "10002").toString())
                .toMap();
    }

    private Project createProjectMky()
    {
        return new Project().self(getRestApiUri("project/MKY")).key("MKY").name("monkey")
                .lead(createUserAdmin()).description("project for monkeys")
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("MKY"))
                .components(Collections.<Component>emptyList()).versions(Collections.<Version>emptyList());
    }

    private Project createProjectHid()
    {
        return new Project().self(getRestApiUri("project/HID")).key("HID").name("HIDDEN")
                .components(Collections.<Component>emptyList()).versions(Collections.<Version>emptyList())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("HID"))
                .issueTypes(createStandardIssueTypes())
                .lead(createUserAdmin());
    }

    private Project createProjectHsp()
    {
        return new Project().self(getRestApiUri("project/HSP")).key("HSP").name("homosapien")
                .description("project for homosapiens")
                .versions(createVersionsHsp()).components(createComponentsHsp())
                .issueTypes(createStandardIssueTypes())                
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("HSP"))
                .lead(createUserAdmin());
    }

    private Project createProjectFred()
    {
        return new Project().self(getRestApiUri("project/FRED")).key("FRED").name("Fred")
                .components(Collections.<Component>emptyList())
                .versions(Collections.<Version>emptyList())
                .issueTypes(createStandardIssueTypes())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("FRED"))
                .lead(createUserFred());
    }

    private Project createProjectDodo()
    {
        return new Project().self(getRestApiUri("project/DD")).key("DD").name("Dead Leader")
                .components(Collections.<Component>emptyList())
                .versions(Collections.<Version>emptyList())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("DD"))
                .lead(createUserDodo());
    }

    private Project createProjectAtl()
    {
        return new Project().self(getRestApiUri("project/ATL")).key("ATL").name("Atlassian")
                .lead(createUserAdmin()).components(createComponentsAtlShort())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("ATL"))
                .versions(createVersionsAtl());
    }

    private List<Version> createVersionsAtl()
    {
        CollectionBuilder<Version> builder = CollectionBuilder.newBuilder();

        builder.add(new Version().self(createVersionUri(10014)).archived(true)
                .released(false).name("Five").description("Five").id(10014L));

        builder.add(new Version().self(createVersionUri(10013)).archived(true)
                .released(true).name("Four").description("Four")
                .releaseDate("09/Mar/11").id(10013L));

        builder.add(new Version().self(createVersionUri(10012)).archived(false)
                .released(true).name("Three")
                .releaseDate("09/Mar/11").id(10012L));

        builder.add(new Version().self(createVersionUri(10011)).archived(false)
                .released(false).name("Two").description("Description").id(10011L));

        builder.add(new Version().self(createVersionUri(10010)).archived(false)
                .released(false).name("One").releaseDate("01/Mar/11").overdue(true).id(10010L));

        return builder.asList();
    }

    private List<Version> createVersionsHsp()
    {
        CollectionBuilder<Version> builder = CollectionBuilder.newBuilder();

        builder.add(new Version().self(createVersionUri(10000)).archived(false)
                .released(false).name("New Version 1").description("Test Version Description 1").id(10000L));

        builder.add(new Version().self(createVersionUri(10001)).archived(false)
                .released(false).name("New Version 4").description("Test Version Description 4").id(10001L));

        builder.add(new Version().self(createVersionUri(10002)).archived(false)
                .released(false).name("New Version 5").description("Test Version Description 5").id(10002L));

        return builder.asList();
    }

    private List<Component> createComponentsHsp()
    {
        CollectionBuilder<Component> builder = CollectionBuilder.newBuilder();

        builder.add(new Component().self(createComponentUri(10000)).id(10000L).name("New Component 1").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));
        builder.add(new Component().self(createComponentUri(10001)).id(10001L).name("New Component 2").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));
        builder.add(new Component().self(createComponentUri(10002)).id(10002L).name("New Component 3").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));

        return builder.asList();
    }

    private List<Component> createComponentsAtlFull()
    {
        CollectionBuilder<Component> builder = CollectionBuilder.newBuilder();

        builder.add(new Component().self(createComponentUri(10003)).id(10003L).name("New Component 4").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));
        builder.add(new Component().self(createComponentUri(10004)).id(10004L).name("New Component 5").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));

        return builder.asList();
    }

    private List<Component> createComponentsAtlShort()
    {
        CollectionBuilder<Component> builder = CollectionBuilder.newBuilder();

        builder.add(new Component().self(createComponentUri(10003)).id(10003L).name("New Component 4"));
        builder.add(new Component().self(createComponentUri(10004)).id(10004L).name("New Component 5"));

        return builder.asList();
    }

    private User createUserAdmin()
    {
        return new User().self(createUserUri("admin")).name("admin").displayName("Administrator").active(true);
    }

    private User createUserFred()
    {
        return new User().self(createUserUri("fred")).name("fred").displayName("Fred Normal").active(true);
    }

    private User createUserDodo()
    {
        return new User().self(createUserUri("dodo")).name("dodo").displayName("dodo").active(false);
    }

    private URI createVersionUri(long id)
    {
        return getRestApiUri("version", String.valueOf(id));
    }

    private URI createComponentUri(long id)
    {
        return getRestApiUri("component", String.valueOf(id));
    }

    private URI createUserUri(String name)
    {
        return getRestApiUri(String.format("user?username=%s", name));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        projectClient = new ProjectClient(getEnvironmentData());
        administration.restoreData("TestProjectResource.xml");
    }

    @Override
    protected void tearDownTest()
    {
        super.tearDownTest();
        projectClient = null;
        administration = null;
    }

    private static <T, S extends T> List<T> list(S...element)
    {
        return Lists.<T>newArrayList(element);
    }
}
