package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.ofbiz.core.entity.GenericValue;

import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.gadgets.system.RoadMapResource.DAYS;
import static com.atlassian.jira.gadgets.system.RoadMapResource.NUM;
import static com.atlassian.jira.gadgets.system.RoadMapResource.PROJECT_OR_CATEGORY_IDS;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;

/**
 * Tests RoadMapResource
 *
 * @since v4.0
 */
public class TestRoadMapResource extends ResourceTest
{
    private static final DateFormat df = new SimpleDateFormat("dd/MMM/yy");
    private static final DateFormat dfIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm");

    // mock objects injected via constructor
    private JiraAuthenticationContext mockAuthenticationContext;
    private PermissionManager mockPermissionManager;
    private ProjectManager mockProjectManager;
    private VersionManager mockVersionManager;
    private SearchProvider mockSearchProvider;
    private SearchService mockSearchService;
    private ApplicationProperties mockApplicationProperties;

    // mock objects derived from injected mock objects
    private int expectedTimesGetUserIsCalled = 0;
    private User mockUser;
    private DateTimeFormatterFactory mockDateTimeFormatterFactory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mockAuthenticationContext = mock(JiraAuthenticationContext.class);
        mockPermissionManager = mock(PermissionManager.class);
        mockProjectManager = mock(ProjectManager.class);
        mockVersionManager = mock(VersionManager.class);
        mockSearchProvider = mock(SearchProvider.class);
        mockSearchService = mock(SearchService.class);
        mockApplicationProperties = mock(ApplicationProperties.class);
        mockDateTimeFormatterFactory = new DateTimeFormatterFactoryStub();

        mockUser = new MockUser("bob");
        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @Override
    protected void replayAll()
    {
        expect(mockAuthenticationContext.getLoggedInUser()).andReturn(mockUser).times(expectedTimesGetUserIsCalled, Integer.MAX_VALUE);
        stubOutlookDate();
        super.replayAll();
    }

    private RoadMapResource createInstance()
    {
        return new RoadMapResource(mockAuthenticationContext, mockPermissionManager,
                mockProjectManager, mockVersionManager, mockSearchProvider, mockSearchService, null);
    }

    public final void testValidate_all_projects_and_defaults()
    {
        RoadMapResource instance =

                createInstance();

        replayAll();

        Response actualRes = instance.validate("123|allprojects", "30", "10");

        assertEquals(200, actualRes.getStatus());

        verifyAll();
    }

    public final void testValidate_single_project()
    {
        RoadMapResource instance = createInstance();

        stubProject(123L);
        replayAll();

        Response actualRes = instance.validate("123", "30", "10");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualRes);

        verifyAll();
    }

    public final void testValidate_multiple_projects()
    {
        RoadMapResource instance = createInstance();

        stubProjects(123L, 456L);
        replayAll();

        Response actualRes = instance.validate("123|456", "30", "10");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualRes);

        verifyAll();
    }

    public final void testValidate_nonexistent_project()
    {
        RoadMapResource instance = createInstance();

        stubProject(123L);
        mockNonexistentProject(456L);
        replayAll();

        Response actualRes = instance.validate("123|456", "30", "10");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.project")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testValidate_single_category()
    {
        RoadMapResource instance = createInstance();

        stubCategoryGV(123L);
        replayAll();

        Response actualRes = instance.validate("cat123", "30", "10");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualRes);

        verifyAll();
    }

    public final void testValidate_multiple_categories()
    {
        RoadMapResource instance = createInstance();

        stubCategoryGVs(123L, 456L);
        replayAll();

        Response actualRes = instance.validate("cat123|cat456", "30", "10");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualRes);

        verifyAll();
    }

    public final void testValidate_nonexistent_category()
    {
        RoadMapResource instance = createInstance();

        stubCategoryGV(123L);
        mockNonexistentCategory(456L);
        replayAll();

        Response actualRes = instance.validate("cat123|cat456", "30", "10");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.projectCategory")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testValidate_both_project_category_selected()
    {
        RoadMapResource instance = createInstance();

        stubProject(123L);
        stubCategoryGV(456L);
        replayAll();

        Response actualRes = instance.validate("123|cat456", "30", "10");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualRes);

        verifyAll();
    }

    public final void testValidate_none_selected()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualRes = instance.validate(null, "30", "10");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.projects.and.categories.none.selected")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testValidate_days_zero()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualRes = instance.validate("allprojects", "0", "10");

        assertEquals(200, actualRes.getStatus());

        verifyAll();
    }

    public final void testValidate_days_not_positive_nor_zero()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualRes = instance.validate("allprojects", "-1", "10");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(DAYS, "gadget.common.negative.days")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testValidate_days_upper_limit()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualResponse = instance.validate("allprojects", "1000", "10");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualResponse);

        actualResponse = instance.validate("allprojects", "1001", "10");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(DAYS, "gadget.common.days.overlimit", "1000")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualResponse);

        verifyAll();
    }

    public final void testValidate_days_not_numeric()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualRes = instance.validate("allprojects", "abc", "10");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(DAYS, "gadget.common.days.nan")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testValidate_num_not_positive()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualRes = instance.validate("allprojects", "30", "0");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(NUM, "gadget.common.num.negative")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testValidate_num_upper_limit()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualResponse = instance.validate("allprojects", "30", "50");

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualResponse);

        actualResponse = instance.validate("allprojects", "30", "51");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(NUM, "gadget.common.num.overlimit", "50")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualResponse);

        verifyAll();
    }

    public final void testValidate_num_not_numeric()
    {
        RoadMapResource instance = createInstance();

        replayAll();

        Response actualRes = instance.validate("allprojects", "30", "abc");

        Response expectedRes = Response.status(400).entity(ErrorCollection.Builder.newBuilder(
                new ValidationError(NUM, "gadget.common.num.nan")
        ).build()).cacheControl(CacheControl.NO_CACHE).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_all_projects_and_day_limit() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        Project p101 = stubProject(101L, "MKY", "monkey", null);
        Collection<Project> mockAllBrowsableProjects = CollectionBuilder.<Project>newBuilder(p100, p101).asList();
        expect(mockPermissionManager.getProjectObjects(Permissions.BROWSE, mockUser)).
                andReturn(mockAllBrowsableProjects);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 31);
        Version p101v1 = stubVersion(4L, p101, "v1.0", "version 1.0", -2);
        Version p101v2 = stubVersion(5L, p101, "v2.0", "version 2.0", 4);
        Version p101v3 = stubVersion(6L, p101, "v3.0", "version 3.0", 6);
        Version p101v4 = stubVersion(7L, p101, "v4.0", "version 4.0", null);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));
        expect(mockVersionManager.getVersionsUnreleased(101L, false)).andReturn(asList(p101v1, p101v2, p101v3, p101v4));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L)).
                add(4L, new SearchExpectation(4L, 3L, 1L)).
                add(5L, new SearchExpectation(5L, 7L, 7L)).
                add(6L, new SearchExpectation(6L, 0L, 0L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("123|allprojects", 30, 10);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        RoadMapResource.ProjectData pd101 = projectData(101L, "MKY", "monkey");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(4L, "v1.0", "version 1.0", pd101, -2, 3, resolvedData(2, 66), unResolvedData(1, 34)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66)),
                        versionData(5L, "v2.0", "version 2.0", pd101, 4, 7, resolvedData(0, 0), unResolvedData(7, 100)),
                        versionData(6L, "v3.0", "version 3.0", pd101, 6, 0, resolvedData(0, 100), unResolvedData(0, 0))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_result_limit() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        Project p101 = stubProject(101L, "MKY", "monkey", null);
        Collection<Project> mockAllBrowsableProjects = CollectionBuilder.<Project>newBuilder(p100, p101).asList();
        expect(mockPermissionManager.getProjectObjects(Permissions.BROWSE, mockUser)).
                andReturn(mockAllBrowsableProjects);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 10);
        Version p101v1 = stubVersion(4L, p101, "v1.0", "version 1.0", -2);
        Version p101v2 = stubVersion(5L, p101, "v2.0", "version 2.0", 4);
        Version p101v3 = stubVersion(6L, p101, "v3.0", "version 3.0", 20);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));
        expect(mockVersionManager.getVersionsUnreleased(101L, false)).andReturn(asList(p101v1, p101v2, p101v3));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L)).
                add(4L, new SearchExpectation(4L, 3L, 1L)).
                add(5L, new SearchExpectation(5L, 7L, 7L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("123|allprojects", 30, 4);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        RoadMapResource.ProjectData pd101 = projectData(101L, "MKY", "monkey");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(4L, "v1.0", "version 1.0", pd101, -2, 3, resolvedData(2, 66), unResolvedData(1, 34)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66)),
                        versionData(5L, "v2.0", "version 2.0", pd101, 4, 7, resolvedData(0, 0), unResolvedData(7, 100))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_single_browsable_project() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        expect(mockProjectManager.getProjectObj(100L)).andReturn(p100);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p100, mockUser)).
                andReturn(true);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 31);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("100", 30, 10);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_single_not_browsable_project() throws SearchException
    {
        expectGetUserAtLeastOnce();
        RoadMapResource instance = createInstance();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        expect(mockProjectManager.getProjectObj(100L)).andReturn(p100);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p100, mockUser)).
                andReturn(false);

        replayAll();

        Response actualRes = instance.generate("100", 30, 10);

        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                new ArrayList<RoadMapResource.VersionData>(), 30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_multiple_projects() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        Project p101 = stubProject(101L, "MKY", "monkey", null);
        Project p102 = stubProject(102L, "CHIMP", "chimpanzee", null);
        expect(mockProjectManager.getProjectObj(100L)).andReturn(p100);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p100, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(101L)).andReturn(p101);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p101, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(102L)).andReturn(p102);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p102, mockUser)).
                andReturn(false);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 31);
        Version p101v1 = stubVersion(4L, p101, "v1.0", "version 1.0", -2);
        Version p101v2 = stubVersion(5L, p101, "v2.0", "version 2.0", 4);
        Version p101v3 = stubVersion(6L, p101, "v3.0", "version 3.0", null);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));
        expect(mockVersionManager.getVersionsUnreleased(101L, false)).andReturn(asList(p101v1, p101v2, p101v3));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L)).
                add(4L, new SearchExpectation(4L, 3L, 1L)).
                add(5L, new SearchExpectation(5L, 7L, 7L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("100|101|102", 30, 10);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        RoadMapResource.ProjectData pd101 = projectData(101L, "MKY", "monkey");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(4L, "v1.0", "version 1.0", pd101, -2, 3, resolvedData(2, 66), unResolvedData(1, 34)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66)),
                        versionData(5L, "v2.0", "version 2.0", pd101, 4, 7, resolvedData(0, 0), unResolvedData(7, 100))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_nonexistent_project() throws SearchException
    {
        RoadMapResource instance = createInstance();

        expect(mockProjectManager.getProjectObj(100L)).andReturn(null);

        replayAll();

        Response actualRes = instance.generate("100", 30, 10);

        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                new ArrayList<RoadMapResource.VersionData>(), 30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_single_category() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        Project p101 = stubProject(101L, "MKY", "monkey", null);
        Project p102 = stubProject(102L, "CHIMP", "chimpanzee", null);
        Collection<Project> mockAllSelectedProjects = CollectionBuilder.<Project>newBuilder(p100, p101, p102).asList();
        expect(mockProjectManager.getProjectObjectsFromProjectCategory(123L)).andReturn(mockAllSelectedProjects);
        expect(mockProjectManager.getProjectObj(100L)).andReturn(p100);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p100, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(101L)).andReturn(p101);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p101, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(102L)).andReturn(p102);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p102, mockUser)).
                andReturn(false);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 31);
        Version p101v1 = stubVersion(4L, p101, "v1.0", "version 1.0", -2);
        Version p101v2 = stubVersion(5L, p101, "v2.0", "version 2.0", 4);
        Version p101v3 = stubVersion(6L, p101, "v3.0", "version 3.0", null);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));
        expect(mockVersionManager.getVersionsUnreleased(101L, false)).andReturn(asList(p101v1, p101v2, p101v3));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L)).
                add(4L, new SearchExpectation(4L, 3L, 1L)).
                add(5L, new SearchExpectation(5L, 7L, 7L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("cat123", 30, 10);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        RoadMapResource.ProjectData pd101 = projectData(101L, "MKY", "monkey");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(4L, "v1.0", "version 1.0", pd101, -2, 3, resolvedData(2, 66), unResolvedData(1, 34)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66)),
                        versionData(5L, "v2.0", "version 2.0", pd101, 4, 7, resolvedData(0, 0), unResolvedData(7, 100))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_multiple_categories() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        Project p101 = stubProject(101L, "BEE", "bumblebee", null);
        Project p102 = stubProject(102L, "MKY", "monkey", null);
        Collection<Project> mockPrimateProjects = CollectionBuilder.<Project>newBuilder(p100, p102).asList();
        Collection<Project> mockInsectProjects = CollectionBuilder.<Project>newBuilder(p101).asList();
        expect(mockProjectManager.getProjectObjectsFromProjectCategory(123L)).andReturn(mockPrimateProjects);
        expect(mockProjectManager.getProjectObjectsFromProjectCategory(456L)).andReturn(mockInsectProjects);
        expect(mockProjectManager.getProjectObj(100L)).andReturn(p100);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p100, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(101L)).andReturn(p101);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p101, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(102L)).andReturn(p102);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p102, mockUser)).
                andReturn(false);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 31);
        Version p101v1 = stubVersion(4L, p101, "v1.0", "version 1.0", -2);
        Version p101v2 = stubVersion(5L, p101, "v2.0", "version 2.0", 4);
        Version p101v3 = stubVersion(6L, p101, "v3.0", "version 3.0", null);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));
        expect(mockVersionManager.getVersionsUnreleased(101L, false)).andReturn(asList(p101v1, p101v2, p101v3));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L)).
                add(4L, new SearchExpectation(4L, 3L, 1L)).
                add(5L, new SearchExpectation(5L, 7L, 7L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("cat123|cat456", 30, 10);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        RoadMapResource.ProjectData pd101 = projectData(101L, "BEE", "bumblebee");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(4L, "v1.0", "version 1.0", pd101, -2, 3, resolvedData(2, 66), unResolvedData(1, 34)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66)),
                        versionData(5L, "v2.0", "version 2.0", pd101, 4, 7, resolvedData(0, 0), unResolvedData(7, 100))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testGenerate_both_project_category_selected() throws SearchException
    {
        expectGetUserAtLeastOnce();

        Project p100 = stubProject(100L, "HOMOSAP", "homosapien", null);
        Project p101 = stubProject(101L, "BEE", "bumblebee", null);
        Project p102 = stubProject(102L, "MKY", "monkey", null);
        Collection<Project> mockPrimateProjects = CollectionBuilder.<Project>newBuilder(p100, p102).asList();
        expect(mockProjectManager.getProjectObjectsFromProjectCategory(123L)).andReturn(mockPrimateProjects);
        expect(mockProjectManager.getProjectObj(100L)).andReturn(p100);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p100, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(101L)).andReturn(p101);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p101, mockUser)).
                andReturn(true);
        expect(mockProjectManager.getProjectObj(102L)).andReturn(p102);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, p102, mockUser)).
                andReturn(false);

        Version p100v1 = stubVersion(1L, p100, "v1", "version 1", -3);
        Version p100v2 = stubVersion(2L, p100, "v2", "version 2", 3);
        Version p100v3 = stubVersion(3L, p100, "v3", "version 3", 31);
        Version p101v1 = stubVersion(4L, p101, "v1.0", "version 1.0", -2);
        Version p101v2 = stubVersion(5L, p101, "v2.0", "version 2.0", 4);
        Version p101v3 = stubVersion(6L, p101, "v3.0", "version 3.0", null);
        expect(mockVersionManager.getVersionsUnreleased(100L, false)).andReturn(asList(p100v1, p100v2, p100v3));
        expect(mockVersionManager.getVersionsUnreleased(101L, false)).andReturn(asList(p101v1, p101v2, p101v3));

        Map<Long, SearchExpectation> searchExpects = MapBuilder.<Long, SearchExpectation>newBuilder().
                add(1L, new SearchExpectation(1L, 7L, 0L)).
                add(2L, new SearchExpectation(2L, 3L, 2L)).
                add(4L, new SearchExpectation(4L, 3L, 1L)).
                add(5L, new SearchExpectation(5L, 7L, 7L))
                .toHashMap();

        RoadMapResource instance = new PartiallyStubbedOutRoadMapResource(searchExpects);

        replayAll();

        Response actualRes = instance.generate("101|cat123", 30, 10);

        RoadMapResource.ProjectData pd100 = projectData(100L, "HOMOSAP", "homosapien");
        RoadMapResource.ProjectData pd101 = projectData(101L, "BEE", "bumblebee");
        Response expectedRes = Response.ok(new RoadMapResource.RoadMapData(
                CollectionBuilder.newBuilder(
                        versionData(1L, "v1", "version 1", pd100, -3, 7, resolvedData(7, 100), unResolvedData(0, 0)),
                        versionData(4L, "v1.0", "version 1.0", pd101, -2, 3, resolvedData(2, 66), unResolvedData(1, 34)),
                        versionData(2L, "v2", "version 2", pd100, 3, 3, resolvedData(1, 34), unResolvedData(2, 66)),
                        versionData(5L, "v2.0", "version 2.0", pd101, 4, 7, resolvedData(0, 0), unResolvedData(7, 100))
                ).asList(),
                30, getFormattedDate(30)
        )).build();

        assertEquals(expectedRes, actualRes);

        verifyAll();
    }

    public final void testBuildAllIssueForFixVersionQuery()
    {
        Version v = stubVersion(456L, stubProject(123L, "pkey", "pname", null), "1.0", "v1.0", null);

        replayAll();
        Query q = createInstance().buildAllIssuesForFixVersionQuery(v);

        verifyJqlQuery(q, "pname", "1.0", null);
        verifyAll();
    }

    public final void testSearchCount() throws SearchException
    {
        Query q = mock(Query.class);
        expect(mockSearchProvider.searchCount(q, mockUser)).andReturn(123L);
        expectGetUserAtLeastOnce();
        replayAll();
        assertEquals(123L, createInstance().searchCount(q));
        verifyAll();
    }

    public final void testGetQueryString() throws SearchException
    {
        Query q = mock(Query.class);
        expect(mockSearchService.getQueryString(mockUser, q)).andReturn("abc");
        expectGetUserAtLeastOnce();
        replayAll();
        assertEquals("abc", createInstance().getQueryString(q));
        verifyAll();
    }

    public final void testBuildUnresolvedIssueForFixVersionQuery()
    {
        Version v = stubVersion(456L, stubProject(123L, "pkey", "pname", null), "1.0", "v1.0", null);

        replayAll();
        Query q = createInstance().buildUnresolvedIssuesForFixVersionQuery(v);

        verifyJqlQuery(q, "pname", "1.0", Operator.IS);
        verifyAll();
    }

    public final void testBuildResolvedIssueForFixVersionQuery()
    {
        Version v = stubVersion(456L, stubProject(123L, "pkey", "pname", null), "1.0", "v1.0", null);

        replayAll();
        Query q = createInstance().buildResolvedIssuesForFixVersionQuery(v);

        verifyJqlQuery(q, "pname", "1.0", Operator.IS_NOT);
        verifyAll();
    }

    private void verifyJqlQuery(final Query q, final String projName, final String verName, final Operator resolutionOperator)
    {
        Clause and = q.getWhereClause();
        assertEquals("AND", and.getName());
        List<Clause> clauses = and.getClauses();
        String projectClauseName = SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName();
        String fixVersionName = SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName();
        String resolutionClauseName = SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName();
        String foundProjectName = null;
        String foundFixVersionName = null;
        Operator foundResolutionOperator = null;
        for (Clause c : clauses)
        {
            if (c instanceof TerminalClause)
            {
                TerminalClause tc = (TerminalClause) c;
                if (tc.getName().equals(resolutionClauseName))
                {
                    foundResolutionOperator = tc.getOperator();
                    assertEquals(EmptyOperand.EMPTY, tc.getOperand());
                }
                else if (tc.getName().equals(projectClauseName))
                {
                    assertEquals(Operator.EQUALS, tc.getOperator());
                    assertTrue(tc.getOperand() instanceof SingleValueOperand);
                    foundProjectName = ((SingleValueOperand) tc.getOperand()).getStringValue();
                }
                else if (tc.getName().equals(fixVersionName))
                {
                    assertEquals(Operator.EQUALS, tc.getOperator());
                    assertTrue(tc.getOperand() instanceof SingleValueOperand);
                    foundFixVersionName = ((SingleValueOperand) tc.getOperand()).getStringValue();
                }
                else
                {
                    fail("Unexpected jql terminal clause [" + tc.getName() + "]");
                }
            }
        }
        assertEquals(projName, foundProjectName);
        assertEquals(verName, foundFixVersionName);
        assertEquals(resolutionOperator, foundResolutionOperator);
    }

    private RoadMapResource.ProjectData projectData(Long id, String key, String name)
    {
        return new RoadMapResource.ProjectData(id, key, name);
    }

    private RoadMapResource.VersionData versionData(Long id, String name, String desc, RoadMapResource.ProjectData proj,
            int daysToAdd, int allCount, int[] resolvedData, int[] unresolvedData)
    {
        return new RoadMapResource.VersionData(id, name, desc, proj, getFormattedDate(daysToAdd),
                getIso8601FormattedDate(daysToAdd), daysToAdd < 0,
                allCount, resolvedData(id, resolvedData[0], resolvedData[1]),
                unResolvedData(id, unresolvedData[0], unresolvedData[1]));
    }

    private int[] resolvedData(int count, int percentage)
    {
        return new int[] { count, percentage };
    }

    private int[] unResolvedData(int count, int percentage)
    {
        return new int[] { count, percentage };
    }

    private RoadMapResource.ResolutionData resolvedData(long vid, int count, int percentage)
    {
        return new RoadMapResource.ResolutionData(count, percentage, "resolved for " + vid);
    }

    private RoadMapResource.ResolutionData unResolvedData(long vid, int count, int percentage)
    {
        return new RoadMapResource.ResolutionData(count, percentage, "unresolved for " + vid);
    }

    private void setMinTimesGetUserIsCalled(int minTimes)
    {
        this.expectedTimesGetUserIsCalled = minTimes;
    }

    private void expectGetUserAtLeastOnce()
    {
        setMinTimesGetUserIsCalled(1);
    }

    private OutlookDate stubOutlookDate()
    {
        Locale locale = Locale.getDefault();
        final OutlookDate outlookDate = new OutlookDate(locale, mockApplicationProperties, null, mockDateTimeFormatterFactory);
        expect(mockAuthenticationContext.getOutlookDate()).andReturn(outlookDate).anyTimes();
        expect(mockApplicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_DMY)).
                andReturn("dd/MMM/yy").anyTimes();
        return outlookDate;
    }

    private List<Project> stubProjects(Long... ids)
    {
        return CollectionUtil.transform(asList(ids), new Function<Long, Project>()
        {
            public Project get(final Long input)
            {
                return stubProject(input);
            }
        });
    }

    private Project stubProject(Long id)
    {
        Project p = mock(Project.class);
        expect(p.getId()).andReturn(id).anyTimes();
        expect(mockProjectManager.getProjectObj(id)).andReturn(p).anyTimes();
        return p;
    }

    private Project stubProject(final Long id, final String key, final String name, final GenericValue optionalCategory)
    {
        final Project project = mock(Project.class);
        expect(project.getId()).andReturn(id).anyTimes();
        expect(project.getKey()).andReturn(key).anyTimes();
        expect(project.getName()).andReturn(name).anyTimes();
        expect(project.getProjectCategory()).andReturn(optionalCategory).anyTimes();
        return project;
    }

    private Version stubVersion(final Long id, final Project parent, final String name, final String desc,
            final Integer daysToRelease)
    {
        final Version v = mock(Version.class);
        expect(v.getProjectObject()).andReturn(parent).anyTimes();
        expect(v.getId()).andReturn(id).anyTimes();
        expect(v.getName()).andReturn(name).anyTimes();
        expect(v.getDescription()).andReturn(desc).anyTimes();
        expect(v.getReleaseDate()).andReturn(
                daysToRelease == null ? null : getDate(daysToRelease)).anyTimes();
        return v;
    }

    private void mockNonexistentProject(Long id)
    {
        expect(mockProjectManager.getProjectObj(id)).andReturn(null);
    }

    private List<GenericValue> stubCategoryGVs(Long... ids)
    {
        return CollectionUtil.transform(asList(ids), new Function<Long, GenericValue>()
        {
            public GenericValue get(final Long input)
            {
                return stubCategoryGV(input);
            }
        });
    }

    private GenericValue stubCategoryGV(Long id)
    {
        GenericValue c = mock(GenericValue.class);
        expect(c.get("id")).andReturn(id).anyTimes();
        expect(c.getLong("id")).andReturn(id).anyTimes();
        expect(mockProjectManager.getProjectCategory(id)).andReturn(c);
        return c;
    }

    private void mockNonexistentCategory(Long id)
    {
        expect(mockProjectManager.getProjectCategory(id)).andReturn(null);
    }

    private Date getDate(int daysToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd);
        //set h m s to fixed values so we avoid random test failures!
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 10);
        cal.set(Calendar.SECOND, 10);
        return cal.getTime();
    }

    private String getFormattedDate(int daysToAdd)
    {
        return df.format(getDate(daysToAdd));
    }

    private String getIso8601FormattedDate(int daysToAdd)
    {
        return dfIso8601.format(getDate(daysToAdd));
    }

    class SearchExpectation
    {
        private Long versionId;
        private Query mockAllQuery = mock(Query.class);
        private Query mockUnresolvedQuery = mock(Query.class);
        private Query mockResolvedQuery = mock(Query.class);
        private long allCount;
        private long unresolvedCount;

        SearchExpectation(Long vId, long allCount, long unresolvedCount)
        {
            this.versionId = vId;
            this.allCount = allCount;
            this.unresolvedCount = unresolvedCount;
        }

        public Query getMockAllQuery()
        {
            return mockAllQuery;
        }

        public Query getMockUnresolvedQuery()
        {
            return mockUnresolvedQuery;
        }

        public Query getMockResolvedQuery()
        {
            return mockResolvedQuery;
        }

        public boolean owns(Query q)
        {
            return q == mockAllQuery || q == mockUnresolvedQuery || q == mockResolvedQuery;
        }

        public long count(Query q)
        {
            if (owns(q))
            {
                if (q == mockAllQuery)
                {
                    return allCount;
                }
                else if (q == mockUnresolvedQuery)
                {
                    return unresolvedCount;
                }
                else
                {
                    return allCount - unresolvedCount;
                }
            }
            else
            {
                return -1;
            }
        }

        public String toQueryString(Query q)
        {
            if (owns(q))
            {
                if (q == mockUnresolvedQuery)
                {
                    return "unresolved for " + versionId;
                }
                else if (q == mockResolvedQuery)
                {
                    return "resolved for " + versionId;
                }
                else
                {
                    return "all for " + versionId;
                }
            }
            else
            {
                return "";
            }
        }
    }

    class PartiallyStubbedOutRoadMapResource extends RoadMapResource
    {
        private Map<Long, SearchExpectation> expectations;

        PartiallyStubbedOutRoadMapResource(Map<Long, SearchExpectation> expectations)
        {
            super(mockAuthenticationContext, mockPermissionManager, mockProjectManager, mockVersionManager,
                    mockSearchProvider, mockSearchService, null);
            this.expectations = expectations;
        }

        @Override
        Query buildAllIssuesForFixVersionQuery(final Version version)
        {
            return expectations.get(version.getId()).getMockAllQuery();
        }

        @Override
        Query buildUnresolvedIssuesForFixVersionQuery(final Version version)
        {
            return expectations.get(version.getId()).getMockUnresolvedQuery();
        }

        @Override
        Query buildResolvedIssuesForFixVersionQuery(final Version version)
        {
            return expectations.get(version.getId()).getMockResolvedQuery();
        }

        @Override
        long searchCount(final Query query) throws SearchException
        {
            SearchExpectation se = CollectionUtil.findFirstMatch(
                    expectations.values(), new OwningSearchExpection(query));
            return se == null ? -1 : se.count(query);
        }

        @Override
        String getQueryString(final Query query)
        {
            SearchExpectation se = CollectionUtil.findFirstMatch(
                    expectations.values(), new OwningSearchExpection(query));
            return se == null ? "" : se.toQueryString(query);
        }
    }

    static class OwningSearchExpection implements Predicate<SearchExpectation>
    {
        private Query query;

        public OwningSearchExpection(Query query)
        {
            this.query = query;
        }

        public boolean evaluate(final SearchExpectation input)
        {
            return input.owns(query);
        }
    }
}
