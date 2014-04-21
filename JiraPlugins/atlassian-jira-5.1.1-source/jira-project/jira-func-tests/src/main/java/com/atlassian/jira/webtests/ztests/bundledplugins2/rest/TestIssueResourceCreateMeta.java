package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.FieldMetaData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueCreateMeta;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueCreateMeta.Expand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceCreateMeta extends RestFuncTest
{
    private IssueClient issueClient;

    private static final Set<String> defaultRequiredFields = CollectionBuilder.<String>newBuilder()
            .add("project")
            .add("versions")
            .add("components")
            .add("description")
            .add("duedate")
            .add("environment")
            .add("fixVersions")
            .add("issuetype")
            .add("labels")
            .add("worklog")
            .add("priority")
            .add("reporter")
            .add("security")
            .add("summary")
            .add("timetracking")
            .asSet();

    private static final Set<String> subtaskRequiredFields = CollectionBuilder.<String>newBuilder()
            .addAll(defaultRequiredFields)
            .add("parent")
            .asSet();

    private static final Set<String> testBugRequiredFields = CollectionBuilder.<String>newBuilder()
            .add("project")
            .add("versions")
            .add("description")
            .add("issuetype")
            .add("priority")
            .add("reporter")
            .add("summary")
            .add("customfield_10000")
            .asSet();

    private static final Map<String, IssueCreateMeta.JsonType> sharedFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
            .add("project", IssueCreateMeta.JsonType.system("project", "project"))
            .add("versions", IssueCreateMeta.JsonType.systemArray("version", "versions"))
            .add("assignee", IssueCreateMeta.JsonType.system("user", "assignee"))
            .add("attachment", IssueCreateMeta.JsonType.systemArray("attachment", "attachment"))
//            .add("comment", IssueCreateMeta.JsonType.systemArray("comment", "comment"))
            .add("description", IssueCreateMeta.JsonType.system("string", "description"))
            .add("environment", IssueCreateMeta.JsonType.system("string", "environment"))
            .add("fixVersions", IssueCreateMeta.JsonType.systemArray("version", "fixVersions"))
            .add("issuetype", IssueCreateMeta.JsonType.system("issuetype", "issuetype"))
            .add("issuelinks", IssueCreateMeta.JsonType.systemArray("issuelinks", "issuelinks"))
            .add("worklog", IssueCreateMeta.JsonType.systemArray("worklog", "worklog"))
            .add("priority", IssueCreateMeta.JsonType.system("priority", "priority"))
            .add("reporter", IssueCreateMeta.JsonType.system("user", "reporter"))
            .add("resolution", IssueCreateMeta.JsonType.system("resolution", "resolution"))
            .add("security", IssueCreateMeta.JsonType.system("securitylevel", "security"))
            .add("summary", IssueCreateMeta.JsonType.system("string", "summary"))
            .add("customfield_10000", IssueCreateMeta.JsonType.custom("date", "com.atlassian.jira.plugin.system.customfieldtypes:datepicker", 10000L))
            .add("customfield_10001", IssueCreateMeta.JsonType.custom("datetime", "com.atlassian.jira.plugin.system.customfieldtypes:datetime", 10001L))
            .toImmutableMap();

    private static final Map<String, IssueCreateMeta.JsonType> defaultFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
            .addAll(sharedFieldTypes)
            .add("duedate", IssueCreateMeta.JsonType.system("date", "duedate"))
            .add("components", IssueCreateMeta.JsonType.systemArray("component", "components"))
            .add("labels", IssueCreateMeta.JsonType.systemArray("string", "labels"))
            .add("timetracking",IssueCreateMeta.JsonType.system("timetracking", "timetracking"))
            .toImmutableMap();

    private static final Map<String, IssueCreateMeta.JsonType> subTaskFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
            .addAll(defaultFieldTypes)
            .add("parent", IssueCreateMeta.JsonType.system("issuelink", "parent"))
            .toImmutableMap();

    private static final Map<String, IssueCreateMeta.JsonType> testBugFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
            .addAll(sharedFieldTypes)
            .toImmutableMap();
    
    private static final Map<String, String> fieldsNamesValues = MapBuilder.<String, String>newBuilder()
            .add("assignee", "Assignee")
            .add("attachment", "Attachment")
            .add("components", "Component/s")
            .add("customfield_10000", "datePickerCF")
            .add("customfield_10001", "dateTimeCF")
            .add("description", "Description")
            .add("duedate", "Due Date")
            .add("environment", "Environment")
            .add("fixVersions", "Fix Version/s")
            .add("issuelinks", "Linked Issues")
            .add("issuetype", "Issue Type")
            .add("labels", "Labels")
            .add("parent", "Parent")
            .add("priority", "Priority")
            .add("project", "Project")
            .add("reporter", "Reporter")
            .add("resolution", "Resolution")
            .add("security", "Security Level")
            .add("summary", "Summary")
            .add("timetracking", "Time Tracking")
            .add("versions", "Affects Version/s")
            .add("worklog", "Log Work")
            .toImmutableMap();
    
    public void testWithNullParams() throws Exception
    {
        // Should return all projects and issue types visible to the user
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, null, null, null, Expand.fields);

        assertEquals(2, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        final IssueCreateMeta.Project project2 = meta.projects.get(1);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        final IssueCreateMeta.IssueType project2Improvement = project2.issuetypes.get(1);
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(2);
        assertBug(project2Bug);
        assertImprovement(project2Improvement);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project1Bug.fields, defaultRequiredFields);
        assertRequiredFields(project1NewFeature.fields, defaultRequiredFields);
        assertRequiredFields(project1Task.fields, defaultRequiredFields);
        assertRequiredFields(project1Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project1SubTask.fields, subtaskRequiredFields);

        assertRequiredFields(project2Bug.fields, testBugRequiredFields);
        assertRequiredFields(project2Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);

        assertFieldNamesAndTypes(project2Bug.fields, testBugFieldTypes);
        assertFieldNamesAndTypes(project2Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithProjectIds() throws Exception
    {
        // Should return just the project specified, and all its issue types
        final List<StringList> projectIds = Arrays.asList(new StringList("10000"));
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project2 = meta.projects.get(0);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        final IssueCreateMeta.IssueType project2Improvement = project2.issuetypes.get(1);
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(2);
        assertBug(project2Bug);
        assertImprovement(project2Improvement);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project2Bug.fields, testBugRequiredFields);
        assertRequiredFields(project2Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project2Bug.fields, testBugFieldTypes);
        assertFieldNamesAndTypes(project2Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithProjectKeys() throws Exception
    {
        // Should return just the project specified, and all its issue types
        final List<StringList> projectKeys = Arrays.asList(new StringList("PEXPRESS"));
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, projectKeys, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        assertRequiredFields(project1Bug.fields, defaultRequiredFields);
        assertRequiredFields(project1NewFeature.fields, defaultRequiredFields);
        assertRequiredFields(project1Task.fields, defaultRequiredFields);
        assertRequiredFields(project1Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project1SubTask.fields, subtaskRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);
    }

    public void testWithIssueTypes() throws Exception
    {
        // Should return all projects visible to the user, and the specified issue types if the project has them
        subtestByIssueTypes(Arrays.asList(new StringList("2,3")), null);
        subtestByIssueTypes(Arrays.asList(new StringList(Arrays.asList("2", "3"))), null);
        subtestByIssueTypes(Arrays.asList(new StringList("2")), Arrays.asList("Task"));
        subtestByIssueTypes(Arrays.asList(new StringList("2,3")), Arrays.asList("Task"));
        subtestByIssueTypes(null, Arrays.asList("New Feature", "Task"));
    }

    private void subtestByIssueTypes(List<StringList> issueTypeIds, List<String> issueTypeNames)
    {
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, null, issueTypeIds, issueTypeNames, Expand.fields);

        assertEquals(2, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(2, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(1);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);

        final IssueCreateMeta.Project project2 = meta.projects.get(1);
        assertTestProject(project2);

        assertEquals(1, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(0);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project1NewFeature.fields, defaultRequiredFields);
        assertRequiredFields(project1Task.fields, defaultRequiredFields);
        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithAllParams() throws Exception
    {
        // Filter on project (union of ids and keys, avoiding duplicates) and issue type
        final List<StringList> projectIds = Arrays.asList(new StringList("10000"));
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST"));
        final List<StringList> issueTypeIds = Arrays.asList(new StringList("2"));
        final List<String> issueTypeNames = Arrays.asList("Task");
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, projectKeys, issueTypeIds, issueTypeNames, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project2 = meta.projects.get(0);
        assertTestProject(project2);

        assertEquals(1, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(0);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithBadParams() throws Exception
    {
        // Invalid projects and issue types
        final List<StringList> projectIds = Arrays.asList(new StringList("-1,10000"));
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST,ABC,XYZ"));
        final List<StringList> issueTypeIds = Arrays.asList(new StringList("1,300"));
        final List<String> issueTypeNames = Arrays.asList("Bug", "ASDASD");
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, projectKeys, issueTypeIds, issueTypeNames, Expand.fields);

        // okay request, just limited results
        assertEquals(1, meta.projects.size());
        final IssueCreateMeta.Project project = meta.projects.get(0);
        assertEquals("10000", project.id);
        assertEquals(1, project.issuetypes.size());
        final IssueCreateMeta.IssueType issueType = project.issuetypes.get(0);
        assertEquals("1", issueType.id);
    }

    public void testWithNoProjectBrowsePermission() throws Exception
    {
        // User fry cannot see the TST project
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST"));
        final IssueCreateMeta meta = issueClient.loginAs("fry").getCreateMeta(null, projectKeys, null, null, Expand.fields);

        assertEquals(0, meta.projects.size()); // no matches
    }

    public void testWithLimitedProjectBrowsePermission() throws Exception
    {
        // User fry cannot browse the TST project, should only get the PEXPRESS project
        final IssueCreateMeta meta = issueClient.loginAs("fry").getCreateMeta(null, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        final Set<String> frysFields = CollectionBuilder.<String>newBuilder()
                .add("project")
                .add("versions")
                .add("components")
                .add("description")
                .add("environment")
                .add("issuetype")
                .add("labels")
                .add("priority")
                .add("security")
                .add("summary")
                .add("timetracking")
                .asSet();

        final Set<String> frysSubtaskRequiredFields = CollectionBuilder.<String>newBuilder()
                .addAll(frysFields)
                .add("parent")
                .asSet();

        final Map<String, IssueCreateMeta.JsonType> defaultFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
                .add("project", IssueCreateMeta.JsonType.system("project", "project"))
                .add("versions", IssueCreateMeta.JsonType.systemArray("version", "versions"))
                .add("attachment", IssueCreateMeta.JsonType.systemArray("attachment", "attachment"))
//                .add("comment", IssueCreateMeta.JsonType.systemArray("comment", "comment"))
                .add("description", IssueCreateMeta.JsonType.system("string", "description"))
                .add("environment", IssueCreateMeta.JsonType.system("string", "environment"))
                .add("issuetype", IssueCreateMeta.JsonType.system("issuetype", "issuetype"))
                .add("issuelinks", IssueCreateMeta.JsonType.systemArray("issuelinks", "issuelinks"))
                .add("priority", IssueCreateMeta.JsonType.system("priority", "priority"))
                .add("resolution", IssueCreateMeta.JsonType.system("resolution", "resolution"))
                .add("security", IssueCreateMeta.JsonType.system("securitylevel", "security"))
                .add("summary", IssueCreateMeta.JsonType.system("string", "summary"))
                .add("customfield_10000", IssueCreateMeta.JsonType.custom("date", "com.atlassian.jira.plugin.system.customfieldtypes:datepicker", 10000L))
                .add("customfield_10001", IssueCreateMeta.JsonType.custom("datetime", "com.atlassian.jira.plugin.system.customfieldtypes:datetime", 10001L))
                .add("components", IssueCreateMeta.JsonType.systemArray("component", "components"))
                .add("labels", IssueCreateMeta.JsonType.systemArray("string", "labels"))
                .add("timetracking", IssueCreateMeta.JsonType.system("timetracking", "timetracking"))
                .toImmutableMap();

       final Map<String, IssueCreateMeta.JsonType> subTaskFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
                .addAll(defaultFieldTypes)
                .add("parent", IssueCreateMeta.JsonType.system("issuelink", "parent"))
                .toImmutableMap();

        assertRequiredFields(project1Bug.fields, frysFields);
        assertRequiredFields(project1NewFeature.fields, frysFields);
        assertRequiredFields(project1Task.fields, frysFields);
        assertRequiredFields(project1Improvement.fields, frysFields);
        assertRequiredFields(project1SubTask.fields, frysSubtaskRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);
    }

    public void testWithNoProjectCreatePermission() throws Exception
    {
        // User farnsworth cannot create issues in the TST project
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST"));
        final IssueCreateMeta meta = issueClient.loginAs("farnsworth").getCreateMeta(null, projectKeys, null, null, Expand.fields);

        assertEquals(0, meta.projects.size()); // no matches
    }

    public void testWithLimitedProjectCreatePermission() throws Exception
    {
        // User farnsworth cannot create issues in the TST project, should only get the PEXPRESS project
        final IssueCreateMeta meta = issueClient.loginAs("farnsworth").getCreateMeta(null, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        Set<String> farnsworthsFields = new HashSet<String>(defaultRequiredFields);
        farnsworthsFields.remove("reporter");

        final Set<String> subtaskRequiredFields = CollectionBuilder.<String>newBuilder()
                .addAll(farnsworthsFields)
                .add("parent")
                .asSet();

        final Map<String, IssueCreateMeta.JsonType> farnsworthFieldTypes = new HashMap<String, IssueCreateMeta.JsonType>(defaultFieldTypes);
        farnsworthFieldTypes.remove("reporter");

        final Map<String, IssueCreateMeta.JsonType> subTaskFieldTypes = MapBuilder.<String, IssueCreateMeta.JsonType>newBuilder()
                .addAll(farnsworthFieldTypes)
                .add("parent", IssueCreateMeta.JsonType.system("issuelink", "parent"))
                .toImmutableMap();

        assertRequiredFields(project1Bug.fields, farnsworthsFields);
        assertRequiredFields(project1NewFeature.fields, farnsworthsFields);
        assertRequiredFields(project1Task.fields, farnsworthsFields);
        assertRequiredFields(project1Improvement.fields, farnsworthsFields);
        assertRequiredFields(project1SubTask.fields, subtaskRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);
    }

    public void testWithoutFields() throws Exception
    {
        // Should not return the fields
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, null, null, null);

        assertEquals(2, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        final IssueCreateMeta.Project project2 = meta.projects.get(1);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        final IssueCreateMeta.IssueType project2Improvement = project2.issuetypes.get(1);
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(2);
        assertBug(project2Bug);
        assertImprovement(project2Improvement);
        assertNewFeature(project2NewFeature);

        assertNull(project1Bug.fields);
        assertNull(project1NewFeature.fields);
        assertNull(project1Task.fields);
        assertNull(project1Improvement.fields);
        assertNull(project1SubTask.fields);

        assertNull(project2Bug.fields);
        assertNull(project2Improvement.fields);
        assertNull(project2NewFeature.fields);
    }

    private void assertPlanetExpressProject(final IssueCreateMeta.Project project)
    {
        assertEquals("10001", project.id);
        assertEquals("PEXPRESS", project.key);
        assertEquals("Planet Express", project.name);
        assertEquals(getBaseUrl() + "/rest/api/2/project/PEXPRESS", project.self);
        assertEquals(createProjectAvatarUrls(10001L, 10011L), project.avatarUrls);
    }

    private void assertTestProject(final IssueCreateMeta.Project project)
    {
        assertEquals("10000", project.id);
        assertEquals("TST", project.key);
        assertEquals("Test", project.name);
        assertEquals(getBaseUrl() + "/rest/api/2/project/TST", project.self);
        assertEquals(createProjectAvatarUrls(10000L, 10011L), project.avatarUrls);
    }

    private void assertBug(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("1", issueType.id);
        assertEquals("Bug", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/bug.gif", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/1", issueType.self);
    }

    private void assertNewFeature(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("2", issueType.id);
        assertEquals("New Feature", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/newfeature.gif", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/2", issueType.self);
    }

    private void assertTask(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("3", issueType.id);
        assertEquals("Task", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/task.gif", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/3", issueType.self);
    }

    private void assertImprovement(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("4", issueType.id);
        assertEquals("Improvement", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/improvement.gif", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/4", issueType.self);
    }

    private void assertSubTask(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("5", issueType.id);
        assertEquals("Sub-task", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issue_subtask.gif", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/5", issueType.self);
    }

    private void assertFieldNamesAndTypes(final Map<String, FieldMetaData> fields, final Map<String, IssueCreateMeta.JsonType> fieldNamesToTypes)
    {
        for (final Map.Entry<String, FieldMetaData> field : fields.entrySet())
        {
            assertTrue("Contains field that should not be visible: " + field.getKey(),
                    fieldNamesToTypes.containsKey(field.getKey()));

            assertEquals("Incorrect field type for field: " + field.getKey(),
                    fieldNamesToTypes.get(field.getKey()), field.getValue().schema);

            assertEquals("Incorrect name for field: " + field.getKey(),
                    fieldsNamesValues.get(field.getKey()), field.getValue().name);
        }
        for (String expectedKey : fieldNamesToTypes.keySet())
        {
            assertTrue("Could not find required key " + expectedKey, fields.containsKey(expectedKey));
        }
    }

    private void assertRequiredFields(final Map<String, FieldMetaData> fields, Set<String> requiredFieldNames)
    {
        for (final Map.Entry<String, FieldMetaData> field : fields.entrySet())
        {
            if (field.getValue().required)
            {
                assertTrue("Field \"" + field.getKey() + "\" should be required",
                        requiredFieldNames.contains(field.getKey()));
            }
            else
            {
                assertFalse("Field \"" + field.getKey() + "\" should not be required",
                        requiredFieldNames.contains(field.getKey()));
            }
        }
        for (String expectedKey : requiredFieldNames)
        {
            assertTrue("Could not find required key " + expectedKey, fields.containsKey(expectedKey));
        }
    }

    private Map<String, String> createProjectAvatarUrls(final Long projectId, final Long avatarId)
    {
        return MapBuilder.<String, String>newBuilder()
            .add("16x16", getBaseUrlPlus("secure/projectavatar?size=small&pid=" + projectId + "&avatarId=" + avatarId))
            .add("48x48", getBaseUrlPlus("secure/projectavatar?pid=" + projectId + "&avatarId=" + avatarId))
            .toMap();
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceCreateMeta.xml");
    }
}
