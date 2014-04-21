package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Avatar;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Component;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueType;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.ProjectClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.User;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Version;
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Func test for ProjectResource.
 *
 * @since v4.2
 */
@WebTest ( { Category.FUNC_TEST, Category.REST })
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

    public void testViewProjectNoPermissionToView() throws Exception
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
        assertThat(response.entity.errorMessages, hasItem(String.format("No project could be found with key '%s'.", "MKY")));
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
        assertThat(response.entity.errorMessages, hasItem(String.format("No project could be found with key '%s'.", "MKY")));
    }

    public void testGetAvatars() throws Exception
    {
        Map<String, List<Avatar>> avatars = projectClient.getAvatars("HSP");

        List<Avatar> systemAvatars = avatars.get("system");
        List<Avatar> customAvatars = avatars.get("custom");

        assertEquals(systemAvatars.size(), 12);
        assertEquals(customAvatars.size(), 1);
    }

    private Project makeSimple(Project project)
    {
        return project.components(null).assigneeType(null).description(null).lead(null).versions(null).issueTypes(null).roles(null);
    }

    private void assertCantSeeProject(String username, String key)
    {
        if (username == null)
        {
            projectClient.anonymous();
        }
        else
        {
            projectClient.loginAs(username);
        }

        Response response = projectClient.getResponse(key);
        assertEquals(404, response.statusCode);
        assertThat(response.entity.errorMessages, hasItem(String.format("No project could be found with key '%s'.", key)));
    }

    private void checkProject(Project expectedProject, boolean fred, boolean anonymous)
    {
        Project actualProject = projectClient.loginAs("admin").get(expectedProject.key);
        assertThat(actualProject, equalTo(expectedProject));

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
                new IssueType().self(getRestApiUrl("issuetype/1")).id("1").name("Bug").iconUrl(getBaseUrlPlus("/images/icons/bug.gif")).description("A problem which impairs or prevents the functions of the product."),
                new IssueType().self(getRestApiUrl("issuetype/2")).id("2").name("New Feature").iconUrl(getBaseUrlPlus("/images/icons/newfeature.gif")).description("A new feature of the product, which has yet to be developed."),
                new IssueType().self(getRestApiUrl("issuetype/3")).id("3").name("Task").iconUrl(getBaseUrlPlus("/images/icons/task.gif")).description("A task that needs to be done."),
                new IssueType().self(getRestApiUrl("issuetype/4")).id("4").name("Improvement").iconUrl(getBaseUrlPlus("/images/icons/improvement.gif")).description("An improvement or enhancement to an existing feature or task.")
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
                .id("10001")
                .lead(createUserAdmin()).description("project for monkeys")
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("MKY"))
                .components(Collections.<Component>emptyList()).versions(Collections.<Version>emptyList())
                .avatarUrls(createProjectAvatarUrls(10001L, 10011L));
    }

    private Project createProjectHid()
    {
        return new Project().self(getRestApiUri("project/HID")).key("HID").name("HIDDEN")
                .id("10110")
                .description("")
                .components(Collections.<Component>emptyList()).versions(Collections.<Version>emptyList())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("HID"))
                .issueTypes(createStandardIssueTypes())
                .lead(createUserAdmin())
                .avatarUrls(createProjectAvatarUrls(10110L, 10011L));
    }

    private Project createProjectHsp()
    {
        return new Project().self(getRestApiUri("project/HSP")).key("HSP").name("homosapien")
                .id("10000")
                .description("project for homosapiens")
                .versions(createVersionsHsp()).components(createComponentsHsp())
                .issueTypes(createStandardIssueTypes())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("HSP"))
                .lead(createUserAdmin())
                .avatarUrls(createProjectAvatarUrls(10000L, 10140L));
    }

    private Project createProjectFred()
    {
        return new Project().self(getRestApiUri("project/FRED")).key("FRED").name("Fred")
                .id("10111")
                .description("")
                .components(Collections.<Component>emptyList())
                .versions(Collections.<Version>emptyList())
                .issueTypes(createStandardIssueTypes())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("FRED"))
                .lead(createUserFred())
                .avatarUrls(createProjectAvatarUrls(10111L, 10011L));
    }

    private Project createProjectDodo()
    {
        return new Project().self(getRestApiUri("project/DD")).key("DD").name("Dead Leader")
                .id("10112")
                .description("")
                .components(Collections.<Component>emptyList())
                .versions(Collections.<Version>emptyList())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("DD"))
                .lead(createUserDodo())
                .avatarUrls(createProjectAvatarUrls(10112L, 10011L));
    }

    private Project createProjectAtl()
    {
        return new Project().self(getRestApiUri("project/ATL")).key("ATL").name("Atlassian")
                .id("10010")
                .description("")
                .lead(createUserAdmin()).components(createComponentsAtlShort())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("ATL"))
                .versions(createVersionsAtl())
                .avatarUrls(createProjectAvatarUrls(10010L, 10011L));
    }

    private Map<String, String> createProjectAvatarUrls(final Long projectId, final Long avatarId)
    {
        return MapBuilder.<String, String>newBuilder()
            .add("16x16", getBaseUrlPlus("secure/projectavatar?size=small&pid=" + projectId + "&avatarId=" + avatarId))
            .add("48x48", getBaseUrlPlus("secure/projectavatar?pid=" + projectId + "&avatarId=" + avatarId))
            .toMap();
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

        return new User()
                .self(createUserUri("admin"))
                .name("admin")
                .displayName("Administrator")
                .active(true)
                .avatarUrls(createAvatarUrls(10062L));
    }

    private User createUserFred()
    {
        return new User()
                .self(createUserUri("fred"))
                .name("fred")
                .displayName("Fred Normal")
                .active(true)
                .avatarUrls(createAvatarUrls(10062L));
    }

    private User createUserDodo()
    {
        return new User()
                .self(createUserUri("dodo"))
                .name("dodo")
                .displayName("dodo")
                .active(false)
                .avatarUrls(createAvatarUrls(10063L));
    }

    private Map<String, String> createAvatarUrls(final Long avatarId)
    {
        return MapBuilder.<String, String>newBuilder()
                .add("16x16", getBaseUrlPlus("secure/useravatar?size=small&avatarId=" + avatarId))
                .add("48x48", getBaseUrlPlus("secure/useravatar?avatarId=" + avatarId))
                .toMap();
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

    private static <T, S extends T> List<T> list(S... element)
    {
        return Lists.<T>newArrayList(element);
    }
}
